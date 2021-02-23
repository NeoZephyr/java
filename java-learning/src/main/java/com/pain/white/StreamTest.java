package com.pain.white;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamTest {

    private static List<Student> students = Arrays.asList(
            new Student("jack", Gender.MALE, 80.0),
            new Student("pain", Gender.MALE, 98.0),
            new Student("nancy", Gender.FEMALE, 78.0),
            new Student("taylor", Gender.FEMALE, 88.0),
            new Student("punk", Gender.MALE, 92.0),
            new Student("peter", Gender.MALE, 69.0)
    );

    public static void main(String[] args) throws IOException {
        AtomicReference<Double> score = new AtomicReference<>(0.0);

        // 中间操作，状态操作

        List<String> names = students
                .stream()
                .peek(stu -> System.out.println(stu))
                .filter(stu -> stu.gender != Gender.FEMALE)
                .sorted(Comparator.comparing(Student::getScore).reversed())
                .limit(2)
                .peek(stu -> score.set(score.get() + stu.getScore()))
                .map(Student::getName)
                .collect(Collectors.toList());

        System.out.println(names);
        System.out.println(score);

        // 短路操作
        boolean allMatch = students.stream().peek(System.out::println).allMatch(stu -> stu.getScore() > 90);

        System.out.println(allMatch);

        boolean anyMatch = students.stream().peek(System.out::println).anyMatch(stu -> stu.getScore() > 90);

        System.out.println(anyMatch);

        Optional<Student> optStudent = students.stream().findFirst();

        System.out.println(optStudent.get());

        OptionalDouble optMax = students.stream().mapToDouble(Student::getScore).max();

        System.out.println(optMax.getAsDouble());

        buildStream();
    }

    private static void buildStream() throws IOException {
        Stream<Integer> stream = Stream.of(100, 200, 300, 400);
        stream.forEach(System.out::println);

        int[] numbers = {100, 200, 300, 400};
        IntStream intStream = Arrays.stream(numbers);
        intStream.forEach(System.out::println);

        Stream<String> stringStream = Files.lines(Paths.get("/Users/pain/Documents/java/java-learning/input/bigdata.txt"));
        stringStream.forEach(System.out::println);

        Stream<Integer> seqStream = Stream.iterate(100, n -> n * 2);
        seqStream.limit(5).forEach(System.out::println);

        Stream<Double> randomStream = Stream.generate(Math::random);
        randomStream.limit(5).forEach(System.out::println);
    }
}

enum Gender {
    MALE,
    FEMALE
}

class Student {
    String name;
    Gender gender;
    Double score;

    public Student(String name, Gender gender, Double score) {
        this.name = name;
        this.gender = gender;
        this.score = score;
    }

    public Double getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", gender=" + gender +
                ", score=" + score +
                '}';
    }
}
