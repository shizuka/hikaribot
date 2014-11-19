/*
 * hikaribot - ConfirmNewToken
 * Shizuka Kamishima - 2014-11-08
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
 * Exchanges pending RequestToken for an AccessToken and stores.
 *
 * @author Shizuka Kamishima
 */
public class ConfirmNewToken extends Command {

  public ConfirmNewToken() {
    this.name = "twitConfirm";
    this.numArgs = 1;
    this.helpArgs.add("PIN");
    this.helpInfo = "confirms authorization using pin";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs) {
      throw new ImproperArgsException(this.name);
    }
    TwitBot twit = bot.getTwitBot();
    log.info("TWITCONFIRM " + params + " from " + sender + " in " + channel);

    if (!twit.pendingRequest()) {
      bot.sendMessage(channel, Colors.OLIVE + "TWITCONFIRM: " + Colors.NORMAL + "No pending request");
      log.error("TWITCONFIRM No pending request");
      return;
    }

    try {
      String name = twit.confirmNewToken(params);
      bot.sendMessage(channel, Colors.DARK_GREEN + "TWITCONFIRM: " + Colors.NORMAL + "Confirmed! Successfully authenticated for + " + Colors.OLIVE + "@" + name);
      log.info("TWITCONFIRM OK: " + name);
    } catch (RequestCancelledException ex) {
      bot.sendMessage(channel, Colors.RED + "TWITCONFIRM: " + Colors.NORMAL + "Confirmation failed. Token request is cancelled");
      log.error("TWITCONFIRM Authorization failed, request cancelled");
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
