/*
 * hikaribot - ConfirmNewToken
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import org.jibble.pircbot.Colors;
import sk.hikaribot.cmd.Command;

/**
 * Exchanges pending RequestToken for an AccessToken and stores.
 */
public class ConfirmNewToken extends Command {

  public ConfirmNewToken() {
    this.name = "twitConfirm";
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
    log.info("TWITCONFIRM " + params + " from " + sender + " in " + channel);

    if (!twit.pendingRequest()) {
      bot.sendMessage(channel, Colors.RED + "TWITCONFIRM: " + Colors.NORMAL + "No pending request");
      log.error("TWITCONFIRM No pending request");
      return;
    }

    try {
      String name = twit.confirmNewToken(params);
      bot.sendMessage(channel, Colors.BLUE + "TWITCONFIRM: " + Colors.NORMAL + "Confirmed! I am now tweeting as @" + name);
      log.info("TWITCONFIRM OK: " + name);
    } catch (TwitBot.RequestCancelledException ex) {
      bot.sendMessage(channel, Colors.RED + "TWITCONFIRM: " + Colors.NORMAL + "Confirmation failed. Token request is cancelled");
      log.error("TWITCONFIRM Authorization failed, request cancelled");
    }
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    throw new ImproperArgsException(this.name);
  }

}
