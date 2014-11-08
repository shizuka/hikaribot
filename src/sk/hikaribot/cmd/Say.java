/*
 * hikaribot - Say
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */

package sk.hikaribot.cmd;


public class Say extends Command {

  public Say() {
    this.name = "say";
    this.numArgs = 2;
    this.helpArgs.add("channel");
    this.helpArgs.add("message");
    this.helpInfo = "says message to channel";
    this.reqPerm = 1; //voice and up
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length < numArgs) {
      throw new ImproperArgsException("say");
    }
    String[] args = params.split(" ",2);
    if (!args[0].startsWith("#")) {
      args[0] = "#" + args[0];
    }
    bot.sendMessage(args[0], args[1]);
    log.info(sender + " in " + channel + " has me SAY to " + args[0] + ": " + args[1]);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException("say");
  }

}
