package com.hex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private RandomAccessFile raf = null;

    public HEXEditor() {
        this.mainJFrame = createMainJFrame();
        mainJFrame.repaint();
    }


    public JFrame createMainJFrame() {
        if(mainJFrame != null){
            mainJFrame.setVisible(false);
        }
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
        jFrame.setVisible(true);

        JPanel headingPanel = createHeadingPanel();
        jFrame.add(headingPanel, BorderLayout.NORTH);

        if (raf == null){
            return jFrame;
        }

        mainTable = new MainTable(raf, nColumns);
        JScrollPane tablePane = new JScrollPane(
                mainTable.getTable(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        jFrame.add(tablePane, BorderLayout.CENTER);

        JPanel controlPanel = createControlPanel();
        jFrame.add(controlPanel, BorderLayout.SOUTH);

        return jFrame;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.add(mainTable.getShiftModePanel(), BorderLayout.NORTH);
        controlPanel.add(mainTable.getToolTipModePanel(), BorderLayout.CENTER);
        return controlPanel;
    }


    private JPanel createHeadingPanel(){
        JPanel headingPanel = new JPanel();

        JButton fileChooserButton = createFileChooserButton();

        JTextField searchTF = new JTextField(30);
        JButton searchButton = createSearchButton(searchTF);

        headingPanel.add(fileChooserButton, BorderLayout.EAST);
        headingPanel.add(searchTF, BorderLayout.CENTER);
        headingPanel.add(searchButton, BorderLayout.WEST);

        return headingPanel;
    }

    private JButton createFileChooserButton(){
        JButton fileChooserButton = new JButton("Загрузить файл");
        fileChooserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                int ret = fileopen.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File name = fileopen.getSelectedFile();
                    try {
                        raf = new RandomAccessFile(name, "rws");
                        mainJFrame = createMainJFrame();
                        mainJFrame.repaint();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        return fileChooserButton;
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
                for (long i = 0; i < Math.ceil((double) raf.length() / nColumns); i++) {
                    dataLine = new StringBuilder();
                    raf.seek(nColumns * i);
                    byte[] line = new byte[nColumns];
                    raf.read(line);
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
        return raf;
    }

    public void setFile(RandomAccessFile file) {
        this.raf = file;
    }
}
