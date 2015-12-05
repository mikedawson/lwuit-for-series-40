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
 * 
 * 
 * @author mike
 */
public class MIDPVideoPlaceholder extends Container implements Runnable{
    
    private VideoComponent videoComp;
    
    private Label statusLabel;
    
    private int percentToUpdateTo = -1;
    
    public MIDPVideoPlaceholder() {
        statusLabel = new Label("0%");
        setLayout(new BorderLayout());
        addComponent(BorderLayout.CENTER, statusLabel);
    }
    
    public VideoComponent getVideoComponent() {
        return videoComp;
    }

    /**
     * Get the percentage status pending to be displayed
     * 
     * @return The percentage status that will be dislpayed the next time run is called
     */
    public synchronized int getAsyncStatus() {
        return percentToUpdateTo;
    }

    /**
     * Async method to set the status: because in LWUIT we should make UI
     * changes using Dislpay.callSerially and this is used to reflect
     * the progress of an IO job it's helpful to be able to set this in an async
     * manner.  That's why this class implements the Runnable interface so a 
     * job running can call this method and then put it in the callSerially list
     * like so:
     * 
     * placeholder.setAsyncStatus(10)
     * Display.getInstance().callSerially(placeholer)
     * 
     * @param percentToUpdateTo the percentage to show the next time run is called
     */
    public synchronized void setAsyncStatus(int percentToUpdateTo) {
        this.percentToUpdateTo = percentToUpdateTo;
    }
    
    
    
    public void setVideoComponent(final VideoComponent videoComp) {
        this.videoComp = videoComp;
        
        Display.getInstance().callSerially(new Runnable() {
            public void run() {
                statusLabel.setText("playing");
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
    
    public void run() {
        int updateTo = getAsyncStatus();
        
        if(updateTo != -1 && statusLabel != null) {
            setStatus(updateTo);
            setAsyncStatus(-1);
        }
        
        
    }
    
}