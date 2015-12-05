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
import com.sun.lwuit.Display;
import com.sun.lwuit.VideoComponent;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/**
 *
 * @author mike
 */
public class MIDPMediaPlayer implements LWUITMediaPlayer, PlayerListener{

    private Hashtable players;
    
    private Hashtable listeners;
    
    Hashtable videoComps;
    
    private HTMLCallback callback;
    
    private static String cacheDir;
    
    public static final int CACHECLEAR_NOCACHEDIR = -2;
    
    /**
     * Hashtable of playerID -> Boolean true or false.  True = continue loading, false = cancel
     */
    private Hashtable bufferingPlayers;
    
    /**
     * The prefix that is used when we need to buffer large media objects
     * to file for them to play successfully.
     * 
     */
    public static final String TMPFILE_PREFIX = "tmp-lwuitmedia";
    
    /**
     * The maximum size of a video file to attempt to play directly through buffering
     */
    public static final int MAXBUFFER = 400000;
    
    
    /**
     * When the size of a file to playback exceeds the available memory
     * we need to save the file to a directory and create the player
     * using the file URI instead
     * 
     * @param newCacheDir Directory to use for cache files
     */
    public static void setCacheDir(String newCacheDir) {
        cacheDir = newCacheDir;
    }
    
    public static String getTempFile(String basename, long sizeRequired) {
        if(cacheDir == null) {
            return null;
        }
        
        String tmpFile = null;
        FileConnection fCon = null;
        try {
            fCon = (FileConnection)Connector.open(cacheDir);
            long cacheAvailable = fCon.availableSize();
            fCon.close();
            fCon = null;
                
            if(cacheAvailable >= sizeRequired) {
                tmpFile = cacheDir;
                if(!tmpFile.endsWith("/")) {
                    tmpFile += '/';
                }
                
                tmpFile += basename;
                
                fCon = (FileConnection)Connector.open(tmpFile);
                if(fCon.exists()) {
                    fCon.delete();
                }
                
                fCon.create();
            }
        }catch(Exception e) {
            
        }finally {
            if(fCon != null) {
                try { fCon.close(); }
                catch(Exception e) {}
            }
        }
        
        return tmpFile;
    }
    
    /**
     * Remove any remaining temporary player files that are in the cache
     * dir with the cache prefix
     * 
     * @param callback HTMLCallback to use to reporting error messages through 
     * 
     * @return the number of files removed or a negative value indicating an error
     */
    public int clearTempFiles() {
        FileConnection con = null;
        int clearedFiles = 0;
        
        if(cacheDir == null) {
            return CACHECLEAR_NOCACHEDIR;
        }
        
        try {
            con = (FileConnection)Connector.open(cacheDir);
            Enumeration tmpFiles = con.list(TMPFILE_PREFIX+"*", true);
            Vector tmpFilesToDel = new Vector();
            while(tmpFiles.hasMoreElements()) {
                tmpFilesToDel.addElement(tmpFiles.nextElement());
            }
            tmpFiles = null;
            con.close();
            
            String dirPrefix = cacheDir;
            if(!dirPrefix.endsWith("/")) {
                dirPrefix += '/';
            }
            
            String fileURI;
            for(int i = 0; i < tmpFilesToDel.size(); i++) {
                fileURI = dirPrefix + tmpFilesToDel.elementAt(i);
                con = (FileConnection)Connector.open(fileURI);
                con.delete();
                clearedFiles++;
                con.close();
                con = null;
            }
        }catch(Exception e) {
            callbackParsingError(155, "video", "clearCacheFiles", e.toString(), 
                e.getMessage());
        }finally {
            if(con != null) {
                try { con.close(); }
                catch(IOException e) {}
                con = null;
            }
        }
        
        return clearedFiles;
    }
    
    
    public MIDPMediaPlayer() {
        players = new Hashtable();
        listeners = new Hashtable();
        bufferingPlayers = new Hashtable();
    }
    
    public void setCallback(HTMLCallback callback) {
        this.callback = callback;
    }
    
    void callbackParsingError(int id, String tag, String attribute, String value, String description) {
        if(callback != null) {
            callback.parsingError(id, tag, attribute, value, description);
        }
    }
    
    public Object realizePlayer(InputStream in, String mimeType, String id, boolean isVideo, int mediaSize) throws MediaException, IOException {
        Player newPlayer = null;
        boolean cancelled = false;
        
        if(isVideo) {
            final MIDPVideoPlaceholder placeholder = (MIDPVideoPlaceholder)videoComps.get(id);
            VideoComponent vc = null;
            
            boolean bufferTofile = mediaSize == -1 || mediaSize > MAXBUFFER;
            
            
            if(bufferTofile) {
                bufferingPlayers.put(id, Boolean.TRUE);
                //we need to copy all this into a temporary file
                callbackParsingError(600, "video", "cacheRequired", ""+mediaSize, 
                    cacheDir);
                String tmpExtension = MediaPlayerComp.getExtensionByMimeType(mimeType);
                
                String tmpFile = getTempFile(TMPFILE_PREFIX +tmpExtension, 
                    mediaSize);
                
                callbackParsingError(600, "video", "getTmpDir", tmpFile, 
                    cacheDir);
                
                
                OutputStream fout = null;
                int bufSize = 10240;
                try {
                    fout = Connector.openOutputStream(tmpFile);
                    byte[] buf = new byte[bufSize];
                    int bytesRead;
                    int bytesCompleted = 0;
                    
                    int percentComplete = 0;
                    int percentDislpayed = 0;
                    long lastUpdate = System.currentTimeMillis();
                    long timeNow;
                    
                    while((bytesRead = in.read(buf)) != -1 && !cancelled) {
                        fout.write(buf, 0, bytesRead);
                        bytesCompleted += bytesRead;
                        percentComplete = bytesCompleted / (mediaSize/100);
                        timeNow = System.currentTimeMillis();
                        
                        if(timeNow - lastUpdate > 1000 && percentComplete - percentDislpayed >= 1) {
                            lastUpdate = timeNow;
                            percentDislpayed = percentComplete;
                            placeholder.setAsyncStatus(percentComplete);
                            Display.getInstance().callSerially(placeholder);
                        }
                        
                        if(bufferingPlayers.get(id).equals(Boolean.FALSE)) {
                            //we have been told to stop - give up...
                            cancelled = true;
                            placeholder.setAsyncStatus(0);
                            Display.getInstance().callSerially(placeholder);
                        }
                    }
                    
                    fout.flush();
                }catch(Exception e) {
                    callbackParsingError(150, "video", "exception-extracting", 
                        e.toString(), e.getMessage());
                }finally {
                    if(fout != null) {
                        try { 
                            fout.close(); 
                        }
                        catch(IOException ioe) {}
                        fout = null;
                    }
                }
                
                callbackParsingError(602, "video", "readyFromTmpFile", tmpFile, 
                    "");
                if(!cancelled) {
                    vc = VideoComponent.createVideoPeer(tmpFile);
                }
                
            }else {
                //we can play it directly...
                vc = VideoComponent.createVideoPeer(in, mimeType);
            }
            
            if(!cancelled) {
                placeholder.setVideoComponent(vc);
                newPlayer = placeholder.getPlayer();
            }
        }else {
            newPlayer = Manager.createPlayer(in, mimeType);
        }
        
        if(!cancelled) {
            players.put(id, newPlayer);
            newPlayer.addPlayerListener(this);
        }
        
        return null;
    }
    
    /**
     * Cancel any ongoing buffernig for the player given by playerID
     * 
     * @param playerID The player to cancel buffering for
     * @return true if the player was buffering and now knows to stop, false otherwise
     */
    private boolean cancelBuffering(String playerID) {
        if(bufferingPlayers.containsKey(playerID)) {
            bufferingPlayers.put(playerID, Boolean.FALSE);
            return true;
        }else {
            return false;
        }
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
            if(cancelBuffering(id)) {
                retVal = LWUITMediaPlayer.BUFFERING_CANCELLED;
            }else {
                retVal = LWUITMediaPlayer.NOTHING_TO_CLOSE;
            }
        }
        
        if(videoComps != null && videoComps.containsKey(id)) {
            ((MIDPVideoPlaceholder)videoComps.get(id)).handleVideoStopped();
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

    public String stopAllPlayers(boolean clearTempFiles)  {
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
        
        if(clearTempFiles) {
            clearTempFiles();
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
