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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.regex.Pattern;

import org.bson.types.BSONTimestamp;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBObject;
import com.mongodb.DBRefBase;

/**
 * <p>
 * Serializer for mongoDB DBObjects into simple JSON
 * </p>
 * <p>
 * Modified from https://github.com/mongodb/mongo-java-driver/blob/master/src/main/com/mongodb/util/JSON.java.
 * </p>
 * <p>
 * Modified to support:
 * <ul>
 * <li>ObjectId - output as string rather than object like: <code>"_id" : { "$oid" : "4d8002fcf24599f624357467"}</code></li>
 * <li>Date - output as string rather than object like:
 * <code>"entry_timestamp" : { "$date" : "2011-03-16T00:23:24Z"}</code>.</li>
 * </p>
 */
public class MongoJsonSerializer {
    /**
     * Serializes an object into it's JSON form
     * 
     * @param o
     *            object to serialize
     * @return String containing JSON form of the object
     */
    public static String serialize(Object o) {
        StringBuilder buf = new StringBuilder();
        serialize(o, buf);
        return buf.toString();
    }

    static void string(StringBuilder a, String s) {
        a.append("\"");
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\\')
                a.append("\\\\");
            else if (c == '"')
                a.append("\\\"");
            else if (c == '\n')
                a.append("\\n");
            else if (c == '\r')
                a.append("\\r");
            else if (c == '\t')
                a.append("\\t");
            else if (c == '\b')
                a.append("\\b");
            else if (c < 32)
                continue;
            else
                a.append(c);
        }
        a.append("\"");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void serialize(Object o, StringBuilder buf) {

        o = Bytes.applyEncodingHooks(o);

        if (o == null) {
            buf.append(" null ");
            return;
        }

        if (o instanceof Number) {
            buf.append(o);
            return;
        }

        if (o instanceof String) {
            string(buf, o.toString());
            return;
        }

        if (o instanceof Iterable) {

            boolean first = true;
            buf.append("[ ");

            for (Object n : (Iterable) o) {
                if (first)
                    first = false;
                else
                    buf.append(" , ");

                serialize(n, buf);
            }

            buf.append("]");
            return;
        }

        if (o instanceof ObjectId) {
            // serialize(new BasicDBObject("$oid", o.toString()), buf);
            string(buf, o.toString());
            return;
        }

        if (o instanceof DBObject) {

            boolean first = true;
            buf.append("{ ");

            DBObject dbo = (DBObject) o;

            for (String name : dbo.keySet()) {
                if (first)
                    first = false;
                else
                    buf.append(" , ");

                string(buf, name);
                buf.append(" : ");
                serialize(dbo.get(name), buf);
            }

            buf.append("}");
            return;
        }

        if (o instanceof Map) {

            boolean first = true;
            buf.append("{ ");

            Map m = (Map) o;

            for (Map.Entry entry : (Set<Map.Entry>) m.entrySet()) {
                if (first)
                    first = false;
                else
                    buf.append(" , ");

                string(buf, entry.getKey().toString());
                buf.append(" : ");
                serialize(entry.getValue(), buf);
            }

            buf.append("}");
            return;
        }

        if (o instanceof Date) {
            Date d = (Date) o;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            format.setCalendar(new GregorianCalendar(new SimpleTimeZone(0, "GMT")));
            // serialize(new BasicDBObject("$date", format.format(d)), buf);
            string(buf, format.format(d));
            return;
        }

        if (o instanceof DBRefBase) {
            buf.append(o);
            return;
        }

        if (o instanceof Boolean) {
            buf.append(o);
            return;
        }

        if (o instanceof byte[] || o instanceof Binary) {
            buf.append("<Binary Data>");
            return;
        }

        if (o instanceof Pattern) {
            DBObject externalForm = new BasicDBObject();
            externalForm.put("$regex", o.toString());
            externalForm.put("$options", Bytes.regexFlags(((Pattern) o).flags()));
            serialize(externalForm, buf);
            return;
        }

        if (o.getClass().isArray()) {
            buf.append("[ ");

            for (int i = 0; i < Array.getLength(o); i++) {
                if (i > 0)
                    buf.append(" , ");
                serialize(Array.get(o, i), buf);
            }

            buf.append("]");
            return;
        }

        if (o instanceof BSONTimestamp) {
            BSONTimestamp t = (BSONTimestamp) o;
            buf.append(t.getTime()).append("|").append(t.getInc());
            return;
        }

        if (o instanceof CodeWScope) {
            CodeWScope c = (CodeWScope) o;

            BasicDBObject temp = new BasicDBObject();
            temp.put("$code", c.getCode());
            temp.put("$scope", c.getScope());
            serialize(temp, buf);
            return;
        }

        if (o instanceof Code) {
            string(buf, ((Code) o).getCode());
            return;
        }

        throw new RuntimeException("json can't serialize type : " + o.getClass());
    }

}
