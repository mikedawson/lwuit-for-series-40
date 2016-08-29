package com.sun.lwuit.html;

import com.sun.lwuit.mediaplayer.LWUITMediaPlayer;
import com.sun.lwuit.mediaplayer.MediaPlayerComp;
import com.sun.lwuit.mediaplayer.MediaPlayerInputProvider;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 *
 * Class that implements MediaPlayerInputProvider to provide information about 
 * a media stream (mediaType, stream length, and input stream) given an HTMLElement 
 * that represents the audio or video tag.
 * 
 *  @author mike
 */
public class HTMLMediaInputProvider implements MediaPlayerInputProvider{

    protected HTMLComponent htmlC;
    protected HTMLElement mediaEl;
    
    protected String mediaURI;
    protected String mimeType;
    
    protected DocumentInfo docInfo; 
    protected int mediaSize;
    
    protected LWUITMediaPlayer player;
    
    /**
     * 
     * @param htmlC
     * @param mediaEl
     * @param player 
     */
    public HTMLMediaInputProvider(HTMLComponent htmlC, HTMLElement mediaEl, LWUITMediaPlayer player) {
        this.htmlC = htmlC;
        this.mediaEl = mediaEl;
        this.player = player;
        this.mediaSize = -1;
    }
    
    /**
     * Figure out the media info - URI and Mime type for this...
     * 
     */
    public void getMediaInfo() {
        if(mediaURI != null) {
            return;//already figured out
        }
        
        //Figure out the source of the element
        String source = mediaEl.getAttributeById(HTMLElement.ATTR_SRC);
        if (source == null) {
            //TODO: Use this area to figure out which of types we prefer amongst multiple source tags
            Vector availableSourceEls = mediaEl.getDescendantsByTagId(HTMLElement.TAG_SOURCE);
            String[] availableFormats = new String[availableSourceEls.size()];
            for(int i = 0; i < availableFormats.length; i++) {
                availableFormats[i] = ((HTMLElement)availableSourceEls.elementAt(i)).getAttributeById(HTMLElement.ATTR_TYPE);
            }
            
            int preferredFormat = player.getPreferredFormat(availableFormats, 
                mediaEl.getTagId() == HTMLElement.TAG_VIDEO ? HTMLCallback.MEDIA_VIDEO : HTMLCallback.MEDIA_AUDIO);
            HTMLElement srcTag = null;
            
            if(preferredFormat != -1) {
                srcTag = (HTMLElement)availableSourceEls.elementAt(preferredFormat);
                source = srcTag.getAttributeById(HTMLElement.ATTR_SRC);
                mimeType = srcTag.getAttributeById(HTMLElement.ATTR_TYPE);
            }
        }
        
        //check and be sure that we have found the source
        if(source == null) {
            //no suitable format found for playing on this device - return null
            return;
        }
        

        mediaURI = htmlC.getDocumentInfo().convertURL(source);
        
        //check that we have a mime type for it - otherwise look at extension
        if(mimeType == null) {
            String uriTrim = mediaURI;
            int qIndex = uriTrim.indexOf('?');
            if(qIndex != -1) {
                uriTrim = uriTrim.substring(0, qIndex);
            }
            
            int extIndex = uriTrim.lastIndexOf('.');
            
            mimeType = MediaPlayerComp.getMimeTypeByExtension(
                extIndex != -1 ? uriTrim.substring(extIndex) : uriTrim);
        }
        
        docInfo = makeRequestDocInfo();
    }
    
    /**
     * Create a DocumentInfo object for the requestHandler 
     * 
     * If there is no suitable media type available in the source tags that
     * can be played by this device we will return null
     * 
     * @return DocumentInfo
     */
    protected DocumentInfo makeRequestDocInfo() {
        getMediaInfo();
        if(mediaURI != null) {
            int type = mediaEl.getTagId() == HTMLElement.TAG_VIDEO ? 
                DocumentInfo.TYPE_VIDEO : DocumentInfo.TYPE_AUDIO;
            return new DocumentInfo(mediaURI, type);
        }else {
            return null;
        }
    }
    
    /**
     * @{inheritDoc}
     */
    public boolean hasSrc() {
        getMediaInfo();
        return mediaURI != null;
    }
    
    
    public InputStream getMediaInputStream() throws IOException {
        InputStream result = null;
        getMediaInfo();
        
        if(docInfo != null) {
            result = htmlC.getRequestHandler().resourceRequested(docInfo);
            mediaSize = docInfo.getContentLength();

            if(result == null && htmlC.getHTMLCallback() != null) {
                htmlC.getHTMLCallback().parsingError(100, mediaEl.getTagName(), "src", 
                    mediaURI, "Request handler gives null accessing stream:");
            }
        }
        
        return result;
    }
    
    public int getMediaSize() {
        return mediaSize;
    }
    
    public String getMimeType() {
        getMediaInfo();
        System.out.println("mime = " + mimeType);
        return mimeType;
    }
    
    public boolean isVideo() {
        return mediaEl.getTagId() == HTMLElement.TAG_VIDEO;
    }
    
}
