/*
 * hikaribot - ConfirmNewToken
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import sk.hikaribot.cmd.Command;

/**
 * Exchanges pending RequestToken for an AccessToken and stores.
 */
public class ConfirmNewToken extends Command {

  public ConfirmNewToken() {
    this.name = "twitConfirmReq";
    this.numArgs = 1;
    this.helpArgs.add("PIN");
    this.helpInfo = "confirms authorization using pin";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    if (params.split(" ").length != numArgs) {
      throw new ImproperArgsException(this.name);
    }
    TwitBot twit = bot.getTwitBot();
    try {
      String name = twit.confirmNewToken(params);
      bot.sendMessage(channel, sender + ": Confirmed! I am now tweeting as @" + name);
    } catch (TwitBot.RequestCancelledException ex) {
      bot.sendMessage(channel, sender + ": Confirmation failed. Token request is cancelled");
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
