package hex;

import hex.gui.MainFrame;
import hex.gui.MainTable;
import jdk.jshell.execution.Util;

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
    public static JFrame jFrame;
    public static ArrayList<ArrayList<Byte>> data;
    public static File file;
    public static int width;
    public static int height;

    public static void run(Properties properties) throws IOException {
        //data loading
        data = new ArrayList<>();
        width = Integer.parseInt(properties.getProperty("application.width"));
        height = Integer.parseInt(properties.getProperty("application.height"));
        MainFrame.create();
    }
}
