package com.sun.lwuit.html;

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
    
    public HTMLMediaInputProvider(HTMLComponent htmlC, HTMLElement mediaEl) {
        this.htmlC = htmlC;
        this.mediaEl = mediaEl;
    }
    
    public InputStream getMediaInputStream() throws IOException {
        InputStream result = null;
        
        //Figure out the source of the element
        String source = mediaEl.getAttributeById(HTMLElement.ATTR_SRC);
        if (source == null) {
            //means the source is not on the tag itself but on the source tags - find them
            HTMLElement srcTag = mediaEl.getFirstChildByTagId(
                    HTMLElement.TAG_SOURCE);
            if (srcTag != null) {
                source = srcTag.getAttributeById(HTMLElement.ATTR_SRC);
            }
        }

        String fullURI = htmlC.getDocumentInfo().convertURL(source);
        int type = mediaEl.getTagId() == HTMLElement.TAG_VIDEO ? 
            DocumentInfo.TYPE_VIDEO : DocumentInfo.TYPE_AUDIO;
        result = htmlC.getRequestHandler().resourceRequested(
            new DocumentInfo(fullURI, type));
        
        if(result == null && htmlC.getHTMLCallback() != null) {
            htmlC.getHTMLCallback().parsingError(100, mediaEl.getTagName(), "src", 
                fullURI, "Request handler gives null accessing stream:");
        }
        
        return result;
    }

    public String getMimeType() {
        return "audio/mpeg";
    }
    
}
