/*
 * hikaribot - ListenerAssign
 * Shizuka Kamishima - 2014-11-18
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
package sk.hikaribot.twitter.cmd;

import org.jibble.pircbot.Colors;
import sk.hikaribot.api.Command;
import sk.hikaribot.api.exception.*;
import sk.hikaribot.twitter.TwitBot;

/**
 * Assigns TwitBot's Listener to this channel.
 *
 * @author Shizuka Kamishima
 */
public class ListenerAssign extends Command {

  public ListenerAssign() {
    this.name = "twitAssign";
    this.numArgs = 1;
    this.helpArgs.add("channel");
    this.helpInfo = "assign the tweet listener to channel, defaults to here";
    this.reqPerm = 3;
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs) {
      throw new ImproperArgsException(this.name);
    }
    TwitBot twit = bot.getTwitBot();
    try {
      twit.assignListener(params);
      bot.sendMessage(channel, Colors.DARK_GREEN + "TWITASSIGN: " + Colors.NORMAL + "Assigned tweet listener to "
            + params + ", add follows/tracks then call " + Colors.OLIVE + bot.getDelimiter() + "twitListen on" + Colors.NORMAL + " to start echoing");
    } catch (NoProfileLoadedException ex) {
      bot.sendMessage(channel, Colors.BROWN + "TWITASSIGN: " + Colors.NORMAL + "No profile loaded, call " + Colors.OLIVE + bot.getDelimiter() + "twitload <profile>");
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    execute(channel, sender, channel);
  }

}
