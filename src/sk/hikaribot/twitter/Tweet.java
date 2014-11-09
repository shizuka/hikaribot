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
    this.helpInfo = "tweet to the current profile, check with .twitWhoAmI";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    TwitBot twit = bot.getTwitBot();
    log.info("TWEET from " + sender + " in " + channel + ": " + params);
    try {
      Status tweet = twit.tweet(params);
      bot.sendMessage(channel, Colors.BLUE + "TWEET @" + twit.getActiveTwitName() + ": "
              + Colors.NORMAL + "https://twitter.com/" + twit.getActiveTwitName() + "/status/" + tweet.getId());
      log.info("TWEET OK: " + "https://twitter.com/" + twit.getActiveTwitName() + "/status/" + tweet.getId());
    } catch (TwitBot.NoProfileLoadedException ex) {
      bot.sendMessage(channel, Colors.RED + "TWEET: " + Colors.NORMAL + "No profile loaded");
      log.error("TWEET No profile loaded");
    } catch (TwitBot.TweetTooLongException ex) {
      bot.sendMessage(channel, Colors.RED + "TWEET: " + Colors.NORMAL + "Message too long! "
              + Colors.RED + params.length() + Colors.OLIVE + "/140 >" + Colors.NORMAL + params.substring(140));
      log.error("TWEET Message too long");
    } catch (TwitterException ex) {
      bot.sendMessage(channel, Colors.RED + "TWEET: " + Colors.NORMAL + "TwitterException occurred");
      log.error("TWEET Twitter Exception");
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
