/**
 * http://www.germane-software.com/software/Java/Gozirra/
 */
package com.chililog.client.stomp;

import java.util.Map;

/**
 * (c)2005 Sean Russell
 */
@SuppressWarnings("rawtypes")
public interface MessageReceiver {
  public void receive( Command c, Map h, String b );
  public void disconnect();
  public boolean isClosed();
}
