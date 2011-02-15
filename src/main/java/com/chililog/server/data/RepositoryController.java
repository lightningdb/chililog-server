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
 * <p>
 * Base controller for all repository controllers
 * </p>
 * 
 * @author vibul
 * 
 */
public abstract class RepositoryController extends BaseController
{
    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
     * @param textEntry
     *            The text for this entry to parse
     * @return <code>RepositoryEntryBO</code> ready for saving to mongoDB. If the entry is to be skipped and not written
     *         to mongoDB, then null is returned
     */
    public abstract RepositoryEntryBO parse(String textEntry);

    /**
     * Returns the last error processed by <code>parse</code>.
     */
    public abstract Exception getLastParseError();
}
