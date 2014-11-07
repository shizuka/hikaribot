/*
 * hikaribot - Help
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

/**
 * Prints help string. From anyone in channel, :help [command]
 */
public class Help extends Command {

  public Help() {
    this.name = "help";
    this.arg = "command";
    this.info = "gives help on commands";
    this.reqPerm = 0;
  }

  @Override
  public void execute(String channel, String sender, String message) {
    /* get command associated with message, return "sender: :[name] - [info]" */
    log.debug("HELP not yet implemented");
    bot.sendMessage(channel, sender + ": help isn't ready yet");
  }

}
