/**
 * http://www.germane-software.com/software/Java/Gozirra/
 */
package com.chililog.client.stomp;

import java.util.Map;
import java.util.Iterator;
import java.io.IOException;

import com.chililog.server.common.Log4JLogger;

/**
 * (c)2005 Sean Russell
 */
@SuppressWarnings("rawtypes")
class Transmitter {
    private static Log4JLogger _logger = Log4JLogger.getLogger(Transmitter.class);

    public static void transmit( Command c, Map h, String b, 
      java.io.OutputStream out ) throws IOException {
    StringBuffer message = new StringBuffer( c.toString() );
    message.append( "\n" );

    if (h != null) {
      for (Iterator keys = h.keySet().iterator(); keys.hasNext(); ) {
        String key = (String)keys.next();
        String value = (String)h.get(key);
        message.append( key );
        message.append( ":" );
        message.append( value );
        message.append( "\n" );
      }
    }
    message.append( "\n" );

    if (b != null) message.append( b );

    message.append( "\000" );

    _logger.debug("STOMP Transmit %s", message.toString());

    out.write( message.toString().getBytes( Command.ENCODING ) );
  }
}
