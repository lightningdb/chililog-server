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

/**
 * JUnit test cases for <code>ChiliLogException</code>
 * @author vibul
 *
 */
public class ChiliLogExceptionTest
{
    private static Logger _logger = Logger.getLogger(ChiliLogExceptionTest.class);

    @Test
    public void testNoSubstitutes()
    {
        ChiliLogException ex = new ChiliLogException("Test"); 
        assertEquals("Test12", ex.getMessage());
        assertEquals("Test", ex.getErrorCode());
    }
    
    @Test
    public void testSubstitutes()
    {
        ChiliLogException ex = new ChiliLogException("TestPlaceholder", "hello"); 
        assertEquals("Test hello", ex.getMessage());
        assertEquals("TestPlaceholder", ex.getErrorCode());
    }

    @Test
    public void testNoCode()
    {
        ChiliLogException ex = new ChiliLogException("This is a message with no matching code"); 
        assertEquals("This is a message with no matching code", ex.getMessage());
        assertEquals("This is a message with no matching code", ex.getErrorCode());
    }
    
    @Test
    public void testWrapping()
    {
        try
        {
            throw new NullPointerException("inner exception");
        }
        catch (Exception innerEx)
        {
            ChiliLogException ex = new ChiliLogException(innerEx, "Test"); 
            assertEquals("Test12", ex.getMessage());
            assertEquals("Test", ex.getErrorCode());
            assertEquals(ex.getCause().getClass().getName(), NullPointerException.class.getName());
            assertEquals(ex.getCause().getMessage(), "inner exception");
            
            _logger.error(ex.toString());
            _logger.error(ex.getStackTraceAsString());
        }
        
    }
}
