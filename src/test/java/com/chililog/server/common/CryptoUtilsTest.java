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

package com.chililog.server.common;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.CryptoUtils;

public class CryptoUtilsTest
{
    private static Logger _logger = Logger.getLogger(CryptoUtilsTest.class);

    @Test
    public void testMD5Hash() throws Exception
    {
        String hashValue = CryptoUtils.createMD5Hash("admin");
        _logger.info("Hashed password of 'admin' is " + hashValue);
        
        hashValue = CryptoUtils.createMD5Hash("sandpit");
        _logger.info("Hashed password of 'sandpit' is " + hashValue);

        hashValue = CryptoUtils.createMD5Hash("hello");
        _logger.info("Hashed password of 'hello' is " + hashValue);

    }
    
    @Test
    public void testSHA512() throws Exception
    {
        String hashValue = CryptoUtils.createSHA512Hash("admin", null);
        _logger.info("Hashed password of 'admin' is " + hashValue);
        assertTrue(CryptoUtils.verifyHash("admin", hashValue));

        hashValue = CryptoUtils.createSHA512Hash("sandpit", null);
        _logger.info("Hashed password of 'sandpit' is " + hashValue);
        assertTrue(CryptoUtils.verifyHash("sandpit", hashValue));

        hashValue = CryptoUtils.createSHA512Hash("YumCha", null);
        _logger.info("Hashed password of 'YumCha' is " + hashValue);
        assertTrue(CryptoUtils.verifyHash("YumCha", hashValue));

        hashValue = CryptoUtils.createSHA512Hash("SpringRolls", null);
        _logger.info("Hashed password of 'SpringRolls' is " + hashValue);
        assertTrue(CryptoUtils.verifyHash("SpringRolls", hashValue));

        hashValue = CryptoUtils.createSHA512Hash("BBQPorkBuns", null);
        _logger.info("Hashed password of 'BBQPorkBuns' is " + hashValue);
        assertTrue(CryptoUtils.verifyHash("BBQPorkBuns", hashValue));

        String s = "asdAd!@#$%^&*() 12344576890-=_+<>,.;'`\"~;':]{}\\|[";
        hashValue = CryptoUtils.createSHA512Hash(s, null);
        assertTrue(CryptoUtils.verifyHash(s, hashValue));
    }

    @Test
    public void testNotOK() throws Exception
    {
        String hashValue = CryptoUtils.createSHA512Hash("admin", null);
        assertFalse(CryptoUtils.verifyHash("not admin", hashValue));
    }

    /**
     * With salt, the same password should not have the same hash
     * 
     * @throws Exception
     */
    @Test
    public void testSaltiness() throws Exception
    {
        String hashValue = CryptoUtils.createSHA512Hash("Yeeeeee Haaaaaa", null);
        String hashValue2 = CryptoUtils.createSHA512Hash("Yeeeeee Haaaaaa", null);
        assertFalse(hashValue.equals(hashValue2));
    }

    /**
     * Test when a salt is supplied and salt not stored with hash
     * 
     * @throws ChiliLogException
     */
    @Test
    public void testSaltSupplied() throws ChiliLogException
    {
        byte[] salt = new byte[]
        { 1, 2, 3, 4, 5, 6, 7, 8 };

        String hashValue = CryptoUtils.createSHA512Hash("rocksalt", salt, false);
        _logger.info("Hashed password of 'rocksalt' is " + hashValue);
        assertTrue(CryptoUtils.verifyHash("rocksalt", salt, hashValue));
    }
        
    @Test
    public void testEncryptDecryptAES() throws ChiliLogException
    {
        String encryptedText = CryptoUtils.encryptAES("secret", "my password");
        String decryptedText = CryptoUtils.decryptAES(encryptedText, "my password");
        _logger.info("Encrypted 'secret' is " + encryptedText);
        assertEquals("secret", decryptedText);
    }
    
    @Test
    public void testEncryptDecrypt2() throws ChiliLogException
    {
        String encryptedText = CryptoUtils.encryptTripleDES("secret", "my password");
        String decryptedText = CryptoUtils.decryptTripleDES(encryptedText, "my password");
        _logger.info("Encrypted 'secret' is " + encryptedText);
        assertEquals("secret", decryptedText);
    }

}
