/*
 * hikaribot - SetOptions
 * Shizuka Kamishima - 2015-04-18
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
 * Sets Banhammer options for a channel.
 *
 * @author Shizuka Kamishima
 */
public class SetOptions extends Command {

  public SetOptions() {
    this.name = "bhSetOps";
    this.numArgs = 4;
    this.helpArgs.add("channel or @");
    this.helpArgs.add("loThreshold");
    this.helpArgs.add("hiThreshold");
    this.helpArgs.add("kickMessage...");
    this.helpInfo = "set options - channel (or @ for global) - limit to start rotating - limit to keep under - message on kick";
    this.reqPerm = 4; //admins
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    String[] args = params.split(" ", 4);
    if (args.length < this.numArgs) {
      throw new ImproperArgsException(this.name);
    }
    String target = args[0];
    boolean global = false;
    if ("@".equals(target)) {
      global = true;
    } else if (target.charAt(0) != '#') {
      target = "#" + target;
    }
    if (!global && !bot.inChannel(target)) {
      bot.sendMessage(channel, Colors.BROWN + "BANHAMMER: " + Colors.NORMAL + "Not in channel " + Colors.OLIVE + target);
    }
    int loThreshold = 0;
    int hiThreshold = 0;
    try {
      loThreshold = Integer.parseInt(args[1]);
      hiThreshold = Integer.parseInt(args[2]);
    } catch (NumberFormatException ex) {
      throw new ImproperArgsException(this.name);
    }
    String kickMessage = args[3];
    //by now we will assume the values are okay
    bot.getBanhammer().setOptions(target, loThreshold, hiThreshold, kickMessage); //it will do messaging
    log.info("BH-SETOPTIONS FROM " + sender + " IN " + channel + ": lo=" + loThreshold + " hi=" + hiThreshold + " kick=" + kickMessage);
    if (global) {
      bot.sendMessage(channel, Colors.DARK_GREEN + "BANHAMMER: " + Colors.NORMAL + "Global options set.");
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
