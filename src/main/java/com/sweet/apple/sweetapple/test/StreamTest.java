package com.sweet.apple.sweetapple.test;

import java.util.stream.Stream;

/**
 * 描述
 *
 * @author yangqifan004
 * @date 2022/3/17 11:57
 */
public class StreamTest {
    public static void main(String[] args) {
        Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .parallel()
                .reduce((a, b) -> {
                    System.out.println(String.format("%s: %d + %d = %d",
                            Thread.currentThread().getName(), a, b, a + b));
                    return a + b;
                })
                .ifPresent(System.out::println);
    }
}
