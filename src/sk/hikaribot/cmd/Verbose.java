/*
 * hikaribot - Verbose
 * Shizuka Kamishima - 2014-11-08
 */

/*
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
package sk.hikaribot.cmd;

import sk.hikaribot.api.Command;
import org.jibble.pircbot.Colors;
import sk.hikaribot.api.exception.ImproperArgsException;

/**
 * Sets HikariBot to verbose logging.
 */
public class Verbose extends Command {

  public Verbose() {
    this.name = "verbose";
    this.numArgs = 0;
    this.helpInfo = "set bot to verbose logging";
    this.reqPerm = 3; //owner only
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    this.execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    log.info("VERBOSE from " + sender + " in " + channel);
    log.warn("All rawlines will now be logged");
    bot.setVerbose(true);
    bot.sendMessage(channel, Colors.OLIVE + "VERBOSE: " + Colors.NORMAL + "Enabled");
  }

}
