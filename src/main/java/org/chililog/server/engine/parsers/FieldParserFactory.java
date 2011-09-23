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

import java.text.ParseException;

import org.apache.commons.lang.NotImplementedException;
import org.chililog.server.data.RepositoryFieldConfigBO;

/**
 * Factory to instance field parsers
 * 
 * @author vibul
 * 
 */
public class FieldParserFactory {

    /**
     * Instances the correct field parser for the field. The parser will convert strings into strongly typed field
     * values.
     * 
     * @param repoFieldInfo
     *            Field meta data
     * @return Field parser
     * @throws ParseException
     */
    public static FieldParser getParser(RepositoryFieldConfigBO repoFieldInfo) throws ParseException {
        switch (repoFieldInfo.getDataType()) {
            case String:
                return new StringFieldParser(repoFieldInfo);
            case Integer:
                return new IntegerFieldParser(repoFieldInfo);
            case Long:
                return new LongIntegerFieldParser(repoFieldInfo);
            case Date:
                return new DateFieldParser(repoFieldInfo);
            case Boolean:
                return new BooleanFieldParser(repoFieldInfo);
            case Double:
                return new DoubleFieldParser(repoFieldInfo);
            default:
                throw new NotImplementedException("Field parser for data type: "
                        + repoFieldInfo.getDataType().toString());
        }
    }
}
