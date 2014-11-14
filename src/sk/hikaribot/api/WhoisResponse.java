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

  /**
   * Prepares WhoisResponse to collect WHOIS information.
   *
   * @param target the nick we're WHOISing
   */
  public WhoisResponse(String target) {
    this.target = target;
  }

  /**
   * 401 ERR_NOSUCHNICK, nick doesn't exist. Immediately followed by ENDOFWHOIS.
   */
  public void onWhoisNoSuchNick() {
    this.exists = false;
  }

  /**
   * 311 RPL_WHOISUSER, target's usermask.
   *
   * @param username 'bar' in foo!bar@baz
   * @param hostname 'baz' in foo!bar@baz
   * @param realname "real name" given by client
   */
  public void onWhoisUser(String username, String hostname, String realname) {
    //bot nick <username hostname * :realname>
    this.realname = realname;
    this.usermask = this.target + "!" + username + "@" + hostname;
    log.debug("WHOIS RESPONSE FOR " + this.target + ": " + this.usermask + " " + this.realname);
  }

  /**
   * 319 RPL_WHOISCHANNELS, list of visible channels the target is in. Omits any
   * +s or +p channel the bot does not share with the target.
   *
   * @param channels
   */
  public void onWhoisChannels(String[] channels) {
    //bot nick <:#channel #channel @#channelwithop #channel>
    log.debug("WHOIS RESPONSE FOR " + this.target + ": Is in channels: " + Arrays.toString(channels));
  }

  /**
   * 312 RPL_WHOISSERVER, network server the target is connected to.
   *
   * @param domainName server domain name
   * @param friendlyName server's friendly name? honestly not sure what it
   * represents
   */
  public void onWhoisServer(String domainName, String friendlyName) {
    //bot nick <server.domain.name :friendly name>
    //don't care yet
    log.debug("WHOIS RESPONSE FOR " + this.target + ": Is connected to server " + domainName + ": " + friendlyName);
  }

  /**
   * 317 RPL_WHOISIDLE, seconds the user has been idle for. Sometimes present
   * sometimes not. Not sure what causes it to not be sent.
   *
   * @param secsIdle
   * @param signonTimestamp
   */
  public void onWhoisIdle(String secsIdle, String signonTimestamp) {
    //bot nick <secsIdle signonTimestamp> :seconds idle, signon time
    //don't care yet
    log.debug("WHOIS RESPONSE FOR " + this.target + ": Has been idle for " + secsIdle + " seconds and signed on at timestamp " + signonTimestamp);
  }

  /**
   * 330 Nickserv login, the nickserv account the target has identified for.
   * Absent if the target is not idented.
   *
   * @param canonicalNick nickserv account the target is identified for
   */
  public void onWhoisNickserv(String canonicalNick) {
    //bot nick <canonical> :is logged in as
    this.canonicalNick = canonicalNick;
    this.isIdented = true;
    log.debug("WHOIS RESPONSE FOR " + this.target + ": Is logged in as: " + this.canonicalNick);
  }

  /**
   * 318 RPL_ENDOFWHOIS, all whois information has been sent by this point.
   * Notifies Observer that the request is complete. Observer will fetch and
   * discard this WhoisResponse.
   */
  public void onEndOfWhois() {
    //bot nick :End of /WHOIS list.
    log.debug("WHOIS RESPONSE FOR " + this.target + " ENDS");
    this.setChanged();
    this.notifyObservers(this);
  }

  /**
   * @return the nick we're WHOISing
   */
  public String getTarget() {
    return target;
  }

  /**
   * @return target's usermask, foo!bar@baz.quux
   */
  public String getUsermask() {
    return usermask;
  }

  /**
   * @return target's realname field
   */
  public String getRealname() {
    return realname;
  }

  /**
   * @return the nickserv account target has identified for, if any
   */
  public String getCanonicalNick() {
    return canonicalNick;
  }

  /**
   * @return is the target identified with nickserv?
   */
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
    } else if (code == ERR_NOSUCHNICK) {
      this.onWhoisNoSuchNick();
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
