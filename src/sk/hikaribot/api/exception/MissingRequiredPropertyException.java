/*
 * hikaribot - MissingRequiredPropertyException
 * Shizuka Kamishima - 2014-11-13
 * Licensed under bsd3
 */
package sk.hikaribot.api.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MissingRequiredPropertyException extends Exception {

  private static final Logger log = LogManager.getLogger("Exception");

  /**
   * Indicates imported properties file was missing a required entry.
   *
   * @param prop required property missing from config file
   */
  public MissingRequiredPropertyException(String prop) {
    super(prop);
    log.fatal("Config file was missing property: " + prop + "");
  }

}
