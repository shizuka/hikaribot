/*
 * hikaribot - CommandRegistry
 * Shizuka Kamishima - 2014-11-07
 * 
 * Copyright (c) 2014, Shizuka Kamishima
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package sk.hikaribot.bot;

import sk.hikaribot.api.exception.CommandNotFoundException;
import sk.hikaribot.api.exception.InsufficientPermissionsException;
import java.util.ArrayList;
import java.util.List;
import sk.hikaribot.api.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.hikaribot.api.exception.ImproperArgsException;

/**
 * Maintains registry of commands, methods to get command handler.
 *
 * @author Shizuka Kamishima
 */
public class CommandRegistry {

  protected static final Logger log = LogManager.getLogger("Cmd");

  private final HikariBot bot;
  private final String delimiter;

  private final List<Command> commands;

  /**
   * Initializes registry. Caller should run series of add()s after
   *
   * @param bot HikariBot
   * @param delimiter the character that denotes a command
   */
  public CommandRegistry(HikariBot bot, String delimiter) {
    this.bot = bot;
    this.delimiter = delimiter;
    this.commands = new ArrayList();
  }

  /**
   * Adds command to list of available commands. Initializes command with
   * reference to HikariBot.
   *
   * @param command Command to add to registry
   */
  public void add(Command command) {
    command.setup(bot);
    commands.add(command);
    log.debug(command);
  }

  /**
   * Attempts to run a command in a message.
   *
   * @param channel source of invocation
   * @param senderPerm invoker's permissions level
   * @param sender invoker's nick
   * @param message contents of PRIVMSG including command and delimiter
   * @throws CommandNotFoundException if command was not found in the registry
   * @throws InsufficientPermissionsException if sender has insufficient
   * permission to invoke
   * @throws ImproperArgsException if command was not called with appropriate
   * args, calls HELP
   */
  public void execute(String channel, String sender, int senderPerm, String message) throws CommandNotFoundException, InsufficientPermissionsException, ImproperArgsException {
    String[] args = message.split(" ", 2);
    Command cmd = this.getCommand(args[0].substring(1));
    if (senderPerm < cmd.reqPerm) {
      throw new InsufficientPermissionsException(cmd.name, sender, channel, senderPerm, cmd.reqPerm);
    }
    try {
      if (args.length > 1) {
        cmd.execute(channel, sender, args[1].trim());
      } else {
        cmd.execute(channel, sender);
      }
    } catch (ImproperArgsException ex) {
      this.getCommand("help").execute(channel, sender, ex.getMessage());
    }
  }

  /**
   * Fetch a Command from the registry.
   *
   * @param command the name of the command
   * @return the Command object
   * @throws CommandNotFoundException if command doesn't exist
   */
  public Command getCommand(String command) throws CommandNotFoundException {
    for (Command cmdObj : commands) {
      if (cmdObj.name.equalsIgnoreCase(command)) {
        return cmdObj;
      }
    }
    throw new CommandNotFoundException(command);
  }

  /**
   * @return the command registry, for HELP to iterate through
   */
  public List<Command> getRegistry() {
    return commands;
  }

  /**
   * @return the command prefix delimiter
   */
  public String getDelimiter() {
    return this.delimiter;
  }

}
