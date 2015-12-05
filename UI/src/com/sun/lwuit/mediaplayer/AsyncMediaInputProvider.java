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

import java.io.InputStream;

/**
 *
 * Like AsyncDocumentRequest but for media input.  A MediaPlayer can wait for an 
 * async callback when the Media Input is ready - which should provide an 
 * inputstream and a mime type.
 * 
 * @author mike
 */
public interface AsyncMediaInputProvider extends MediaPlayerInputProvider {
    
    /**
     * Like MediaInputPorivder.getMediaInputStream but asynchronous
     * 
     * @see MediaPlayerInputProvider#getMediaInputStream() 
     * 
     * @param callback The IOCallback interface that will use the media input
     */
    public void getMediaInputStreamAsync(IOCallback callback);
    
    /**
     * Interface to be implemented by components that want to be able to use
     * an async source to get a mediainput (e.g. the MediaPlayerComponent)
     */
    public interface IOCallback {
        
        /**
         * This method is called when the media input stream is ready
         * 
         * @param in InputStream containing media to play
         * @param mimeType The mime type of the media e.g. audio/mpeg
         */
        public void mediaReady(InputStream in, String mimeType, int mediaSize);
        
    } 
}
