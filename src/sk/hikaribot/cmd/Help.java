/*
 * hikaribot - Help
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import sk.hikaribot.bot.CommandRegistry;

/**
 * Prints help string. From anyone in channel, :help [command]
 */
public class Help extends Command {

  public Help() {
    this.name = "help";
    this.args.add("command");
    this.help = "gives help on commands";
    this.reqPerm = 0;
  }

  @Override
  public void execute(String channel, String sender, String params) {
    /* get command associated with message, return "sender: :[name] - [info]" */
    String cmdName = params.split(" ", 1)[0];
    try {
      Command command = this.cmdRegistry.getCommand(cmdName);
      String help = sender + ": " + cmdRegistry.getDelimiter() + command.name + " ";
      if (command.args.isEmpty()) {
        help += " - " + command.help;
      } else {
        for (String arg : command.args) {
          help += "<" + arg + "> ";
        }
        help += "- " + command.help;
      }
      bot.sendMessage(channel, help);
      log.info("HELP " + cmdName + " from " + sender + " in " + channel);
    } catch (CommandRegistry.CommandNotFoundException ex) {
      bot.sendMessage(channel, sender + ": Command '" + cmdName + "' was not found");
      log.error("HELP " + cmdName + " from " + sender + " in " + channel + " was not found");
    }
  }

  @Override
  public void execute(String channel, String sender) {
    execute(channel, sender, "help");
  }

}
