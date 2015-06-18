package orwell.proxy.controller;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by Michaël Ludmann on 6/18/15.
 */
public class GameGUI extends JFrame {
    private static final String TITLE = "Direct control";
    private static final int GUI_WIDTH = 200;
    private static final int GUI_HEIGHT = 200;

    public GameGUI() {
        this.run();
    }

    private void run() {
        //Setting frame
        this.setTitle(TITLE);
        this.setSize(GUI_WIDTH, GUI_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);

        final WindowListener listener = new WindowAdapter() {
            public void windowClosing(WindowEvent w) {
                //m_imageStreamComponent.Kill();
            }
        };

        this.addWindowListener(listener);
        //m_frame.pack();
        this.setVisible(true);
    }
}
