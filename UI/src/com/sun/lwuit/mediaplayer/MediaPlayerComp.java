/*
 * 
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
import java.io.IOException;
import java.io.InputStream;

/**
 * This component is intended to produce an all in one MediaPlayer for
 * audio or video with play/pause button and stop button (maybe even a progress
 * bar).
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
public class MediaPlayerComp extends Container implements ActionListener, MediaPlayerListener{
    
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
    
    /**
     * The player is currently paused
     */
    public static final int PAUSE = 100003;
    
    /**
     * The player is currently stopped
     */
    public static final int STOP = 100004;
    
    //Button that shows play/pause icon
    private MediaButton playPauseButton;
    
    //Button for stop
    private MediaButton stopButton;
    
    //Current state of the player
    private int state;
    
    //The default width of the icon to go within the button
    public static final int PREFER_ICON_WIDTH = 18;
    
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
    
    /**
     * Make a new MediaPlayerComponent 
     * 
     * @param mediaPlayer The underlying media player implementation to take control of playing the actual media
     * @param provider MediaPlayerInputProvider that will give us the required stream
     * @param callback Optional HTMLCallback that can be used to reporting back using the parseError method (can be null)
     */
    public MediaPlayerComp(LWUITMediaPlayer mediaPlayer, MediaPlayerInputProvider provider, HTMLCallback callback) {
        this.mediaPlayer = mediaPlayer;
        this.callback = callback;
        this.provider = provider;
        
        setLayout(new BoxLayout(BoxLayout.X_AXIS));
        
        playPauseButton = new MediaButton(PLAY);
        playPauseButton.addActionListener(this);
        stopButton = new MediaButton(STOP);
        stopButton.addActionListener(this);
        
        addComponent(playPauseButton);
        addComponent(stopButton);
        
        state = UNREALIZED;
        
        playerID = "mplayer" + getNextAutoID();
    }
    
    public static int getNextAutoID() {
        int retVal = autoIDCount;
        autoIDCount++;
        
        return retVal;
    }

    public void actionPerformed(ActionEvent evt) {
        int cmdId = evt.getCommand() != null ? evt.getCommand().getId() : -1;
        
        switch(cmdId) {
            case PLAY:
                play();
                break;
        }
    }
    
    public void play() {
        if(state == UNREALIZED) {
            realizeThread = new RealizePlayerThread();
            state = LOADING;
            realizeThread.start();
        }
    }
    
    void startPlaying() {
        try {
            mediaPlayer.realizePlayer(in, provider.getMimeType(), playerID);
            mediaPlayer.startPlayer(playerID);
            state = PLAY;
        }catch(Exception e) {
            if(callback != null) {
                callback.parsingError(103, "MediaPlayerComp", "startPlaying", null, 
                    e.getMessage() + ": " + e.toString());
            }
            stop();
        }
    }
    
    public void pause() {
        
    }
    
    public void stop() {
        try {
            mediaPlayer.stopPlayer(playerID);
        }catch(Exception e) {
            if(callback != null) {
                callback.parsingError(104, "MediaPlayerComp", "stop", null, 
                e.getMessage() + ": " + e.toString());
            }
        }
        
        if(in != null) {
            try {
                in.close();
                in = null;
            }catch(IOException e) {
                callback.parsingError(105, "MediaPlayerComp", "stop-close", null, 
                    e.getMessage() + ": " + e.toString());
            }
        }
    }

    public void playerUpdate(LWUITMediaPlayer player, String id, String event, Object objectData) {
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
                comp.mediaPlayer.addMediaPlayerListener(comp.playerID, comp);
                comp.startPlaying();
            }catch(Exception e) {
                if(comp.callback != null) {
                    comp.callback.parsingError(101, "RealizePlayerThread", 
                        "inputStream", null, null);
                }
                comp.stop();
            }
        }
    }
    
    
    public static class MediaButton extends Button {
        
        private int mediaCommand;
        
        public MediaButton(int mediaCommand) {
            super(new Command(" ", mediaCommand));
            this.mediaCommand = mediaCommand;
        }

        public int getPreferredW() {
            return PREFER_ICON_WIDTH + getStyle().getPadding(Component.LEFT) 
                + getStyle().getPadding(Component.RIGHT);
        }

        
        
        public void paint(Graphics g) {
            super.paint(g); 
            
            //now put the correct symbol over the top
            int w = getWidth();
            int h = getHeight();
            int x = getX();
            int y = getY();
            int padL = getStyle().getPadding(Component.LEFT);
            int padR = getStyle().getPadding(Component.RIGHT);
            int padT = getStyle().getPadding(Component.TOP);
            int padB = getStyle().getPadding(Component.BOTTOM);
                        
            g.setColor(0);
            g.setAlpha(128);
            switch(mediaCommand) {
                case MediaPlayerComp.PLAY:
                    //Draw the play triangle
                    g.fillPolygon(
                        new int[] {x + padL, (x + w) - padR, x+ padL},
                        new int[] {y + padT, y + (h / 2), (y + h) - padB},
                        3);
                    break;
                case MediaPlayerComp.STOP:
                    //Draw the stop square
                    g.fillRect(x + padL, y+padT, w - padL - padR, h- padT - padB);
                    break;
            }
            
            
            
        }
        
        
        
    }
    
}
