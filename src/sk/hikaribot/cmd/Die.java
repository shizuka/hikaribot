/*
 * hikaribot - Die
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

/**
 * Kills the bot.
 */
public class Die extends Command {

  public Die() {
    this.name = "die";
    this.numArgs = 0;
    this.helpInfo = "this kills the bot";
    this.reqPerm = 3;
  }

  @Override
  public void execute(String channel, String sender) {
    log.fatal("DIE from " + sender + " in " + channel);
    bot.quitServer("Killed by " + sender);
  }

  @Override
  public void execute(String channel, String sender, String params) {
    execute(channel, sender);
  }

}
