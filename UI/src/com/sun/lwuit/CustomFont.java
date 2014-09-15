/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package com.sun.lwuit;

import java.util.Hashtable;



/**
 * Implements a bitmap font that uses an image and sets of offsets to draw a font
 * with a given character set.
 *
 * @author Shai Almog
 */
class CustomFont extends Font {
    /**
     * Keep two colors in cache by default to allow faster selection colors
     */
    private static final int COLOR_CACHE_SIZE = 20;
    
    private Hashtable colorCache = new Hashtable();

    private String charsets;
    private int color;
    
    
    /*
     * forms of arabic / connected characters
     */
    
    /** Isolated Form**/
    public static final int ISO = 1;
    
    /** Beginning Form connected to next character */
    public static final int BEG = 4;
    
    /** Middle Form connected on both sides */
    public static final int MID = 3;
    
    /** End form connected to the previous character only*/
    public static final int END = 2;
    
    /** Whitespace in word */
    public static final int WS = 4;
    
    /**
     * This is a mapping of arabic characters to glyph forms.  
     * 
     * For standard Arabic or Persian this is as per default unicode.  For
     * Pashto this uses little used space within the unicode space (e.g. <65000) 
     * 
     * See the DejaVuModifiedPS.ttf file 
     * 
     */
    static char[][] arabicCharList = {
        //START ARABIC
            //NORMAL UNICODE, ISOLATED, END, MIDDLE, BEGINNING
            {'\u0623', '\uFE83', '\uFE84'}, //ALEF with hamza above
            {'\u0622', '\uFE81', '\uFE82'}, //ALEF with madda above
            {'\u0627', '\uFE8D', '\uFE8E'}, //ALEF
            {'\u0626', '\uFE87', '\uFE88'}, //ALEF with hamza below
            {'\u0628', '\uFE8F', '\uFE90', '\uFE92','\uFE91'},//BA
            {'\u062A', '\uFE95', '\uFE96', '\uFE98', '\uFE97'},//TA
            {'\u062B', '\uFE99', '\uFE9A', '\uFE9C', '\uFE9B'},//TAY
            {'\u062C', '\uFE9D', '\uFE9E', '\uFEA0', '\uFE9F'},//GIM
            {'\u062D', '\uFEA1', '\uFEA2', '\uFEA4', '\uFEA3'},//HA
            {'\u062E', '\uFEA5', '\uFEA6', '\uFEA8', '\uFEA7'},//HA
            {'\u062F', '\uFEA9', '\uFEAA'},//DAL
            {'\u0630', '\uFEAB', '\uFEAC'},//DAL
            {'\u0631', '\uFEAD', '\uFEAE'},//RA
            {'\u0632', '\uFEAF', '\uFEB0'},//ZAYN
            {'\u0633', '\uFEB1', '\uFEB2', '\uFEB4', '\uFEB3'},//SIN
            {'\u0634', '\uFEB5', '\uFEB6', '\uFEB8', '\uFEB7'},//SHEEN
            {'\u0635', '\uFEB9', '\uFEBa', '\uFEBC', '\uFEBB'},//SAD
            {'\u0636', '\uFEBD', '\uFEBE', '\uFEC0', '\uFEBF'},//DAD
            {'\u0637', '\uFEC1', '\uFEC2', '\uFEC4', '\uFEB3'},//TA
            {'\u0638', '\uFEC5', '\uFEC6', '\uFEC8', '\uFEB7'},//ZA
            {'\u0639', '\uFEC9', '\uFECA', '\uFECC', '\uFEBB'},//AYN
            {'\u063A', '\uFECD', '\uFECE', '\uFED0', '\uFECF'},//GAYN
            {'\u0641', '\uFED1', '\uFED2', '\uFED4', '\uFED3'},//FA
            {'\u0642', '\uFED5', '\uFED6', '\uFED8', '\uFED7'},//QAF
            {'\u0643', '\uFED9', '\uFEDA', '\uFEDC', '\uFEDB'},//KAF
            {'\u0644', '\uFEDD', '\uFEDE', '\uFEE0', '\uFEDF'},//LAM
            {'\u0645', '\uFEE1', '\uFEE2', '\uFEE4', '\uFEE3'},//MIM
            {'\u0646', '\uFEE5', '\uFEE6', '\uFEE8', '\uFEE7'},//NUN
            {'\u0647', '\uFEE9', '\uFEEA', '\uFEEC', '\uFEEB'},//HA
            {'\u0648', '\uFEED', '\uFEEE'},//WAW
            {'\u064A', '\uFEF1', '\uFEF2', '\uFEF4', '\uFEF3'},//YA
            {'\u0622', '\uFE81', '\uFE82'},//ALIF MADDAH
            {'\u0629', '\uFE93', '\uFE94'},//TA MURBAUTAH
            {'\u0649', '\uFEEF', '\uFEF0'},//ALIF MAQSURAH
            {'\u06A9', '\uFB8E', '\uFB8F', '\uFB91', '\uFB90'}, //GOF? ARABIC KEHEH
            //END ARABIC
            //START PERSIAN ISOLATED, END, MIDDLE, Beginning
            {'\u067E', '\uFB56', '\uFB57', '\uFB59', '\uFB58'},   //Peh
            {'\u0686', '\uFB7A', '\uFB7B', '\uFB7D', '\uFB7C'}, //CHEH
            {'\u0698', '\uFB8A', '\uFB8B'  }, //ZEH
            {'\u06AF', '\uFB92', '\uFB93', '\uFB95', '\uFB94' }, //GOF
            //END PERSIAN
            //START PASHTO - matches Pashto Kror Asisatype mappings
            { '\u067c', '\u067c', '\uff00', '\uff02', '\uff01'},            //SPECIAL TAY with two dots below ??
            { '\u0681', '\u0681', '\uff03', '\uff05', '\uff04'},   //special zim    ???
            { '\u0689', '\u0689', '\uff16' }, //special doll with dot below ??
            { '\u0693', '\u0693', '\uff17' }, // special ray with dot below ??
            { '\u0696', '\u0696', '\uff18' }, //special gay ?
            { '\u069A', '\u069A', '\uFF09', '\uFF0B', '\uFF0A'}, // seen with two dots ?
            { '\u06AB', '\u06AB', '\uff0c', '\uff0f', '\uff0d' }, //special gof with dot below hook   ??
            { '\u06BC', '\u06BC', '\uff10', '\uff12', '\uff11' }, //special noon with dot ??
            { '\u06D0', '\u06D0', '\uff13', '\uff15', '\uff14' }, //pasta yey  ??
            { '\u06CC', '\u06cc', '\ufbfd', '\ufbff', '\ufbfe' }, //narina yey ??
            { '\u06CD', '\u06CD', '\uff19' }, //xezina yey ??
            { '\u0626', '\u0626', '\ufe8a', '\ufe8c', '\ufe8b'}  //f?iliya ye  ?? 
    };
    
    // package protected for the resource editor
    Image cache;
    
    /**
     * The offset in which to cut the character from the bitmap
     */
    int[] cutOffsets;

    /**
     * The width of the character when drawing... this should not be confused with
     * the number of cutOffset[o + 1] - cutOffset[o]. They are completely different
     * since a character can be "wider" and "seep" into the next region. This is
     * especially true with italic characters all of which "lean" outside of their 
     * bounds.
     */
    int[] charWidth;

    private int imageWidth;
    private int imageHeight;
    private Object imageArrayRef;
    
    
    private int[] getImageArray() {
        if(imageArrayRef != null) {
            int[] a = (int[])Display.getInstance().extractHardRef(imageArrayRef);
            if(a != null) {
                return a;
            }
        }
        int[] a = cache.getRGBCached();
        
        imageArrayRef = Display.getInstance().createSoftWeakRef(a);
        return a;
    }
    
    /**
     * Creates a bitmap font with the given arguments
     * 
     * @param bitmap a transparency map in red and black that indicates the characters
     * @param cutOffsets character offsets matching the bitmap pixels and characters in the font 
     * @param charWidth The width of the character when drawing... this should not be confused with
     *      the number of cutOffset[o + 1] - cutOffset[o]. They are completely different
     *      since a character can be "wider" and "seep" into the next region. This is
     *      especially true with italic characters all of which "lean" outside of their 
     *      bounds.
     * @param charsets the set of characters in the font
     * @return a font object to draw bitmap fonts
     */
    public CustomFont(Image bitmap, int[] cutOffsets, int[] charWidth, String charsets) {
        this.cutOffsets = cutOffsets;
        this.charWidth = charWidth;
        this.charsets = charsets;
        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();
        int[] imageArray = new int[imageWidth * imageHeight];
        
        // default to black colored font
        bitmap.getRGB(imageArray, 0, 0, 0, imageWidth, imageHeight);
        for(int iter = 0 ; iter < imageArray.length ; iter++) {
            // extract the red component from the font image
            // shift the alpha 8 bits to the left
            // apply the alpha to the image
            imageArray[iter] = ((imageArray[iter] & 0xff0000) << 8);
        }
        cache = Image.createImage(imageArray, imageWidth, imageHeight);
        imageArrayRef = Display.getInstance().createSoftWeakRef(imageArray);
    }
    
    /**
     * @inheritDoc
     */
    public int charWidth(char ch) {
        int i = charsets.indexOf(ch);
        if(i < 0) {
            return 0;
        }
        return charWidth[i];
    }

    /**
     * @inheritDoc
     */
    public int getHeight() {
        return imageHeight;
    }

    private boolean checkCacheCurrentColor(int newColor) {
        Integer currentColor = new Integer(color);
        Integer newColorKey = new Integer(newColor);
        if(colorCache.get(currentColor) == null){
            colorCache.put(currentColor, Display.getInstance().createSoftWeakRef(cache));
        }
        color = newColor;
        Object newCache = Display.getInstance().extractHardRef(colorCache.get(newColorKey));
        if(newCache != null) {
            Image i = (Image)newCache;
            if(i != null) {
                cache = i;
                if(colorCache.size() > COLOR_CACHE_SIZE) {
                    // remove a random cache element
                    colorCache.remove(colorCache.keys().nextElement());
                }
                return true;
            }else{
                colorCache.remove(newColorKey);
            }
        }
        if(colorCache.size() > COLOR_CACHE_SIZE) {
            // remove a random cache element
            colorCache.remove(colorCache.keys().nextElement());
        }        
        return false;
    }
    
    private void initColor(Graphics g) {
        int newColor = g.getColor();
        
        if(newColor != color && !checkCacheCurrentColor(newColor)) {
            color = newColor & 0xffffff;
            int[] imageArray = getImageArray();
            for(int iter = 0 ; iter < imageArray.length ; iter++) {
                // extract the red component from the font image
                // shift the alpha 8 bits to the left
                // apply the alpha to the image
                imageArray[iter] = color | (imageArray[iter] & 0xff000000);
            }
            cache = Image.createImage(imageArray, imageWidth, imageHeight);
        }
    }
    
    /**
     * @inheritDoc
     */
    void drawChar(Graphics g, char character, int x, int y) {
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();

        int i = charsets.indexOf(character);
        if(i > -1) {
            initColor(g);
            
            // draw region is flaky on some devices, use setClip instead
            g.clipRect(x, y, charWidth[i], imageHeight);
            g.drawImage(cache, x - cutOffsets[i], y);
            //g.drawRegion(cache, cutOffsets[i], 0, charWidth[i], imageHeight, x, y);
        }

        // restore the clip
        g.setClip(clipX, clipY, clipWidth, clipHeight);
    }

    /**
     * @inheritDoc
     */
    public void addContrast(byte value) {
        int[] imageArray = getImageArray();
        for(int iter = 0 ; iter < imageArray.length ; iter++) {
            int alpha = (imageArray[iter] >> 24) & 0xff;
            if(alpha != 0) {
                alpha = Math.min(alpha + value, 255);
                imageArray[iter] = ((alpha << 24) & 0xff000000) | color;
            }
        }
    }
    
    /**
     * Provides the required sub character 
     * 
     * @param c - the character to get a glyph for
     * @param form (ISO, BEG, MID or END)
     * @return the glyph according to arabicCharList
     */
    private static final char subChar(char c, int form) {
        for(int i = 0; i < arabicCharList.length; i++) {
           if(arabicCharList[i][0] == c) {
               if(arabicCharList[i].length > form) {
                   return arabicCharList[i][form];
               }
           }
        }
        return c;
    }
    
    /**
     * Determines if the character c has a form (ISO, BEG, MID or END) 
     * according to arabicCharList
     * 
     * @param c
     * @param form (ISO, BEG, MID or END)
     * @return true if the form exists, false otherwise
     */
    private static final boolean charHasForm(char c, int form) {
        for(int i = 0; i < arabicCharList.length; i++) {
           if(arabicCharList[i][0] == c) {
               if(arabicCharList[i].length > form) {
                   return true;
               }
           }
        }
        return false;
    }
    
    
    /**
     * Given a string that has been detected as an arabic String it will 
     * 'arabize' by replacing glyphs according to if they are at the start
     * middle or end of the word and if they are intended to connect
     * 
     * This works by having an array called arabicCharList.  It is a multidimensional
     * in the format of with an array for each character.
     * 
     *  Character Code => isolated form, end form, middle form, beginning form
     * 
     * For this to work the LWUIT CustomFont in use must have a bitmap for each
     * glyph that will be used.
     * 
     * @see arabiccharlist
     * 
     * @param - s - the string to be arabized
     * @return s as an array of arabized glyphs
     */
    public String arabize(String s) {
        char[] ret = s.toCharArray();
        char[] c = s.toCharArray();
        for(int i = 0; i < c.length; i++) {
            boolean prevCharConnects = false;
            int form = ISO;
            if(i < c.length -1) {
                char prevChar = c[i+1];
                prevCharConnects = charHasForm(c[i+1], BEG) || charHasForm(c[i+1], MID);
            }
            boolean nextCharConnects = false;
            if(i > 0){
                char nextChar = c[i-1];
                nextCharConnects = charHasForm(c[i-1], MID) || charHasForm(c[i-1], END);
            }
            if(prevCharConnects) {
                if(nextCharConnects) {
                    form = MID;
                }else {
                    form = END;
                }
            }else {
                if(nextCharConnects) {
                    form = BEG;
                }else {
                    form = ISO;
                }
            }
            if(form == MID && charHasForm(c[i], MID) == false) {
                //this is a character that has no middle form and goes only on the end
                form = END;
            }
            ret[i] = subChar(c[i], form);
        }
        return new String(ret);
    }

    /**
     * Override this frequently used method for a slight performance boost...
     * 
     * @param g the component graphics
     * @param data the chars to draw
     * @param offset the offset to draw the chars 
     * @param length the length of chars 
     * @param x the x coordinate to draw the chars
     * @param y the y coordinate to draw the chars
     */
    void drawChars(Graphics g, char[] data, int offset, int length, int x, int y) {
        if(Display.getInstance().isBidiAlgorithm()) {
            for(int i = offset ; i < length ; i++) {
                if(Display.getInstance().isRTL(data[i])) {
                    String s = Display.getInstance().convertBidiLogicalToVisual(new String(data, offset, length));
                    if(Display.getInstance().isArabized()) {
                        s = arabize(s);
                    }
                    data = s.toCharArray();
                    offset = 0;
                    length = s.length();
                    break;
                }
            }
        }
        initColor(g);
        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipWidth = g.getClipWidth();
        int clipHeight = g.getClipHeight();

        if(clipY <= y + getHeight() && clipY + clipHeight >= y) {
            char c;
            for ( int i = 0; i < length; i++ ) {
                c = data[offset+i];
                int position = charsets.indexOf(c);
                if(position < 0) {
                    continue;
                }
                // draw region is flaky on some devices, use setClip instead
                g.clipRect(x, y, charWidth[position], imageHeight);
                if(g.getClipWidth() > 0 && g.getClipHeight() > 0) {
                    g.drawImage(cache, x - cutOffsets[position], y);
                }
                x += charWidth[position];
                g.setClip(clipX, clipY, clipWidth, clipHeight);
            }
        }
    }

    /**
     * @inheritDoc
     */
    public String getCharset() {
        return charsets;
    }

    /**
     * @inheritDoc
     */
    public int charsWidth(char[] ch, int offset, int length){
        int retVal = 0;
        for(int i=0; i<length; i++){
            retVal += charWidth(ch[i + offset]);
        }
        return retVal;
    }


    /**
     * @inheritDoc
     */
    public int substringWidth(String str, int offset, int len){
        return charsWidth(str.toCharArray(), offset, len);
    }

    /**
     * @inheritDoc
     */
    public int stringWidth(String str){
        if( str==null || str.length()==0)
            return 0;
        return substringWidth(str, 0, str.length());
    }

    /**
     * @inheritDoc
     */
    public int getFace(){
        return 0;
    }

    /**
     * @inheritDoc
     */
    public int getSize(){
        return 0;
    }

    /**
     * @inheritDoc
     */
    public int getStyle() {
        return 0;
    }
    
    /**
    * @inheritDoc
    */
   public boolean equals(Object o) {
       if(o == this) {
           return true;
       }
       if(o != null && o.getClass() == getClass()) {
           CustomFont f = (CustomFont)o;
           if(charsets.equals(f.charsets)) {
               for(int iter = 0 ; iter < cutOffsets.length ; iter++) {
                   if(cutOffsets[iter] != f.cutOffsets[iter]) {
                       return false;
                   }
               }
               return true;
           }
       }
       return false;
   }
}
