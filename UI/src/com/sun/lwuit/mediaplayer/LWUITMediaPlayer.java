/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.lwuit.mediaplayer;

import java.io.InputStream;

/**
 *
 * @author mike
 */
public interface LWUITMediaPlayer {
    
    public static final int UNREALIZED = 100;
    
    public static final int REALIZED = 200;
    
    public static final int PREFETCHED = 300;
    
    public static final int STARTED = 400;
    
    public static final int CLOSED = 0;
    
    public static final int INACTIVE = -100;
    
    public void realizePlayer(InputStream in, String mimeType, String id) throws Exception;
    
    public void startPlayer(String id) throws Exception;
    
    public void stopPlayer(String id) throws Exception;
    
    public void pausePlayer(String id) throws Exception;
    
    public void stopAllPlayers() throws Exception;
    
    public void addMediaPlayerListener(String id, MediaPlayerListener listener);
    
    public int getState(String id);
    
}
