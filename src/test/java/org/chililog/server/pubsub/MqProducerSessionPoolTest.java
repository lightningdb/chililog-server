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

package org.chililog.server.pubsub;

import static org.junit.Assert.*;

import org.chililog.server.common.ChiliLogException;
import org.chililog.server.engine.MqService;
import org.chililog.server.pubsub.MqProducerSessionPool;
import org.chililog.server.pubsub.Strings;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MqProducerSessionPoolTest {

    @BeforeClass
    public static void classSetup() throws Exception {
        MqService.getInstance().start();
    }

    @AfterClass
    public static void classTeardown() throws Exception {
        MqService.getInstance().stop();
    }

    @Test
    public void testExhaustPool() throws Exception {
        MqProducerSessionPool.Pooled p;
        MqProducerSessionPool pool = new MqProducerSessionPool(5);
        assertEquals(5, pool.size());

        for (int i = 0; i < 5; i++) {
            p = pool.getPooled();
            p.session.close();
        }

        try {
            p = pool.getPooled();
            fail("Exception expected");
        } catch (ChiliLogException ex) {
            assertEquals(Strings.GET_POOLED_PUBLISHER_SESSION_TIMEOUT_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testUseAndReturn() throws Exception {
        MqProducerSessionPool.Pooled p;
        MqProducerSessionPool pool = new MqProducerSessionPool(5);
        assertEquals(5, pool.size());

        p = pool.getPooled();
        assertEquals(4, pool.size());

        pool.returnPooled(p);
        assertEquals(5, pool.size());
    }

    @Test
    public void testUseAndReplace() throws Exception {
        MqProducerSessionPool.Pooled p;
        MqProducerSessionPool pool = new MqProducerSessionPool(5);
        assertEquals(5, pool.size());

        // Use and close session because of assumed error
        p = pool.getPooled();
        assertEquals(4, pool.size());
        p.session.close();

        // Add a new connection
        pool.addPooled();
        assertEquals(5, pool.size());
    }

}
