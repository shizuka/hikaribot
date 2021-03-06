/*
 * Hikari IRC Bot - Executable
 * Shizuka Kamishima - 2014-11-06
 * Executable
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
package sk.hikaribot;

import sk.hikaribot.api.exception.MissingRequiredPropertyException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.hikaribot.bot.HikariBot;

/**
 * Executable.
 * 
 * @author Shizuka Kamishima
 */
public class Main {

  private static final Logger log = LogManager.getLogger("Exe");
  private static final String[] reqProps = {
    "version",
    "nick",
    "pass",
    "owner",
    "chan",
    "server"
  };
  private static final String[] reqTwitProps = {
    "consumer.key",
    "consumer.secret"
  };

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    /**
     * Read in IRC configuration and connect.
     */

    Properties config = new Properties();
    Properties twitConfig = new Properties();

    try {
      /* Sanity check the config file */
      FileReader configFile = new FileReader("config.properties");
      config.load(configFile);
      log.info("HikariBot config file loaded, checking sanity...");

      for (String prop : reqProps) {
        if (config.getProperty(prop) == null) {
          throw new MissingRequiredPropertyException(prop);
        }
        log.debug("OK " + prop);
      }
      log.info("HikariBot config file is sane");

      /* Sanity check the twitbot config file */
      FileReader twitConfigFile = new FileReader("twitbot.properties");
      twitConfig.load(twitConfigFile);
      log.info("TwitBot config file loaded, checking sanity...");

      for (String prop : reqTwitProps) {
        if (twitConfig.getProperty(prop) == null) {
          throw new MissingRequiredPropertyException(prop);
        }
        log.debug("OK " + prop);
      }
      log.info("TwitBot config file is sane");

    } catch (FileNotFoundException ex) {
      log.fatal("I couldn't find the config file!");
      System.exit(1);
    } catch (IOException ex) {
      log.fatal("I couldn't read the config file!");
      System.exit(1);
    } catch (MissingRequiredPropertyException ex) {
      System.exit(1);
    } /* end config file sanity checking */

    /* Initialize and Connect bot */
    HikariBot bot = new HikariBot(config, twitConfig);
  }

}
