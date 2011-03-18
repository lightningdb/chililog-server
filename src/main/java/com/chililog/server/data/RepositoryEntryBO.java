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

import java.io.Serializable;
import java.util.Date;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.DBObject;

/**
 * <p>
 * The Business Object encapsulating an entry (record or row) in a repository. All the data is stored and accessed by
 * <code>toDBOject</code>.
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryEntryBO extends BO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Date _entryTimestamp;
    private String _entryInputName;
    private String _entryInputIpAddress;
    private String _entryText;

    public static final String ENTRY_TIMESTAMP_FIELD_NAME = "entry_timestamp";
    public static final String ENTRY_INPUT_NAME_FIELD_NAME = "entry_input_name";
    public static final String ENTRY_INPUT_IP_ADDRESS_FIELD_NAME = "entry_input_ip_address";
    public static final String ENTRY_TEXT_FIELD_NAME = "entry_text";

    /**
     * Basic constructor
     */
    public RepositoryEntryBO()
    {
        return;
    }

    /**
     * Constructor that loads our properties retrieved from the mongoDB dbObject
     * 
     * @param dbObject
     *            database object as retrieved from mongoDB
     * @throws ChiliLogException
     */
    public RepositoryEntryBO(DBObject dbObject) throws ChiliLogException
    {
        super(dbObject);
        _entryTimestamp = MongoUtils.getDate(dbObject, ENTRY_TIMESTAMP_FIELD_NAME, true);
        _entryInputName = MongoUtils.getString(dbObject, ENTRY_INPUT_NAME_FIELD_NAME, true);
        _entryInputIpAddress = MongoUtils.getString(dbObject, ENTRY_INPUT_IP_ADDRESS_FIELD_NAME, true);
        _entryText = MongoUtils.getString(dbObject, ENTRY_TEXT_FIELD_NAME, true);
        return;
    }

    /**
     * Constructor for a new entry.
     * 
     * @param dbObject
     *            database object as parsed by a repository controller
     * @param inputName
     *            Name of the input (device or application) that created this log entry
     * @param inputIpAddress
     *            IP address of the input (device or application) that created this entry
     * @param text
     *            the text or string version of this entry that was parsed
     * 
     * @throws ChiliLogException
     */
    RepositoryEntryBO(DBObject dbObject, String inputName, String inputIpAddress, String text) throws ChiliLogException
    {
        super(dbObject);
        _entryTimestamp = new Date();
        _entryInputName = inputName;
        _entryInputIpAddress = inputIpAddress;
        _entryText = text;
        return;
    }

    /**
     * Puts our properties into the mongoDB object so that it can be saved
     * 
     * @param dbObject
     *            mongoDB database object that can be used for saving
     */
    @Override
    protected void savePropertiesToDBObject(DBObject dbObject) throws ChiliLogException
    {
        MongoUtils.setDate(dbObject, ENTRY_TIMESTAMP_FIELD_NAME, _entryTimestamp);
        MongoUtils.setString(dbObject, ENTRY_INPUT_NAME_FIELD_NAME, _entryInputName);
        MongoUtils.setString(dbObject, ENTRY_INPUT_IP_ADDRESS_FIELD_NAME, _entryInputIpAddress);
        MongoUtils.setString(dbObject, ENTRY_TEXT_FIELD_NAME, _entryText);
        return;
    }

    /**
     * Returns the date on which the entry was save into the ChiliLog repository
     */
    public Date getEntryTimestamp()
    {
        return _entryTimestamp;
    }

    public void setEntryTimestamp(Date chililoggedOn)
    {
        _entryTimestamp = chililoggedOn;
    }

    /**
     * Returns the name of the device or application that created the entry      
     */
    public String getEntryInputName()
    {
        return _entryInputName;
    }

    public void setEntryInputName(String entryInputName)
    {
        _entryInputName = entryInputName;
    }

    /**
     * Returns the IP address of the device or application that created the entry 
     */
    public String getEntryInputIpAddress()
    {
        return _entryInputIpAddress;
    }

    public void setEntryInputIpAddress(String entryInputIpAddress)
    {
        _entryInputIpAddress = entryInputIpAddress;
    }

    /**
     * Returns the original text format of the entry what was parsed
     */
    public String getEntryText()
    {
        return _entryText;
    }

    public void setEntryText(String text)
    {
        _entryText = text;
    }

}
