package orwell.proxy.controller;

import lejos.mf.common.UnitMessage;
import lejos.mf.common.UnitMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.robot.IRobot;
import orwell.proxy.robot.MessageNotSentException;

import javax.swing.*;
import java.awt.event.*;

/**
 * Created by Michaël Ludmann on 6/18/15.
 */
public class GameGUI extends JFrame implements KeyListener {
    private final static Logger logback = LoggerFactory.getLogger(GameGUI.class);

    private static final String TITLE = "Direct control";
    private static final int GUI_WIDTH = 200;
    private static final int GUI_HEIGHT = 200;
    private final IRobot robot;

    public GameGUI(IRobot robot) {
        this.robot = robot;
        robot.connect();
        this.run();
    }

    private void run() {
        //Setting frame
        this.setTitle(TITLE);
        this.setSize(GUI_WIDTH, GUI_HEIGHT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(true);

        final WindowListener listener = new WindowAdapter() {
            public void windowClosing(WindowEvent w) {
                closeConnectionAndExit();
            }
        };

        this.addWindowListener(listener);
        this.addKeyListener(this);
        this.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        logback.debug("Key released: " + e.getKeyChar());


        UnitMessage unitMessage;
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            unitMessage = new UnitMessage(UnitMessageType.Command, "move 0.50 0.50");
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            unitMessage = new UnitMessage(UnitMessageType.Command, "move -0.50 -0.50");
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            unitMessage = new UnitMessage(UnitMessageType.Command, "move -0.50 0.50");
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            unitMessage = new UnitMessage(UnitMessageType.Command, "move 0.50 -0.50");
        } else if (e.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            unitMessage = new UnitMessage(UnitMessageType.Command, "fire true false");
        } else if (e.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            unitMessage = new UnitMessage(UnitMessageType.Command, "fire false true");
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            unitMessage = new UnitMessage(UnitMessageType.Command, "stop");
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            unitMessage = new UnitMessage(UnitMessageType.Command, "stopPrg");
        } else {
            unitMessage = new UnitMessage(UnitMessageType.Command, "game nini");
        }
        try {
            robot.sendUnitMessage(unitMessage);
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                closeConnectionAndExit();
            }
        } catch (MessageNotSentException ex) {
            logback.error(ex.getMessage());
        }
    }

    private void closeConnectionAndExit() {
        logback.debug("Close connection");
        robot.closeConnection();
        System.exit(0);
    }
}
