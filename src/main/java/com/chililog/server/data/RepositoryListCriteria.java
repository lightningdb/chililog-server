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

import java.util.Date;

/**
 * Criteria for selecting user records
 * 
 * @author vibul
 * 
 */
public class RepositoryListCriteria extends ListCriteria
{
    private Date _from = null;
    private Date _to = null;
    private String _jsonCriteria = null;

    /**
     * Basic constructor
     */
    public RepositoryListCriteria()
    {
        return;
    }

    /**
     * Returns the timestamp from which the search should start
     */
    public Date getFrom()
    {
        return _from;
    }

    public void setFrom(Date from)
    {
        _from = from;
    }

    /**
     * Returns the timestamp from which the search should stop
     */
    public Date getTo()
    {
        return _to;
    }

    public void setTo(Date to)
    {
        _to = to;
    }

    /**
     * <p>
     * JSON criteria string. See mongoDB for more details. See http://www.mongodb.org/display/DOCS/Java+Tutorial and
     * http://www.mongodb.org/display/DOCS/Advanced+Queries.
     * </p>
     * <p>
     * For dates, the format "yyyy-MM-dd'T'HH:mm:ssZ" is used. For example, "2011-01-01T23:01:02Z". It assumes the
     * timezone is UTC.
     * </p>
     * <p>
     * For long numbers, a string like "LongNumber(888)" is converted into a long number of value 888. If a JSON number
     * is more 10 digits long, it is also converted to a long integer.
     * </p>
     */
    public String getJsonCriteria()
    {
        return _jsonCriteria;
    }

    public void setJsonCriteria(String jsonCriteria)
    {
        _jsonCriteria = jsonCriteria;
    }

}
