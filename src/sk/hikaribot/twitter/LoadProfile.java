/*
 * hikaribot - LoadProfile
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import java.io.IOException;
import org.jibble.pircbot.Colors;
import sk.hikaribot.bot.Main;
import sk.hikaribot.cmd.Command;
import twitter4j.TwitterException;

/**
 * Loads AccessToken profile to tweet as.
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
    log.info("TWITLOAD " + params + " from " + sender + " in " + channel);
    TwitBot twit = this.bot.getTwitBot();
    try {
      twit.loadAccessToken(params);
      bot.sendMessage(channel, Colors.BLUE + "TOKEN: " + Colors.NORMAL + "Success, I will now be tweeting as @" + params);
      log.info("TWITLOAD successful");
    } catch (IOException ex) {
      bot.sendMessage(channel, Colors.RED + "TOKEN: " + Colors.NORMAL + "Unable to read token file for " + params);
      log.error("TWITLOAD Unable to read token file for " + params);
    } catch (TwitBot.TokenMismatchException ex) {
      bot.sendMessage(channel, Colors.RED + "TOKEN: " + Colors.NORMAL + "Token file and contents did not match for " + params);
      log.error("TWITLOAD Token file and contents did not match for " + params);
    } catch (Main.MissingRequiredPropertyException ex) {
      bot.sendMessage(channel, Colors.RED + "TOKEN: " + Colors.NORMAL + "Token file for " + params + " was not sane");
      log.error("TWITLOAD Token file for " + params + " was not sane");
    } catch (TwitterException ex) {
      bot.sendMessage(channel, Colors.RED + "TOKEN: " + Colors.NORMAL + "Unable to authenticate with token file for " + params);
      log.error("TWITLOAD Unable to authenticate with token file for " + params);
    }

  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
