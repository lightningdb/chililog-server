//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.chililog.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.chililog.server.engine;

import org.apache.commons.lang.NullArgumentException;
import org.hornetq.api.core.Message;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;

import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryController;
import com.chililog.server.data.RepositoryControllerFactory;
import com.chililog.server.data.RepositoryEntryBO;
import com.mongodb.DB;

/**
 * Responsible for reading entries off the message queue and writing it to mongoDB
 * 
 * @author vibul
 * 
 */
public class RepositoryWriter extends Thread
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(RepositoryWriter.class);
    private Repository _repo = null;
    private boolean _stopRunning = false;
    private boolean _isRunning = false;

    /**
     * Basic constructor
     * 
     * @param name
     *            name to give this thread
     * @param repo
     *            Repository that we are writing
     * @throws NullArgumentException
     *             if repo is null
     */
    public RepositoryWriter(String name, Repository repo) throws NullArgumentException
    {
        super(name);

        if (repo == null)
        {
            throw new NullArgumentException("repo");
        }
        _repo = repo;
    }

    /**
     * Receive incoming messages and write to the database
     */
    @Override
    public void run()
    {
        if (_isRunning)
        {
            throw new RuntimeException("RepositoryWriter " + this.getName() + " is alrady running");
        }

        _logger.info("RepositoryWriter '%s' started", this.getName());
        _stopRunning = false;
        _isRunning = true;
        DB db = null;
        ClientSession session = null;
        RepositoryController controller = null;

        try
        {
            db = MongoConnection.getInstance().getConnection();

            controller = RepositoryControllerFactory.make(_repo.getRepoInfo());

            session = MqManager.getInstance().getTransactionalSystemClientSession();
            ClientConsumer messageConsumer = session.createConsumer(_repo.getRepoInfo().getWriteQueueAddress());
            session.start();

            ClientProducer dlqProducer = session.createProducer(_repo.getRepoInfo().getDeadLetterAddress());

            while (true)
            {
                // Wait (sleep) 1/2 second for messages
                ClientMessage messageReceived = messageConsumer.receive(500);
                if (messageReceived != null)
                {
                    try
                    {
                        String textEntry = messageReceived.getBodyBuffer().readString();

                        // Process message
                        RepositoryEntryBO repoEntry = controller.parse(textEntry);
                        if (repoEntry != null)
                        {
                            controller.save(db, repoEntry);
                            _logger.debug("RepositoryWriter '%s' processed message id %s: %s", this.getName(),
                                    messageReceived.getMessageID(), textEntry);
                        }
                        messageReceived.acknowledge();
                        session.commit();

                        // Text could not be parsed so add to dead letter queue
                        // Do it after session.commit() because adding to DLA requires another commit
                        // and we want to flag the original message as having been processed so it is not re-processed
                        if (repoEntry == null)
                        {
                            // Cannot parse. Commit and add to Dead Letter Queue with the error
                            _logger.debug("RepositoryWriter '%s' parse error processing message id %s: '%s'. "
                                    + "Moved message to dead letter queue.", this.getName(),
                                    messageReceived.getMessageID(), textEntry);

                            addToDeadLetterQueue(session, dlqProducer, textEntry, controller.getLastParseError());
                        }
                    }
                    catch (Exception ex)
                    {
                        // This exception really should only be for mongoDB write errors

                        // Rollback and try delivery again (just in case we have bad DB connection or other)
                        // We want to ack the message so that we don't get in an endless try again loop
                        // Without ack, message delivery count does not get incremented!
                        messageReceived.acknowledge();
                        session.rollback();

                        String msg = null;
                        msg = "This is delivery attempt # " + messageReceived.getDeliveryCount();
                        _logger.error(ex, "RepositoryWriter '%s' processing error. %s. %s", this.getName(),
                                ex.getMessage(), msg);
                    }
                }

                // See if we want to quit
                if (_stopRunning)
                {
                    break;
                }

                // Loop again
            }

            // We are done
            _logger.info("RepositoryWriter '%s' stopped", this.getName());
            return;
        }
        catch (Exception ex)
        {
            // Just log and terminate
            // TODO Repository or some class should have a periodic check to make sure this thread is started again in
            // the event of an exception stopping the thread
            _logger.error(ex, "RepositoryWriter '%s' error. %s", this.getName(), ex.getMessage());
        }
        finally
        {
            _isRunning = false;
            MqManager.getInstance().closeClientSession(session);
        }
    }

    /**
     * Write a message to the dead letter queue
     * 
     * @param session
     * @param dlqProducer
     * @param textEntry
     * @param ex
     */
    private void addToDeadLetterQueue(ClientSession session, ClientProducer dlqProducer, String textEntry, Exception ex)
    {
        try
        {
            ClientMessage message = session.createMessage(Message.TEXT_TYPE, false);
            message.putStringProperty("ParseException", ex.toString());
            message.getBodyBuffer().writeString(textEntry);
            dlqProducer.send(message);
            session.commit();
        }
        catch (Exception ex2)
        {
            // Just log can continue
            _logger.error(ex2, "RepositoryWriter '%s' could not add message to dead letter queue error. %s",
                    this.getName(), ex2.getMessage());
        }
    }

    /**
     * Returns the repository to which this thread belongs
     */
    public Repository getRepository()
    {
        return _repo;
    }

    /**
     * Returns true if this thread is running, false if not
     */
    public boolean isRunning()
    {
        return _isRunning;
    }

    /**
     * Make this thread stop running
     */
    public void stopRunning()
    {
        _stopRunning = true;
    }

}
