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

import java.security.MessageDigest;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;

import com.chililog.server.common.ChiliLogException;

/**
 * <p>
 * Utilities methods for managing passwords
 * </p>
 * 
 * @author vibul
 * 
 */
public class PasswordManager
{
    /**
     * <p>
     * From a password, a number of iterations and a salt, returns the corresponding hash. For convenience, the salt is
     * stored within the hash.
     * </p>
     * 
     * <p>
     * This convention is used: <code>base64(hash(plainTextPassword + salt)+salt)</code>
     * </p>
     * 
     * @param plainTextPassword
     *            String The password to encrypt
     * @param salt
     *            byte[] The salt. If null, one will be created on your behalf.
     * @return String The hash password
     * @throws ChiliLogException
     *             if SHA-512 is not supported or UTF-8 is not a supported encoding algorithm
     */
    public static String hashPassword(String plainTextPassword, byte[] salt) throws ChiliLogException
    {
        try
        {
            // Uses a secure Random not a simple Random
            if (salt == null)
            {
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                // Salt generation 64 bits long
                salt = new byte[8];
                random.nextBytes(salt);
            }

            // Convert plain text into a byte array.
            byte[] plainTextBytes = plainTextPassword.getBytes("UTF-8");

            // Allocate array, which will hold plain text and salt.
            byte[] plainTextWithSaltBytes = new byte[plainTextBytes.length + salt.length];

            // Copy plain text bytes into resulting array.
            for (int i = 0; i < plainTextBytes.length; i++)
            {
                plainTextWithSaltBytes[i] = plainTextBytes[i];
            }

            // Append salt bytes to the resulting array.
            for (int i = 0; i < salt.length; i++)
            {
                plainTextWithSaltBytes[plainTextBytes.length + i] = salt[i];
            }

            // Create hash
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            byte[] hashBytes = digest.digest(plainTextWithSaltBytes);

            // Create array which will hold hash and original salt bytes.
            byte[] hashWithSaltBytes = new byte[hashBytes.length + salt.length];

            // Copy hash bytes into resulting array.
            for (int i = 0; i < hashBytes.length; i++)
            {
                hashWithSaltBytes[i] = hashBytes[i];
            }

            // Append salt bytes to the result.
            for (int i = 0; i < salt.length; i++)
            {
                hashWithSaltBytes[hashBytes.length + i] = salt[i];
            }

            // Convert hash to string
            Base64 encoder = new Base64(1000, new byte[] {}, false);
            return encoder.encodeToString(hashWithSaltBytes);
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, "Error attempting to hash passwords. " + ex.getMessage());
        }
    }

    /**
     * Verifies if a plain text password is the same as its hash
     * 
     * @param plainTextPassword
     *            plain text password
     * @param hashValue
     *            expected has value as returned by <code>hashPassword</code>.
     * @return true if the password is valid, false if not
     * @throws ChiliLogException
     *             if SHA-512 is not supported or UTF-8 is not a supported encoding algorithm
     */
    public static boolean verifyPassword(String plainTextPassword, String hashValue) throws ChiliLogException
    {
        try
        {
            // Convert base64-encoded hash value into a byte array.
            Base64 decoder = new Base64(1000, new byte[] {}, false);
            byte[] hashWithSaltBytes = decoder.decode(hashValue);

            // We must know size of hash (without salt).
            int hashSizeInBits, hashSizeInBytes;

            // Size of hash is based on the specified algorithm - i.e. 512 for SHA-512.
            hashSizeInBits = 512;

            // Convert size of hash from bits to bytes.
            hashSizeInBytes = hashSizeInBits / 8;

            // Make sure that the specified hash value is long enough.
            if (hashWithSaltBytes.length < hashSizeInBytes)
            {
                return false;
            }

            // Allocate array to hold original salt bytes retrieved from hash.
            byte[] saltBytes = new byte[hashWithSaltBytes.length - hashSizeInBytes];

            // Copy salt from the end of the hash to the new array.
            for (int i = 0; i < saltBytes.length; i++)
            {
                saltBytes[i] = hashWithSaltBytes[hashSizeInBytes + i];
            }

            // Compute a new hash string.
            String expectedHashString = hashPassword(plainTextPassword, saltBytes);

            // If the computed hash matches the specified hash,
            // the plain text value must be correct.
            return (hashValue.equals(expectedHashString));
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, "Error attempting to verify passwords. " + ex.getMessage());
        }
    }

}
