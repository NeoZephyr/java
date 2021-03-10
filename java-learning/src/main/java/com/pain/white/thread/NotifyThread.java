package com.pain.white.thread;

import java.util.LinkedList;

public class NotifyThread {
    private static final Object resource = new Object();

    public static void main(String[] args) throws InterruptedException {
        // notifyThread();
        productAndConsume();
    }

    // 使用 synchronized, wait, notify 交替打印 1 - 100

    private static void notifyThread() throws InterruptedException {
        Runnable run = () -> {
            // 1. 线程进入 entry set 尝试获取锁
            synchronized (resource) {
                // 2. 线程进入获取锁
                System.out.printf("Thread %s get lock\n", Thread.currentThread().getName());
                try {
                    System.out.printf("Thread %s wait resource begin\n", Thread.currentThread().getName());

                    // 3. 线程释放锁，进入 wait set
                    // 线程需要获取 resource monitor 才能执行 wait
                    resource.wait();

                    // 4. 尝试获取锁
                    System.out.printf("Thread %s wait resource end\n", Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }

            // 5. 释放锁
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

    private static void productAndConsume() throws InterruptedException {
        Queue<String> queue = new Queue<>(5);
        Thread consumer = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(10000);
                    String data = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });
        Thread producer = new Thread(() -> {
            int count = 0;
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(10);
                    count++;
                    queue.put(String.valueOf(count));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });

        consumer.start();
        producer.start();
        Thread.sleep(1000);
        producer.interrupt();
        consumer.interrupt();
    }

    static class Queue<T> {
        private int capacity;
        private LinkedList<T> dataList;

        public Queue(int capacity) {
            this.capacity = capacity;
            dataList = new LinkedList<>();
        }

        public void put(T data) throws InterruptedException {
            synchronized (this) {
                while (dataList.size() >= capacity) {
                    // wait 被中断后，会尝试获取锁，然后继续向下运行 -> 抛出中断异常，情况中断状态
                    wait();
                }

                dataList.add(data);
                System.out.printf("[producer], put: %s, size: %d\n", data, dataList.size());
                notify();
            }
        }

        public T take() throws InterruptedException {
            T data;

            synchronized (this) {
                while (dataList.size() <= 0) {
                    wait();
                }

                data = dataList.poll();
                System.out.printf("[consumer], take: %s, size: %d\n", data, dataList.size());
                notify();
            }

            return data;
        }
    }
}
