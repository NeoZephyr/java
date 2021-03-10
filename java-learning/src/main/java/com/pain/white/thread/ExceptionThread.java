package com.pain.white.thread;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionThread {
    public static void main(String[] args) throws InterruptedException {
        Thread.setDefaultUncaughtExceptionHandler(new ThreadExceptionHandler());
        Runnable run = () -> {
            throw new RuntimeException("crashed");
        };
        Thread thread = new Thread(run);
        thread.start();
        thread.join();
    }

    static class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.WARNING, "catch exception: ", e);
        }
    }
}
