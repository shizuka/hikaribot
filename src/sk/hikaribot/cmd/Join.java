/*
 * hikaribot - Join
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import java.util.Arrays;
import org.jibble.pircbot.Colors;

/**
 * Joins a channel.
 */
public class Join extends Command {

  public Join() {
    this.name = "join";
    this.numArgs = 1;
    this.helpArgs.add("channel");
    this.helpInfo = "joins channel";
    this.reqPerm = 3; //owner only
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs) {
      throw new ImproperArgsException(this.name);
    }
    if (!params.startsWith("#")) {
      params = "#" + params;
    }
    bot.joinChannel(params);
    bot.sendMessage(channel, Colors.GREEN + "JOIN: " + Colors.NORMAL + "Joined channel " + params);
    log.info("JOIN " + params + " from " + sender + " in " + channel);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
