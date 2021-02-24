package com.pain.white;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.primitives.Chars;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GuavaTest {
    public static void main(String[] args) throws IOException {
        copyFileTest("/Users/pain/Documents/java/java-learning/input/bigdata.txt", "/Users/pain/Documents/java/java-learning/input/guava.txt");
    }

    private static void immutableTest() {
        List<Integer> nums = new ArrayList<>();
        nums.add(1);
        nums.add(2);
        nums.add(3);

        ImmutableSet<Integer> immutableSet1 = ImmutableSet.copyOf(nums);
        ImmutableSet<Integer> immutableSet2 = ImmutableSet.of(1, 2, 3);
        ImmutableSet<Object> immutableSet3 = ImmutableSet.builder()
                .add(1)
                .addAll(Sets.newHashSet(2, 3))
                .build();
    }

    private static void multisetTest() {
        String text = "春江潮水连海平，海上明月共潮生。" +
                "潋滟随波千万里，何处春江无月明？" +
                "江流宛转绕芳甸，月照花林皆似霰。" +
                "空里流霜不觉飞，汀上白沙看不见。" +
                "江天一色无纤尘，皎皎空中孤月轮。" +
                "江畔何人初见月？江月何年初照人？" +
                "人生代代无穷已，江月年年只相似；" +
                "不知江月待何人？但见长江 送流水。" +
                "白云一片去悠悠，青枫浦上不胜愁。" +
                "谁家今夜扁舟子？何处相思明月楼？" +
                "可怜楼上月徘徊，应照离人妆镜台。" +
                "玉户帘中卷不去，捣衣砧上拂还来。" +
                "此时相望不相闻，愿逐月华流照君。" +
                "鸿雁长飞光不度，鱼龙潜跃水成纹。" +
                "昨夜闲谭梦落花，可怜春半不逞家。" +
                "江水流春去欲尽，江谭落月复西斜。" +
                "斜月沉沉藏海雾，碣石潇湘无限路。" +
                "不知乘月几人归? 落月摇情满江树。";
        Multiset<Character> multiset = HashMultiset.create();
        char[] chars = text.toCharArray();
        Chars.asList(chars).stream().forEach(c -> {
            multiset.add(c);
        });

        System.out.println(multiset.size());
        System.out.println(multiset.count('春'));
        System.out.println(multiset.count('江'));
        System.out.println(multiset.count('花'));
        System.out.println(multiset.count('月'));
        System.out.println(multiset.count('夜'));
    }

    private static void setsTest() {
        HashSet<Integer> set1 = Sets.newHashSet(1, 2, 3);
        HashSet<Integer> set2 = Sets.newHashSet(2, 3, 4);

        System.out.println(Sets.union(set1, set2));
        System.out.println(Sets.intersection(set1, set2));
        System.out.println(Sets.difference(set1, set2));
        System.out.println(Sets.symmetricDifference(set1, set2));

        System.out.println(Sets.powerSet(set1));
        System.out.println(Sets.cartesianProduct(set1, set2));

        ArrayList<Integer> list1 = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7);
        List<List<Integer>> list2 = Lists.partition(list1, 3);
        System.out.println(list2);

        List<Integer> list3 = Lists.newLinkedList();
        list3.add(1);
        list3.add(2);
        list3.add(3);
        System.out.println(Lists.reverse(list3));
    }

    private static void copyFileTest(String src, String dest) throws IOException {
        CharSource charSource = Files.asCharSource(new File(src), Charsets.UTF_8);
        CharSink charSink = Files.asCharSink(new File(dest), Charsets.UTF_8);

        charSource.copyTo(charSink);
    }
}
