/*
 * hikaribot - CancelNewToken
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import sk.hikaribot.cmd.Command;

public class CancelNewToken extends Command {

  public CancelNewToken() {
    this.name = "twitCancelReq";
    this.numArgs = 0;
    this.helpInfo = "cancels pending requestToken";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    TwitBot twit = bot.getTwitBot();
    twit.cancelNewToken();
    bot.sendMessage(channel, sender + ": Pending token request was cancelled");
  }

}
