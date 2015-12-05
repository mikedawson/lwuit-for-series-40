/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.lwuit.html;

import com.sun.lwuit.mediaplayer.AsyncMediaInputProvider;
import java.io.InputStream;

/**
 *
 * @author mike
 */
public class AsyncHTMLMediaInputProvider extends HTMLMediaInputProvider implements AsyncMediaInputProvider, AsyncDocumentRequestHandler.IOCallback{
    
    private IOCallback ioCallback;
    
    public AsyncHTMLMediaInputProvider(HTMLComponent htmlC, HTMLElement mediaEl) {
        super(htmlC, mediaEl);
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
