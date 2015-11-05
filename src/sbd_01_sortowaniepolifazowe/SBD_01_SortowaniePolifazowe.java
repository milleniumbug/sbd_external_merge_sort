package sbd_01_sortowaniepolifazowe;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SBD_01_SortowaniePolifazowe {

    public static final int NULLS_ARE_BIGGER = -1;
    public static final int NULLS_ARE_SMALLER = 1;

    public static <T extends Comparable<T>> int compare(T left, T right, int null_importance) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1 * null_importance;
        }
        if (right == null) {
            return 1 * null_importance;
        }
        int res = left.compareTo(right);
        if (res < 0) {
            return -1;
        } else if (res > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public static boolean dystrybuuj(List<Tape> tapes) throws IOException {
        Iterator<Integer> dest_index_iter = Stream.iterate(2, x -> 2+(x-2+1)%2).iterator();
        //Iterator<Integer> dest_index_iter = Fibonacci.fibonacciAlternatorStream(2, 3).iterator();
        Integer current_destination = dest_index_iter.next();
        long liczba_serii_ogolem = 1;

        tapes.get(0).reset();
        tapes.get(1).reset();
        tapes.get(0).retrieve();
        tapes.get(1).retrieve();
        tapes.get(2).rewrite();
        tapes.get(3).rewrite();

        while (tapes.stream().limit(2).anyMatch(x -> x.top() != null)) {
            System.out.println("(" + tapes.get(0).top() + " czy " + tapes.get(1).top() + ")");

            int left_cmp = compare(tapes.get(0).top(), tapes.get(current_destination).top(), NULLS_ARE_SMALLER) < 0 ? 1 : 0;
            int right_cmp = compare(tapes.get(1).top(), tapes.get(current_destination).top(), NULLS_ARE_SMALLER) < 0 ? 1 : 0;
            int stacks_cmp = compare(tapes.get(0).top(), tapes.get(1).top(), NULLS_ARE_BIGGER) < 0 ? 1 : 0;
            final int to_adv = new int[]{1, 0, 0, 0, 1, 1, 1, 0}[(left_cmp << 2) | (right_cmp << 1) | (stacks_cmp << 0)];
            String to_put = tapes.get(to_adv).top();
            tapes.get(to_adv).retrieve();
            assert to_put != null;

            if (left_cmp != 0 && right_cmp != 0) {
                System.out.println("--- NOWA SERIA ---");
                liczba_serii_ogolem++;
                current_destination = dest_index_iter.next();
            }
            tapes.get(current_destination).receive(to_put);
        }
        return liczba_serii_ogolem == 1;
    }

    public static void sortuj(Tape a1, Tape a2, Tape a3, Tape a4) throws IOException {
        List<Tape> tapes = Arrays.asList(a1, a2, a3, a4);
        int ilosc_przejsc = 1;
        while (!dystrybuuj(tapes)) {
            System.out.println("!!NOWE SORTOWANIE!!!");
            Collections.rotate(tapes, 2);
            ilosc_przejsc++;
        }
        System.out.printf("Ilość przejść: %s\n", ilosc_przejsc);
        for(Tape t : tapes) t.close();
    }

    public static void generateRandomFile(File f, long length) throws FileNotFoundException, IOException {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(f))) {
            Random rand = new Random();
            String[] alphabet = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z".split(",");
            rand.ints(0, alphabet.length)
                    .mapToObj(x -> alphabet[x])
                    .limit(30 * length)
                    .forEachOrdered(x -> {
                        try {
                            os.write(x.getBytes());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
    }

    public static <T extends Comparable<T>> void dystrybuuj(Stream<T> input1, Stream<T> input2, Consumer<T> output1, Consumer<T> output2) {
        ArrayList<Iterator<T>> input_iters = new ArrayList<>(2);
        input_iters.add(input1.iterator());
        input_iters.add(input2.iterator());

        ArrayList<Consumer<T>> output_iters = new ArrayList<>(2);
        output_iters.add(output1);
        output_iters.add(output2);

        ArrayList<T> input_items = new ArrayList<>(2);
        input_items.add(input_iters.get(0).hasNext() ? input_iters.get(0).next() : null);
        input_items.add(input_iters.get(1).hasNext() ? input_iters.get(1).next() : null);

        ArrayList<T> output_items = new ArrayList<>(2);
        output_items.add(null);
        output_items.add(null);

        Iterator<Integer> dest_index_iter = Fibonacci.fibonacciAlternatorStream(0, 1).iterator();
        Integer current_destination = dest_index_iter.next();
        while (input_items.stream().anyMatch(x -> x != null)) {
            //System.out.println("(" + input_items.get(0) + " czy " + input_items.get(1) + ")");

            int left_cmp = compare(input_items.get(0), output_items.get(current_destination), NULLS_ARE_SMALLER);
            int right_cmp = compare(input_items.get(1), output_items.get(current_destination), NULLS_ARE_SMALLER);
            int stacks_cmp = compare(input_items.get(0), input_items.get(1), NULLS_ARE_BIGGER);
            int to_adv = new int[]{1, 0, 0, 0, 1, 1, 1, 0}[(left_cmp < 0 ? 1 : 0) * 4 + (right_cmp < 0 ? 1 : 0) * 2 + (stacks_cmp < 0 ? 1 : 0) * 1];
            T to_put = input_items.get(to_adv);
            input_items.set(to_adv, input_iters.get(to_adv).hasNext() ? input_iters.get(to_adv).next() : null);
            assert to_put != null;

            if (left_cmp < 0 && right_cmp < 0) {
                //System.out.println("--- NOWA SERIA ---");
                current_destination = dest_index_iter.next();
            }
            output_iters.get(current_destination).accept(to_put);
            output_items.set(current_destination, to_put);
        }
    }

    public static void main(String[] args) throws Exception {
        //dystrybuuj(Stream.of(1, 9, 3), Stream.of(4, 5, 6, 7, 2), (x) -> { System.out.println(x); }, (x) -> { System.out.println("          "+x); });
        int bufor = 4096;
        generateRandomFile(new File("abcdef.whatever"), 5000);
        Tape a = new Tape(new File("aaa.whatever"), bufor);
        File f = new File("bbb.whatever");
        f.createNewFile();
        sortuj(a, new Tape(f, bufor), new Tape(new File("ccc.whatever"), bufor), new Tape(new File("ddd.whatever"), bufor));
        /*System.out.println("aaaaaa");
         dystrybuuj(new Random().ints(-100, 100).map(x -> x*10+0).limit(75).boxed(),
         IntStream.empty().boxed(),
         (x) -> { System.out.println(x); },
         (x) -> { System.out.println("          "+x); });
         assert compare(null, null, NULLS_ARE_BIGGER) == 0;
         assert compare(null, "aaaa", NULLS_ARE_BIGGER) > 0;
         assert compare(null, "aaaa", NULLS_ARE_SMALLER) < 0;
         assert compare("aaaa", null, NULLS_ARE_BIGGER) < 0;
         assert compare("aaaa", null, NULLS_ARE_SMALLER) > 0;
         assert compare("aaaa", "bbbb", NULLS_ARE_BIGGER) == "aaaa".compareTo("bbbb");
         CountedInputStream cis = new CountedInputStream(new FileInputStream(new File("asdf")));
         InputStream is = new BufferedInputStream(cis);
         byte[] buffer = new byte[30];
         for(int i = 0; i < 50; ++i)
         {
         int read = is.read(buffer, 0, buffer.length);
         assert read == buffer.length;
         System.out.println(Arrays.toString(buffer));
         }
         System.out.println(cis.readCount());*/
    }
}
