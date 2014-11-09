/*
 * hikaribot - CommandRegistry
 * Shizuka Kamishima - 2014-11-07
 * Licensed under bsd3
 */
package sk.hikaribot.bot;

import java.util.ArrayList;
import java.util.List;
import org.jibble.pircbot.PircBot;
import sk.hikaribot.cmd.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.hikaribot.cmd.Command.ImproperArgsException;

/**
 * Maintains registry of commands, methods to get command handler.
 */
public class CommandRegistry {

  protected static final Logger log = LogManager.getLogger("Cmd");

  private final HikariBot bot;
  private final String delimiter;

  private final List<Command> commands;

  /**
   * Initializes registry. Caller should run series of add()s after
   *
   * @param bot the bot that commands will act upon
   * @param delimiter the character that denotes a command
   */
  public CommandRegistry(HikariBot bot, String delimiter) {
    this.bot = bot;
    this.delimiter = delimiter;
    this.commands = new ArrayList();
  }

  /**
   * Adds command to list of available commands.
   *
   * @param command
   */
  public void add(Command command) {
    command.setup(bot, this);
    commands.add(command);
    log.debug(command);
  }

  /**
   * Attempts to run given message name.
   *
   * @param channel
   * @param senderPerm
   * @param sender
   * @param message
   * @throws sk.hikaribot.bot.CommandRegistry.CommandNotFoundException
   * @throws sk.hikaribot.bot.CommandRegistry.InsufficientPermissionsException
   * @throws sk.hikaribot.cmd.Command.ImproperArgsException
   */
  public void execute(String channel, String sender, int senderPerm, String message) throws CommandNotFoundException, InsufficientPermissionsException, ImproperArgsException {
    String[] args = message.split(" ", 2);
    Command cmd = this.getCommand(args[0].substring(1));
    if (senderPerm < cmd.reqPerm) {
      throw new InsufficientPermissionsException(cmd.name, sender, channel, senderPerm, cmd.reqPerm);
    }
    try {
      if (args.length > 1) {
        cmd.execute(channel, sender, args[1]);
      } else {
        cmd.execute(channel, sender);
      }
    } catch (ImproperArgsException ex) {
      this.getCommand("help").execute(channel, sender, ex.getMessage());
    }
  }

  public Command getCommand(String command) throws CommandNotFoundException {
    for (Command cmdObj : commands) {
      if (cmdObj.name.equalsIgnoreCase(command)) {
        return cmdObj;
      }
    }
    throw new CommandNotFoundException(command);
  }

  public List<Command> getRegistry() {
    return commands;
  }

  public String getDelimiter() {
    return this.delimiter;
  }

  public static class CommandNotFoundException extends Exception {

    public CommandNotFoundException(String command) {
      super(command);
      log.error("Command " + command + " not found");
    }
  }

  public static class InsufficientPermissionsException extends Exception {

    public InsufficientPermissionsException(String command, String sender, String channel, int userPerm, int reqPerm) {
      super(Integer.toString(reqPerm));
      log.error(sender + " in " + channel + " has insufficient permissions to invoke " + command.toUpperCase() + ", has:" + userPerm + " needs:" + reqPerm);
    }
  }

}
