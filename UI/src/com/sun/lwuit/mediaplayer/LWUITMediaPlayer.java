/*
 * Copyright (c) 2015 UstadMobile, Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  UstadMobile designates this
 * particular file as subject to the "Classpath" exception as provided
 * by UstadMobile in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores
 * CA 94065 USA or visit www.oracle.com if you need additional information or
 * have any questions.
 */

package com.sun.lwuit.mediaplayer;

import com.sun.lwuit.Component;
import com.sun.lwuit.html.HTMLCallback;
import java.io.InputStream;

/**
 * This interface represents a class that takes responsibility for playing a
 * media file.  On J2ME this is done using JSR-135 Player and java media extensions
 * on the J2SE platform.
 * 
 * The class is given an id for each item to play.  Certain implementations may
 * limit the number of items that can play simultaneously.
 * 
 * The flow of use would be:
 * 
 * 1. realizePlayer - Given inputstream, the mimetype and a unique id
 * 2. (optional) addMediaPlayerListener to listen for events
 * 3. startPlayer to commence playback
 * 
 * @author Mike Dawson <mike@ustadmobile.com>
 */
public interface LWUITMediaPlayer {
    
    /**
     * The player has not loaded anything and does not have any resources locked 
     * at all.  Value is the same as JSR-135 Player
     */
    public static final int UNREALIZED = 100;
    
    /**
     * Player has resources allocated.  Value as per JSR-135 Player.
     */
    public static final int REALIZED = 200;
    
    /**
     * Player has loaded content and is ready to go. Value as per JSR-135 Player.
     */
    public static final int PREFETCHED = 300;
    
    /**
     * Player has started playing Value as per JSR-135 Player.
     */
    public static final int STARTED = 400;
    
    /**
     * Player is closed. Value as per JSR-135 Player.
     */
    public static final int CLOSED = 0;
    
    /**
     * The id mentioned is not active here
     */
    public static final int INACTIVE = -100;
    
    /**
     * Status returned when the stop method stops an active player
     */
    public static final int CLOSED_DEALLOCATED_OK = 0;
    
    /**
     * Status returned when the given id has already been paused
     */
    public static final int CLOSED_ALEADY = 1;
    
    /**
     * Status returned when the given player has already been stopped and
     * deallocated
     */
    public static final int NOTHING_TO_CLOSE = 1;
    
    /**
     * Status returned when the player was buffering and that has been cancelledd
     */
    public static final int BUFFERING_CANCELLED = 2;
    
    /**
     * Realize a player for the given inputstream and mimetype.  It can be referred
     * to by the given id which must be unique
     * 
     * @param in InputStream that provides media to play
     * @param mimeType Mime type of the content to play e.g. audio/mpeg
     * @param id An id that will be used to refer to it for future operations, events, etc.
     * @param mediaSize The length of the media to play or -1 if length is unknown
     * 
     * @throws Exception If something goes wrong in the underlying implementation
     */
    public Object realizePlayer(InputStream in, String mimeType, String id, boolean hasVideo, int mediaSize) throws Exception;
    
    /**
     * Start the media playing
     * 
     * @param id as supplied to realizePlayer
     * @throws Exception if something goes wrong in the underlying implementation
     */
    public void startPlayer(String id) throws Exception;
    
    /**
     * Stops the input stream playing and releases resources (including removing
     * event listeners).  Will close the inputstream provided to realizePlayer
     * 
     * @param id as supplied to realizePlayer
     * @return status of request to stop
     * @throws Exception  If something goes wrong in the underlying implementation
     */
    public int stopPlayer(String id) throws Exception;
    
    /**
     * Used temporarily stop playback with the option to continue by calling
     * startPlayer again (e.g. pauses)
     * 
     * @param id as supplied to realizePlayer
     * @throws Exception  If something goes wrong in the underlying implementation
     */
    public void pausePlayer(String id) throws Exception;
    
    /**
     * Stop all currently active players
     * 
     * @param clearTempFiles True to make the player clear out all temp files as well
     * @return Description of errors encountered as a string.  A blank String "" means everything is OK
     */
    public String stopAllPlayers(boolean clearTempFiles);
    
    /**
     * Listener for player events (follows the JSR-135 scheme)
     * 
     * @param id of player to listen to
     * @param listener Listener to receive events
     */
    public void addMediaPlayerListener(String id, MediaPlayerListener listener);
    
    /**
     * Gets the current state of the given player
     * 
     * @param id as supplied to realizePlayer
     * @return  State of the given player as per the status constants
     */
    public int getState(String id);
    
    public Component makeVideoPlaceholder(String id);
    
    /**
     * Remove any temporary files that may have been created and are not being
     * used with players (this should only be run once all players have
     * stopped)
     * 
     * @return Number of temp files removed
     */
    public int clearTempFiles();
    
    /**
     * When a media file is available in a variety of different sources this
     * method should be used to determine which format the player would
     * prefer for this platform.
     * 
     * @param availableFormats: An array of the formats in which this media is available: e.g. video/3gpp, video/mp4, etc.
     * @param mediaType: HTMLCallback.MEDIA_VIDEO or HTMLCallback.MEDIA_AUDIO
     * 
     * @return The index from the array of the preferred format to use; or -1 if no suitable type is found in the array
     */
    public int getPreferredFormat(String[] availableFormats, int mediaType);
    
    
}
