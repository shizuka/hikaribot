/*
 * hikaribot - ModeResponse
 * Shizuka Kamishima - 2015-04-10
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
package sk.hikaribot.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.jibble.pircbot.ReplyConstants.*;

/**
 * Is passed to HikariBot when Banhammer wants a list of modes, populates with
 * active modes, then calls Observer.
 *
 * @author Shizuka Kamishima
 */
public class ModeResponse extends Observable implements Observer {

  private static final Logger log = LogManager.getLogger("MODE");

  private final String channel;
  private final String replyToChannel;
  private final String mode;

  private final List<ModeEntry> entries;

  /**
   * Prepares ModelistResponse for MODE [channel] +[mode] responses.
   *
   * @param channel the channel to gather from
   * @param mode the mode to check
   */
  public ModeResponse(String channel, String mode, String replyToChannel) {
    this.channel = channel;
    this.replyToChannel = replyToChannel;
    this.mode = mode;
    this.entries = new ArrayList();
  }
  
  public ModeResponse(String channel, String mode) {
    this(channel, mode, channel);
  }

  @Override
  public void update(Observable o, Object arg) {
    //368:Asuka #cathedral [q] *!*@foo.bar.baz Shizuka!hikari@shizuka.kamishima 1428726302
    //q is all special at Ponychat, beI don't include their mode here
    String line = arg.toString();
    String[] serverResponse = line.split(":", 2);
    int code = Integer.parseInt(serverResponse[0]);
    String response = serverResponse[1];
    this.onServerResponse(code, response);
  }

  private void onServerResponse(int code, String response) {
    if (!response.split(" ")[1].equals(channel)) {
      //split out the channel at the front, if it's not ours, we don't care
      return;
    }

    switch (code) {
      case RPL_BANLIST:
        this.onList(response);
        break;
      case RPL_ENDOFBANLIST:
        this.onEndOfList();
        break;
      default:
        break;
    }
  }

  private void onEndOfList() {
    this.setChanged();
    this.notifyObservers(this);
  }

  private void onList(String response) {
    String[] parts = response.split(" ");
    /*
     * [0] botname - always comes back
     * [1] #cathedral - we already checked that this is our channel
     * [n-3 / 2] *!*@foo.bar.baz - mask the mode applies to
     * [n-2 / 3] Shizuka!hikari@shizuka.kamishima - who set it
     * [n-1 / 4] timestamp - when
     */
    String mask = parts[parts.length-3];
    String usermaskWhoSet = parts[parts.length-2];
    String timestamp = parts[parts.length-1];
    this.entries.add(new ModeEntry(channel, mode, mask, usermaskWhoSet, timestamp));
  }
  
  public List<ModeEntry> getEntries() {
    return this.entries;
  }
  
  public String getChannel() {
    return this.channel;
  }
  
  public String getReplyToChannel() {
    return this.replyToChannel;
  }

}
