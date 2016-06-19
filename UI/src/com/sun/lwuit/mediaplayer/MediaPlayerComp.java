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

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.html.HTMLCallback;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.Border;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This component is intended to produce an all in one MediaPlayer for
 * audio or video with play/pause button and stop button (maybe even a progress
 * bar some day).
 * 
 * This goes together with LWUITMediaPlayer provided by the underlying (e.g. MIDP or 
 * J2SE implementation) that actually plays the files (using the appropriate
 * APIs on MIDP and J2SE respectively).
 * 
 * Use a MediaInputProvider as an underlying class that can return an inputstream
 * and a mimetype - an implementation of this is com.sun.lwuit.HTMLMediaInputProvider
 * which does this using an HTML component's document loader and an audio or
 * video tag element.
 * 
 * An HTMLCallback can be used with this class just so that the parseError method
 * can be called to report back on errors that get encountered (so the underlying
 * app can log them etc).  Cross platform media can be fun :)
 * 
 * @author mike
 */
public class MediaPlayerComp extends Container implements ActionListener, MediaPlayerListener, AsyncMediaInputProvider.IOCallback{
    
    /**
     * The player has not loaded the file yet at all / no resources allocated
     */
    public static final int UNREALIZED = 100000;
    
    /**
     * The player is loading the given file
     */
    public static final int LOADING = 100001;
    
    /**
     * The player is playing the file currently
     */
    public static final int PLAY = 100002;
    
    public static final Integer PLAY_OBJ = new Integer(PLAY);
    
    /**
     * The player is currently paused
     */
    public static final int PAUSE = 100003;
    
    private static final Integer PAUSE_OBJ = new Integer(PAUSE);
    
    /**
     * The player is currently stopped
     */
    public static final int STOP = 100004;
    
    //Button that shows play/pause icon
    private MediaPlayerCompButton playPauseButton;
    
    //Button for stop
    private MediaPlayerCompButton stopButton;
    
    //Current state of the player
    private int state;
    
    
    
    //The underlying platform player implementation to actually play streams
    private LWUITMediaPlayer mediaPlayer;

    //Used to generate an ID when communicating with the underlying player
    private static int autoIDCount = 1000;
        
    //InputStream from the MediaPlayerInputProvider
    private InputStream in = null;
    
    //Realizing the player is always forked into another thread to avoid unresponsive UI
    private RealizePlayerThread realizeThread;
    
    //The input provider 
    private MediaPlayerInputProvider provider;
    
    //optional can be used to send parseError to
    private HTMLCallback callback;
    
    // can be the id attribute if present from html or generated playerID
    private String playerID = null;
    
    private boolean controlsEnabled = true;
    
    private static Hashtable mimeTypes = new Hashtable();
    
    private Container controlsContainer;
    
    private Component videoContainer;
    
    private boolean isVideo;
    
    private int preferVideoW;
    
    private int preferVideoH;
    
    private int mediaSize;
    
    static {
        mimeTypes.put(".mp3", "audio/mpeg");
        mimeTypes.put(".wav", "audio/x-wav");
        mimeTypes.put(".3gp", "video/3gpp");
        mimeTypes.put(".mp4", "video/mp4");
    }
    
    private Hashtable actionAfterRealize;
    
    /**
     * Make a new MediaPlayerComponent 
     * 
     * @param mediaPlayer The underlying media player implementation to take control of playing the actual media
     * @param provider MediaPlayerInputProvider that will give us the required stream
     * @param callback Optional HTMLCallback that can be used to reporting back using the parseError method (can be null)
     * @param controlsEnabled True to enable play/pause/stop buttons false otherwise
     * @param preferVideoW Preferred width for the video component (applies only to video)
     * @param preferVideoH Preferred height for the video component (applies only to video)
     */
    public MediaPlayerComp(LWUITMediaPlayer mediaPlayer, MediaPlayerInputProvider provider, HTMLCallback callback, boolean controlsEnabled, int preferVideoW, int preferVideoH) {
        this.mediaPlayer = mediaPlayer;
        this.callback = callback;
        this.provider = provider;
        this.controlsEnabled = controlsEnabled;
        this.isVideo = provider.isVideo();
        
        if(controlsEnabled) {
            controlsContainer = new Container(new BoxLayout(BoxLayout.X_AXIS));
        }
        
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        state = UNREALIZED;
        
        playerID = "mplayer" + getNextAutoID();
        
        this.preferVideoW = preferVideoW;
        this.preferVideoH = preferVideoH;
        mediaSize = -1;
        actionAfterRealize = new Hashtable();
    }
    
    public MediaPlayerComp(LWUITMediaPlayer mediaPlayer, MediaPlayerInputProvider provider, HTMLCallback callback, boolean controlsEnabled) {
        this(mediaPlayer, provider, callback, controlsEnabled, -1, -1);
    }
    
    /**
     * Set the preferred video width and height
     * 
     * @param width Width in pixels or -1 for auto
     * @param height Height in pixels or -1 for auto
     */
    public void setPreferredVideoDimension(int width, int height) {
        this.preferVideoW = width;
        this.preferVideoH = height;
        
        if(videoContainer != null) {
            if(preferVideoW != -1)
                videoContainer.setPreferredW(preferVideoW);
            if(preferVideoH != -1)
                videoContainer.setPreferredH(preferVideoH);
        }
    }
    
    void setupButtons() {
        if(isVideo && videoContainer == null) {
            videoContainer = mediaPlayer.makeVideoPlaceholder(playerID);
            videoContainer.getStyle().setBorder(Border.createLineBorder(2));
            setPreferredVideoDimension(preferVideoW, preferVideoH);
            addComponent(videoContainer);
        }
        
        if(controlsEnabled && playPauseButton == null) {
            playPauseButton = new MediaPlayerCompButton(PLAY);
            playPauseButton.addActionListener(this);
            stopButton = new MediaPlayerCompButton(STOP);
            stopButton.addActionListener(this);

            controlsContainer.addComponent(playPauseButton);
            controlsContainer.addComponent(stopButton);
            addComponent(controlsContainer);
        }
    }
    
    public void initComponent() {
        super.initComponent();
        
        setupButtons();
    }

    /**
     * Return the buttons that are focusable (used by HTMLComponent) for 
     * firstFocusable
     * 
     * @return Array of focusable components or null if there are none
     */
    public Component[] getUIComponents() {
        setupButtons();
        if(controlsEnabled) {
            return new Component[]{playPauseButton, stopButton};
        }else {
            return null;
        }
    }
    
    /**
     * This is really just being abused to provide additional logging
     */
    void callbackParsingError(int id, String tag, String attribute, String value, String description) {
        if(callback != null) {
            callback.parsingError(id, tag, attribute, value, description);
        }
    }
    
    /**
     * Get the mime type likely for this media extension type
     * 
     * @param extension Extension with the . - e.g. .mp3
     * 
     * @return The known mime type if in the list or application/octet-stream if not known
     */
    public static String getMimeTypeByExtension(String extension) {
        Object val = mimeTypes.get(extension.toLowerCase());
        if(val != null) {
            return (String)val;
        }else {
            return "application/octet-stream";
        }
    }
    
    /**
     * Get the expected file extension according to the mime type
     * 
     * @param mimeType The mime type e.g. audio/mpeg
     * @return The expected file extension including . e.g. ".mp3" or null if not known
     */
    public static String getExtensionByMimeType(String mimeType) {
        String extension = null;
        Enumeration keys = mimeTypes.keys();
        while(keys.hasMoreElements()) {
            extension = (String)keys.nextElement();
            if(mimeTypes.get(extension).equals(mimeType)) {
                return extension;
            }
        }
        
        return null;
    }
    
    /**
     * Register a file extension so that it gets returned when getMimeTypeByExtension
     * is used
     * 
     * @param extension The full extension in lower case with . - e.g. .mp3
     * @param mimeType The mime type e.g. audio/mpeg
     */
    public static void registerMimeExtension(String extension, String mimeType) {
        mimeTypes.put(extension.toLowerCase(), mimeType);
    }
    
    /**
     * Sets whether or not controls are enabled for this player (pause/stop button)
     * 
     * @return true if controls are shown; false otherwise
     */
    public boolean isControlsEnabled() {
        return this.controlsEnabled;
    }
    
    public static int getNextAutoID() {
        int retVal = autoIDCount;
        autoIDCount++;
        
        return retVal;
    }

    public void actionPerformed(ActionEvent evt) {
        callbackParsingError(150, "MediaPlayerComp", "actionPerformed", ""+state, 
            "");
        int cmdId = evt.getCommand() != null ? evt.getCommand().getId() : -1;
        
        switch(cmdId) {
            case PLAY:
                play();
                break;
            case STOP:
                stop();
                break;
        }
    }
    
    /**
     * Start or continue playing the media
     */
    public void play() {
        callbackParsingError(150, "MediaPlayerComp", "startPlaying", ""+state, 
            "");
        if(state == UNREALIZED) {
            state = LOADING;
            actionAfterRealize.put(playerID, new Integer(PLAY));
            setPlayButtonCommand(PAUSE);
            
            if(provider instanceof AsyncMediaInputProvider) {
                ((AsyncMediaInputProvider)provider).getMediaInputStreamAsync(this);
            }else {
                realizeThread = new RealizePlayerThread();
                realizeThread.start();
            }
        }else if(state == LOADING) {
            //it's loading right now - so toggle what to do after realization finishes
            Object statusObj = actionAfterRealize.get(playerID);
            if(statusObj != null && statusObj.equals(PLAY_OBJ)) {
                actionAfterRealize.put(playerID, PAUSE_OBJ);
                setPlayButtonCommand(PLAY);
            }else {
                actionAfterRealize.put(playerID, PLAY_OBJ);
                setPlayButtonCommand(PAUSE);
            }
        }else if(state == PLAY) {
            //time to pause
            try {
                mediaPlayer.pausePlayer(playerID);
                
            }catch(Exception e) {
                callbackParsingError(103, "MediaPlayerComp", "pausePlayer", 
                        null, e.getMessage() + ": " + e.toString());
                
            }
            setPlayButtonCommand(PLAY);
            state = PAUSE;
        }else if(state == PAUSE) {
            try {
                mediaPlayer.startPlayer(playerID);
            }catch(Exception e) {
                callbackParsingError(103, "MediaPlayerComp", "resumePlayer", 
                        null, e.getMessage() + ": " + e.toString());
                
            }
            setPlayButtonCommand(PAUSE);
            state = PLAY;
        }
    }
    
    /**
     * Used to change the play button between the play and pause states
     * @param state 
     */
    void setPlayButtonCommand(int state) {
        if(controlsEnabled) {
            playPauseButton.setMediaCommand(state);
        }
    }
    
    void startPlaying() {
        try {
            callbackParsingError(200, "video", playerID,
                    "request video realizePlayer", "");
            
            callbackParsingError(200, "video", playerID,
                        "got component", "");
            
            mediaPlayer.realizePlayer(in, provider.getMimeType(), playerID, 
                provider.isVideo(), mediaSize);
            callbackParsingError(201, "video", playerID,
                        "realized player", "");
            Object whatNext = actionAfterRealize.get(playerID);
            if(whatNext == null || whatNext.equals(PLAY_OBJ)) {
                mediaPlayer.startPlayer(playerID);
                state = PLAY;
            }else if(whatNext.equals(PAUSE_OBJ)) {
                state = PAUSE;
            }
        }catch(Exception e) {
            callbackParsingError(103, "MediaPlayerComp", "startPlaying", null, 
                    e.getMessage() + ": " + e.toString());
            stop();
        }
    }
    
    
    public void mediaReady(InputStream in, String mimeType, int mediaSize) {
        this.in = in;
        this.mediaSize = mediaSize;
        callbackParsingError(101, "RealizePlayerThread", 
                "mediaReady", null, " : comp.in=" + in + 
                " comp.mediaPlayer =" + mediaPlayer);
        
        mediaPlayer.addMediaPlayerListener(playerID, this);
        startPlaying();
    }
    
    public void stop() {
        try {
            int result = mediaPlayer.stopPlayer(playerID);
            callbackParsingError(104, "MediaPlayerComp", "stopOK", ""+result, 
                null);
        }catch(Exception e) {
            callbackParsingError(104, "MediaPlayerComp", "stop", null, 
                e.getMessage() + ": " + e.toString());
        }
        
        state = UNREALIZED;
        System.gc();
        setPlayButtonCommand(PLAY);
        callbackParsingError(104, "MediaPlayerComp", "closeSetPlayButton:" + in, "", 
            null);
    }

    public void playerUpdate(LWUITMediaPlayer player, String id, String event, Object objectData) {
        callbackParsingError(105, "MediaPlayerComp", "playerUpdate", id, 
            event + ":" + objectData);
        if(id.equals(playerID)) {
            if(event.equals(MediaPlayerListener.END_OF_MEDIA)) {
                stop();
            }
        }
    }

    
    
    class RealizePlayerThread extends Thread {
        public void run() {
            MediaPlayerComp comp = MediaPlayerComp.this;
            
            try {
                comp.in = comp.provider.getMediaInputStream();
                comp.mediaSize = comp.provider.getMediaSize();
                comp.callbackParsingError(101, "RealizePlayerThread", 
                        "addPlayerListener", null, " : comp.in=" + comp.in + " comp.mediaPlayer =" + comp.mediaPlayer);
                comp.mediaPlayer.addMediaPlayerListener(comp.playerID, comp);
                comp.callbackParsingError(101, "RealizePlayerThread", 
                        "startPlaying", null, " : comp.in=" + comp.in + " playerID=" + comp.playerID);
                comp.startPlaying();
            }catch(Exception e) {
                if(comp.callback != null) {
                    comp.callbackParsingError(101, "RealizePlayerThread", 
                        "inputStream1.1", e.toString(), e.getMessage()
                        +" : comp.in=" + comp.in + " playerID=" + comp.playerID);
                }
                comp.stop();
            }
        }
    }
    
    
    
    
}
