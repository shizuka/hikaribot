/*
 * hikaribot - GetActiveProfile
 * Shizuka Kamishima - 2014-11-08
 * Licensed under bsd3
 */
package sk.hikaribot.twitter;

import org.jibble.pircbot.Colors;
import sk.hikaribot.cmd.Command;

/**
 * Prints active Twitter profile.
 */
public class GetActiveProfile extends Command {

  public GetActiveProfile() {
    this.name = "twitWhoAmI";
    this.numArgs = 0;
    this.helpInfo = "who am I currently tweeting as";
    this.reqPerm = 2; //ops
  }

  @Override
  public void execute(String channel, String sender, String params) throws ImproperArgsException {
    execute(channel, sender);
  }

  @Override
  public void execute(String channel, String sender) throws ImproperArgsException {
    TwitBot twit = bot.getTwitBot();
    String name = twit.getActiveTwitName();
    long id = twit.getActiveTwitId();
    String friendlyname;
    if (name != null) {
      friendlyname = "@" + name;
    } else {
      friendlyname = "no one";
    }
    bot.sendMessage(channel, Colors.BLUE + "WHOAMI: " + Colors.NORMAL + "Currently tweeting as " + friendlyname);
    log.info("TWITWHOAMI requested by " + sender + " in " + channel);
  }

}
