/*
 * hikaribot - ListenerToggleEcho
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
import sk.hikaribot.twitter.TwitListener;

/**
 * Toggles whether TwitListener will echo tweets to this channel.
 *
 * @author Shizuka Kamishima
 */
public class ListenerToggleEcho extends Command {

  public ListenerToggleEcho() {
    this.name = "twitListen";
    this.numArgs = 1;
    this.helpArgs.add("on|off");
    this.helpInfo = "turn on or off echoing tweets to this channel";
    this.reqPerm = 2;
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs) {
      throw new ImproperArgsException(this.name);
    }
    TwitListener twl = bot.getTwitBot().getListener();
    switch (params) {
      case "on":
        if (twl.isEchoing()) {
          break;
        }
        try {
          bot.getTwitBot().startListener();
          twl.setEchoing(true);
          log.info("TWITLISTEN ON FROM " + sender + " in " + channel);
          bot.sendMessage(channel, Colors.OLIVE + "TWEET ECHOING: " + Colors.NORMAL + "Enabled");
        } catch (NoProfileLoadedException ex) {
          bot.sendMessage(channel, Colors.RED + "TWEET ECHOING: " + Colors.NORMAL + "Listener not initialized, call " + Colors.OLIVE + bot.getDelimiter() + "twitAssign");
        }
        break;
      case "off":
        if (!twl.isEchoing()) {
          break;
        }
        bot.getTwitBot().stopListener();
        twl.setEchoing(false);
        log.info("TWITLISTEN OFF FROM " + sender + " in " + channel);
        bot.sendMessage(channel, Colors.OLIVE + "TWEET ECHOING: " + Colors.NORMAL + "Disabled");
        break;
      default:
        throw new ImproperArgsException(this.name);
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
