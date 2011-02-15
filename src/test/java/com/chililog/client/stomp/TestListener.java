package com.chililog.client.stomp;

import java.util.Map;

@SuppressWarnings("rawtypes")
public class TestListener implements Listener
{

    String _lastMessageBody;
    
    public void message(Map headers, String body)
    {
        // TODO Auto-generated method stub
        _lastMessageBody = body;
    }

    public String getLastMessageBody()
    {
        return _lastMessageBody;
    }
}
