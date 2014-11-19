/*
 * hikaribot - LoadProfile
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

import java.io.IOException;
import org.jibble.pircbot.Colors;
import sk.hikaribot.api.exception.MissingRequiredPropertyException;
import sk.hikaribot.api.Command;
import sk.hikaribot.api.exception.ImproperArgsException;
import sk.hikaribot.api.exception.TokenMismatchException;
import sk.hikaribot.twitter.TwitBot;
import twitter4j.TwitterException;

/**
 * Loads AccessToken profile to tweet as.
 *
 * @author Shizuka Kamishima
 */
public class LoadProfile extends Command {

  public LoadProfile() {
    this.name = "twitLoad";
    this.numArgs = 1;
    this.helpArgs.add("username");
    this.helpInfo = "no @ - change who I will tweet as";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs
            || params.startsWith("@")) {
      throw new ImproperArgsException(this.name);
    }
    params = params.toLowerCase();
    log.info("TWITLOAD " + params + " from " + sender + " in " + channel);
    TwitBot twit = this.bot.getTwitBot();
    try {
      twit.loadAccessToken(params);
      bot.sendMessage(channel, Colors.DARK_GREEN + "TWITLOAD: " + Colors.NORMAL + "Successfully loaded AccessToken for " + Colors.OLIVE + "@" + params);
      log.info("TWITLOAD successful");
    } catch (IOException ex) {
      bot.sendMessage(channel, Colors.RED + "TWITLOAD: " + Colors.NORMAL + "Unable to read token file for " + Colors.OLIVE + params);
      log.error("TWITLOAD Unable to read token file for " + params);
    } catch (TokenMismatchException ex) {
      bot.sendMessage(channel, Colors.RED + "TWITLOAD: " + Colors.NORMAL + "Token file and contents did not match for " + Colors.OLIVE + params);
      log.error("TWITLOAD Token file and contents did not match for " + params);
    } catch (MissingRequiredPropertyException ex) {
      bot.sendMessage(channel, Colors.RED + "TWITLOAD: " + Colors.NORMAL + "Token file for " + Colors.OLIVE + params + Colors.NORMAL + " was not sane");
      log.error("TWITLOAD Token file for " + params + " was not sane");
    } catch (TwitterException ex) {
      bot.sendMessage(channel, Colors.RED + "TWITLOAD: " + Colors.NORMAL + "Unable to authenticate with token file for " + Colors.OLIVE + params);
      log.error("TWITLOAD Unable to authenticate with token file for " + params);
    }

  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
