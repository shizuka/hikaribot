/*
 * hikaribot - DoAction
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import sk.hikaribot.api.Command;

/**
 * Performs /me action
 */
public class DoAction extends Command {

  public DoAction() {
    this.name = "do";
    this.numArgs = 2;
    this.helpArgs.add("channel");
    this.helpArgs.add("message");
    this.helpInfo = "does action to channel";
    this.reqPerm = 1; //voice and up
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length < numArgs) {
      throw new ImproperArgsException(this.name);
    }
    String[] args = params.split(" ", 2);
    if (!args[0].startsWith("#")) {
      args[0] = "#" + args[0];
    }
    bot.sendAction(args[0], args[1]);
    log.info("DO " + args[0] + " from " + sender + " in " + channel + ": " + args[1]);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
