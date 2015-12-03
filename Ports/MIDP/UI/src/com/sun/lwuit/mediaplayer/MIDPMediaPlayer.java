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

import com.sun.lwuit.html.HTMLCallback;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import com.sun.lwuit.Component;
import com.sun.lwuit.VideoComponent;

/**
 *
 * @author mike
 */
public class MIDPMediaPlayer implements LWUITMediaPlayer, PlayerListener{

    private Hashtable players;
    
    private Hashtable listeners;
    
    private Hashtable videoComps;
    
    private HTMLCallback callback;
    
    /**
     * The maximum size of a video file to attempt to play directly through buffering
     */
    public static final int MAXBUFFER = 300000;
    
    public MIDPMediaPlayer() {
        players = new Hashtable();
        listeners = new Hashtable();
    }
    
    public void setCallback(HTMLCallback callback) {
        this.callback = callback;
    }
    
    void callbackParsingError(int id, String tag, String attribute, String value, String description) {
        if(callback != null) {
            callback.parsingError(id, tag, attribute, value, description);
        }
    }
    
    public Object realizePlayer(InputStream in, String mimeType, String id, boolean isVideo) throws MediaException, IOException {
        Player newPlayer = null;
        if(isVideo) {
            MIDPVideoPlaceholder placeholder = (MIDPVideoPlaceholder)videoComps.get(id);
            
            if(in.available() > MAXBUFFER || in.available() == -1) {
                //we need to copy all this into a temporary file
                
            }else {
                //we can play it directly...
                
            }
            
            placeholder.setVideoComponent(VideoComponent.createVideoPeer(in, mimeType));
            newPlayer = placeholder.getPlayer();
        }else {
            newPlayer = Manager.createPlayer(in, mimeType);
        }
        
        
        players.put(id, newPlayer);
        newPlayer.addPlayerListener(this);
        
        return null;
    }
    
    Player getPlayerByID(String id) {
        Object playerObj = players.get(id);
        return playerObj != null ? (Player)playerObj : null;
    }
    
    String getIDByPlayer(Player player) {
        Enumeration ids = players.keys();
        Object key;
        Object playerObj;
        while(ids.hasMoreElements()) {
            key = ids.nextElement();
            playerObj = players.get(key);
            if(playerObj == player) {
                return (String)key;
            }
        }
        
        return null;
    }
    
    public Component makeVideoPlaceholder(String id) {
        if(videoComps == null) {
            videoComps = new Hashtable();
        }
        
        Component comp = new MIDPVideoPlaceholder();
        videoComps.put(id, comp);
        return comp;
    }
    
    public void startPlayer(String id) throws MediaException {
        if(videoComps != null && videoComps.containsKey(id)) {
            ((MIDPVideoPlaceholder)videoComps.get(id)).getVideoComponent().start();
        }else {
            getPlayerByID(id).start();
        }
    }

    public synchronized int stopPlayer(String id) throws MediaException {
        int retVal = -1;
        Player player = getPlayerByID(id);
        MediaException me = null;
        if(player != null) {
            int state = player.getState();
            if(state != Player.CLOSED) {
                try {
                    player.stop();
                    player.deallocate();
                    retVal = LWUITMediaPlayer.CLOSED_DEALLOCATED_OK;
                }catch(MediaException e) {
                    me = e;
                }
            }else {
                retVal = LWUITMediaPlayer.CLOSED_ALEADY;
            }
            player.close();
            players.remove(id);
            listeners.remove(id);
        }else {
            retVal = LWUITMediaPlayer.NOTHING_TO_CLOSE;
        }
        
        if(me != null) {
            throw me;
        }
        
        return retVal;
    }

    public void pausePlayer(String id) throws MediaException {
        Player player = getPlayerByID(id);
        player.stop();
    }

    public String stopAllPlayers()  {
        Vector playerIDS = new Vector();
        Enumeration idsE = players.keys();
        while(idsE.hasMoreElements()) {
            playerIDS.addElement(idsE.nextElement());
        }
        
        StringBuffer errors = new StringBuffer();
        for(int i = 0; i < playerIDS.size(); i++) {
            try {
                stopPlayer(playerIDS.elementAt(i).toString());
            }catch(Exception e) {
                errors.append(e.toString()).append(e.getMessage()).append('\n');
            }
        }
        
        return errors.toString();
    }

    public void addMediaPlayerListener(String id, MediaPlayerListener listener) {
        Object listenerVectorObj = listeners.get(id);
        Vector listenerVector;
        if(listenerVectorObj == null) {
            listenerVector = new Vector();
            listeners.put(id, listenerVector);
        }else {
            listenerVector = (Vector)listenerVectorObj;
        }
        listenerVector.addElement(listener);
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        callbackParsingError(180, "MIDMediaPlayer", "playerUpdate", event, 
            ""+eventData);
        String id = getIDByPlayer(player);
        callbackParsingError(180, "MIDMediaPlayer", "playerUpdate-id", event, 
            ""+id);
        if(id != null) {
            firePlayerUpdate(player, id, event, eventData);
        }
        callbackParsingError(180, "MIDMediaPlayer", "playerUpdate-done", event, 
            ""+id);
    }
    
    public int getState(String id) {
        Player p = getPlayerByID(id);
        if(p != null) {
            return p.getState();
        }
        
        return LWUITMediaPlayer.INACTIVE;
    }
    
    void firePlayerUpdate(Player player, String id, String event, Object eventData) {
        Object playerListenersObj = listeners.get(id);
        if(playerListenersObj != null) {
            Vector playerListeners = (Vector)playerListenersObj;
            for(int i = 0; i < playerListeners.size(); i++) {
                ((MediaPlayerListener)playerListeners.elementAt(i)).playerUpdate(this, 
                    id, event, eventData);
            }
        }
    }
    
}
