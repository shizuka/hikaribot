/*
 * hikaribot - Help
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jibble.pircbot.Colors;

/**
 * Prints helpInfo string.
 */
public class Help extends Command {

  public Help() {
    this.name = "help";
    this.numArgs = 1;
    this.helpArgs.add("command");
    this.helpInfo = "prints command help, omit command for list of all commands";
    this.reqPerm = 1; //voice and up
  }

  @Override
  public void execute(String channel, String sender, String params) {
    /* get command associated with message, return "sender: :[name] - [info]" */
    String cmdName = params.split(" ", 1)[0];
    try {
      Command command = this.cmdRegistry.getCommand(cmdName);
      String help = Colors.BLUE + "HELP: " + cmdRegistry.getDelimiter() + command.name + " ";
      if (!command.helpArgs.isEmpty()) {
        for (String arg : command.helpArgs) {
          help += "<" + arg + "> ";
        }
      }
      help += Colors.NORMAL + "- " + command.helpInfo + " - " + Colors.BLUE + "P: " + Colors.OLIVE + command.reqPerm;
      bot.sendMessage(channel, help);
      log.info("HELP " + cmdName + " from " + sender + " in " + channel);
    } catch (sk.hikaribot.bot.CommandRegistry.CommandNotFoundException ex) {
      bot.sendMessage(channel, Colors.RED + "HELP: " + Colors.NORMAL + "Command '" + cmdName + "' was not found");
      log.error("HELP " + cmdName + " from " + sender + " in " + channel + " FAILED command not found");
    }
  }

  @Override
  public void execute(String channel, String sender) {
    /* list commands */
    List<String> commands = new ArrayList();
    List<Command> registry = this.cmdRegistry.getRegistry();
    /* get all command names and sort */
    for (Command command : registry) {
      commands.add(command.name);
    }
    Collections.sort(commands);
    /* build output string */
    String out = Colors.BLUE + "COMMANDS: " + Colors.NORMAL;
    for (String name : commands) {
      out += name + ", ";
    }
    /* print */
    bot.sendMessage(channel, out);
    log.info("HELP-LIST from " + sender + " in " + channel);
  }

}
