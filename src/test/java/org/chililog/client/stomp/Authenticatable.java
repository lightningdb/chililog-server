/**
 * http://www.germane-software.com/software/Java/Gozirra/
 */
package org.chililog.client.stomp;

import java.util.Map;

/**
 * (c)2005 Sean Russell
 */
@SuppressWarnings({ "rawtypes" })
public interface Authenticatable extends MessageReceiver {
  public void error( Map headers, String b );
  public Object token();
}

