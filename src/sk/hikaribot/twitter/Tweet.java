/*
 * hikaribot - Tweet
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import org.jibble.pircbot.Colors;
import sk.hikaribot.cmd.Command;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Posts a tweet as the active profile.
 */
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
      Status tweet = twit.tweet(params);
      bot.sendMessage(channel, Colors.BLUE + "TWEET @" + twit.getActiveTwitName() + ": "
              + Colors.NORMAL + "https://twitter.com/" + twit.getActiveTwitName() + "/status/" + tweet.getId());
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
