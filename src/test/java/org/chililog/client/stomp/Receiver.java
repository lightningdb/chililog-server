/**
 * http://www.germane-software.com/software/Java/Gozirra/
 */
package org.chililog.client.stomp;

import java.io.*;
import java.util.HashMap;

import org.chililog.server.common.Log4JLogger;


/**
 * (c)2005 Sean Russell
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class Receiver extends Thread {
  private static Log4JLogger _logger = Log4JLogger.getLogger(Receiver.class);
  
  private MessageReceiver _receiver;
  private BufferedReader _input;
  private InputStream _stream;

  protected Receiver() {
    super();
  }
  public Receiver( MessageReceiver m, InputStream input ) { 
    super();
    setup( m, input );
  }

  protected void setup( MessageReceiver m, InputStream input ) {
    _receiver = m;
    try {
      _stream = input;
      _input = new BufferedReader(new InputStreamReader(input,Command.ENCODING));
    } catch (UnsupportedEncodingException e) {
      // No, no, no.  Stupid Java.
    }
  }

  public void run() {
    // Loop reading from stream, calling receive()
    try {
      while (!isInterrupted()) {
        // Get command
        if (_input.ready()) {
          String command = _input.readLine();
          if (command.length() > 0) {
            try {
              Command c = Command.valueOf( command );
              _logger.info("STOMP Receive command: %s", command);

              // Get headers
              HashMap headers = new HashMap();
              String header;
              while ((header = _input.readLine()).length() > 0) {
                _logger.info("STOMP Receive header: %s", header);
                int ind = header.indexOf( ':' );
                String k = header.substring( 0, ind );
                String v = header.substring( ind+1, header.length() );
                headers.put(k.trim(),v.trim());
              }
              // Read body
              StringBuffer body = new StringBuffer();
              int b;
              while ((b = _input.read()) != 0) {
                body.append( (char)b );
              }
              _logger.info("STOMP Receive body: %s", body.toString());

              try {
                _receiver.receive( c, headers, body.toString() );
              } catch (Exception e) {
                // We ignore these errors; we don't want client code
                // crashing our listener.
              }
            } catch (Error e) {
              try {
                while (_input.read() != 0);
              } catch (Exception ex) { }
              try {
                _receiver.receive( Command.ERROR, null, e.getMessage()+"\n" );
              } catch (Exception ex) {
                // We ignore these errors; we don't want client code
                // crashing our listener.
              }
            }
          }
        } else {
          if (_receiver.isClosed()) {
            _receiver.disconnect();
            return;
          }
          try {Thread.sleep(200);}catch(InterruptedException e){interrupt();}
        }
      }
    } catch (IOException e) {
      // What do we do with IO Exceptions?  Report it to the receiver, and 
      // exit the thread.
      System.err.println("Stomp exiting because of exception");
      e.printStackTrace( System.err );
      _receiver.receive( Command.ERROR, null, e.getMessage() );
    } catch (Exception e) {
      System.err.println("Stomp exiting because of exception");
      e.printStackTrace( System.err );
      _receiver.receive( Command.ERROR, null, e.getMessage() );
    }
  }
}
