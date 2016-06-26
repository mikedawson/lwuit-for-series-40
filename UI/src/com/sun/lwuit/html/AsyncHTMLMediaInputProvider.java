/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.lwuit.html;

import com.sun.lwuit.mediaplayer.AsyncMediaInputProvider;
import com.sun.lwuit.mediaplayer.LWUITMediaPlayer;
import java.io.InputStream;

/**
 *
 * @author mike
 */
public class AsyncHTMLMediaInputProvider extends HTMLMediaInputProvider implements AsyncMediaInputProvider, AsyncDocumentRequestHandler.IOCallback{
    
    private IOCallback ioCallback;
    
    public AsyncHTMLMediaInputProvider(HTMLComponent htmlC, HTMLElement mediaEl, LWUITMediaPlayer player) {
        super(htmlC, mediaEl, player);
    }

    public void getMediaInputStreamAsync(IOCallback ioCallback) {
        this.ioCallback = ioCallback;
        AsyncDocumentRequestHandler requestHandler = 
            (AsyncDocumentRequestHandler)htmlC.getRequestHandler();
        requestHandler.resourceRequestedAsync(makeRequestDocInfo(), this);
    }

    public void streamReady(InputStream is, DocumentInfo docInfo) {
        ioCallback.mediaReady(is, mimeType, docInfo.getContentLength());
    }
    
}
