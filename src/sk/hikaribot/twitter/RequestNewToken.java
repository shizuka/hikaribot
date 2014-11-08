/*
 * hikaribot - RequestNewToken
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import sk.hikaribot.cmd.Command;

public class RequestNewToken extends Command {

  public RequestNewToken() {
    this.name = "twitNewReq";
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
      bot.sendMessage(sender, "Then use '" + cmdRegistry.getDelimiter() + "twitConfirmNew <PIN>' in " +
              channel + " to complete the process, where <PIN> is the seven digit code given on that page");
    } catch (TwitBot.RequestInProgressException ex) {
      bot.sendMessage(channel, sender + ": A token request is already in progress. Please complete that request with '" +
              cmdRegistry.getDelimiter() + "twitConfirmReq <PIN>' or '" + cmdRegistry.getDelimiter() + "twitCancelReq' to cancel");
    } catch (TwitBot.RequestCancelledException ex) {
      bot.sendMessage(channel, sender + ": Token request was cancelled due to a Twitter error");
    }
    bot.sendMessage(channel, sender + ": Please check PM for further instructions");
    log.info("TWITREQUESTNEW from " + sender + " in " + channel);
  }

}
