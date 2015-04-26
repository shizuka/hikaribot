/*
 * hikaribot - RemoveChannel
 * Shizuka Kamishima - 2015-04-26
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
package sk.hikaribot.banhammer.cmd;

import org.jibble.pircbot.Colors;
import sk.hikaribot.api.Command;
import sk.hikaribot.api.exception.ImproperArgsException;

/**
 * Destroys BanChannel worker for a channel. Turns off Banhammer.
 * 
 * @author Shizuka Kamishima
 */
public class RemoveChannel extends Command {
  
  public RemoveChannel() {
    this.name = "bhDel";
    this.numArgs = 1;
    this.helpArgs.add("channel");
    this.helpInfo = "disables Banhammer in <channel>, default here";
    this.reqPerm = 4; //admin
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    //sanity check params to be a channel name and one we're in
    if (params.charAt(0) != '#') {
      params = "#" + params;
    }
    if (!bot.inChannel(params)) {
      bot.sendMessage(channel, Colors.BROWN + "BANHAMMER: " + Colors.NORMAL + "I'm not in channel " + Colors.OLIVE + params);
      return;
    }
    bot.getBanhammer().removeChannel(channel);
    log.info("BH-REMCHANNEL " + params + " FROM " + sender + " IN " + channel);
    //the resulting BanChannel will handle any further messages/logs
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    this.execute(channel, sender, channel);
  }

}
