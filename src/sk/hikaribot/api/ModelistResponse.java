/*
 * hikaribot - ModelistResponse
 * Shizuka Kamishima - 2015-04-10
 * Observable
 * 
 * Copyright (c) 2015, Shizuka Kamishima
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
package sk.hikaribot.api;

import java.util.Observable;
import java.util.Observer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Is passed to HikariBot when Banhammer wants a list of modes, populates with
 * active modes, then calls Observer.
 * 
 * @author Shizuka Kamishima
 */
public class ModelistResponse extends Observable implements Observer {
  
  private static final Logger log = LogManager.getLogger("MODE");
  
  private final String channel;
  private final String mode;
  
  //this should probably be its own sk.hikaribot.api class if i go full java
  private enum Mode {
    I(346), //+I invite exemption
    ENDI(347),
    E(348), //+e ban exemption
    ENDE(349),
    B(367), //+b ban
    ENDB(368),
    Q(728), //+q quiet ban
    ENDQ(729);
    private final int val;
    private Mode(int val) {
      this.val = val;
    }
  }
  
  /**
   * Prepares ModelistResponse for MODE [channel] +[mode] responses.
   * 
   * @param channel the channel to gather from
   * @param mode the mode to check
   */
  public ModelistResponse(String channel, String mode) {
    this.channel = channel;
    this.mode = mode;
  }
  
  @Override
  public void update(Observable o, Object arg) {
    //368:Asuka #cathedral [q] *!*@foo.bar.baz Shizuka!hikari@shizuka.kamishima 1428726302
    //q is all special at Ponychat, beI don't include their mode here
    String line = arg.toString();
    String[] serverResponse = line.split(":", 2);
    int code = Integer.parseInt(serverResponse[0]);
    String response = serverResponse[1];
    this.onServerResponse(code, response);
  }
  
  private void onServerResponse(int code, String response) {
    log.trace(code + ":" + response);
  }

}
