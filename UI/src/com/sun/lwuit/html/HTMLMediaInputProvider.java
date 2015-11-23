package com.sun.lwuit.html;

import com.sun.lwuit.mediaplayer.MediaPlayerComp;
import com.sun.lwuit.mediaplayer.MediaPlayerInputProvider;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author mike
 */
public class HTMLMediaInputProvider implements MediaPlayerInputProvider{

    private HTMLComponent htmlC;
    private HTMLElement mediaEl;
    
    private String mediaURI;
    private String mimeType;
    
    public HTMLMediaInputProvider(HTMLComponent htmlC, HTMLElement mediaEl) {
        this.htmlC = htmlC;
        this.mediaEl = mediaEl;
    }
    
    /**
     * Figure out the media info - URI and Mime type for this...
     * 
     * @return 
     */
    public void getMediaInfo() {
        if(mediaURI != null) {
            return;//already figured out
        }
        
        //Figure out the source of the element
        String source = mediaEl.getAttributeById(HTMLElement.ATTR_SRC);
        if (source == null) {
            //means the source is not on the tag itself but on the source tags - find them
            HTMLElement srcTag = mediaEl.getFirstChildByTagId(
                    HTMLElement.TAG_SOURCE);
            if (srcTag != null) {
                source = srcTag.getAttributeById(HTMLElement.ATTR_SRC);
                mimeType = srcTag.getAttributeById(HTMLElement.ATTR_TYPE);
            }
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
    }
    
    
    
    public InputStream getMediaInputStream() throws IOException {
        InputStream result = null;
        getMediaInfo();
        
        int type = mediaEl.getTagId() == HTMLElement.TAG_VIDEO ? 
            DocumentInfo.TYPE_VIDEO : DocumentInfo.TYPE_AUDIO;
        result = htmlC.getRequestHandler().resourceRequested(
            new DocumentInfo(mediaURI, type));
        
        if(result == null && htmlC.getHTMLCallback() != null) {
            htmlC.getHTMLCallback().parsingError(100, mediaEl.getTagName(), "src", 
                mediaURI, "Request handler gives null accessing stream:");
        }
        
        return result;
    }

    public String getMimeType() {
        getMediaInfo();
        System.out.println("mime = " + mimeType);
        return mimeType;
    }
    
}
