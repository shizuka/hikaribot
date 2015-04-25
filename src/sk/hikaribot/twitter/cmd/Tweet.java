/*
 * hikaribot - Tweet
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
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Posts a tweet as the active profile.
 *
 * @author Shizuka Kamishima
 */
public class Tweet extends Command {

  public Tweet() {
    this.name = "tweet";
    this.numArgs = 1;
    this.helpArgs.add("message");
    this.helpInfo = "tweet to the current profile, check with .twitWhoAmI";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    TwitBot twit = bot.getTwitBot();
    log.info("TWEET FROM " + sender + " IN " + channel + ": " + params);
    try {
      Status tweet = twit.tweet(params);
      //tweet echo now handled by TwitListener
      log.info("TWEET OK: " + "https://twitter.com/" + twit.getActiveTwitName() + "/status/" + tweet.getId());
    } catch (NoProfileLoadedException ex) {
      bot.sendMessage(channel, Colors.RED + "TWEET: " + Colors.NORMAL + "No profile loaded");
      log.error("TWEET No profile loaded");
    } catch (TweetTooLongException ex) {
      bot.sendMessage(channel, Colors.RED + "TWEET: " + Colors.NORMAL + "Message too long! "
              + Colors.RED + params.length() + Colors.OLIVE + "/140 >" + Colors.NORMAL + params.substring(140));
      log.error("TWEET Message too long");
    } catch (TwitterException ex) {
      bot.sendMessage(channel, Colors.RED + "TWEET: " + Colors.NORMAL + "TwitterException occurred: " + Colors.OLIVE + ex.getMessage());
      log.error("TWEET Twitter Exception: " + ex.getMessage());
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
