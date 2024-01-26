package com.hex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HEXEditor {
    private JFrame mainJFrame;
    private int nColumns = 10;

    private MainTable mainTable;

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

        JTextField searchTF = new JTextField(30);
        JButton searchButton = createSearchButton(searchTF);
        return jFrame;
    }

    private JButton createSearchButton(JTextField searchTF){
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e  -> {
            String searchInput = searchTF.getText();
            Pattern pattern = Pattern.compile(searchInput);
            Matcher matcher;
            StringBuilder dataLine;
            int startIndex;
            int endIndex;
            try {
                for (long i = 0; i < Math.ceil((double) file.length() / nColumns); i++) {
                    dataLine = new StringBuilder();
                    file.seek(nColumns * i);
                    byte[] line = new byte[nColumns];
                    file.read(line);
                    for (byte b : line) {
                        dataLine.append(String.format("%02X", b));
                    }
                    matcher = pattern.matcher(dataLine);
                    if (matcher.find()) {
                        startIndex = matcher.start();
                        endIndex = matcher.end();
                        JTable table = mainTable.getTable();
                        table.setRowSelectionInterval((int) i, (int) i);
                        table.setColumnSelectionInterval(startIndex / 2, (endIndex - 1) / 2);
                        table.scrollRectToVisible(table.getCellRect((int) i, startIndex / 2, true));
                        mainTable.setTable(table);
                        break;
                    }
                }
            }
            catch (IOException ignored){}
        });
        return searchButton;
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
