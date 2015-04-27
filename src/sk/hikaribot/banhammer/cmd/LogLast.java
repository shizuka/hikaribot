/*
 * hikaribot - LogLast
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

import sk.hikaribot.api.Command;
import sk.hikaribot.api.exception.ImproperArgsException;
import sk.hikaribot.banhammer.api.BanChannel;

/**
 * Add a note to the most recent ban set.
 * 
 * @author Shizuka Kamishima
 */
public class LogLast extends Command {

  public LogLast() {
    this.name = "logLast";
    this.numArgs = 1;
    this.helpArgs.add("note...");
    this.helpInfo = "adds a note to the most recent ban set in this channel";
    this.reqPerm = 2; //staff
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    BanChannel ch = bot.getBanhammer().get(channel);
    int banId = ch.getNewestBanId();
    ch.addNote(banId, sender, params);
    log.info("BH-LOGLAST (" + banId + ") FROM " + sender + " IN " + channel + ": " + params);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }
  
  

}
