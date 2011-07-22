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

package com.chililog.server.management.workers;

/**
 * <p>
 * Repository Property API Object. Used for representing key-value pairs.
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryPropertyInfoAO extends AO
{
    private String _key;
    private String _value;

    /**
     * Basic constructor
     */
    public RepositoryPropertyInfoAO()
    {
        return;
    }

    /**
     * Constructor that sets the initial key and value of this property
     * 
     * @param key
     *            Property key
     * @param value
     *            Property value
     */
    public RepositoryPropertyInfoAO(String key, String value)
    {
        _key = key;
        _value = value;
        return;
    }

    public String getKey()
    {
        return _key;
    }

    public void setKey(String key)
    {
        _key = key;
    }

    public String getValue()
    {
        return _value;
    }

    public void setValue(String value)
    {
        _value = value;
    }

}
