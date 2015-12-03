/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.lwuit.mediaplayer;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Graphics;

/**
 *
 * @author mike
 */
public class MediaPlayerCompButton extends Button {
        
    private int mediaCommand;
        
    //The default width of the icon to go within the button
    public static final int PREFER_ICON_WIDTH = 18;

    public MediaPlayerCompButton(int mediaCommand) {
        super(new Command(" ", mediaCommand));
        this.mediaCommand = mediaCommand;
    }

    public int getPreferredW() {
        return PREFER_ICON_WIDTH + getStyle().getPadding(Component.LEFT)
            + getStyle().getPadding(Component.RIGHT);
    }

    public void setMediaCommand(int mediaCommand) {
        if (mediaCommand != this.mediaCommand) {
            this.mediaCommand = mediaCommand;
            repaint();
        }
    }

    public void paint(Graphics g) {
        super.paint(g);

        //now put the correct symbol over the top
        int w = getWidth();
        int h = getHeight();
        int x = getX();
        int y = getY();
        int padL = getStyle().getPadding(Component.LEFT);
        int padR = getStyle().getPadding(Component.RIGHT);
        int padT = getStyle().getPadding(Component.TOP);
        int padB = getStyle().getPadding(Component.BOTTOM);

        g.setColor(0);
        g.setAlpha(128);
        switch (mediaCommand) {
            case MediaPlayerComp.PLAY:
                //Draw the play triangle
                g.fillPolygon(
                    new int[]{x + padL, (x + w) - padR, x + padL},
                    new int[]{y + padT, y + (h / 2), (y + h) - padB},
                    3);
                break;
            case MediaPlayerComp.STOP:
                //Draw the stop square
                g.fillRect(x + padL, y + padT, w - padL - padR, h - padT - padB);
                break;
            case MediaPlayerComp.PAUSE:
                //Draw the pause rectangles
                int rectW = (w - padL - padR) / 3;
                g.fillRect(x + padL, y + padT, rectW, h - padT - padB);
                g.fillRect(x + padL + (rectW * 2), y + padT, rectW, h - padT - padB);
                break;
        }

    }

}
