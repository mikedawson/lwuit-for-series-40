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
public class DefaultLWUITMediaPlayerManager implements LWUITMediaPlayerManager{

    private static DefaultLWUITMediaPlayerManager instance;
    
    private LWUITMediaPlayer player;
    
    public static DefaultLWUITMediaPlayerManager getInstance() {
        if(instance == null) {
             instance = new DefaultLWUITMediaPlayerManager();
        }
        
        return instance;
    }
    
    public LWUITMediaPlayer getPlayer() {
        if(player == null) {
            player = MediaPlayerFactory.getInstance().createMediaPlayer();
        }
        
        System.out.println("Player = " + player);
        
        return player;
    }

}
