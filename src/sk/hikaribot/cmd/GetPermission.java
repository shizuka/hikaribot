/*
 * hikaribot - GetPermission
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import org.jibble.pircbot.Colors;

/**
 * Prints invoker's permission level.
 */
public class GetPermission extends Command {

  public GetPermission() {
    this.name = "permLevel";
    this.numArgs = 1;
    this.helpArgs.add("nick");
    this.helpInfo = "prints user permissions level, defaults to invoker";
    this.reqPerm = 1;
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs) {
      throw new ImproperArgsException(this.name);
    }
    int permission = bot.getUserPermission(channel, params);
    bot.sendMessage(channel, Colors.BLUE + "PERMISSION: " + Colors.NORMAL + params + " - " + Colors.YELLOW + permission);
    log.info("PERMISSION " + params + " from " + sender + " in " + channel + ": " + permission);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    execute(channel, sender, sender);
  }

}
