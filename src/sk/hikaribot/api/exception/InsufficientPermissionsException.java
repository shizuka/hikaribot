/*
 * hikaribot - InsufficientPermissionsException
 * Shizuka Kamishima - 2014-11-13
 * Licensed under bsd3
 */
package sk.hikaribot.api.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InsufficientPermissionsException extends Exception {

  private static final Logger log = LogManager.getLogger("Exception");

  /**
   * Indicates invoker has insufficient permissions to invoke command.
   *
   * @param command the command invoked
   * @param sender nick of the invoker
   * @param channel channel we were called from
   * @param userPerm sender's permission level
   * @param reqPerm permission level required by command
   */
  public InsufficientPermissionsException(String command, String sender, String channel, int userPerm, int reqPerm) {
    super(Integer.toString(reqPerm));
    log.error(sender + " in " + channel + " has insufficient permissions to invoke " + command.toUpperCase() + ", has:" + userPerm + " needs:" + reqPerm);
  }

}
