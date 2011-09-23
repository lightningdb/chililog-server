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

import org.chililog.server.common.Log4JLogger;
import org.junit.Test;

public class Log4JLoggerTest {
    private static Log4JLogger _logger = Log4JLogger.getLogger(Log4JLoggerTest.class);

    @Test
    public void testOK() {
        _logger.debug("debug");
        _logger.trace("trace");
        _logger.info("info");
        _logger.warn("warn");
        _logger.error("error");
        _logger.fatal("fatal");
    }

    @Test
    public void testNull() {
        // Should be no NullPointerExceptoin
        _logger.debug(null);
        _logger.trace(null);
        _logger.info(null);
        _logger.warn(null);
        _logger.error(null);
        _logger.fatal(null);
    }

    @Test
    public void testStringFormat() {
        _logger.debug("%s", "debug");
        _logger.trace("%s", "trace");
        _logger.info("%s", "info");
        _logger.warn("%s", "warn");
        _logger.error("%s", "error");
        _logger.fatal("%s", "fatal");
    }

    @Test
    public void testStringFormatError() {
        // There should be no java.util.MissingFormatArgumentException.
        _logger.debug("%s %s", "debug");
        _logger.trace("%s %s", "trace");
        _logger.info("%s %s", "info");
        _logger.warn("%s %s", "warn");
        _logger.error("%s %s", "error");
        _logger.fatal("%s %s", "fatal");
    }

}
