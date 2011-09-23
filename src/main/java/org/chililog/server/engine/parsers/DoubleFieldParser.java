//
// Copyright 2010 Cinch Logic Pty Ltd
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

package org.chililog.server.engine.parsers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.data.RepositoryFieldConfigBO;

/**
 * Parses an double floating point number field.
 * 
 * @author vibul
 * 
 */
public class DoubleFieldParser extends FieldParser {
    private Double _defaultValue = null;
    private NumberFormat _numberFormatter = null;

    /**
     * Constructor
     * 
     * @param repoFieldInfo
     *            Field meta data
     * @throws ParseException
     */
    public DoubleFieldParser(RepositoryFieldConfigBO repoFieldInfo) throws ParseException {
        super(repoFieldInfo);

        Hashtable<String, String> properties = repoFieldInfo.getProperties();

        String d = properties.get(RepositoryFieldConfigBO.DEFAULT_VALUE_PROPERTY_NAME);
        String s = properties.get(RepositoryFieldConfigBO.NUMBER_FORMAT_PROPERTY_NAME);
        if (!StringUtils.isBlank(s)) {
            _numberFormatter = new DecimalFormat(s);
        }

        if (!StringUtils.isBlank(d)) {
            if (_numberFormatter == null) {
                _defaultValue = Double.parseDouble(d);
            }
            else {
                _defaultValue = _numberFormatter.parse(d).doubleValue();
            }
        }
    }

    /**
     * Parses a double floating point number.
     */
    @Override
    public Object parse(String value) throws ParseException {
        return parseDouble(value);
    }

    /**
     * <p>
     * Parse a double.
     * </p>
     * <p>
     * If a number patter is set, the the number pattern is used for parsing. Otherwise, standard integer parsing is
     * performed where only digits are allowed.
     * </p>
     * <p>
     * If parsing fails and a default value is set, the default value is returned.
     * </p>
     * 
     * @param value
     *            string value to parse into an integer
     * @return integer value.
     * @throws ParseException
     */
    private Double parseDouble(String value) throws ParseException {
        try {
            value = preparse(value);

            if (!StringUtils.isBlank(value)) {
                value = value.trim();
            }
            if (_numberFormatter == null) {
                return Double.parseDouble(value);
            }
            else {
                return _numberFormatter.parse(value).doubleValue();
            }
        }
        catch (ParseException ex) {
            if (_defaultValue != null) {
                return (Double) _defaultValue;
            }
            throw ex;
        }
        catch (NumberFormatException ex2) {
            if (_defaultValue != null) {
                return (Double) _defaultValue;
            }
            throw ex2;
        }
        catch (NullPointerException ex3) {
            if (_defaultValue != null) {
                return (Double) _defaultValue;
            }
            throw ex3;
        }
    }

}
