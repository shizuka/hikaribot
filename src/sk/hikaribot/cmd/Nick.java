/*
 * hikaribot - Nick
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import sk.hikaribot.api.Command;
import org.jibble.pircbot.Colors;

/**
 * Changes nick.
 */
public class Nick extends Command {

  public Nick() {
    this.name = "nick";
    this.numArgs = 1;
    this.helpArgs.add("newnick");
    this.helpInfo = "changes nick";
    this.reqPerm = 3; //owner only
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs) {
      throw new ImproperArgsException(this.name);
    }
    bot.changeNick(params);

    if (!bot.getNick().equals(params)) {
      bot.sendMessage(channel, Colors.RED + "NICK: " + Colors.NORMAL + "Unable to change nick!");
      log.error("NICK " + params + " from " + sender + " in " + channel + " FAILED");
    } else {
      log.info("NICK " + params + " from " + sender + " in " + channel);
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
