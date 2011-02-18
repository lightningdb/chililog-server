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

package com.chililog.server.common;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * <p>
 * Wrapper for Log4J that does string formatting only when required. See
 * http://stackoverflow.com/questions/943367/string-format-with-lazy-evaluation.
 * </p>
 * <p>
 * To debug Log4J, add the following VM command line argument <code>-Dlog4j.debug</code>.
 * </p>
 * 
 * @author vibul
 * 
 */
public class Log4JLogger
{
    private final Logger _inner;

    /**
     * Initialize log4J to look for the override configuration if one exists
     */
    static
    {
        // We don't use the SystemProperties class because it triggers the logger!
        // See code in org.apache.log4j.LogManager static block

        String configDir = System.getProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY);
        if (!StringUtils.isEmpty(configDir))
        {
            File overrideLog4JXML = new File(configDir, "log4j.xml");
            if (overrideLog4JXML.exists())
            {
                DOMConfigurator config = new DOMConfigurator();
                config.doConfigure(overrideLog4JXML.getPath(), LogManager.getLoggerRepository());
            }
        }

    }

    /**
     * Returns an instance of this Logger to use
     * 
     * @param clazz
     *            Class in which the logger is going to be used. Class name will be used for the log category.
     * @return Instance of the logger for the specific class that is to be used.
     */
    public static Log4JLogger getLogger(Class<?> clazz)
    {
        return new Log4JLogger(clazz);
    }

    /**
     * Private constructor. User <code>getLogger</code> instead.
     * 
     * @param clazz
     *            Class in which the logger is going to be used. Class name will be used for the log category.
     */
    private Log4JLogger(Class<?> clazz)
    {
        _inner = Logger.getLogger(clazz);
    }

    /**
     * Write a message with a priority of TRACE
     * 
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void trace(String format, Object... args)
    {
        try
        {
            if (_inner.isTraceEnabled())
            {
                _inner.trace(String.format(format, args));
            }
        }
        catch (Exception ex)
        {
            _inner.trace(format);
        }
    }

    /**
     * Write a message with a priority of TRACE
     * 
     * @param t
     *            Exception
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void trace(Throwable t, String format, Object... args)
    {
        try
        {
            if (_inner.isTraceEnabled())
            {
                _inner.trace(String.format(format, args), t);
            }
        }
        catch (Exception ex)
        {
            _inner.trace(t);
        }
    }

    /**
     * Write a message with a priority of DEBUG
     * 
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void debug(String format, Object... args)
    {
        try
        {
            if (_inner.isDebugEnabled())
            {
                _inner.debug(String.format(format, args));
            }
        }
        catch (Exception ex)
        {
            _inner.debug(format);
        }
    }

    /**
     * Write a message with a priority of DEBUG
     * 
     * @param t
     *            Exception
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void debug(Throwable t, String format, Object... args)
    {
        try
        {
            if (_inner.isDebugEnabled())
            {
                _inner.debug(String.format(format, args), t);
            }
        }
        catch (Exception ex)
        {
            _inner.debug(t);
        }
    }

    /**
     * Write a message with a priority of INFO
     * 
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void info(String format, Object... args)
    {
        try
        {
            if (_inner.isInfoEnabled())
            {
                _inner.info(String.format(format, args));
            }
        }
        catch (Exception ex)
        {
            _inner.info(format);
        }
    }

    /**
     * Write a message with a priority of INFO
     * 
     * @param t
     *            Exception
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void info(Throwable t, String format, Object... args)
    {
        try
        {
            if (_inner.isInfoEnabled())
            {
                _inner.info(String.format(format, args), t);
            }
        }
        catch (Exception ex)
        {
            _inner.info(t);
        }
    }

    /**
     * Write a message with a priority of WARN
     * 
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void warn(String format, Object... args)
    {
        try
        {
            _inner.warn(String.format(format, args));
        }
        catch (Exception ex)
        {
            _inner.warn(format);
        }
    }

    /**
     * Write a message with a priority of WARN
     * 
     * @param t
     *            Exception
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void warn(Throwable t, String format, Object... args)
    {
        try
        {
            _inner.warn(String.format(format, args), t);
        }
        catch (Exception ex)
        {
            _inner.warn(t);
        }
    }

    /**
     * Write a message with a priority of ERROR
     * 
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void error(String format, Object... args)
    {
        try
        {
            _inner.error(String.format(format, args));
        }
        catch (Exception ex)
        {
            _inner.error(format);
        }
    }

    /**
     * Write a message with a priority of ERROR
     * 
     * @param t
     *            Exception
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void error(Throwable t, String format, Object... args)
    {
        try
        {
            _inner.error(String.format(format, args), t);
        }
        catch (Exception ex)
        {
            _inner.error(t);
        }
    }

    /**
     * Write a message with a priority of FATAL
     * 
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void fatal(String format, Object... args)
    {
        try
        {
            _inner.fatal(String.format(format, args));
        }
        catch (Exception ex)
        {
            _inner.fatal(format);
        }
    }

    /**
     * Write a message with a priority of FATAL
     * 
     * @param t
     *            Exception
     * @param format
     *            string with placeholders
     * @param args
     *            placeholder substitutes
     */
    public void fatal(Throwable t, String format, Object... args)
    {
        try
        {
            _inner.fatal(String.format(format, args), t);
        }
        catch (Exception ex)
        {
            _inner.fatal(t);
        }
    }

}
