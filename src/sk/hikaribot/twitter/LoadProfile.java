/*
 * hikaribot - LoadProfile
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import java.io.IOException;
import sk.hikaribot.bot.Main;
import sk.hikaribot.cmd.Command;
import twitter4j.TwitterException;

public class LoadProfile extends Command {

  public LoadProfile() {
    this.name = "loadTwit";
    this.numArgs = 1;
    this.helpArgs.add("username");
    this.helpInfo = "no @ - change who I will tweet as";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs ||
            params.startsWith("@")) {
      throw new ImproperArgsException(this.name);
    }
    log.info("LOADTWIT " + params + " requested by " + sender + " in " + channel);
    TwitBot twit = this.bot.getTwitBot();
    try {
      twit.loadAccessToken(params);
    } catch (IOException ex) {
      bot.sendMessage(channel, sender + ": Unable to read token file for " + params);
    } catch (TwitBot.TokenMismatchException ex) {
      bot.sendMessage(channel, sender + ": Token file and contents did not match for " + params);
    } catch (Main.MissingRequiredPropertyException ex) {
      bot.sendMessage(channel, sender + ": Token file for " + params + " was not sane");
    } catch (TwitterException ex) {
      bot.sendMessage(channel, sender + ": Unable to authenticate with token file for " + params);
    }
    bot.sendMessage(channel, sender + ": Success, I will now be tweeting as @" + params);
    log.info("LOADTWIT successful");
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
