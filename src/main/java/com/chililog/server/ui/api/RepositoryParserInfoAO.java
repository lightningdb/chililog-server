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

package com.chililog.server.ui.api;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.RepositoryFieldInfoBO;
import com.chililog.server.data.RepositoryParserInfoBO;
import com.chililog.server.data.RepositoryParserInfoBO.AppliesTo;
import com.chililog.server.data.RepositoryParserInfoBO.ParseFieldErrorHandling;

/**
 * <p>
 * Repository Parser API Object
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryParserInfoAO extends AO
{
    private String _name;
    private AppliesTo _appliesTo = AppliesTo.None;
    private String _appliesToSourceFilter;
    private String _appliesToHostFilter;
    private String _className;
    private ParseFieldErrorHandling _parseFieldErrorHandling = ParseFieldErrorHandling.SkipField;
    private RepositoryFieldInfoAO[] _fields = null;
    private RepositoryPropertyInfoAO[] _properties = null;

    /**
     * Basic constructor
     */
    public RepositoryParserInfoAO()
    {
        return;
    }

    /**
     * Constructor that copies properties form the business object
     * 
     * @param repoParserInfo
     *            Repository info business object
     */
    public RepositoryParserInfoAO(RepositoryParserInfoBO repoParserInfo)
    {
        _name = repoParserInfo.getName();
        _appliesTo = repoParserInfo.getAppliesTo();
        _appliesToSourceFilter = repoParserInfo.getAppliesToSourceFilter();
        _appliesToHostFilter = repoParserInfo.getAppliesToHostFilter();
        _className = repoParserInfo.getClassName();

        _parseFieldErrorHandling = repoParserInfo.getParseFieldErrorHandling();

        if (repoParserInfo.getFields() == null || repoParserInfo.getFields().isEmpty())
        {
            _fields = null;
        }
        else
        {
            ArrayList<RepositoryFieldInfoAO> fieldList = new ArrayList<RepositoryFieldInfoAO>();
            for (RepositoryFieldInfoBO fieldInfo : repoParserInfo.getFields())
            {
                fieldList.add(new RepositoryFieldInfoAO(fieldInfo));
            }
            _fields = fieldList.toArray(new RepositoryFieldInfoAO[] {});
        }

        if (repoParserInfo.getProperties() == null || repoParserInfo.getProperties().isEmpty())
        {
            _properties = null;
        }
        else
        {
            ArrayList<RepositoryPropertyInfoAO> propertyList = new ArrayList<RepositoryPropertyInfoAO>();
            for (Entry<String, String> e : repoParserInfo.getProperties().entrySet())
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
     * @param repoParserInfo
     *            business object to update
     * @throws ChiliLogException
     */
    public void toBO(RepositoryParserInfoBO repoParserInfo) throws ChiliLogException
    {
        _name = repoParserInfo.getName();
        _appliesTo = repoParserInfo.getAppliesTo();
        _appliesToSourceFilter = repoParserInfo.getAppliesToSourceFilter();
        _appliesToHostFilter = repoParserInfo.getAppliesToHostFilter();
        _className = repoParserInfo.getClassName();

        repoParserInfo.setName(checkRequiredField("Field Name", _name));
        repoParserInfo.setAppliesTo(_appliesTo);
        repoParserInfo.setAppliesToSourceFilter(_appliesToSourceFilter);
        repoParserInfo.setAppliesToHostFilter(_appliesToHostFilter);
        repoParserInfo.setClassName(checkRequiredField("Class Name", _className));

        repoParserInfo.setParseFieldErrorHandling(_parseFieldErrorHandling);

        repoParserInfo.getFields().clear();
        if (_fields != null && _fields.length > 0)
        {
            for (RepositoryFieldInfoAO fieldInfo : _fields)
            {
                RepositoryFieldInfoBO bo = new RepositoryFieldInfoBO();
                fieldInfo.toBO(bo);
                repoParserInfo.getFields().add(bo);
            }
        }

        repoParserInfo.getProperties().clear();
        if (_properties != null && _properties.length > 0)
        {
            for (RepositoryPropertyInfoAO property : _properties)
            {
                repoParserInfo.getProperties().put(property.getKey(), property.getValue());
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

    public AppliesTo getAppliesTo()
    {
        return _appliesTo;
    }

    public void setAppliesTo(AppliesTo appliesTo)
    {
        _appliesTo = appliesTo;
    }

    public String getAppliesToSourceFilter()
    {
        return _appliesToSourceFilter;
    }

    public void setAppliesToSourceFilter(String appliesToSourceFilter)
    {
        _appliesToSourceFilter = appliesToSourceFilter;
    }

    public String getAppliesToHostFilter()
    {
        return _appliesToHostFilter;
    }

    public void setAppliesToHostFilter(String appliesToHostFilter)
    {
        _appliesToHostFilter = appliesToHostFilter;
    }

    public String getClassName()
    {
        return _className;
    }

    public void setClassName(String className)
    {
        _className = className;
    }

    public ParseFieldErrorHandling getParseFieldErrorHandling()
    {
        return _parseFieldErrorHandling;
    }

    public void setParseFieldErrorHandling(ParseFieldErrorHandling parseFieldErrorHandling)
    {
        _parseFieldErrorHandling = parseFieldErrorHandling;
    }

    public RepositoryFieldInfoAO[] getFields()
    {
        return _fields;
    }

    public void setFields(RepositoryFieldInfoAO[] fields)
    {
        _fields = fields;
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
