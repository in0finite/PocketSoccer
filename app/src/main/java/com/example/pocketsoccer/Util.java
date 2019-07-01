package com.example.pocketsoccer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Util {

    public static int randomInt(int min, int max) {
        return min + (int) Math.round( Math.random() * (max - min) );
    }

    public static byte[] readFile(File file) throws IOException {
        byte[] data = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(data);
        fis.close();
        return data;
    }

}
