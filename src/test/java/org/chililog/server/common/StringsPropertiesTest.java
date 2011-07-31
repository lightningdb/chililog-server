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

package org.chililog.server.common;

import static org.junit.Assert.*;

import org.chililog.server.common.StringsProperties;
import org.junit.Test;

/**
 * JUnit for <code>StringsProperties</code> class
 * 
 * @author vibul
 * 
 */
public class StringsPropertiesTest
{
    @Test
    public void testOK()
    {
        String s = StringsProperties.getInstance().getString("Test");
        assertTrue(s.equals("Test12"));
    }

    @Test
    public void testBlank()
    {
        String s = StringsProperties.getInstance().getString("TestBlank");
        assertTrue(s.equals(""));
    }

    @Test
    public void testNotFound()
    {
        String s = StringsProperties.getInstance().getString("NotFound");
        assertNull(s);
    }

    @Test
    public void testDefaultValue()
    {
        String s = StringsProperties.getInstance().getString("NotFound", "default");
        assertTrue(s.equals("default"));
    }
}
