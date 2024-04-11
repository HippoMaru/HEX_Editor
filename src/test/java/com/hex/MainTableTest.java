package com.hex;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class MainTableTest extends TestCase {

    /**
     * Tests if changing nColumns calls proper changing of rows amount
     */
    public void testRowCountChangesProperly() throws IOException {
        Path tempPath = Path.of("7ebba773ded88fa94b2fdba343723f55.txt");
        try {
            Files.delete(tempPath);
        }
        catch (NoSuchFileException ignored){}
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        RandomAccessFile tempRaf;
        try {
            Files.createFile(tempPath);
            tempRaf = new RandomAccessFile("7ebba773ded88fa94b2fdba343723f55.txt", "rws");
            tempRaf.seek(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tempRaf.seek(0);
        tempRaf.write(new byte[1000]);
        MainTable testTable;
        for(int i=1; i < 50; i++){
            testTable = new MainTable(tempRaf, i);
            assertEquals(testTable.getTable().getRowCount(), Math.ceilDiv(tempRaf.length(), i));}
        try {
            tempRaf.close();
            Files.delete(tempPath);
        }
        catch (NoSuchFileException ignored){}
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
