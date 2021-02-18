package com.pain.white;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;

public class LambdaTest {
    public static void main(String[] args) {
        Runnable lambda1 = () -> System.out.println("hello");
        Consumer<String> lambda2 = name -> System.out.println("hello, " + name);
        Runnable lambda3 = () -> {
            System.out.println("hello, world");
            System.out.println("hello, java");
        };
        BinaryOperator<Long> lambda4 = (x, y) -> x + y;
        lambda1.run();
        lambda2.accept("jack");
        lambda3.run();
        System.out.println(lambda4.apply(1L, 2L));;
    }
}
