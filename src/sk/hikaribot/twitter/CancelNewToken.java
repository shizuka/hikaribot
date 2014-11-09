/*
 * hikaribot - CancelNewToken
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import org.jibble.pircbot.Colors;
import sk.hikaribot.cmd.Command;

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
      bot.sendMessage(channel, Colors.RED + "REQUEST: " + Colors.NORMAL + "No pending request");
      return;
    }
    
    twit.cancelNewToken();
    bot.sendMessage(channel, Colors.RED + "REQUEST: " + Colors.NORMAL + "Pending token request was cancelled");
    log.error("TWITREQUEST Pending request was cancelled");
  }

}
