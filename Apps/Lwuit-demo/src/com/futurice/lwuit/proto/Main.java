/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.futurice.lwuit.proto;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import javax.microedition.midlet.*;

/**
 * @author tkor
 */
public class Main extends MIDlet {

    final static Command[] commands = {
        new Command("First"), new Command("Second"), new Command("Third"),
        new Command("Fourth"), new Command("Fifth")
    };
    int nextCommand = 0;
    Command backCommand = new Command("Back");
    
    public void startApp() {
        try {
        Display.init(Main.this);
        final Form f = new Form();
        f.setTitle("TextArea test");
        f.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        final Label l = new Label();
        l.setFocusable(false);
        /*TextArea multi = new TextArea(3,3,TextArea.ANY);
        TextArea area3 = new TextArea(1, 3, TextArea.EMAILADDR | TextArea.UNEDITABLE);
        area3.setText("lolo@mail.com");
        TextArea area = new TextArea(1,3, TextArea.NUMERIC);        
        TextArea area6 = new TextArea(1,3, TextArea.ANY | TextArea.PASSWORD);
        
        f.addComponent(new Label("Scroll down to see buttons."));
        f.addComponent(new Label("Multiline"));
        f.addComponent(multi);
        f.addComponent(new Button("Push me"));
        f.addComponent(new Label("Email"));
        f.addComponent(area3);
        
        f.addComponent(new Label("Numberic"));
        f.addComponent(area);
        
        f.addComponent(new Label("password | any"));
        f.addComponent(area6);*/
        f.addComponent(new Label("original area"));
        TextArea area7 = new TextArea(2,3,TextArea.ANY);
        area7.setTextEditorEnabled(false);
        f.addComponent(area7);
        f.addComponent(new Label("textfield"));
        f.addComponent(TextField.create());
        f.addComponent(new Label("with create method"));
        f.addComponent(TextField.create());
        
        // Print something for all commands
        f.addCommandListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.out.println("actionPerformed: " + evt.getCommand().getCommandName());
            }
        });
        
        // Button for adding commands
        Button b = new Button("+button");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.out.println(nextCommand + " < " + commands.length + "?");
                if (nextCommand < commands.length) {
                    f.addCommand(commands[nextCommand++]);
                }
            }
        });
        f.addComponent(b);
        
        // Button for removing commands
        b = new Button("-button");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (nextCommand > 0) {
                    f.removeCommand(commands[nextCommand-1]);
                    nextCommand--;
                }
            }
        });
        f.addComponent(b);
        
        // Button for adding the back command
        b = new Button("+back");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f.addCommand(backCommand);
                f.setBackCommand(backCommand);
            }
        });
        f.addComponent(b);
        
        // Button for removing the back command
        b = new Button("-back");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f.removeCommand(backCommand);
                f.setBackCommand(null);
            }
        });
        f.addComponent(b);
        
        f.show();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
