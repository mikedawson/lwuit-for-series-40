/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.lwuit.mediaplayer;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author mike
 */
public interface MediaPlayerInputProvider {
    
    /**
     * Must return an input stream for the media item required
     * 
     * @return InputStream for the media to play
     * 
     * @throws IOException If something goes wrong with the underlying operation
     */
    public InputStream getMediaInputStream() throws IOException;
    
    public String getMimeType();
    
}
