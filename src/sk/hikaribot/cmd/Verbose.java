/*
 * hikaribot - Verbose
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import org.jibble.pircbot.Colors;

/**
 * Sets HikariBot to verbose logging.
 */
public class Verbose extends Command {

  public Verbose() {
    this.name = "verbose";
    this.numArgs = 0;
    this.helpInfo = "set bot to verbose logging";
    this.reqPerm = 3; //owner only
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    this.execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    log.info("VERBOSE from " + sender + " in " + channel);
    log.warn("All rawlines will now be logged");
    bot.setVerbose(true);
    bot.sendMessage(channel, Colors.YELLOW + "VERBOSE: " + Colors.NORMAL + "Enabled");
  }

}
