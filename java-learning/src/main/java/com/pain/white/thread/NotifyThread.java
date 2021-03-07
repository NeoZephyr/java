package com.pain.white.thread;

public class NotifyThread {
    private static final Object resource = new Object();

    public static void main(String[] args) throws InterruptedException {
        notifyThread();
    }

    private static void notifyThread() throws InterruptedException {
        Runnable run = () -> {
            synchronized (resource) {
                System.out.printf("Thread %s get lock\n", Thread.currentThread().getName());
                try {
                    System.out.printf("Thread %s wait resource begin\n", Thread.currentThread().getName());
                    resource.wait();
                    System.out.printf("Thread %s wait resource end\n", Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        Thread thread1 = new Thread(run);
        Thread thread2 = new Thread(run);
        Thread thread3 = new Thread(() -> {
            synchronized (resource) {
                System.out.printf("Thread %s get lock\n", Thread.currentThread().getName());
                System.out.printf("Thread %s notify begin\n", Thread.currentThread().getName());
                // resource.notifyAll();
                resource.notify();
                System.out.printf("Thread %s notify end\n", Thread.currentThread().getName());
            }
        });
        thread1.start();
        thread2.start();

        Thread.sleep(100);
        thread3.start();
    }
}
