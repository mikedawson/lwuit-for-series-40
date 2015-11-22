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
    
    public static final int CLOSED_DEALLOCATED_OK = 0;
    
    public static final int CLOSED_ALEADY = 1;
    
    public static final int NOTHING_TO_CLOSE = 1;
   
    
    public MIDPMediaPlayer() {
        players = new Hashtable();
        listeners = new Hashtable();
    }
    
    
    public synchronized void realizePlayer(InputStream in, String mimeType, String id) throws MediaException, IOException {
        Player newPlayer = Manager.createPlayer(in, mimeType);
        players.put(id, newPlayer);
        newPlayer.addPlayerListener(this);
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
        getPlayerByID(id).start();
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
                    retVal = CLOSED_DEALLOCATED_OK;
                }catch(MediaException e) {
                    me = e;
                }
            }else {
                retVal = CLOSED_ALEADY;
            }
            player.close();
            players.remove(id);
            listeners.remove(id);
        }else {
            retVal = NOTHING_TO_CLOSE;
        }
        
        if(me != null) {
            throw me;
        }
        
        return retVal;
    }

    public void pausePlayer(String id) throws MediaException {
        
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
        String id = getIDByPlayer(player);
        System.out.println("playerUpdate: " + id + "/" + event);
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
        Object playerListenersObj = listeners.get(id);
        if(playerListenersObj != null) {
            Vector playerListeners = (Vector)listeners.get(id);
            for(int i = 0; i < playerListeners.size(); i++) {
                ((MediaPlayerListener)playerListeners.elementAt(i)).playerUpdate(this, 
                    id, event, eventData);
            }
        }
    }
    
}
