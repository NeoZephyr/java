package com.pain.white;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TwrTest {

    public static void main(String[] args) {
        copyFile("/Users/pain/Documents/java/java-learning/input/bigdata.txt", "/Users/pain/Documents/java/java-learning/input/copy.txt");
    }

    private static void copyFile(String src, String dest) {
        try (
                FileInputStream inputStream = new FileInputStream(src);
                FileOutputStream outputStream = new FileOutputStream(dest)
        ) {

            int content = -1;

            while ((content = inputStream.read()) != -1) {
                outputStream.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
