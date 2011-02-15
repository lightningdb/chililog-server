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

package com.chililog.server.security;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.chililog.server.security.PasswordManager;

public class PasswordManagerTest
{
    private static Logger _logger = Logger.getLogger(PasswordManagerTest.class);

    @Test
    public void testOK() throws Exception
    {
        String hashValue = PasswordManager.hashPassword("admin", null);
        _logger.info("Hashed password of 'admin' is " + hashValue);
        assertTrue(PasswordManager.verifyPassword("admin", hashValue));
        
        hashValue = PasswordManager.hashPassword("YumCha", null);
        _logger.info("Hashed password of 'YumCha' is " + hashValue);
        assertTrue(PasswordManager.verifyPassword("YumCha", hashValue));
        
        hashValue = PasswordManager.hashPassword("SpringRolls", null);
        _logger.info("Hashed password of 'SpringRolls' is " + hashValue);
        assertTrue(PasswordManager.verifyPassword("SpringRolls", hashValue));

        hashValue = PasswordManager.hashPassword("BBQPorkBuns", null);
        _logger.info("Hashed password of 'BBQPorkBuns' is " + hashValue);
        assertTrue(PasswordManager.verifyPassword("BBQPorkBuns", hashValue));

        String s = "asdAd!@#$%^&*() 12344576890-=_+<>,.;'`\"~;':]{}\\|[";
        hashValue = PasswordManager.hashPassword(s, null);
        assertTrue(PasswordManager.verifyPassword(s, hashValue));        
    }
    
    @Test
    public void testNotOK() throws Exception
    {
        String hashValue = PasswordManager.hashPassword("admin", null);
        assertFalse(PasswordManager.verifyPassword("not admin", hashValue));
    }
    
    /**
     * With salt, the same password should not have the same hash
     * @throws Exception
     */
    @Test
    public void testSalt() throws Exception
    {
        String hashValue = PasswordManager.hashPassword("Yeeeeee Haaaaaa", null);
        String hashValue2 = PasswordManager.hashPassword("Yeeeeee Haaaaaa", null);
        assertFalse(hashValue.equals(hashValue2));
    }
}
