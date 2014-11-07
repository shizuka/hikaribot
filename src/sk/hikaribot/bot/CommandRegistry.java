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

/**
 * Maintains registry of commands, methods to get command handler.
 */
public class CommandRegistry {
  
  protected static final Logger log = LogManager.getLogger("Cmd");

  private final PircBot bot;

  private final List<Command> commands;

  /**
   * Initializes registry. Caller should run series of add()s after
   *
   * @param bot the bot that commands will act upon
   */
  public CommandRegistry(PircBot bot) {
    this.bot = bot;
    this.commands = new ArrayList();
  }

  /**
   * Adds command to list of available commands.
   *
   * @param command
   */
  public void add(Command command) {
    command.setBot(bot);
    commands.add(command);
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
   */
  public void execute(String channel, String sender, int senderPerm, String message) throws CommandNotFoundException, InsufficientPermissionsException {
    Command cmd = this.getCommand(message);
    if (cmd == null) {
      throw new CommandNotFoundException(message);
    }
    if (senderPerm < cmd.reqPerm) {
      throw new InsufficientPermissionsException(message);
    }
    cmd.execute(channel, sender, message);
  }

  private Command getCommand(String message) {
    String command = message.split(" ")[0]; //get first word, the command, remainder of message is parameter(s)
    command = command.substring(1); //strip the delimiter
    for (Command cmdObj : commands) {
      if (cmdObj.name.equals(command)) {
        return cmdObj;
      }
    }
    return null;
  }

  public static class CommandNotFoundException extends Exception {

    public CommandNotFoundException(String command) {
      super(command);
    }
  }

  public static class InsufficientPermissionsException extends Exception {

    public InsufficientPermissionsException(String command) {
      super(command);
    }
  }

}
