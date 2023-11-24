package hex;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import static hex.HEXEditor.data;
import static hex.HEXEditor.file;

public class Utils {

    public static void loadData() throws IOException {
        String strEndl = System.getProperty("line.separator");
        byte[] bEndl = strEndl.getBytes();
        data = new ArrayList<>();
        byte[] array = Files.readAllBytes(file.toPath());
        int j = 0;
        data.add(new ArrayList<>());
        for (byte i: array) {
            data.get(j).add(i);
            if (i == bEndl[bEndl.length - 1]) {
                data.add(new ArrayList<>());
                j++;
            }
        }
    }
    public static void updateFile() throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (ArrayList<Byte> byteLine : data){
                for (byte b : byteLine) fileWriter.write(b);
            }
            fileWriter.flush();
        }
    }

    public static void updateOne(Byte b, int row, int col) {
        while (data.get(row).size() <= col){
            data.get(row).add((byte) 0);
        }
        data.get(row).set(col, b);
    }

    public static void updateMany(byte[] byteArray, int iStart, int jStart, int iEnd, int jEnd) {
        int byteArrayIndex = 0;
        for(int i=iStart;i<=iEnd; i++){
            if(i >= data.size()) break;
            for(int j=jStart; j<=jEnd; j++){
                if(byteArrayIndex == byteArray.length || j >= data.get(i).size()) break;
                data.get(i).set(j, byteArray[byteArrayIndex++]);
            }
        }
    }
    public static void deleteOne(int i, int j) throws IOException {
        updateOne((byte) 0, i, j);
    }
    public static void deleteMany(int iStart, int jStart, int iEnd, int jEnd) {
        byte [] byteArray = new byte[(iEnd - iStart + 1) * (jEnd - jStart + 1)];
        updateMany(byteArray, iStart, jStart, iEnd, jEnd);
    }

    public static void insertOne(Byte b, int i, int j) throws IOException {
        if (data.get(i).size() <= j) deleteMany(data.get(i).size(), j, i, j);
        data.get(i).add(j, b);
    }

}
