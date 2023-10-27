package hex.gui;

import hex.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hex.HEXEditor.*;
import static hex.gui.MainTable.*;

public class MainFrame {

    private static JTextField searchTF;
    private static JButton searchButton;
    public static void create() {
        jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        jFrame.setBounds((dimension.width - width)/2, (dimension.height - height)/2, width, height); //mid loc
        jFrame.setTitle("HEX Editor 1.0 by HippoMaru");
        jFrame.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent e){
                try {
                    Utils.updateFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        searchTF = new JTextField(30);
        createSearchButton();
        createTable();
        JScrollPane pane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setRowHeaderView(headerTable);
        JPanel searchPanel = new JPanel();
        JButton fileChooserButton = new JButton("Загрузить файл");

        fileChooserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                int ret = fileopen.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    file = fileopen.getSelectedFile();
                    Utils.loadData();
                    create();
                }
            }
        });
        searchPanel.add(fileChooserButton, BorderLayout.EAST);
        searchPanel.add(searchTF, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.WEST);
        jFrame.add(searchPanel, BorderLayout.NORTH);
        jFrame.add(pane, BorderLayout.CENTER);
        JPanel controlPanel = new JPanel();
        controlPanel.add(shiftModePanel, BorderLayout.NORTH);
        controlPanel.add(toolTipModePanel, BorderLayout.SOUTH);
        jFrame.add(controlPanel, BorderLayout.SOUTH);
        jFrame.pack();
        jFrame.setVisible(true);
    }

    private static void createSearchButton(){
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> {
            String searchInput = searchTF.getText();
            Pattern pattern = Pattern.compile(searchInput);
            Matcher matcher;
            StringBuilder dataLine;
            int startIndex;
            int endIndex;
            for (int i = 0; i < data.size(); i++) {
                dataLine = new StringBuilder();
                for (byte b : data.get(i)) {
                    dataLine.append(String.format("%02X", b));
                }
                matcher = pattern.matcher(dataLine);
                if (matcher.find()) {
                    startIndex = matcher.start();
                    endIndex = matcher.end();
                    table.setRowSelectionInterval(i, i);
                    table.setColumnSelectionInterval(startIndex / 2, (endIndex - 1) / 2);
                    table.scrollRectToVisible(table.getCellRect(i, startIndex / 2, true));
                    break;
                }
            }
        });
    }
}
