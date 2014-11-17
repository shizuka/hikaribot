/*
 * hikaribot - WhoResponse
 * Shizuka Kamishima - 2014-11-17
 * Observable
 * 
 * Copyright (c) 2014, Shizuka Kamishima
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package sk.hikaribot.api;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.jibble.pircbot.ReplyConstants.*;

/**
 * Collects responses to a WHO command, notifies calling Observer when ENDOFWHO
 * is reached.
 * 
 * @author Shizuka Kamishima
 */
public class WhoResponse extends Observable implements Observer {
  
  private static final Logger log = LogManager.getLogger("WHO");
  
  private final HashMap<String, String> users;
  private final String targetChannel;

  public WhoResponse(String channel) {
    users = new HashMap();
    this.targetChannel = channel;
  }
  
  public void onWho(String nick, String account) {
    users.put(nick, account);
  }
  
  public void onEndOfWho() {
    this.setChanged();
    this.notifyObservers(this);
  }

  @Override
  public void update(Observable o, Object arg) {
    String line = arg.toString();
    String[] serverResponse = line.split(":", 2);
    int code = Integer.parseInt(serverResponse[0]);
    String response = serverResponse[1];
    this.onServerResponse(code, response);
  }
  
  private void onServerResponse(int code, String response) {
    String[] info = response.split(" ");
    /**
     * [0] bot nick
     * [1] user nick
     * [2] user nickserv account, 0 if not idented
     * target channel is not echoed with response
     * WHO is a network-wide query so each response is one user
     * WHOIS queries one user and has multiple responses specific to them
     */
    if (code == 354) {
      //botname usernick useraccount
      this.onWho(info[1], info[2]);
    } else if (code == RPL_ENDOFWHO) {
      //botname channel :End of /WHO list.
      this.onEndOfWho();
    }
  }
  
  public HashMap<String, String> getResponses() {
    return users;
  }
        
}
