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

import java.io.Reader;
import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.workbench.HttpRequestHandler;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>
 * Serializes Java objects into JSON format and deserializes JSON back into Java object(s).
 * </p>
 * <p>
 * Wrapper for GSON (http://code.google.com/p/google-gson/).
 * </p>
 * 
 * @author vibul
 * 
 */
public class JsonTranslator {
    private static Log4JLogger _logger = Log4JLogger.getLogger(HttpRequestHandler.class);

    private Gson _gson = null;

    /**
     * Returns the singleton instance for this class
     */
    public static JsonTranslator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     * 
     * @see http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class SingletonHolder {
        public static final JsonTranslator INSTANCE = new JsonTranslator();
    }

    /**
     * <p>
     * Singleton constructor that creates our reusable GSON object.
     * </p>
     * 
     * <p>
     * If there are any errors, the JVM is terminated. Without valid application properties, we will fall over elsewhere
     * so might as well terminate here.
     * </p>
     */
    private JsonTranslator() {
        try {
            GsonBuilder builder = new GsonBuilder();
            if (AppProperties.getInstance().getJsonPretty()) {
                builder.setPrettyPrinting();
            }
            builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            builder.setFieldNamingStrategy(new ChiliLogFieldNamingStrategy());
            builder.disableHtmlEscaping();
            _gson = builder.create();
        }
        catch (Exception e) {
            _logger.error("Error initializing JSON translator: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Serialize a Java object to a String
     * 
     * @param o
     *            Object to serialize
     * @return String serialization of the java object in JSON format.
     */
    public String toJson(Object o) {
        return _gson.toJson(o);
    }

    /**
     * Serialize a Java object into the specified appender.
     * 
     * @param o
     *            Object to serialize
     * @param appender
     *            Appender into which the JSON form of the object will be written
     */
    public void toJson(Object o, Appendable appender) {
        _gson.toJson(o, appender);
    }

    /**
     * Deserializes a JSON string into a Java object of the specified class.
     * 
     * @param <T>
     *            the type of the desired object
     * @param json
     *            the string from which the object is to be deserialized
     * @param classOfT
     *            the class of T
     * @return an object of type T from the string. null if json is null or empty string.
     */
    public <T> T fromJson(String json, Class<T> classOfT) {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        // Have to trim because of bug with trailing white space
        // http://groups.google.com/group/google-gson/browse_thread/thread/6f12cf80b12a85b8
        return _gson.fromJson(json.trim(), classOfT);
    }

    /**
     * Deserializes a JSON stream into a Java object of the specified class.
     * 
     * @param <T>
     *            the type of the desired object
     * @param json
     *            the reader containing the JSON string from which the object is to be deserialized
     * @param classOfT
     *            the class of T
     * @return an object of type T from the reader
     */
    public <T> T fromJson(Reader json, Class<T> classOfT) {
        return _gson.fromJson(json, classOfT);
    }

    /**
     * This class tells gson to convert <code>_myFieldName</code> to <code>MyFieldName</code>
     * 
     * @author vibul
     * 
     */
    public static class ChiliLogFieldNamingStrategy implements FieldNamingStrategy {

        @Override
        public String translateName(Field field) {
            StringBuilder sb = new StringBuilder(field.getName());
            sb.replace(0, 2, sb.substring(1, 2).toUpperCase());
            return sb.toString();
        }

    }
}
