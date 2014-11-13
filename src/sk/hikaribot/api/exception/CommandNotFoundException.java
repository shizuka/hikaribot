/*
 * hikaribot - CommandNotFoundException
 * Shizuka Kamishima - 2014-11-13
 * Licensed under bsd3
 */
package sk.hikaribot.api.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandNotFoundException extends Exception {

  private static final Logger log = LogManager.getLogger("Exception");

  /**
   * Indicates command was not found in the CommandRegistry.
   *
   * @param command the command name we tried to get
   */
  public CommandNotFoundException(String command) {
    super(command);
    log.error("Command " + command + " not found");
  }

}
