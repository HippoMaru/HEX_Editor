package com.hex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;

public class HEXEditor {
    private JFrame mainJFrame;

    private RandomAccessFile file;

    public HEXEditor() {
        this.mainJFrame = createMainJFrame();
    }


    public JFrame createMainJFrame() {
        JFrame jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        jFrame.setBounds((dimension.width - 1000) / 2, (dimension.height - 300) / 2, 1000, 300);
        jFrame.setTitle("HEX Editor 2.0 by HippoMaru");
        jFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                updateFile();
            }
        });
        return jFrame;
    }

    private void updateFile() {
    }

    public void run() {
        mainJFrame.setVisible(true);
    }

    public JFrame getMainJFrame() {
        return mainJFrame;
    }

    public void setMainJFrame(JFrame mainJFrame) {
        this.mainJFrame = mainJFrame;
    }

    public RandomAccessFile getFile() {
        return file;
    }

    public void setFile(RandomAccessFile file) {
        this.file = file;
    }
}
