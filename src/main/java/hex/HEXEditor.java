package hex;
import hex.gui.MainFrame;
import javax.swing.*;
import java.io.*;
import java.util.*;


public class HEXEditor {
    public static JFrame jFrame;
    public static ArrayList<ArrayList<Byte>> data;
    public static File file;
    public static int width;
    public static int height;

    public static void run(Properties properties) {
        //data loading
        data = new ArrayList<>();
        width = Integer.parseInt(properties.getProperty("application.width"));
        height = Integer.parseInt(properties.getProperty("application.height"));
        MainFrame.create();
    }
}
