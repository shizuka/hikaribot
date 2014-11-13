/*
 * hikaribot - TokenMismatchException
 * Shizuka Kamishima - 2014-11-13
 * Licensed under bsd3
 */
package sk.hikaribot.api.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenMismatchException extends Exception {

  private static final Logger log = LogManager.getLogger("Exception");

  /**
   * Indicates the AccessToken loaded from a @profile.properties didn't match
   * the name in the .properties file. Sanity check against messing with profile
   * files.
   *
   * @param expected the @profile.properties name we tried to load
   * @param got the profile name stored in the @profile.properties
   */
  public TokenMismatchException(String expected, String got) {
    super(expected);
    log.error("Token mismatch! Accessed " + expected + " but was reported as " + got);
  }

}
