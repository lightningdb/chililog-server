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

import org.apache.log4j.Logger;

/**
 * Wrapper for Log4J that does string formatting only when required
 * 
 * @author vibul
 * @see http://stackoverflow.com/questions/943367/string-format-with-lazy-evaluation
 */
public class Log4JLogger
{
    private final Logger _inner;

    /**
     * Private constructor. User <code>getLogger</code> instead.
     * 
     * @param clazz
     */
    private Log4JLogger(Class<?> clazz)
    {
        _inner = Logger.getLogger(clazz);
    }

    /**
     * Returns an instance of this Logger to use
     * 
     * @param clazz
     * @return
     */
    public static Log4JLogger getLogger(Class<?> clazz)
    {
        return new Log4JLogger(clazz);
    }

    /**
     * Write a message with a priority of TRACE
     * 
     * @param format
     * @param args
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
     * @param args
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
     * @param args
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
     * @param args
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
     * @param args
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
     * @param args
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
     * @param args
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
     * @param args
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
     * @param args
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
     * @param args
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
     * @param args
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
     * @param args
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
