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

package org.chililog.server.data;

import static org.junit.Assert.*;

import org.chililog.server.common.ChiliLogException;
import org.chililog.server.data.MongoConnection;
import org.junit.Test;

import com.mongodb.DB;

/**
 * <p>
 * This test assumes the following configuration in app.properties and mongodb has been accordingly setup.
 * </p>
 * <p>
 * <code>
 * # IP address or host name of MongoDb server
 * db.ip_address=localhost
 * # The ip port to use to connect to the MongoDb server. Defaults to 27017
 * db.ip_port=
 * # Name of MongoDb database to use
 * db.name=chililog
 * # Username for MongoDb authentication 
 * db.username=chililog
 * # Password for MongoDb authentication
 * db.password=chililog12
 * </code>
 * </p>
 * 
 * @author vibul
 * 
 */
public class MongoConnectionTest
{
    @Test
    public void testOK() throws Exception
    {
        DB db = MongoConnection.getInstance().getConnection();
        assertNotNull(db);
        assertEquals(db.getName(), "chililog");
        assertTrue(db.isAuthenticated());        
    }
    
    @Test(expected = ChiliLogException.class)
    public void testBadDbName() throws Exception
    {
        MongoConnection.getInstance().getConnection("baddbname", "1", "2");
    }

    @Test(expected = ChiliLogException.class)
    public void testBadUserName() throws Exception
    {
        // If we don't reload mongo, we get "can't call authenticate twice on the same DBObject" exception
        MongoConnection.getInstance().loadMongo();
        MongoConnection.getInstance().getConnection("chililog", "1", "2");
    }

    @Test(expected = ChiliLogException.class)
    public void testBadPassword() throws Exception
    {
        MongoConnection.getInstance().loadMongo();
        MongoConnection.getInstance().getConnection("chililog", "chililog", "2");
    }
}
