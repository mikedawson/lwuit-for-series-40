/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.lwuit.mediaplayer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

/**
 *
 * @author mike
 */
public class MIDPMediaPlayer implements LWUITMediaPlayer, PlayerListener{

    private Hashtable players;
    
    private Hashtable listeners;
    
    public MIDPMediaPlayer() {
        players = new Hashtable();
        listeners = new Hashtable();
    }
    
    
    public synchronized void realizePlayer(InputStream in, String mimeType, String id) throws MediaException, IOException {
        Player newPlayer = Manager.createPlayer(in, mimeType);
        players.put(id, newPlayer);
        listeners.put(id, new Vector());
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
    
    public void startPlayer(String id) throws MediaException {
        
    }

    public synchronized void stopPlayer(String id) throws MediaException {
        Player player = getPlayerByID(id);
        MediaException me = null;
        if(player != null) {
            int state = player.getState();
            if(state != Player.CLOSED) {
                try {
                    player.stop();
                    player.deallocate();
                }catch(MediaException e) {
                    me = e;
                }
            }
            player.close();
            players.remove(id);
            listeners.remove(id);
        }
        
        if(me != null) {
            throw me;
        }
    }

    public void pausePlayer(String id) throws MediaException {
        
    }

    public void stopAllPlayers() throws MediaException {
        
    }

    public void addMediaPlayerListener(String id, MediaPlayerListener listener) {
        ((Vector)listeners.get(id)).addElement(listener);
    }

    public void playerUpdate(Player player, String event, Object eventData) {
        String id = getIDByPlayer(player);
        firePlayerUpdate(player, id, event, eventData);
    }
    
    public int getState(String id) {
        Player p = getPlayerByID(id);
        if(p != null) {
            return p.getState();
        }
        
        return LWUITMediaPlayer.INACTIVE;
    }
    
    void firePlayerUpdate(Player player, String id, String event, Object eventData) {
        Vector playerListeners = (Vector)listeners.get(id);
        for(int i = 0; i < playerListeners.size(); i++) {
            ((MediaPlayerListener)playerListeners.elementAt(i)).playerUpdate(this, 
                id, event, eventData);
        }
    }

    
}
