/*
 * hikaribot - RequestNewToken
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter.cmd;

import org.jibble.pircbot.Colors;
import sk.hikaribot.api.Command;
import sk.hikaribot.bot.CommandRegistry;
import sk.hikaribot.twitter.TwitBot;

/**
 * Generates new requestToken URL for authorization.
 */
public class RequestNewToken extends Command {
  
  private CommandRegistry cmdRegistry;

  public RequestNewToken() {
    this.name = "twitRequest";
    this.numArgs = 0;
    this.helpInfo = "generates new requestToken URL for authorization, returns as PM";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    log.info("TWITREQUEST from " + sender + " in " + channel);
    this.cmdRegistry = bot.getCommandRegistry();
    TwitBot twit = bot.getTwitBot();
    String authUrl;
    try {
      authUrl = twit.requestNewToken();
      bot.sendMessage(sender, "Open the following URL and grant access to the target account: " + authUrl);
      bot.sendMessage(sender, "Then use '" + cmdRegistry.getDelimiter() + "twitConfirm <PIN>' in "
              + channel + " to complete the process, where <PIN> is the seven digit code given on that page");
      bot.sendMessage(channel, Colors.DARK_GREEN + "TWITREQUEST: " + Colors.NORMAL + "Please check PM for further instructions");
      log.info("TWITREQUEST Passed to PM");
    } catch (TwitBot.RequestInProgressException ex) {
      bot.sendMessage(channel, Colors.OLIVE + "TWITREQUEST: " + Colors.NORMAL + ": A token request is already in progress. Please complete that request with '"
              + cmdRegistry.getDelimiter() + "twitConfirm <PIN>' or '" + cmdRegistry.getDelimiter() + "twitCancel' to cancel");
      log.warn("TWITREQUEST Request already in progress");
      return;
    } catch (TwitBot.RequestCancelledException ex) {
      bot.sendMessage(channel, Colors.RED + "TWITREQUEST: " + Colors.NORMAL + "Token request was cancelled due to a Twitter error");
      log.error("TWITREQUEST Cancelled due to TwitterException");
      return;
    }
  }

}
