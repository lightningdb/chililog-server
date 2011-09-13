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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.TextTokenizer;

import com.mongodb.BasicDBObject;

/**
 * Criteria for selecting repository log entries
 * 
 * @author vibul
 * 
 */
public class RepositoryEntryListCriteria extends ListCriteria
{
    private Date _from = null;
    private Date _to = null;
    private String _fields = null;
    private String _conditions = null;
    private String _keywords = null;
    private KeywordUsage _keywordUsage = KeywordUsage.All;
    private String _orderBy = null;
    private String _initial = null;
    private String _reduceFunction = null;
    private String _finalizeFunction = null;

    public static final Pattern DATE_PATTERN = Pattern
            .compile("^([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}Z)$");
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final Pattern LONG_NUMBER_PATTERN = Pattern.compile("^NumberLong\\(([0-9]+)\\)$");

    /**
     * Basic constructor
     */
    public RepositoryEntryListCriteria()
    {
        return;
    }

    /**
     * Returns the timestamp from which the search should start. If set, this is added to the condition.
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
     * Returns the timestamp from which the search should stop. If set, this is added to the condition.
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
     * The fields to retrieve.
     * </p>
     * <p>
     * For example, to retrieve field "a" but not field "b": <code>{ "a" : 1, "b" : 0 }</code>.
     * </p>
     * <p>
     * See http://www.mongodb.org/display/DOCS/Retrieving+a+Subset+of+Fields.
     * </p>
     */
    public String getFields()
    {
        return _fields;
    }

    public void setFields(String fields)
    {
        _fields = fields;
    }

    /**
     * Returns the fields as DBObject
     */
    public BasicDBObject getFieldsDbObject()
    {
        if (StringUtils.isBlank(_fields))
        {
            return null;
        }

        MongoJsonParser parser = new MongoJsonParser(_fields);
        BasicDBObject queryParameters = (BasicDBObject) parser.parse();
        return queryParameters;
    }

    /**
     * <p>
     * The conditions used to filter entries.
     * </p>
     * <p>
     * For example, to retrieve entries where "j" is not equals to 3 and "k" is greater than 10:
     * <code>{"j": {"$ne": 3}, "k": {"$gt": 10} }</code>.
     * </p>
     * <p>
     * For dates, the format "yyyy-MM-dd'T'HH:mm:ssZ" is used. For example, "2011-01-01T23:01:02Z". It assumes the
     * timezone is UTC.
     * </p>
     * <p>
     * For long numbers, a string like "LongNumber(888)" is converted into a long number of value 888. If a JSON number
     * is more 10 digits long, it is also converted to a long integer.
     * </p>
     * <p>
     * See http://www.mongodb.org/display/DOCS/Advanced+Queries.
     * </p>
     */
    public String getConditions()
    {
        return _conditions;
    }

    public void setConditions(String conditions)
    {
        _conditions = conditions;
    }

    /**
     * <p>
     * The keywords to append to the conditions property (if any). Keywords are parsed and normalized via
     * </p>    
     */
    public String getKeywords()
    {
        return _keywords;
    }

    public void setKeywords(String keywords)
    {
        _keywords = keywords;
    }
    
    /**
     * <p>
     * How the keywords are to be used in the search criteria.  All or any keywords are to be used
     * </p> 
     */
    public KeywordUsage getKeywordUsage()
    {
        return _keywordUsage;
    }

    public void setKeywordUsage(KeywordUsage keywordUsage)
    {
        _keywordUsage = keywordUsage;
    }

    /**
     * Returns the conditions as DBObject
     * @throws IOException 
     */
    public BasicDBObject getConditionsDbObject() throws IOException
    {
        BasicDBObject o = null;

        if (StringUtils.isBlank(_conditions))
        {
            o = new BasicDBObject();
        }
        else
        {
            MongoJsonParser parser = new MongoJsonParser(_conditions, DATE_PATTERN, DATE_FORMAT, LONG_NUMBER_PATTERN);
            o = (BasicDBObject) parser.parse();
        }

        if (_from != null)
        {
            o.put(RepositoryEntryBO.TIMESTAMP_FIELD_NAME, new BasicDBObject("$gte", _from));
        }
        if (_to != null)
        {
            o.put(RepositoryEntryBO.TIMESTAMP_FIELD_NAME, new BasicDBObject("$lte", _to));
        }
        if (!StringUtils.isBlank(_keywords)) 
        {
            String operator = _keywordUsage == KeywordUsage.All ? "$all" : "$in";
            ArrayList<String> l = TextTokenizer.getInstance().tokenize(_keywords, 200);
            o.put(RepositoryEntryBO.KEYWORDS_FIELD_NAME, new BasicDBObject(operator, l));
        }

        return o;
    }

    /**
     * <p>
     * Field to use to sort the result set.
     * </p>
     * <p>
     * For example, by 'name' ascending, then 'age' descending: <code>{ "name" : 1, "age" : -1 }</code>.
     * </p>
     * <p>
     * See http://www.mongodb.org/display/DOCS/Sorting+and+Natural+Order
     * </p>
     */
    public String getOrderBy()
    {
        return _orderBy;
    }

    public void setOrderBy(String orderBy)
    {
        _orderBy = orderBy;
    }

    /**
     * Returns the order by fields as DBObject
     */
    public BasicDBObject getOrderByDbObject()
    {
        if (StringUtils.isBlank(_orderBy))
        {
            return null;
        }

        MongoJsonParser parser = new MongoJsonParser(_orderBy);
        BasicDBObject queryParameters = (BasicDBObject) parser.parse();
        return queryParameters;
    }

    /**
     * <p>
     * Initial values of the aggregation counter object.
     * </p>
     * <p>
     * For example, to initialize, the count and total time: <code> {"count": 0, "total_time":0}</code>.
     * </p>
     * <p>
     * For dates, the format "yyyy-MM-dd'T'HH:mm:ssZ" is used. For example, "2011-01-01T23:01:02Z". It assumes the
     * timezone is UTC.
     * </p>
     * <p>
     * For long numbers, a string like "LongNumber(888)" is converted into a long number of value 888. If a JSON number
     * is more 10 digits long, it is also converted to a long integer.
     * </p>
     * <p>
     * See http://www.mongodb.org/display/DOCS/Advanced+Queries.
     * </p>
     */
    public String getInitial()
    {
        return _initial;
    }

    public void setInitial(String initial)
    {
        _initial = initial;
    }

    /**
     * Returns the initial values as DBObject
     */
    public BasicDBObject getIntialDbObject()
    {
        if (StringUtils.isBlank(_initial))
        {
            return null;
        }

        MongoJsonParser parser = new MongoJsonParser(_initial, DATE_PATTERN, DATE_FORMAT, LONG_NUMBER_PATTERN);
        BasicDBObject queryParameters = (BasicDBObject) parser.parse();
        return queryParameters;
    }

    /**
     * <p>
     * The reduce function aggregates (reduces) the objects iterated. Typical operations of a reduce function include
     * summing and counting. Reduce takes two arguments: the current entry (document) being iterated over and the
     * aggregation counter object.
     * </p>
     * <p>
     * For example,
     * </p>
     * 
     * <pre>
     * function(entry, aggregation_counter){ aggregation_counter.count++; aggregation_counter.total_time+=entry.response_time }
     * </pre>
     * 
     * <p>
     * See http://www.mongodb.org/display/DOCS/Advanced+Queries.
     * </p>
     */
    public String getReduceFunction()
    {
        return _reduceFunction;
    }

    public void setReduceFunction(String reduce)
    {
        _reduceFunction = reduce;
    }

    /**
     * <p>
     * An optional function to be run on each item in the result set just before the item is returned. Can either modify
     * the item (e.g., add an average field given a count and a total) or return a replacement object (returning a new
     * object with just _id and average fields).
     * </p>
     * <p>
     * For example,
     * </p>
     * 
     * <pre>
     * function(aggregation_counter){ aggregation_countert.avg_time = aggregation_counter.total_time / aggregation_counter.count }
     * </pre>
     * 
     * <p>
     * See http://www.mongodb.org/display/DOCS/Advanced+Queries.
     * </p>
     */
    public String getFinalizeFunction()
    {
        return _finalizeFunction;
    }

    public void setFinalizeFunction(String finalize)
    {
        _finalizeFunction = finalize;
    }

    /**
     * The type of query that can be performed
     */
    public static enum QueryType
    {
        /**
         * <p>
         * Find and return matching entries. See http://www.mongodb.org/display/DOCS/Advanced+Queries.
         * </p>
         * <p>
         * The following parameters will be used.
         * <dl>
         * <dt>Fields</dt>
         * <dd>Optional list of fields in the entry to return. If not supplied, all fields will be returned.</dd>
         * 
         * <dt>Conditions</dt>
         * <dd>Optional list of condition to use to filter entries. If not supplied, all entries will be returned.</dd>
         * 
         * <dt>OrderBy</dt>
         * <dd>Optional list of fields to use in ordering the results</dd>
         * </dl>
         * </p>
         */
        FIND,

        /**
         * <p>
         * Counts the number of matching entries. See http://www.mongodb.org/display/DOCS/Aggregation#Aggregation-Count
         * </p>
         * <p>
         * The following parameter will be used.
         * <dl>
         * <dt>Conditions</dt>
         * <dd>Optional list of condition to use to filter entries. If not supplied, all entries will be returned.</dd>
         * </dl>
         * </p>
         */
        COUNT,

        /**
         * <p>
         * Returns a list of distinct values for the specified field. See
         * http://www.mongodb.org/display/DOCS/Aggregation#Aggregation-Distinct
         * </p>
         * <p>
         * The following parameters will be used.
         * <dl>
         * <dt>Fields</dt>
         * <dd>One and only 1 field must be supplied. The distinct value of this field will be returned.</dd>
         * 
         * <dt>Conditions</dt>
         * <dd>Optional list of condition to use to filter entries. If not supplied, the distinct values of the
         * specified field for all entries will be returned.</dd>
         * </dl>
         * </p>
         */
        DISTINCT,

        /**
         * <p>
         * Returns an array of grouped items like the SQL group by. See
         * http://www.mongodb.org/display/DOCS/Aggregation#Aggregation-Group.
         * </p>
         * <p>
         * The following parameters will be used.
         * <dl>
         * <dt>Fields</dt>
         * <dd>Fields to group by. If not supplied, only the aggregation counter will be returned.</dd>
         * 
         * <dt>Conditions</dt>
         * <dd>Optional list of condition to use to filter entries. If not supplied, all entries will be grouped.</dd>
         * 
         * <dt>Initial</dt>
         * <dd>Initial value of the aggregation counter object.</dd>
         * 
         * <dt>Reduce</dt>
         * <dd>The reduce function aggregates (reduces) the objects iterated. Typical operations of a reduce function
         * include summing and counting. reduce takes two arguments: the current document being iterated over and the
         * aggregation counter object.</dd>
         * 
         * <dt>Finalize</dt>
         * <dd>An optional function to be run on each item in the result set just before the item is returned. Can
         * either modify the item (e.g., add an average field given a count and a total) or return a replacement object
         * (returning a new object with just _id and average fields).</dd>
         * </dl>
         * </p>
         */
        GROUP
    }
    
    /**
     * How the keywords are to be used as conditions 
     * @author vibul
     *
     */
    public static enum KeywordUsage
    {
        /**
         * Any (one or more) keywords must exist
         */
        Any,

        /**
         * All keywords must exist
         */
        All
    }
}
