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

package org.chililog.server.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * The standard Exception class used throughout our application.
 * <p>
 * <code>ChiliLogException</code> supports loading of exception messages using <code>StringsProperties</code>. We make
 * an assumption that the supplied <code>errorCode</code> is the same as the string id that is used to load the string.
 * </p>
 * 
 * @author vibul
 * 
 */
public class ChiliLogException extends Exception {
    private static final long serialVersionUID = 1L;
    private String _errorCode = null;

    /**
     * Error code associated with this exception
     */
    public String getErrorCode() {
        return _errorCode;
    }

    /**
     * Basic constructor
     */
    public ChiliLogException() {
        super();
    }

    /**
     * Constructor with error code
     * 
     * @param errorCode
     *            Error code to use to load message
     * @param args
     *            Error message place holder substitutes
     */
    public ChiliLogException(String errorCode, Object... args) {
        super(getErrorMessage(errorCode, args));
        _errorCode = errorCode;
    }

    /**
     * Wrapper constructor
     * 
     * @param ex
     *            Exception that triggered this exception and is to be wrapped
     * @param errorCode
     *            Error code to use to load message
     * @param args
     *            Error message place holder substitutes
     */
    public ChiliLogException(Throwable ex, String errorCode, Object... args) {
        super(getErrorMessage(errorCode, args), ex);
        _errorCode = errorCode;
    }

    /**
     * Gets the message using the error code as the string id.
     * 
     * @param errorCode
     *            Error code to use to load message
     * @param args
     *            Error message place holder substitutes
     * 
     * @return Message for this exception
     */
    private static String getErrorMessage(String errorCode, Object[] args) {
        String s = StringsProperties.getInstance().getString(errorCode, errorCode);
        return String.format(s, args);
    }

    /**
     * @return The stack trace as a string
     */
    public String getStackTraceAsString() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, true, "UTF-8");
            this.printStackTrace(ps);
            return baos.toString("UTF-8");
        }
        catch (Exception ex) {
            return this.toString();
        }
    }
}
