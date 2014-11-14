/*
 * hikaribot - WhoisResponse
 * Shizuka Kamishima - 2014-11-13
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

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.jibble.pircbot.ReplyConstants.*;

/**
 * Is passed to HikariBot when a command wants a WHOIS done, populates with
 * WHOIS responses until ENDOFWHOIS is reached, then notifies Observer
 *
 * @author Shizuka Kamishima
 */
public class WhoisResponse extends Observable implements Observer {

  private static final Logger log = LogManager.getLogger("WHOISRESPONSE");

  private final String target;
  private boolean exists = true;
  private String usermask;
  private String realname;
  private String canonicalNick;
  private boolean isIdented = false;

  public WhoisResponse(String target) {
    this.target = target;
  }

  public void onWhoisNoSuchNick(String response) {
    this.exists = false;
  }

  public void onWhoisUser(String username, String hostname, String realname) {
    //bot nick <username hostname * :realname>
    this.realname = realname;
    this.usermask = this.target + "!" + username + "@" + hostname;
    log.debug("WHOIS RESPONSE FOR " + this.target + ": " + this.usermask + " " + this.realname);
  }

  public void onWhoisChannels(String[] channels) {
    //bot nick <:#channel #channel @#channelwithop #channel>
    log.debug("WHOIS RESPONSE FOR " + this.target + ": Is in channels: " + Arrays.toString(channels));
  }

  public void onWhoisServer(String domainName, String friendlyName) {
    //bot nick <server.domain.name :friendly name>
    //don't care yet
    log.debug("WHOIS RESPONSE FOR " + this.target + ": Is connected to server " + domainName + ": " + friendlyName);
  }

  public void onWhoisIdle(String secsIdle, String signonTimestamp) {
    //bot nick <secsIdle signonTimestamp> :seconds idle, signon time
    //don't care yet
    log.debug("WHOIS RESPONSE FOR " + this.target + ": Has been idle for " + secsIdle + " seconds and signed on at timestamp " + signonTimestamp);
  }

  public void onWhoisNickserv(String canonicalNick) {
    //bot nick <canonical> :is logged in as
    this.canonicalNick = canonicalNick;
    this.isIdented = true;
    log.debug("WHOIS RESPONSE FOR " + this.target + ": Is logged in as: " + this.canonicalNick);
  }

  public void onEndOfWhois() {
    //bot nick :End of /WHOIS list.
    log.debug("WHOIS RESPONSE FOR " + this.target + " ENDS");
    this.setChanged();
    this.notifyObservers(this);
  }

  public String getTarget() {
    return target;
  }

  public String getUsermask() {
    return usermask;
  }

  public String getRealname() {
    return realname;
  }

  public String getCanonicalNick() {
    return canonicalNick;
  }

  public boolean isIdented() {
    return isIdented;
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
    String[] parts = response.split(":");
    String[] info = parts[0].split(" ");
    String extra = parts[1];
    if (info[1] == null ? this.target != null : !info[1].equals(this.target)) {
      /**
       * if there is a target nick and it doesn't equal this.target then this
       * response contains WHOIS for some other WhoisResponse so we can ignore
       * it
       *
       * it's exceedingly unlikely we will ever have more than one WHOIS
       * pending, nigh impossible, but better than a bug
       */
      return;
    }
    if (code == RPL_WHOISUSER) {
      //botname target username hostname * :realname
      this.onWhoisUser(info[2], info[3], extra);
    } else if (code == RPL_WHOISCHANNELS) {
      //botname target :#channel #channel @#channelwithop
      this.onWhoisChannels(extra.split(" "));
    } else if (code == RPL_WHOISSERVER) {
      //botname target server.domain.name :serverfriendlyname?
      this.onWhoisServer(info[2], extra);
    } else if (code == RPL_WHOISIDLE) {
      //botname target secondsidle logontimestamp :"seconds idle, signon time"
      this.onWhoisIdle(info[2], info[3]);
    } else if (code == 330) {
      //botname target canonicalNick :is logged in as
      this.onWhoisNickserv(info[2]);
    } else if (code == RPL_ENDOFWHOIS) {
      //botname target :End of /WHOIS list.
      this.onEndOfWhois();
    }
  }

}
