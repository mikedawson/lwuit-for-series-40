/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.lwuit.mediaplayer;

import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Label;
import com.sun.lwuit.VideoComponent;
import com.sun.lwuit.layouts.BorderLayout;
import javax.microedition.media.Player;

/**
 *
 * @author mike
 */
public class MIDPVideoPlaceholder extends Container{
    
    private VideoComponent videoComp;
    
    private Label statusLabel;
    
    public MIDPVideoPlaceholder() {
        statusLabel = new Label("0%");
        setLayout(new BorderLayout());
        addComponent(BorderLayout.CENTER, statusLabel);
    }
    
    public VideoComponent getVideoComponent() {
        return videoComp;
    }
    
    public void setVideoComponent(final VideoComponent videoComp) {
        this.videoComp = videoComp;
        Display.getInstance().callSerially(new Runnable() {
            public void run() {
                removeComponent(statusLabel);
                addComponent(BorderLayout.CENTER, videoComp);
                revalidate();
            }
        });
        
        
    }
    
    public void setStatus(int loadedPercent) {
        statusLabel.setText(loadedPercent + "%");
    }
    
    public Player getPlayer() {
        if(videoComp != null) {
            return (Player)videoComp.getClientProperty("Player");
        }else {
            return null;
        }
    }
    
    
}