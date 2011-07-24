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

package com.chililog.server.workbench.workers;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.RepositoryFieldInfoBO;
import com.chililog.server.data.RepositoryFieldInfoBO.DataType;

/**
 * <p>
 * Repository Field API Object
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryFieldInfoAO extends AO
{
    private String _name;
    private String _displayName;
    private String _description;
    private DataType _dataType;
    private RepositoryPropertyInfoAO[] _properties = null;

    /**
     * Basic constructor
     */
    public RepositoryFieldInfoAO()
    {
        return;
    }

    /**
     * Constructor that copies properties form the business object
     * 
     * @param repoFieldInfo
     *            Repository info business object
     */
    public RepositoryFieldInfoAO(RepositoryFieldInfoBO repoFieldInfo)
    {
        _name = repoFieldInfo.getName();
        _displayName = repoFieldInfo.getDisplayName();
        _description = repoFieldInfo.getDescription();
        _dataType = repoFieldInfo.getDataType();

        if (repoFieldInfo.getProperties() == null || repoFieldInfo.getProperties().isEmpty())
        {
            _properties = null;
        }
        else
        {
            ArrayList<RepositoryPropertyInfoAO> propertyList = new ArrayList<RepositoryPropertyInfoAO>();
            for (Entry<String, String> e : repoFieldInfo.getProperties().entrySet())
            {
                propertyList.add(new RepositoryPropertyInfoAO(e.getKey(), e.getValue()));
            }
            _properties = propertyList.toArray(new RepositoryPropertyInfoAO[] {});
        }

        return;
    }

    /**
     * Updates the supplied business object with info from this api object
     * 
     * @param repoFieldInfo
     *            business object to update
     * @throws ChiliLogException
     */
    public void toBO(RepositoryFieldInfoBO repoFieldInfo) throws ChiliLogException
    {
        repoFieldInfo.setName(checkRequiredField("Field Name", _name));
        repoFieldInfo.setDisplayName(_displayName);
        repoFieldInfo.setDescription(_description);
        repoFieldInfo.setDataType(_dataType);

        repoFieldInfo.getProperties().clear();
        if (_properties != null && _properties.length > 0)
        {
            for (RepositoryPropertyInfoAO property : _properties)
            {
                repoFieldInfo.getProperties().put(property.getKey(), property.getValue());
            }
        }

        return;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getDisplayName()
    {
        return _displayName;
    }

    public void setDisplayName(String displayName)
    {
        _displayName = displayName;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public DataType getDataType()
    {
        return _dataType;
    }

    public void setDataType(DataType dataType)
    {
        _dataType = dataType;
    }

    public RepositoryPropertyInfoAO[] getProperties()
    {
        return _properties;
    }

    public void setProperties(RepositoryPropertyInfoAO[] properties)
    {
        _properties = properties;
    }
}
