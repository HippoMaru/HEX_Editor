package hex;

import hex.gui.MainFrame;
import hex.gui.MainTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HEXEditor {

    private final String filePath;
    private final JFrame jFrame;
    private final ArrayList<ArrayList<Byte>> data;

    public HEXEditor(Properties properties) throws IOException {
        //data loading
        this.filePath = properties.getProperty("data.filepath");
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        data = new ArrayList<>();
        try (BufferedInputStream is =
                     (BufferedInputStream) classloader.getResourceAsStream(filePath)) {
            byte i;
            int j = 0;
            data.add(new ArrayList<>());
            while (true) {
                assert is != null;
                if ((i = (byte) is.read()) == -1) break; //checks if EOF
                data.get(j).add(i);
                if (i == 10) { //checks if line break
                    data.add(new ArrayList<>());
                    j++;
                }
            }
        }

        int width = Integer.parseInt(properties.getProperty("application.width"));
        int height = Integer.parseInt(properties.getProperty("application.height"));
        MainFrame mainFrame = new MainFrame(width, height, filePath, data);
        jFrame = mainFrame.getJFrame();

    }

    public void run() {
        jFrame.setVisible(true);
    }
}
