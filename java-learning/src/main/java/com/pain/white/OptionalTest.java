package com.pain.white;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class OptionalTest {

    public static void main(String[] args) {
        Optional<Object> emptyOption = Optional.empty();
        Optional<String> stringOption = Optional.of("hello");
        Optional<Object> nullOption = Optional.ofNullable(null);

        emptyOption.ifPresent(System.out::println);
        stringOption.ifPresent(System.out::println);
        nullOption.ifPresent(System.out::println);

        System.out.println(nullOption.orElse("null option"));
        System.out.println(nullOption.orElseGet(() -> {
            return "null option";
        }));

        List nullList = null;

        Optional
                .ofNullable(nullList)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .forEach(System.out::println);
    }
}
