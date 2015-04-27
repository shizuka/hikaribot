/*
 * hikaribot - BanCount
 * Shizuka Kamishima - 2015-04-11
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
import sk.hikaribot.banhammer.api.BanChannel;

/**
 * Prints status of Banhammer in a channel.
 * BANHAMMER: Tracking [x]/lo/hi active (and y inactive) bans
 * 
 * @author Shizuka Kamishima
 */
public class BanCount extends Command {

  public BanCount() {
    this.name = "bhstatus";
    this.helpInfo = "prints status of Banhammer in <channel>, default here";
    this.numArgs = 1;
    this.helpArgs.add("channel");
    this.reqPerm = 0;
  }
  
  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (!bot.inChannel(channel)) {
      bot.sendMessage(channel, Colors.BROWN + "BANHAMMER: " + Colors.NORMAL + "Not in that channel.");
      return;
    }
    BanChannel ch = bot.getBanhammer().get(channel);
    int numActive = ch.getNumActive();
    int numInactive = ch.getNumInactive();
    String fLo = Colors.OLIVE + ch.ops().loThreshold + Colors.NORMAL;
    String fHi = Colors.BROWN + ch.ops().hiThreshold + Colors.NORMAL;
    String num = "[" + numActive + "]" + Colors.NORMAL;
    String prefix = Colors.BLUE + "BANHAMMER: " + Colors.NORMAL + "Tracking ";
    String midNoIn = " active bans";
    String midWIn = " active and " + numInactive + " inactive bans";
    String out = prefix;
    if (numActive > ch.ops().hiThreshold) {
      out += fLo + "/" + fHi + "/" + Colors.RED + num;
    } else if (numActive > ch.ops().loThreshold) {
      out += fLo + "/" + Colors.YELLOW + num + "/" + fHi;
    } else {
      out += Colors.GREEN + num + "/" + fLo + "/" + fHi;
    }
    if (numInactive > 0) {
      out += midWIn;
    } else {
      out += midNoIn;
    }
    bot.sendMessage(channel, out);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    this.execute(channel, sender, channel);
  }

}
