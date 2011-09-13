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

package org.chililog.server.workbench.workers;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.chililog.server.common.ChiliLogException;
import org.chililog.server.data.RepositoryFieldConfigBO;
import org.chililog.server.data.RepositoryParserConfigBO;
import org.chililog.server.data.RepositoryParserConfigBO.AppliesTo;
import org.chililog.server.data.RepositoryParserConfigBO.ParseFieldErrorHandling;


/**
 * <p>
 * Repository Parser API Object
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryParserConfigAO extends AO
{
    private String _name;
    private AppliesTo _appliesTo = AppliesTo.None;
    private String _appliesToSourceFilter;
    private String _appliesToHostFilter;
    private String _className;
    private long _maxKeywords = -1;
   private ParseFieldErrorHandling _parseFieldErrorHandling = ParseFieldErrorHandling.SkipField;
    private RepositoryFieldConfigAO[] _fields = null;
    private RepositoryPropertyConfigAO[] _properties = null;

    /**
     * Basic constructor
     */
    public RepositoryParserConfigAO()
    {
        return;
    }

    /**
     * Constructor that copies properties form the business object
     * 
     * @param repoParserConfig
     *            Repository parser configuration business object
     */
    public RepositoryParserConfigAO(RepositoryParserConfigBO repoParserConfig)
    {
        _name = repoParserConfig.getName();
        _appliesTo = repoParserConfig.getAppliesTo();
        _appliesToSourceFilter = repoParserConfig.getAppliesToSourceFilter();
        _appliesToHostFilter = repoParserConfig.getAppliesToHostFilter();
        _className = repoParserConfig.getClassName();
        _maxKeywords = repoParserConfig.getMaxKeywords();

        _parseFieldErrorHandling = repoParserConfig.getParseFieldErrorHandling();

        if (repoParserConfig.getFields() == null || repoParserConfig.getFields().isEmpty())
        {
            _fields = null;
        }
        else
        {
            ArrayList<RepositoryFieldConfigAO> fieldList = new ArrayList<RepositoryFieldConfigAO>();
            for (RepositoryFieldConfigBO fieldInfo : repoParserConfig.getFields())
            {
                fieldList.add(new RepositoryFieldConfigAO(fieldInfo));
            }
            _fields = fieldList.toArray(new RepositoryFieldConfigAO[] {});
        }

        if (repoParserConfig.getProperties() == null || repoParserConfig.getProperties().isEmpty())
        {
            _properties = null;
        }
        else
        {
            ArrayList<RepositoryPropertyConfigAO> propertyList = new ArrayList<RepositoryPropertyConfigAO>();
            for (Entry<String, String> e : repoParserConfig.getProperties().entrySet())
            {
                propertyList.add(new RepositoryPropertyConfigAO(e.getKey(), e.getValue()));
            }
            _properties = propertyList.toArray(new RepositoryPropertyConfigAO[] {});
        }

        return;
    }

    /**
     * Updates the supplied business object with info from this api object
     * 
     * @param repoParserConfig
     *            business object to update
     * @throws ChiliLogException
     */
    public void toBO(RepositoryParserConfigBO repoParserConfig) throws ChiliLogException
    {
        repoParserConfig.setName(_name);
        repoParserConfig.setAppliesTo(_appliesTo);
        repoParserConfig.setAppliesToSourceFilter(_appliesToSourceFilter);
        repoParserConfig.setAppliesToHostFilter(_appliesToHostFilter);
        repoParserConfig.setClassName(_className);
        repoParserConfig.setMaxKeywords(_maxKeywords);

        repoParserConfig.setParseFieldErrorHandling(_parseFieldErrorHandling);

        repoParserConfig.getFields().clear();
        if (_fields != null && _fields.length > 0)
        {
            for (RepositoryFieldConfigAO fieldInfo : _fields)
            {
                RepositoryFieldConfigBO bo = new RepositoryFieldConfigBO();
                fieldInfo.toBO(bo);
                repoParserConfig.getFields().add(bo);
            }
        }

        repoParserConfig.getProperties().clear();
        if (_properties != null && _properties.length > 0)
        {
            for (RepositoryPropertyConfigAO property : _properties)
            {
                repoParserConfig.getProperties().put(property.getKey(), property.getValue());
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
    
    public long getMaxKeywords()
    {
        return _maxKeywords;
    }

    public void setMaxKeywords(long maxKeywords)
    {
        _maxKeywords = maxKeywords;
    }

    public ParseFieldErrorHandling getParseFieldErrorHandling()
    {
        return _parseFieldErrorHandling;
    }

    public void setParseFieldErrorHandling(ParseFieldErrorHandling parseFieldErrorHandling)
    {
        _parseFieldErrorHandling = parseFieldErrorHandling;
    }

    public RepositoryFieldConfigAO[] getFields()
    {
        return _fields;
    }

    public void setFields(RepositoryFieldConfigAO[] fields)
    {
        _fields = fields;
    }

    public RepositoryPropertyConfigAO[] getProperties()
    {
        return _properties;
    }

    public void setProperties(RepositoryPropertyConfigAO[] properties)
    {
        _properties = properties;
    }

}
