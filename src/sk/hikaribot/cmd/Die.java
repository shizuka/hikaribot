/*
 * hikaribot - Die
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import org.jibble.pircbot.Colors;

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
    bot.sendMessage(channel, Colors.RED + "The wicked enchantress has cursed them all!");
    log.fatal("DIE requested by " + sender + " in " + channel);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      log.error("Something threw an InterruptedException at me");
    }
    bot.quitServer("Killed by " + sender);
  }

  @Override
  public void execute(String channel, String sender, String params) {
    execute(channel, sender);
  }

}
