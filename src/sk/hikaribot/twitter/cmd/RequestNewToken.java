/*
 * hikaribot - RequestNewToken
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
import sk.hikaribot.api.exception.ImproperArgsException;
import sk.hikaribot.bot.CommandRegistry;
import sk.hikaribot.twitter.TwitBot;

/**
 * Generates new requestToken URL for authorization.
 *
 * @author Shizuka Kamishima
 */
public class RequestNewToken extends Command {

  private CommandRegistry cmdRegistry;

  public RequestNewToken() {
    this.name = "twitRequest";
    this.numArgs = 0;
    this.helpInfo = "generates new requestToken URL for authorization, returns as PM";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    log.info("TWITREQUEST from " + sender + " in " + channel);
    this.cmdRegistry = bot.getCommandRegistry();
    TwitBot twit = bot.getTwitBot();
    String authUrl;
    try {
      authUrl = twit.requestNewToken();
      bot.sendMessage(sender, "Open the following URL and grant access to the target account: " + authUrl);
      bot.sendMessage(sender, "Then use '" + cmdRegistry.getDelimiter() + "twitConfirm <PIN>' in "
              + channel + " to complete the process, where <PIN> is the seven digit code given on that page");
      bot.sendMessage(channel, Colors.DARK_GREEN + "TWITREQUEST: " + Colors.NORMAL + "Please check PM for further instructions");
      log.info("TWITREQUEST Passed to PM");
    } catch (TwitBot.RequestInProgressException ex) {
      bot.sendMessage(channel, Colors.OLIVE + "TWITREQUEST: " + Colors.NORMAL + ": A token request is already in progress. Please complete that request with '"
              + cmdRegistry.getDelimiter() + "twitConfirm <PIN>' or '" + cmdRegistry.getDelimiter() + "twitCancel' to cancel");
      log.warn("TWITREQUEST Request already in progress");
      return;
    } catch (TwitBot.RequestCancelledException ex) {
      bot.sendMessage(channel, Colors.RED + "TWITREQUEST: " + Colors.NORMAL + "Token request was cancelled due to a Twitter error");
      log.error("TWITREQUEST Cancelled due to TwitterException");
      return;
    }
  }

}
