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

package com.chililog.server.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bson.BSONCallback;

import com.chililog.server.App;
import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.Log4JLogger;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.util.JSONCallback;

/**
 * <p>
 * Controller to read/write into a repository with a json log format. For example,
 * </p>
 * <code>
 * { "field1": 1, "field2": "abc", "field3": 123 }
 * </code>
 * <p>
 * Field definitions are not required because it is defined in the JSON format.
 * </p>
 * <p>
 * Unlike other data controllers, this controller is NOT a singleton. This is because many repositories may need to use
 * this class
 * </p>
 * 
 * @author vibul
 * 
 */
public class JsonRepositoryController extends RepositoryController
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(App.class);
    private String _mongoDBCollectionName;
    private RepositoryInfoBO _repoInfo;
    private Exception _lastParseError = null;
    private Pattern _datePattern = null;
    private String _dateFormat = null;
    private Pattern _longNumberPattern = null;

    /**
     * <p>
     * Regular expression pattern to use in order to identify a date within a string. For example, if
     * "^([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)$", then "2000-01-01T01:01:01Z" will be treated as a
     * Date.
     * </p>
     * <p>
     * If not set, date parsing will not be performed.
     * </p>
     */
    public static final String DATE_PATTERN_REPO_PROPERTY_NAME = "date_pattern";

    /**
     * <p>
     * Date format to use with {@link SimpleDateFormat} when parsing for a date format. For example,
     * "yyyy-MM-dd'T'HH:mm:ssZ". Parsing is only performed on a {@link String} if it passes the string length and
     * pattern checks.
     * </p>
     * <p>
     * If not set, date parsing will not be performed.
     * </p>
     */
    public static final String DATE_FORMAT_REPO_PROPERTY_NAME = "date_format";

    /**
     * <p>
     * Regular expression pattern to use in order to identify a long within a string. For example, if "^([0-9]+L)$",
     * then "88888L" will be treated as a Long.
     * </p>
     * <p>
     * If not set, long number parsing on a string will not be performed.
     * </p>
     */
    public static final String LONG_NUMBER_PATTERN_REPO_PROPERTY_NAME = "long_suffix";

    /**
     * <p>
     * Basic constructor
     * </p>
     * 
     * @param repoInfo
     *            information on the repository we are going to read/write
     * @throws ChiliLogException
     */
    public JsonRepositoryController(RepositoryInfoBO repoInfo) throws ChiliLogException
    {
        _repoInfo = repoInfo;

        _mongoDBCollectionName = repoInfo.getMongoDBCollectionName();
        if (StringUtils.isBlank(repoInfo.getName()))
        {
            throw new ChiliLogException(Strings.REPO_NAME_NOT_SET_ERROR);
        }

        Hashtable<String, String> properties = repoInfo.getProperties();

        String s = properties.get(DATE_PATTERN_REPO_PROPERTY_NAME);
        if (!StringUtils.isBlank(s))
        {
            _datePattern = Pattern.compile(s);
        }

        _dateFormat = properties.get(DATE_FORMAT_REPO_PROPERTY_NAME);

        s = properties.get(LONG_NUMBER_PATTERN_REPO_PROPERTY_NAME);
        if (!StringUtils.isBlank(s))
        {
            _longNumberPattern = Pattern.compile(s);
        }

        _repoInfo.loadFieldDataTypeProperties();
        return;
    }

    /**
     * Returns the name of the mongoDB collection for this business object
     */
    @Override
    protected String getDBCollectionName()
    {
        return _mongoDBCollectionName;
    }

    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
     * @param textEntry
     *            The text for this entry to parse
     * @return <code>RepositoryEntryBO</code> ready for saving to mongoDB. If the entry cannot be parsed, then null is
     *         returned
     */
    public RepositoryEntryBO parse(String textEntry)
    {
        try
        {
            _lastParseError = null;

            if (StringUtils.isBlank(textEntry))
            {
                throw new ChiliLogException(Strings.REPO_PARSE_BLANK_ERROR, _repoInfo.getName());
            }

            JSONParser parser = new JSONParser(textEntry, _datePattern, _dateFormat, _longNumberPattern);
            DBObject dbObject = (DBObject) parser.parse();
            return new RepositoryEntryBO(dbObject, textEntry);
        }
        catch (Exception ex)
        {
            _lastParseError = ex;
            _logger.error(ex, "Error parsing json entry: " + textEntry);
            return null;
        }
    }

    /**
     * Saves the repository into mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param repositoryEntry
     *            Repository entry to save
     * @throws ChiliLogException
     *             if there are errors
     */
    public void save(DB db, RepositoryEntryBO repositoryEntry) throws ChiliLogException
    {
        super.save(db, repositoryEntry);
    }

    /**
     * Removes the specified user from mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param repositoryEntry
     *            Repository entry to remove
     * @throws ChiliLogException
     *             if there are errors
     */
    public void remove(DB db, RepositoryEntryBO repositoryEntry) throws ChiliLogException
    {
        super.remove(db, repositoryEntry);
    }

    /**
     * Returns the last error processed by <code>parse</code>.
     */
    public Exception getLastParseError()
    {
        return _lastParseError;
    }

    /**
     * <p>
     * Parser for JSON objects
     * </p>
     * <p>
     * Supports all types described at www.json.org, except for numbers with "e" or "E" in them.
     * </p>
     * <p>
     * Modified from https://github.com/mongodb/mongo-java-driver/blob/master/src/main/com/mongodb/util/JSON.java.
     * </p>
     * <p>
     * Modified to support:
     * <ul>
     * <li>Long - number >10 digits or string of digits with L at the end "123412341234L"</li>
     * <li>Date - string in the format "2010-01-01T01:01:01Z". No other formats supported.</li>
     * </p>
     */
    static class JSONParser
    {

        private Pattern _datePattern;
        private Pattern _longNumberPattern;
        private SimpleDateFormat _dateFormat;

        String s;
        int pos = 0;
        BSONCallback _callback;

        /**
         * Create a new parser.
         */
        public JSONParser(String s, Pattern datePattern, String dateFormat, Pattern longNumberPattern)
        {
            this(s, null);

            _datePattern = datePattern;
            _dateFormat = StringUtils.isBlank(dateFormat) ? null : new SimpleDateFormat(dateFormat);
            _longNumberPattern = longNumberPattern;
        }

        /**
         * Create a new parser.
         */
        public JSONParser(String s, BSONCallback callback)
        {
            this.s = s;
            _callback = (callback == null) ? new JSONCallback() : callback;
        }

        /**
         * Parse an unknown type.
         * 
         * @return Object the next item
         * @throws JSONParseException
         *             if invalid JSON is found
         */
        public Object parse()
        {
            return parse(null);
        }

        /**
         * Parse an unknown type.
         * 
         * @return Object the next item
         * @throws JSONParseException
         *             if invalid JSON is found
         */
        protected Object parse(String name)
        {
            Object value = null;
            char current = get();

            switch (current)
            {
                // null
                case 'n':
                    read('n');
                    read('u');
                    read('l');
                    read('l');
                    value = null;
                    break;
                // true
                case 't':
                    read('t');
                    read('r');
                    read('u');
                    read('e');
                    value = true;
                    break;
                // false
                case 'f':
                    read('f');
                    read('a');
                    read('l');
                    read('s');
                    read('e');
                    value = false;
                    break;
                // string
                case '\'':
                case '\"':
                    String stringValue = parseString();

                    // Check for long
                    value = stringValue;
                    if (!StringUtils.isBlank(stringValue))
                    {
                        if (_longNumberPattern != null)
                        {
                            // If "8888888888L", then remove L and parse long
                            Matcher m = _longNumberPattern.matcher(stringValue);
                            if (m.matches())
                            {
                                try
                                {
                                    value = Long.parseLong(m.group(1));
                                }
                                catch (Exception ex)
                                {
                                    throw new JSONParseException(s, pos);
                                }
                            }
                        }
                        if (_dateFormat != null && _datePattern != null)
                        {
                            // If "2010-01-01T01:01:01Z", then parse
                            Matcher m = _datePattern.matcher(stringValue);
                            if (m.matches())
                            {
                                try
                                {
                                    String dateString = m.group(1);
                                    if (dateString.endsWith("Z"))
                                    {
                                        // Simple date format does not recognise Z time zone so make it GMT
                                        dateString = dateString.substring(0, dateString.length() - 1) + "GMT";
                                    }
                                    value = _dateFormat.parse(dateString);
                                }
                                catch (Exception ex)
                                {
                                    throw new JSONParseException(ex, s, pos);
                                }
                            }
                        }
                    }

                    break;
                // number
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '+':
                case '-':
                    value = parseNumber();
                    break;
                // array
                case '[':
                    value = parseArray(name);
                    break;
                // object
                case '{':
                    value = parseObject(name);
                    break;
                default:
                    throw new JSONParseException(s, pos);
            }
            return value;
        }

        /**
         * Parses an object for the form <i>{}</i> and <i>{ members }</i>.
         * 
         * @return DBObject the next object
         * @throws JSONParseException
         *             if invalid JSON is found
         */
        public Object parseObject()
        {
            return parseObject(null);
        }

        /**
         * Parses an object for the form <i>{}</i> and <i>{ members }</i>.
         * 
         * @return DBObject the next object
         * @throws JSONParseException
         *             if invalid JSON is found
         */
        protected Object parseObject(String name)
        {
            if (name != null)
            {
                _callback.objectStart(name);
            }
            else
            {
                _callback.objectStart();
            }

            read('{');
            @SuppressWarnings("unused")
            char current = get();
            while (get() != '}')
            {
                String key = parseString();
                read(':');
                Object value = parse(key);
                doCallback(key, value);

                if ((current = get()) == ',')
                {
                    read(',');
                }
                else
                {
                    break;
                }
            }
            read('}');

            return _callback.objectDone();
        }

        protected void doCallback(String name, Object value)
        {
            if (value == null)
            {
                _callback.gotNull(name);
            }
            else if (value instanceof String)
            {
                _callback.gotString(name, (String) value);
            }
            else if (value instanceof Boolean)
            {
                _callback.gotBoolean(name, (Boolean) value);
            }
            else if (value instanceof Integer)
            {
                _callback.gotInt(name, (Integer) value);
            }
            else if (value instanceof Long)
            {
                _callback.gotLong(name, (Long) value);
            }
            else if (value instanceof Double)
            {
                _callback.gotDouble(name, (Double) value);
            }
            else if (value instanceof Date)
            {
                _callback.gotDate(name, ((Date) value).getTime());
            }
        }

        /**
         * Read the current character, making sure that it is the expected character. Advances the pointer to the next
         * character.
         * 
         * @param ch
         *            the character expected
         * 
         * @throws JSONParseException
         *             if the current character does not match the given character
         */
        public void read(char ch)
        {
            if (!check(ch))
            {
                throw new JSONParseException(s, pos);
            }
            pos++;
        }

        public char read()
        {
            if (pos >= s.length())
                throw new IllegalStateException("string done");
            return s.charAt(pos++);
        }

        /**
         * Read the current character, making sure that it is a hexidecimal character.
         * 
         * @throws JSONParseException
         *             if the current character is not a hexidecimal character
         */
        public void readHex()
        {
            if (pos < s.length()
                    && ((s.charAt(pos) >= '0' && s.charAt(pos) <= '9')
                            || (s.charAt(pos) >= 'A' && s.charAt(pos) <= 'F') || (s.charAt(pos) >= 'a' && s.charAt(pos) <= 'f')))
            {
                pos++;
            }
            else
            {
                throw new JSONParseException(s, pos);
            }
        }

        /**
         * Checks the current character, making sure that it is the expected character.
         * 
         * @param ch
         *            the character expected
         * 
         * @throws JSONParseException
         *             if the current character does not match the given character
         */
        public boolean check(char ch)
        {
            return get() == ch;
        }

        /**
         * Advances the position in the string past any whitespace.
         */
        public void skipWS()
        {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos)))
            {
                pos++;
            }
        }

        /**
         * Returns the current character. Returns -1 if there are no more characters.
         * 
         * @return the next character
         */
        public char get()
        {
            skipWS();
            if (pos < s.length())
                return s.charAt(pos);
            return (char) -1;
        }

        /**
         * Parses a string.
         * 
         * @return the next string.
         * @throws JSONParseException
         *             if invalid JSON is found
         */
        public String parseString()
        {
            char quot;
            if (check('\''))
                quot = '\'';
            else if (check('\"'))
                quot = '\"';
            else
                throw new JSONParseException(s, pos);

            char current;

            read(quot);
            StringBuilder buf = new StringBuilder();
            int start = pos;
            while (pos < s.length() && (current = s.charAt(pos)) != quot)
            {
                if (current == '\\')
                {
                    pos++;

                    char x = get();

                    char special = 0;

                    switch (x)
                    {

                        case 'u':
                        { // decode unicode
                            buf.append(s.substring(start, pos - 1));
                            pos++;
                            int tempPos = pos;

                            readHex();
                            readHex();
                            readHex();
                            readHex();

                            int codePoint = Integer.parseInt(s.substring(tempPos, tempPos + 4), 16);
                            buf.append((char) codePoint);

                            start = pos;
                            continue;
                        }
                        case 'n':
                            special = '\n';
                            break;
                        case 'r':
                            special = '\r';
                            break;
                        case 't':
                            special = '\t';
                            break;
                        case 'b':
                            special = '\b';
                            break;
                        case '"':
                            special = '\"';
                            break;
                        case '\\':
                            special = '\\';
                            break;
                    }

                    buf.append(s.substring(start, pos - 1));
                    if (special != 0)
                    {
                        pos++;
                        buf.append(special);
                    }
                    start = pos;
                    continue;
                }
                pos++;
            }
            read(quot);

            buf.append(s.substring(start, pos - 1));
            return buf.toString();
        }

        /**
         * Parses a number.
         * 
         * @return the next number (int or double).
         * @throws JSONParseException
         *             if invalid JSON is found
         */
        public Number parseNumber()
        {

            @SuppressWarnings("unused")
            char current = get();
            int start = this.pos;
            boolean isDouble = false;

            if (check('-') || check('+'))
            {
                pos++;
            }

            outer: while (pos < s.length())
            {
                switch (s.charAt(pos))
                {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        pos++;
                        break;
                    case '.':
                        isDouble = true;
                        parseFraction();
                        break;
                    case 'e':
                    case 'E':
                        isDouble = true;
                        parseExponent();
                        break;
                    default:
                        break outer;
                }
            }

            if (isDouble)
                return Double.valueOf(s.substring(start, pos));
            if (pos - start >= 10)
                return Long.valueOf(s.substring(start, pos));
            return Integer.valueOf(s.substring(start, pos));
        }

        /**
         * Advances the pointed through <i>.digits</i>.
         */
        public void parseFraction()
        {
            // get past .
            pos++;

            outer: while (pos < s.length())
            {
                switch (s.charAt(pos))
                {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        pos++;
                        break;
                    case 'e':
                    case 'E':
                        parseExponent();
                        break;
                    default:
                        break outer;
                }
            }
        }

        /**
         * Advances the pointer through the exponent.
         */
        public void parseExponent()
        {
            // get past E
            pos++;

            if (check('-') || check('+'))
            {
                pos++;
            }

            outer: while (pos < s.length())
            {
                switch (s.charAt(pos))
                {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        pos++;
                        break;
                    default:
                        break outer;
                }
            }
        }

        /**
         * Parses the next array.
         * 
         * @return the array
         * @throws JSONParseException
         *             if invalid JSON is found
         */
        public Object parseArray()
        {
            return parseArray(null);
        }

        /**
         * Parses the next array.
         * 
         * @return the array
         * @throws JSONParseException
         *             if invalid JSON is found
         */
        protected Object parseArray(String name)
        {
            if (name != null)
            {
                _callback.arrayStart(name);
            }
            else
            {
                _callback.arrayStart();
            }

            read('[');

            int i = 0;
            char current = get();
            while (current != ']')
            {
                String elemName = String.valueOf(i++);
                Object elem = parse(elemName);
                doCallback(elemName, elem);

                if ((current = get()) == ',')
                {
                    read(',');
                }
                else if (current == ']')
                {
                    break;
                }
                else
                {
                    throw new JSONParseException(s, pos);
                }
            }

            read(']');

            return _callback.arrayDone();
        }

    }

    /**
     * Exception throw when invalid JSON is passed to JSONParser.
     * 
     * This exception creates a message that points to the first offending character in the JSON string:
     * 
     * <pre>
     * { "x" : 3, "y" : 4, some invalid json.... }
     *                     ^
     * </pre>
     */
    static class JSONParseException extends RuntimeException
    {

        private static final long serialVersionUID = -4415279469780082174L;

        String s;
        int pos;

        public String getMessage()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append(s);
            sb.append("\n");
            for (int i = 0; i < pos; i++)
            {
                sb.append(" ");
            }
            sb.append("^");
            return sb.toString();
        }

        public JSONParseException(String s, int pos)
        {
            this.s = s;
            this.pos = pos;
        }

        public JSONParseException(Throwable ex, String s, int pos)
        {
            super(ex);
            this.s = s;
            this.pos = pos;
        }
    }

}
