/*
 * hikaribot - CancelNewToken
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter.cmd;

import org.jibble.pircbot.Colors;
import sk.hikaribot.api.Command;
import sk.hikaribot.twitter.TwitBot;

/**
 * Cancels pending RequestToken process.
 */
public class CancelNewToken extends Command {

  public CancelNewToken() {
    this.name = "twitCancel";
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

    if (!twit.pendingRequest()) {
      bot.sendMessage(channel, Colors.OLIVE + "TWITCANCEL: " + Colors.NORMAL + "No pending request");
      log.error("TWITCANCEL No pending request");
      return;
    }

    twit.cancelNewToken();
    bot.sendMessage(channel, Colors.DARK_GREEN + "TWITCANCEL: " + Colors.NORMAL + "Pending token request was cancelled");
    log.warn("TWITCANCEL Pending request was cancelled");
  }

}
