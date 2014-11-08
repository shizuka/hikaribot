/*
 * hikaribot - Verbose
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

/**
 * Sets HikariBot to disable verbose logging.
 */
public class NoVerbose extends Command {

  public NoVerbose() {
    this.name = "noverbose";
    this.numArgs = 0;
    this.helpInfo = "turn off verbose logging";
    this.reqPerm = 3; //owner only
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    this.execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    log.info("NOVERBOSE requested by " + sender + " in " + channel);
    log.warn("Only bot commands will be logged");
    bot.setVerbose(false);
    bot.sendMessage(channel, "Verbose logging disabled");
  }

}
