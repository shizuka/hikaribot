/*
 * hikaribot - RequestNewToken
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import org.jibble.pircbot.Colors;
import sk.hikaribot.cmd.Command;

/**
 * Generates new requestToken URL for authorization.
 */
public class RequestNewToken extends Command {

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
    TwitBot twit = bot.getTwitBot();
    String authUrl;
    try {
      authUrl = twit.requestNewToken();
      bot.sendMessage(sender, "Open the following URL and grant access to the target account: " + authUrl);
      bot.sendMessage(sender, "Then use '" + cmdRegistry.getDelimiter() + "twitConfirm <PIN>' in "
              + channel + " to complete the process, where <PIN> is the seven digit code given on that page");
    } catch (TwitBot.RequestInProgressException ex) {
      bot.sendMessage(channel, Colors.YELLOW + "REQUEST: " + Colors.NORMAL + ": A token request is already in progress. Please complete that request with '"
              + cmdRegistry.getDelimiter() + "twitConfirm <PIN>' or '" + cmdRegistry.getDelimiter() + "twitCancel' to cancel");
    } catch (TwitBot.RequestCancelledException ex) {
      bot.sendMessage(channel, Colors.RED + "REQUEST: " + Colors.NORMAL + "Token request was cancelled due to a Twitter error");
    }
    bot.sendMessage(channel, Colors.BLUE + "REQUEST: " + Colors.NORMAL + "Please check PM for further instructions");
    log.info("TWITREQUEST from " + sender + " in " + channel);
  }

}
