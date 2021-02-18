package com.pain.white;

import java.io.*;

public class FunctionalTest {
    public static void main(String[] args) throws IOException {
        handleFile(content -> {
            System.out.println("content:");
            System.out.println(content);
        });

        handleInt(num -> {
            System.out.println("number: ");
            System.out.println(num);
        });
    }

    private static void handleFile(StringConsumer consumer) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream("/Users/pain/Documents/java/java-learning/input/bigdata.txt")));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        consumer.handle(sb.toString());
    }

    private static void handleInt(GeneralConsumer generalConsumer) {
        generalConsumer.handle(12);
    }
}

@FunctionalInterface
interface StringConsumer {
    void handle(String content);
}

@FunctionalInterface
interface GeneralConsumer<T> {
    void handle(T content);
}