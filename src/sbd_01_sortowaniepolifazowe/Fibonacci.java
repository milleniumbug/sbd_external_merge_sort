package sbd_01_sortowaniepolifazowe;

import java.util.PrimitiveIterator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Fibonacci {

    public static LongStream fibonacciStream() {
        long[] fibo = new long[]{1, 0};
        int[] which = new int[]{0};
        return LongStream.generate(() -> {
            int prev = which[0];
            which[0] = (which[0] + 1) % 2;
            int next = which[0];
            fibo[next] = fibo[prev] + fibo[next];
            return fibo[prev];
        });
    }

    public static <T> Stream<T> fibonacciAlternatorStream(T left, T right) {
        class IndexFibo {

            long fibo;
            int index;

            public IndexFibo(long fibo, int index) {
                this.fibo = fibo;
                this.index = index;
            }
        }
        Object elements[] = new Object[]{left, right};
        PrimitiveIterator.OfInt indices = IntStream.iterate(0, x -> x + 1).iterator();
        return LongStream.concat(LongStream.of(1), fibonacciStream())
                .mapToObj(x -> new IndexFibo(x, indices.next()))
                .flatMap(x -> Stream.generate(() -> x.index).limit(x.fibo))
                .map(x -> (T) elements[x % 2]);
    }

    public static void main(String[] args) {
        fibonacciStream().limit(10).forEachOrdered(System.out::println);
        System.out.println();
        for (Object s : fibonacciAlternatorStream("0", "    1").limit(70).toArray()) {
            System.out.println(s);
        }
    }
}
