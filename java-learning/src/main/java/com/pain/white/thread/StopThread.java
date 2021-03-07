package com.pain.white.thread;

public class StopThread {

    public static void main(String[] args) throws InterruptedException {
        interruptBlockedThread();
    }

    private static void interruptBlockedThread() throws InterruptedException {
        Runnable run = () -> {
            int count = 0;

            while (!Thread.currentThread().isInterrupted() && count <= 1000) {
                count++;

                if (count % 100 == 0) {
                    try {
                        System.out.println("count: " + count);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Thread thread = new Thread(run);
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
    }
}
