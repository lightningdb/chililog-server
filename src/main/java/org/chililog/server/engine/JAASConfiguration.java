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

package org.chililog.server.engine;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public class JAASConfiguration extends Configuration {

    private String _loginModuleClassName;

    /**
     * Basic constructor
     */
    public JAASConfiguration() {
        _loginModuleClassName = JAASLoginModule.class.getName();
        return;
    }

    /**
     * Retrieves the AppConfigurationEntries for the specified name from this Configuration.
     * 
     * @param name
     *            The name will be passed in by <code>LoginContext</code> constructor. We don't use this parameter
     *            because our configuration only supports 1 configuration.
     */
    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
        Map<String, ?> options = new HashMap<String, String>();

        AppConfigurationEntry entry = new AppConfigurationEntry(_loginModuleClassName,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);

        return new AppConfigurationEntry[] { entry };
    }

    @Override
    public void refresh() {
        return;
    }
}
