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
        System.out.println("WTF: Default mediaplayerfactory loads nothing");
        throw new RuntimeException("This class must be overriden in the LWUIT implementation");
    }
    
    public LWUITMediaPlayer createMediaPlayer() {
        return null;
    }
    
    
    
}
