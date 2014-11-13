/*
 * hikaribot - CancelNewToken
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
package sk.hikaribot.twitter.cmd;

import org.jibble.pircbot.Colors;
import sk.hikaribot.api.Command;
import sk.hikaribot.api.exception.ImproperArgsException;
import sk.hikaribot.twitter.TwitBot;

/**
 * Cancels pending RequestToken process.
 */
public class CancelNewToken extends Command {

  public CancelNewToken() {
    this.name = "twitCancel";
    this.numArgs = 0;
    this.helpInfo = "cancels pending requestToken";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    TwitBot twit = bot.getTwitBot();

    if (!twit.pendingRequest()) {
      bot.sendMessage(channel, Colors.OLIVE + "TWITCANCEL: " + Colors.NORMAL + "No pending request");
      log.error("TWITCANCEL No pending request");
      return;
    }

    twit.cancelNewToken();
    bot.sendMessage(channel, Colors.DARK_GREEN + "TWITCANCEL: " + Colors.NORMAL + "Pending token request was cancelled");
    log.warn("TWITCANCEL Pending request was cancelled");
  }

}
