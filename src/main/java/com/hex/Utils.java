package com.hex;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class Utils {

    public static void loadData(HEXEditor hexEditor) throws IOException {
//        ArrayList<ArrayList<Byte>> data = hexEditor.getData();
//        File file = hexEditor.getFile();
//        data = new ArrayList<>();
//        int nColumns = hexEditor.getnColumns();
//        byte[] array = Files.readAllBytes(file.toPath());
//        int j = 0;
//        data.add(new ArrayList<>());
//        for (int i = 0; i < array.length; i++) {
//            data.get(j).add(array[i]);
//            if ((i + 1) % nColumns == 0) {
//                data.add(new ArrayList<>());
//                j++;
//            }
//        }
//        hexEditor.setData(data);
    }
    public static void updateFile(HEXEditor hexEditor) throws IOException {
//        ArrayList<ArrayList<Byte>> data = hexEditor.getData();
//        File file = hexEditor.getFile();
//        try (FileWriter fileWriter = new FileWriter(file)) {
//            for (ArrayList<Byte> byteLine : data){
//                for (byte b : byteLine) fileWriter.write(b);
//            }
//            fileWriter.flush();
//        }
//        catch (NullPointerException ignored){}
//        hexEditor.setFile(file);
    }

    public static void updateOne(HEXEditor hexEditor, Byte b, int row, int col) {
//        ArrayList<ArrayList<Byte>> data = hexEditor.getData();
//        while (data.get(row).size() <= col){
//            data.get(row).add((byte) 0);
//        }
//        data.get(row).set(col, b);
//        hexEditor.setData(data);
    }
    public static void updateMany(HEXEditor hexEditor, byte[] byteArray, int iStart, int jStart, int iEnd, int jEnd) {
//        ArrayList<ArrayList<Byte>> data = hexEditor.getData();
//        int byteArrayIndex = 0;
//        for(int i=iStart;i<=iEnd; i++){
//            if(i >= data.size()) break;
//            for(int j=jStart; j<=jEnd; j++){
//                if(byteArrayIndex == byteArray.length || j >= data.get(i).size()) break;
//                data.get(i).set(j, byteArray[byteArrayIndex++]);
//            }
//        }
//        hexEditor.setData(data);
    }
    public static void deleteOne(HEXEditor hexEditor, int i, int j) throws IOException {
        updateOne(hexEditor, (byte) 0, i, j);
    }
    public static void deleteMany(HEXEditor hexEditor, int iStart, int jStart, int iEnd, int jEnd) {
        byte [] byteArray = new byte[(iEnd - iStart + 1) * (jEnd - jStart + 1)];
        updateMany(hexEditor, byteArray, iStart, jStart, iEnd, jEnd);
    }

    public static void insertOne(HEXEditor hexEditor, Byte b, int i, int j) throws IOException {
//        ArrayList<ArrayList<Byte>> data = hexEditor.getData();
//        if (data.get(i).size() <= j) deleteMany(hexEditor, data.get(i).size(), j, i, j);
//        data.get(i).add(j, b);
//        hexEditor.setData(data);
    }
}
