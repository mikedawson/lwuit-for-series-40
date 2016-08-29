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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * This interface is used by whatever class can generate an inputstream and 
 * a mime type that is used to play media; e.g. the HTMLMediaInputProvider
 * which uses the media html tags and it's DocumentRequestHandler to produce
 * an input stream and determine the mime type of the media to be played
 * 
 * @author mike
 */
public interface MediaPlayerInputProvider {
    
    /**
     * Must return an input stream for the media item required
     * 
     * @return InputStream for the media to play, null if no suitable stream is available (e.g. incompatible with device)
     * 
     * @throws IOException If something goes wrong with the underlying operation
     */
    public InputStream getMediaInputStream() throws IOException;
    
    /**
     * Must provide the mime type of the audio or video to be played
     * 
     * @return Mime type as string e.g. audio/mpeg
     */
    public String getMimeType();
    
    /**
     * Used to tell if this media input has video or not - technically
     * one could look at the mime type if it starts with audio/ or video/
     * however one might use a video file only for it's sound...
     * 
     * @return true if this media input has video - false otherwise
     */
    public boolean isVideo();
    
    /**
     * Should return the size of the media that is to be played.  Live / infinite
     * streams are impossible to handle here.(we don't supoprt the J2ME DataSource
     * setup and Nokia implementations will attempt to fully read a stream 
     * resulting in an OutOfMemory Exception).: 
     * 
     * It is likely that the size is accessible only AFTER getting the input
     * stream : this would require connecting to the remote source / server
     * etc.
     * 
     * @return Length of the input or -1 if not known.
     */
    public int getMediaSize();
    
    /**
     * Returns true if there is a source eligible to try playing on this device.
     * This is useful when there is a list of source elements with mime types: if
     * we can see by looking at this that no src is compatible with this device we
     * can drop it.  It might be the mime type is not yet known... in which case
     * it might still fail.
     * 
     * @return true if there's hope to play this file (eligible source), false otherwise
     */
    public boolean hasSrc();
    
}
