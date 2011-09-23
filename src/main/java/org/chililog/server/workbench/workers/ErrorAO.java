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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.ChiliLogException;

/**
 * <p>
 * Error API Object is returned to the caller in the event of an error or exception during processing.
 * </p>
 * 
 * @author vibul
 * 
 */
public class ErrorAO extends AO {

    private String _errorCode;

    private String _message;

    private String _stackTrace;

    /**
     * Basic constructor
     */
    public ErrorAO() {
        return;
    }

    /**
     * Constructor allowing the setting to properties
     * 
     * @param message
     *            Human readable message
     * @param stackTrace
     *            Stack trace
     */
    public ErrorAO(String message, String stackTrace) {
        _message = message;
        _stackTrace = stackTrace;
    }

    /**
     * Basic constructor
     */
    public ErrorAO(Throwable ex) {
        _message = ex.getMessage();
        if (StringUtils.isBlank(_message)) {
            _message = ex.toString();
        }

        if (ex instanceof ChiliLogException) {
            _errorCode = "ChiliLogException:" + ((ChiliLogException) ex).getErrorCode();
        } else {
            _errorCode = ex.getClass().getName();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        ex.printStackTrace(pw);
        pw.close();
        _stackTrace = sw.getBuffer().toString();

        return;
    }

    /**
     * The error code if one exists
     */
    public String getErrorCode() {
        return _errorCode;
    }

    public void setErrorCode(String errorCode) {
        _errorCode = errorCode;
    }

    /**
     * Human readable error message that can be displayed to the user
     */
    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

    /**
     * Stack trace to help debugging
     */
    public String getStackTrace() {
        return _stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        _stackTrace = stackTrace;
    }

}
