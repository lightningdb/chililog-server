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

/**
 * Base class for criteria for listing records from mongoDB
 * 
 * @author vibul
 * 
 */
public abstract class BaseListCriteria
{
    private int _recordsPerPage = Integer.MAX_VALUE;
    private int _startPage = 1;
    private boolean _doPageCount = false;
    private int _pageCount = -1;

    /**
     * Basic constructor
     */
    public BaseListCriteria()
    {
        return;
    }

    /**
     * Returns the number of records or records to return in this query. Defaults to maximum integer value.
     */
    public int getRecordsPerPage()
    {
        return _recordsPerPage;
    }

    /**
     * Sets the number of records to return in this query
     */
    public void setRecordsPerPage(int rowsPerPage)
    {
        _recordsPerPage = rowsPerPage;
    }

    /**
     * Returns the page at which to start retrieving records
     */
    public int getStartPage()
    {
        return _startPage;
    }

    /**
     * Sets the page at which to start retrieving records.
     * 
     * @param startPage
     */
    public void setStartPage(int startPage)
    {
        _startPage = startPage;
    }

    /**
     * Flag to indicate if a page count is to be returned in <code>getPageCount</code>. Default is false and a page
     * count will not be returned.
     */
    public boolean getDoPageCount()
    {
        return _doPageCount;
    }

    /**
     * Sets flag to indicate if a page count is to be returned in <code>getPageCount</code>.
     * 
     * @param doPageCount
     */
    public void setDoPageCount(boolean doPageCount)
    {
        _doPageCount = doPageCount;
    }

    /**
     * Returns the total number of pages. -1 indicates that the page count has not been set.
     */
    public int getPageCount()
    {
        return _pageCount;
    }

    /**
     * Calculate the number of pages given the total number of records matching the criteria
     * 
     * @param recordCount number of records matching the criteria
     */
    protected void calculatePageCount(int recordCount)
    {
        _pageCount = recordCount / _recordsPerPage;
        int mod = recordCount % _recordsPerPage;
        if (mod > 0)
        {
            _pageCount++;
        }
    }
}
