package hex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Utils {
    public static void updateFile(ArrayList<ArrayList<Byte>> data,
                                  String filePath) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        File file = new File(Objects.requireNonNull(classloader.getResource(filePath)).getFile());
        try (FileWriter fileWriter = new FileWriter(file)) {
            for (ArrayList<Byte> byteLine : data){
                for (byte b : byteLine) fileWriter.write(b);
            }
            fileWriter.flush();
        }
    }

    public static void updateOne(Byte b, int row, int col, ArrayList<ArrayList<Byte>> data) {
        while (data.get(row).size() <= col){
            data.get(row).add((byte) 0);
        }
        data.get(row).set(col, b);
    }

    public static void updateMany(byte[] byteArray, int iStart, int jStart, int iEnd, int jEnd, ArrayList<ArrayList<Byte>> data) {
        int byteArrayIndex = 0;
        for(int i=iStart;i<=iEnd; i++){
            if(i >= data.size()) break;
            for(int j=jStart; j<=jEnd; j++){
                if(byteArrayIndex == byteArray.length || j >= data.get(i).size()) break;
                data.get(i).set(j, byteArray[byteArrayIndex++]);
            }
        }
    }
    public static void deleteOne(int i, int j, ArrayList<ArrayList<Byte>> data) throws IOException {
        updateOne((byte) 0, i, j, data);
    }
    public static void deleteMany(int iStart, int jStart, int iEnd, int jEnd, ArrayList<ArrayList<Byte>> data) {
        byte [] byteArray = new byte[(iEnd - iStart + 1) * (jEnd - jStart + 1)];
        updateMany(byteArray, iStart, jStart, iEnd, jEnd, data);
    }

    public static void insertOne(Byte b, int i, int j, ArrayList<ArrayList<Byte>> data) throws IOException {
        if (data.get(i).size() <= j) deleteMany(data.get(i).size(), j, i, j, data);
        data.get(i).add(j, b);
    }

}
