
package com.chililog.server.common;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class JsonTranslatorTest
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(JsonTranslatorTest.class);

    @Test
    public void testBasic()
    {
        TestClass t1 = new TestClass();

        String json = JsonTranslator.getInstance().toJson(t1);
        _logger.debug(json);
        TestClass t2 = JsonTranslator.getInstance().fromJson(json, TestClass.class);

        assertTrue(json.contains("\"StringWithBigName\": \"hello\""));
        assertEquals(t1.getStringWithBigName(), t2.getStringWithBigName());

        assertTrue(json.contains("\"Boolean\": false"));
        assertEquals(t1.isBoolean(), t2.isBoolean());

        assertTrue(json.contains("\"Integer\": 1000"));
        assertEquals(t1.getInteger(), t2.getInteger());

        assertTrue(json.contains("\"Date\": \"2011-01-02T23:22:21+1100\""));
        assertEquals(t1.getDate(), t2.getDate());

        assertTrue(json.contains("\"Colour\": \"Blue\"")); 
        assertEquals(t1.getColour(), t2.getColour());
        
        
        // Show work with trailing white spaces
        JsonTranslator.getInstance().fromJson(json + " \r\n  ", TestClass.class);
    }

    /**
     * Inner classes have to be static before GSON can deserialize
     */
    public static class TestClass
    {
        private String _stringWithBigName = "hello";
        private boolean _boolean = false;
        private int _integer = 1000;
        private Date _date = new GregorianCalendar(2011, 0, 2, 23, 22, 21).getTime();
        private String[] _stringsList = new String[]
        { "one", "two", "three" };
        private Colour _colour = Colour.Blue;
        private long _longNumber = 123123123L;

        public TestClass()
        {
            return;
        }

        public String getStringWithBigName()
        {
            return _stringWithBigName;
        }

        public void setStringWithBigName(String stringWithBigName)
        {
            _stringWithBigName = stringWithBigName;
        }

        public boolean isBoolean()
        {
            return _boolean;
        }

        public void setBoolean(boolean b)
        {
            _boolean = b;
        }

        public int getInteger()
        {
            return _integer;
        }

        public void setInteger(int integer)
        {
            _integer = integer;
        }

        public Date getDate()
        {
            return _date;
        }

        public void setDate(Date date)
        {
            _date = date;
        }

        public String[] getStringsList()
        {
            return _stringsList;
        }

        public void setStringsList(String[] stringsList)
        {
            _stringsList = stringsList;
        }

        public Colour getColour()
        {
            return _colour;
        }

        public void setColour(Colour colour)
        {
            _colour = colour;
        }

        public long getLongNumber()
        {
            return _longNumber;
        }

        public void setLongNumber(long longNumber)
        {
            _longNumber = longNumber;
        }

    }

    public static enum Colour
    {
        Red, Green, Blue
    }
}
