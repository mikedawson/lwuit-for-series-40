/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.lwuit.mediaplayer;

/**
 *
 * @author mike
 */
public class MediaPlayerFactory {
    
    private static MediaPlayerFactory instance = new MediaPlayerFactory();
    
    public static MediaPlayerFactory getInstance() {
        System.out.println("getInstance");
        return instance;
    }
    
    
    public LWUITMediaPlayer createMediaPlayer() {
        System.out.println("makeMediaPlayer");
        return new MIDPMediaPlayer();
    }
    
    
    
}
