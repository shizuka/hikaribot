/*
 * hikaribot - TwitListener
 * Shizuka Kamishima - 2014-11-17
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
package sk.hikaribot.twitter;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jibble.pircbot.Colors;
import sk.hikaribot.api.exception.*;
import sk.hikaribot.bot.HikariBot;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;

/**
 * Listens to tweets and passes them back to TwitBot for processing.
 *
 * @author Shizuka Kamishima
 */
public class TwitListener implements StatusListener {

  private static final Logger log = LogManager.getLogger("TwitListener");

  private final HikariBot bot;
  private final TwitBot twitBot;
  private final String channel;

  private boolean echoing = false;
  private final List<Long> userIdsToFollow = new ArrayList();
  private final List<String> usersToFollow = new ArrayList();
  private final List<String> keywordsToTrack = new ArrayList();

  public TwitListener(HikariBot bot, String channel) {
    log.debug("TwitListener started...");
    this.bot = bot;
    this.twitBot = bot.getTwitBot();
    this.channel = channel;
  }

  @Override
  public void onStatus(Status status) {
    if (echoing) {
      String link = "https://twitter.com/" + status.getUser().getId() + "/status/" + status.getId();
      String text = Colors.BLUE + "TWEET @" + status.getUser().getScreenName() + ": " + Colors.NORMAL + status.getText();
      bot.sendMessage(channel, text);
    }
    log.debug("TWEET @" + status.getUser().getScreenName() + ": " + status.getText());
  }

  @Override
  public void onDeletionNotice(StatusDeletionNotice sdn) {
    //we can't delete lines from IRC
  }

  @Override
  public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
    //not sure what to do with this
  }

  @Override
  public void onScrubGeo(long userId, long upToStatusId) {
    //we don't care about location data
  }

  @Override
  public void onStallWarning(StallWarning sw) {
    log.debug(sw.getMessage());
  }

  @Override
  public void onException(Exception excptn) {
    log.error(excptn.getMessage());
  }

  /**
   * @return are we currently echoing tweets to channel?
   */
  public boolean isEchoing() {
    return echoing;
  }

  /**
   * @param echoing turn channel echoing on or off
   */
  public void setEchoing(boolean echoing) {
    this.echoing = echoing;
  }

  /**
   * Follows a user ID, firing onStatus for their tweets.
   *
   * @param user id to follow
   * @throws InvalidFollowException if we're already following that user
   */
  public void followUser(String user) throws InvalidFollowException {
    long userId = Long.parseLong(user);
    if (this.userIdsToFollow.contains(userId)) {
      log.error("Already following " + userId);
      throw new InvalidFollowException(userId);
    }
    this.userIdsToFollow.add(userId);
    log.debug("Following '" + userId + "' in " + channel);
    updateFilter();
  }

  /**
   * Removes a username from following list. Automatically converts to long
   * userId, pass screen-names not ids.
   *
   * @param user screenname to unfollow
   * @throws InvalidFollowException if we aren't following that user
   */
  public void unfollowUser(String user) throws InvalidFollowException {
    long userId = Long.parseLong(user);
    if (!this.userIdsToFollow.contains(userId)) {
      throw new InvalidFollowException(userId);
    }
    this.userIdsToFollow.remove(userId);
    log.debug("Unfollowed '" + userId + "' in " + channel);
    updateFilter();
  }

  /**
   * Removes all users from following.
   */
  public void unfollowAll() {
    this.userIdsToFollow.clear();
    this.usersToFollow.clear();
    log.debug("Unfollowed all from " + channel);
    updateFilter();
  }

  /**
   * Tracks a new keyword, such as a #hashtag.
   *
   * @param keyword keyword to track
   * @throws InvalidFollowException if we're already tracking that keyword
   */
  public void trackKeyword(String keyword) throws InvalidFollowException {
    if (this.keywordsToTrack.contains(keyword)) {
      throw new InvalidFollowException(keyword);
    }
    this.keywordsToTrack.add(keyword);
    log.debug("Tracking '" + keyword + "' in " + channel);
    updateFilter();
  }

  /**
   * Untrack a specific keyword.
   *
   * @param keyword keyword to untrack
   * @throws InvalidFollowException if we aren't tracking that keyword
   */
  public void untrackKeyword(String keyword) throws InvalidFollowException {
    if (!this.keywordsToTrack.contains(keyword)) {
      throw new InvalidFollowException(keyword);
    }
    this.keywordsToTrack.remove(keyword);
    log.debug("Untracking '" + keyword + "' in " + channel);
    updateFilter();
  }

  /**
   * Remove all keywords from tracking.
   */
  public void untrackAll() {
    this.keywordsToTrack.clear();
    log.debug("Untracked all from " + channel);
    updateFilter();
  }

  private void updateFilter() {
    long[] users = toLongArray(this.userIdsToFollow);
    String[] keywords = this.keywordsToTrack.toArray(new String[this.keywordsToTrack.size()]);
    twitBot.onFilterChange(users, keywords);
  }

  private long[] toLongArray(List<Long> list) {
    long[] ret = new long[list.size()];
    int i = 0;
    for (Long l : list) {
      ret[i++] = l;
    }
    return ret;
  }

  public long[] getUserIdsFollowing() {
    return toLongArray(this.userIdsToFollow);
  }

  public String[] getKeywordsTracking() {
    return this.keywordsToTrack.toArray(new String[this.keywordsToTrack.size()]);
  }

}
