/*
 * hikaribot - Tweet
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import sk.hikaribot.cmd.Command;
import twitter4j.TwitterException;

public class Tweet extends Command {

  public Tweet() {
    this.name = "tweet";
    this.numArgs = 1;
    this.helpArgs.add("message");
    this.helpInfo = "tweet to the current profile, check with .whoisTwit";
    this.reqPerm = 1; //voice
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    TwitBot twit = bot.getTwitBot();
    try {
      twit.tweet(params);
    } catch (TwitBot.NoProfileLoadedException ex) {
      bot.sendMessage(channel, sender + ": No profile loaded!");
    } catch (TwitBot.TweetTooLongException ex) {
      bot.sendMessage(channel, sender + ": Message too long! What part of 140 characters have you forgotten?");
    } catch (TwitterException ex) {
      bot.sendMessage(channel, sender + ": TwitterException occurred");
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
