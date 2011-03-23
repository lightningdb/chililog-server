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

package com.chililog.server.engine.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.chililog.server.data.RepositoryFieldInfoBO;

public class FieldParserTest
{

    @Test
    public void testParseString() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);

        StringFieldParser p = new StringFieldParser(repoFieldInfo);

        assertEquals("abc", p.parse("abc"));
        assertEquals("", p.parse(""));
        assertEquals(null, p.parse(null));
    }

    @Test
    public void testParseInteger() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);

        IntegerFieldParser p = new IntegerFieldParser(repoFieldInfo);

        assertEquals(123, p.parse("123"));
        assertEquals(123, p.parse(" 123 "));

        try
        {
            p.parse("123.45");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            p.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            p.parse("123adb");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            p.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        p = new IntegerFieldParser(repoFieldInfo);

        assertEquals(123, p.parse("123"));
        assertEquals(123, p.parse(" 123 "));
        assertEquals(1, p.parse("abc"));
        assertEquals(1, p.parse("123abc"));
        assertEquals(1, p.parse(""));
        assertEquals(1, p.parse(null));
    }

    @Test
    public void testParseIntegerNumberFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.NUMBER_FORMAT_PROPERTY_NAME, "#,##0");

        IntegerFieldParser p = new IntegerFieldParser(repoFieldInfo);

        assertEquals(222222222, p.parse("222222222"));
        assertEquals(1234, p.parse("1,234"));
        assertEquals(123, p.parse("123"));
        assertEquals(123, p.parse(" 123 "));
        assertEquals(2222, p.parse("2222d df22222"));
        assertEquals(123, p.parse("123.11"));
        assertEquals(123, p.parse("123.99"));

        try
        {
            p.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            p.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        p = new IntegerFieldParser(repoFieldInfo);

        assertEquals(222222222, p.parse("222222222"));
        assertEquals(1234, p.parse("1,234"));
        assertEquals(123, p.parse("123"));
        assertEquals(123, p.parse(" 123 "));
        assertEquals(2222, p.parse("2222d df22222"));
        assertEquals(1, p.parse("abc"));
        assertEquals(123, p.parse("123abc"));
        assertEquals(1, p.parse(""));
        assertEquals(1, p.parse(null));

    }

    @Test
    public void testParseLong() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Long);

        LongIntegerFieldParser p = new LongIntegerFieldParser(repoFieldInfo);

        assertEquals(123L, p.parse("123"));
        assertEquals(123L, p.parse(" 123 "));

        try
        {
            p.parse("123.45");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            p.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            p.parse("123adb");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            p.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        p = new LongIntegerFieldParser(repoFieldInfo);

        assertEquals(123L, p.parse("123"));
        assertEquals(123L, p.parse(" 123 "));
        assertEquals(1L, p.parse("abc"));
        assertEquals(1L, p.parse("123abc"));
        assertEquals(1L, p.parse(""));
        assertEquals(1L, p.parse(null));
    }

    @Test
    public void testParseLongNumberFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Long);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.NUMBER_FORMAT_PROPERTY_NAME, "#,##0");

        LongIntegerFieldParser p = new LongIntegerFieldParser(repoFieldInfo);

        assertEquals(222222222L, p.parse("222222222"));
        assertEquals(1234L, p.parse("1,234"));
        assertEquals(123L, p.parse("123"));
        assertEquals(123L, p.parse(" 123 "));
        assertEquals(2222L, p.parse("2222d df22222"));
        assertEquals(123L, p.parse("123.45"));
        assertEquals(123L, p.parse("123.99"));

        try
        {
            p.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            p.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        p = new LongIntegerFieldParser(repoFieldInfo);

        assertEquals(222222222L, p.parse("222222222"));
        assertEquals(1234L, p.parse("1,234"));
        assertEquals(123L, p.parse("123"));
        assertEquals(123L, p.parse(" 123 "));
        assertEquals(2222L, p.parse("2222d df22222"));
        assertEquals(1L, p.parse("abc"));
        assertEquals(123L, p.parse("123abc"));
        assertEquals(1L, p.parse(""));
        assertEquals(1L, p.parse(null));

    }

    @Test
    public void testParseDouble() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Double);

        DoubleFieldParser p = new DoubleFieldParser(repoFieldInfo);

        assertEquals(123d, p.parse("123"));
        assertEquals(123d, p.parse(" 123 "));
        assertEquals(123.45, p.parse("123.45"));
        assertEquals(123.99, p.parse("123.99"));

        try
        {
            p.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            p.parse("123adb");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            p.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        p = new DoubleFieldParser(repoFieldInfo);

        assertEquals(123d, p.parse("123"));
        assertEquals(123d, p.parse(" 123 "));
        assertEquals(1d, p.parse("abc"));
        assertEquals(1d, p.parse("123abc"));
        assertEquals(1d, p.parse(""));
        assertEquals(1d, p.parse(null));
    }

    @Test
    public void testParseDoubleNumberFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Double);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.NUMBER_FORMAT_PROPERTY_NAME, "#,##0");

        DoubleFieldParser p = new DoubleFieldParser(repoFieldInfo);

        assertEquals(222222222d, p.parse("222222222"));
        assertEquals(1234d, p.parse("1,234"));
        assertEquals(123d, p.parse("123"));
        assertEquals(123d, p.parse(" 123 "));
        assertEquals(2222d, p.parse("2222d df22222"));
        assertEquals(123.45, p.parse("123.45"));
        assertEquals(123.99, p.parse("123.99"));

        try
        {
            p.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            p.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        p = new DoubleFieldParser(repoFieldInfo);

        assertEquals(222222222d, p.parse("222222222"));
        assertEquals(1234d, p.parse("1,234"));
        assertEquals(123d, p.parse("123"));
        assertEquals(123d, p.parse(" 123 "));
        assertEquals(2222d, p.parse("2222d df22222"));
        assertEquals(1d, p.parse("abc"));
        assertEquals(123d, p.parse("123abc"));
        assertEquals(1d, p.parse(""));
        assertEquals(1d, p.parse(null));

    }

    @Test
    public void testParseBoolean() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Boolean);

        BooleanFieldParser p = new BooleanFieldParser(repoFieldInfo);

        assertEquals(true, p.parse("true"));
        assertEquals(true, p.parse("True"));
        assertEquals(true, p.parse("TRUE"));
        assertEquals(false, p.parse("asfd"));
        assertEquals(false, p.parse(""));
        assertEquals(false, p.parse(null));
    }

    @Test
    public void testParseBooleanFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Boolean);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.TRUE_PATTERN_PROPERTY_NAME, "[\\s]*[A-Z]+[\\s]*");

        BooleanFieldParser p = new BooleanFieldParser(repoFieldInfo);

        assertEquals(true, p.parse("TRUE"));
        assertEquals(true, p.parse("AAAAAA"));
        assertEquals(false, p.parse("true"));
        assertEquals(false, p.parse("True"));
        assertEquals(false, p.parse("asfd123"));
        assertEquals(false, p.parse(""));
        assertEquals(false, p.parse(null));
    }

    @Test
    public void testParseDate() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Date);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss");

        DateFieldParser p = new DateFieldParser(repoFieldInfo);

        assertEquals(new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime(), p.parse("2011-01-02 03:04:05"));
        assertEquals(new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime(), p.parse("2011-1-2 3:4:5"));
        assertEquals(new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime(),
                p.parse("2011-1-2 3:4:5 this is not parsed"));

        try
        {
            p.parse("xx 2011-1-2 3:4:5 zzz");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            p.parse("2011-01-02");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            p.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            p.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "2011-01-02 03:04:05");
        p = new DateFieldParser(repoFieldInfo);

        assertEquals(new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime(), p.parse("123"));
    }

    @Test
    public void testParseDateFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Date);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss,SSS");
        
        DateFieldParser p = new DateFieldParser(repoFieldInfo);

        Date d = new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime();
        d.setTime(d.getTime() + 123);
        assertEquals(d, p.parse("2011-01-02 03:04:05,123"));

        try
        {
            p.parse("2011-01-02");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            p.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            p.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "2011-01-02 03:04:05,123");
        p = new DateFieldParser(repoFieldInfo);

        d = new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime();
        d.setTime(d.getTime() + 123);

        assertEquals(d, p.parse("123"));
        assertEquals(d, p.parse("xxxxx"));
    }

    @Test
    public void testParseDateFormatTimezone() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Date);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss,SSSZ");

        DateFieldParser p = new DateFieldParser(repoFieldInfo);

        Date d = new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime();
        d.setTime(d.getTime() + 123);
        assertEquals(d, p.parse("2011-01-02 03:04:05,123+1100"));

        // Set default timezone as UTC - i.e. all time is assumed to be UTC
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss,SSS");
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_TIMEZONE_PROPERTY_NAME, "UTC");

        p = new DateFieldParser(repoFieldInfo);

        GregorianCalendar c = new GregorianCalendar(2011, 0, 2, 3, 4, 5);
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        d = c.getTime();
        d.setTime(d.getTime() + 123);
        assertEquals(d, p.parse("2011-01-02 03:04:05,123"));
    }

    @Test
    public void testPreParsingExamples()
    {
        // Strip white spaces
        Pattern p = Pattern.compile("[\\s]*([A-Z]+)[\\s]*");
        Matcher m = p.matcher("ABC");
        assertTrue(m.find());
        assertEquals("ABC", m.group(1));

        // http://download.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html
        // Capturing groups are numbered by counting their opening parentheses from left to right. In the expression
        // ((A)(B(C))), for example, there are four such groups:
        //
        // 1 ((A)(B(C)))
        // 2 (A)
        // 3 (B(C))
        // 4 (C)
        // Group zero always stands for the entire expression.

        // Matching by group non capturing group
        p = Pattern.compile("^(?:[0-9]+ [\\w\\.\\[\\]]+ )([\\w\\.]+) ");
        m = p.matcher("913745345 [Main] com.test.abc - test");
        assertTrue(m.find());
        assertEquals("com.test.abc", m.group(1));

        m = p.matcher("test");
        assertFalse(m.find());

    }
    

}
