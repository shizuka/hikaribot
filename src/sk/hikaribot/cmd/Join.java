/*
 * hikaribot - Join
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

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
      throw new ImproperArgsException("join");
    }
    if (!params.startsWith("#")) {
      params = "#" + params;
    }

    bot.joinChannel(params);

    bot.sendMessage(channel, "I want to join channel " + params + " but I don't know how yet D:");
    log.info("JOIN " + params + " from " + sender + " in " + channel);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException("join");
  }

}
