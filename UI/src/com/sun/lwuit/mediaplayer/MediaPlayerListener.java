/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.lwuit.mediaplayer;

/**
 *
 * Cross platform wrapper class for events from the media player
 * 
 * @author mike
 */
public interface MediaPlayerListener {
    
    /**
     * Event constant indicating that the player has reached the end of the media
     */
    public static final String END_OF_MEDIA = "endOfMedia";
    
    /**
     * 
     */
    public static final String STOPPED = "stopped";
    
    
    public void playerUpdate(LWUITMediaPlayer player, String id, String event, Object objectData);
    
}
