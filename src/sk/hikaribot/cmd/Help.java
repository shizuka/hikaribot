/*
 * hikaribot - Help
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

/**
 * Prints helpInfo string.
 */
public class Help extends Command {

  public Help() {
    this.name = "help";
    this.numArgs = 1;
    this.helpArgs.add("command");
    this.helpInfo = "gives help on commands";
    this.reqPerm = 1; //voice and up
  }

  @Override
  public void execute(String channel, String sender, String params) {
    /* get command associated with message, return "sender: :[name] - [info]" */
    String cmdName = params.split(" ", 1)[0];
    try {
      Command command = this.cmdRegistry.getCommand(cmdName);
      String help = sender + ": " + cmdRegistry.getDelimiter() + command.name + " ";
      if (command.helpArgs.isEmpty()) {
        help += " - " + command.helpInfo;
      } else {
        for (String arg : command.helpArgs) {
          help += "<" + arg + "> ";
        }
        help += "- " + command.helpInfo;
      }
      bot.sendMessage(channel, help);
      log.info("HELP " + cmdName + " from " + sender + " in " + channel);
    } catch (sk.hikaribot.bot.CommandRegistry.CommandNotFoundException ex) {
      bot.sendMessage(channel, sender + ": Command '" + cmdName + "' was not found");
      log.error("HELP " + cmdName + " from " + sender + " in " + channel + " was not found");
    }
  }

  @Override
  public void execute(String channel, String sender) {
    execute(channel, sender, "help");
  }

}
