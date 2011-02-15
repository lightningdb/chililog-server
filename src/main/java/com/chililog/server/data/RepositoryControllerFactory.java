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

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;

/**
 * Factory class to instance our controllers
 * 
 * @author vibul
 * 
 */
public class RepositoryControllerFactory
{
    /**
     * Instances a repository controller for our use
     * 
     * @param repoInfo
     *            repository information that contains information on the type of contoller
     * @return <code>RepositoryController</code>
     * @throws Exception
     */
    public static RepositoryController make(RepositoryInfoBO repoInfo) throws Exception
    {
        Class<?> controllerClass = ClassUtils.getClass(repoInfo.getControllerClassName());
        return (RepositoryController) ConstructorUtils.invokeConstructor(controllerClass, repoInfo);
    }
}
