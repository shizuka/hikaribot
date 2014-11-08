/*
 * hikaribot - UnloadProfile
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import sk.hikaribot.cmd.Command;

/**
 * Unloads active Twitter profile.
 */
public class UnloadProfile extends Command {

  public UnloadProfile() {
    this.name = "unloadTwit";
    this.numArgs = 0;
    this.helpInfo = "clears active profile";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    log.info("UNLOADTWIT requested by " + sender + " in " + channel);
    TwitBot twit = bot.getTwitBot();
    twit.clearAccessToken();
    bot.sendMessage(channel, sender + ": Profile unloaded");
  }

}
