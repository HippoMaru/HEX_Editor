package hex.gui;

import hex.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFrame {

    private final JFrame jFrame;
    private final ArrayList<ArrayList<Byte>> data;
    private final JTable table;
    private final JTextField searchTF;

    public MainFrame(int width, int height, String filePath, ArrayList<ArrayList<Byte>> data) {
        this.data = data;
        jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        jFrame.setBounds((dimension.width - width)/2, (dimension.height - height)/2, width, height); //mid loc
        jFrame.setTitle("HEX Editor 1.0 by HippoMaru");
        jFrame.addWindowListener(new WindowAdapter(){

            public void windowClosing(WindowEvent e){
                try {
                    Utils.updateFile(data, filePath);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        MainTable mainTable = new MainTable(data);
        table = mainTable.getTable();
        searchTF = new JTextField(30);
        JButton searchButton = createSearchButton();
        JScrollPane pane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setRowHeaderView(mainTable.getHeaderTable());
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchTF, BorderLayout.EAST);
        searchPanel.add(searchButton, BorderLayout.WEST);
        jFrame.add(searchPanel, BorderLayout.NORTH);
        jFrame.add(pane, BorderLayout.CENTER);
        JPanel controlPanel = new JPanel();
        controlPanel.add(mainTable.getShiftModePanel(), BorderLayout.NORTH);
        controlPanel.add(mainTable.getToolTipModePanel(), BorderLayout.SOUTH);
        jFrame.add(controlPanel, BorderLayout.SOUTH);
        jFrame.pack();
    }

    public JFrame getJFrame() {
        return jFrame;
    }

    private JButton createSearchButton(){
        JButton searchButton = new JButton("Search");
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
        return searchButton;
    }
}
