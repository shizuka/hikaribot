/*
 * hikaribot - Help
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
package sk.hikaribot.cmd;

import sk.hikaribot.api.Command;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jibble.pircbot.Colors;
import sk.hikaribot.bot.CommandRegistry;

/**
 * Prints helpInfo string.
 *
 * @author Shizuka Kamishima
 */
public class Help extends Command {

  public Help() {
    this.name = "help";
    this.numArgs = 1;
    this.helpArgs.add("command");
    this.helpInfo = "prints command help, omit command for list of all commands";
    this.reqPerm = 0;
  }

  @Override
  public void execute(String channel, String sender, String params) {
    CommandRegistry cr = bot.getCommandRegistry();
    /* get command associated with message, return "sender: :[name] - [info]" */
    String cmdName = params.split(" ", 1)[0];
    try {
      Command command = cr.getCommand(cmdName);
      String help = Colors.BLUE + "HELP: " + bot.getDelimiter() + command.name + " ";
      if (!command.helpArgs.isEmpty()) {
        for (String arg : command.helpArgs) {
          help += "<" + arg + "> ";
        }
      }
      help += Colors.NORMAL + "- " + command.helpInfo + " - " + Colors.BLUE + "P: " + Colors.OLIVE + command.reqPerm;
      bot.sendMessage(channel, help);
      log.info("HELP " + cmdName + " from " + sender + " in " + channel);
    } catch (sk.hikaribot.api.exception.CommandNotFoundException ex) {
      bot.sendMessage(channel, Colors.RED + "HELP: " + Colors.NORMAL + "Command '" + cmdName + "' was not found");
      log.error("HELP " + cmdName + " from " + sender + " in " + channel + " FAILED command not found");
    }
  }

  @Override
  public void execute(String channel, String sender) {
    CommandRegistry cr = bot.getCommandRegistry();
    /* list commands */
    List<String> commands = new ArrayList();
    List<Command> registry = cr.getRegistry();
    /* get all command names and sort */
    for (Command command : registry) {
      if (command.reqPerm <= bot.getPermissionsManager().getUserLevel(sender)) {
        commands.add(command.name);
      }
    }
    Collections.sort(commands);
    /* build output string */
    String out = Colors.BLUE + "COMMANDS: " + Colors.NORMAL;
    for (String cmdName : commands) {
      out += cmdName + ", ";
    }
    /* print */
    bot.sendMessage(channel, out);
    log.info("HELP-LIST from " + sender + " in " + channel);
  }

}
