/*
 * hikaribot - Die
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

/**
 * Kills the bot. From owner anywhere, :die
 */
public class Die extends Command {

  public Die() {
    this.name = "die";
    this.arg = "";
    this.info = "this kills the bot";
    this.reqPerm = 3;
  }

  @Override
  public void execute(String channel, String sender, String message) {
    log.info("DIE from " + sender);
    super.bot.quitServer("Killed by " + sender);
  }

}
