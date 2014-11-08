/*
 * hikaribot - Version
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */

package sk.hikaribot.cmd;


public class Version extends Command {

  public Version() {
    this.name = "version";
    this.numArgs = 0;
    this.helpInfo = "prints version";
    this.reqPerm = 1; //voice and up
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    this.execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    bot.sendMessage(channel, sender + ": " + bot.getVersion());
    log.info("VERSION requested by " + sender + " in " + channel);
  }

}
