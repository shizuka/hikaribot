/*
 * hikaribot - Part
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import org.jibble.pircbot.Colors;

/**
 * Parts a channel.
 */
public class Part extends Command {

  public Part() {
    this.name = "part";
    this.numArgs = 1;
    this.helpArgs.add("channel");
    this.helpInfo = "parts channel";
    this.reqPerm = 2; //ops and up
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs) {
      throw new ImproperArgsException(this.name);
    }
    if (!params.startsWith("#")) {
      params = "#" + params;
    }
    bot.partChannel(params, "THE HORROR!");
    if (!params.equals(channel)) {
      bot.sendMessage(channel, Colors.BLUE + "PART: " + Colors.NORMAL + "Parted channel " + params);
    }
    log.info("PART " + params + " from " + sender + " in " + channel);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
