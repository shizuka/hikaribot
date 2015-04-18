/*
 * hikaribot - BanlistResponse
 * Shizuka Kamishima - 2015-04-15
 * Observable
 * 
 * Copyright (c) 2015, Shizuka Kamishima
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
package sk.hikaribot.banhammer.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import static org.jibble.pircbot.ReplyConstants.*;

/**
 * Is passed to HikariBot when a channel wants the list of bans in it.
 *
 * @author Shizuka Kamishima
 */
public class BanlistResponse extends Observable implements Observer {

  /**
   * We only care about incoming RPL_BANLIST for this channel.
   */
  public final String channel;
  public final List<ScrapedBan> bans;

  public BanlistResponse(String channel) {
    this.channel = channel;
    this.bans = new ArrayList();
  }

  @Override
  public void update(Observable o, Object arg) {
    String line = arg.toString();
    String[] serverResponse = line.split(":", 2);
    int code = Integer.parseInt(serverResponse[0]);
    String response = serverResponse[1];
    if (!response.split(" ")[1].equals(channel)) {
      //we don't care about messages for channels other than ours
      return;
    }
    if (code == RPL_BANLIST) { //367
      this.onBanlist(response);
    } else if (code == RPL_ENDOFBANLIST) { //368
      this.onEndOfBanlist();
    }
  }

  private void onBanlist(String response) {
    /*
     * response = "botname #channel banmask author timestamp"
     * where banmask is the mask applied, *!*@foo.bar.baz
     * author is the usermask who set it, Foo!bar@baz.quux
     * timestamp is unix time, handling as string
     * (not that HikariBot will still be in service in 2038)
     *
     * botname is our nick, always present, never needed
     * channel was already checked by update() to be us
     */
    String[] parts = response.split(" ");
    String banmask = parts[2];
    String author = parts[3];
    String timestamp = parts[4];
    this.bans.add(new ScrapedBan(banmask, author, timestamp));
  }

  private void onEndOfBanlist() {
    this.setChanged();
    this.notifyObservers(this.bans);
  }

}
