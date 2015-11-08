package sbd_01_sortowaniepolifazowe;

import com.sun.istack.internal.logging.Logger;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SBD_01_SortowaniePolifazowe {

    public static final int NULLS_ARE_BIGGER = -1;
    public static final int NULLS_ARE_SMALLER = 1;
    private static final Logger logger = Logger.getLogger(SBD_01_SortowaniePolifazowe.class);

    public static <T extends Comparable<T>> int compareNullable(T left, T right, int null_importance) {
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

    public static boolean dystrybuujNaturalne(List<Tape> tapes) throws IOException {
        Iterator<Integer> dest_index_iter = Stream.iterate(2, x -> 2 + (x - 2 + 1) % 2).iterator();
        Integer current_destination = dest_index_iter.next();
        long liczba_serii_ogolem = 1;

        tapes.get(0).reset();
        tapes.get(1).reset();
        tapes.get(2).rewrite();
        tapes.get(3).rewrite();

        while (tapes.stream().limit(2).anyMatch(x -> x.top() != null)) {

            int left_cmp = compareNullable(tapes.get(0).top(), tapes.get(current_destination).top(), NULLS_ARE_SMALLER) < 0 ? 1 : 0;
            int right_cmp = compareNullable(tapes.get(1).top(), tapes.get(current_destination).top(), NULLS_ARE_SMALLER) < 0 ? 1 : 0;
            int stacks_cmp = compareNullable(tapes.get(0).top(), tapes.get(1).top(), NULLS_ARE_BIGGER) < 0 ? 1 : 0;
            final int to_adv = new int[]{1, 0, 0, 0, 1, 1, 1, 0}[(left_cmp << 2) | (right_cmp << 1) | (stacks_cmp << 0)];
            TapeSentinel<String> to_put = tapes.get(to_adv).top();
            tapes.get(to_adv).retrieve();
            assert to_put != null;

            if (left_cmp != 0 && right_cmp != 0) {
                liczba_serii_ogolem++;
                current_destination = dest_index_iter.next();
            }
            tapes.get(current_destination).receive(to_put);
        }
        return liczba_serii_ogolem == 1;
    }

    public static void sortujNaturalne(Tape a1, Tape a2, Tape a3, Tape a4) throws IOException {
        List<Tape> tapes = Arrays.asList(a1, a2, a3, a4);
        List<Tape> tapes_in_original_order = new ArrayList<>(tapes);
        try {
            int ilosc_przejsc = 1;
            while (!dystrybuujNaturalne(tapes)) {
                for (Tape t : tapes) {
                    t.close();
                }
                String komunikat = String.format("--- KOLEJNA FAZA ---\n"
                        + "Liczba odczytów na każdą taśmę do tej pory: %s\n"
                        + "Liczba zapisów na każdą taśmę do tej pory: %s\n",
                        tapes_in_original_order.stream().map(x -> x.readCount()).collect(Collectors.toList()).toString(),
                        tapes_in_original_order.stream().map(x -> x.writeCount()).collect(Collectors.toList()).toString());
                logger.log(Level.INFO, komunikat);
                Collections.rotate(tapes, 2);
                ilosc_przejsc++;
            }
            logger.log(Level.INFO, String.format("Ilość przejść: %s\n", ilosc_przejsc));
        } finally {
            for (Tape t : tapes) {
                t.close();
            }
        }
    }

    public static File generateRandomFile(File f, long length) throws FileNotFoundException, IOException {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(f))) {
            Random rand = new Random();
            String lowalpha = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z";
            String highalpha = lowalpha.toUpperCase();
            String digits = "0,1,2,3,4,5,6,7,8,9";
            String[] all_letters = String.join(",", lowalpha, highalpha, digits).split(",");
            rand.ints(0, all_letters.length)
                    .mapToObj(x -> all_letters[x])
                    .limit(30 * length)
                    .forEachOrdered(x -> {
                        try {
                            os.write(x.getBytes());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
        return f;
    }

    public static class ZczytywaczSerii {

        Tape tasma;
        boolean koniec_serii = false;
        TapeSentinel<String> poprzedni;
        IOException exception;

        public ZczytywaczSerii(Tape tasma) {
            this.tasma = tasma;
        }

        IOException ioException() {
            return exception;
        }

        TapeSentinel<String> next() {
            try {
                if (koniec_serii) {
                    return null;
                }
                poprzedni = tasma.top();
                tasma.retrieve();
                if (compareNullable(poprzedni, tasma.top(), NULLS_ARE_SMALLER) > 0) {
                    koniec_serii = true;
                    return new TapeSentinel<>(1);
                }
                return tasma.top();
            } catch (IOException ex) {
                this.exception = ex;
            }
            return null;
        }

        boolean hasNext() {
            return !koniec_serii;
        }

        TapeSentinel<String> current() {
            return koniec_serii ? null : tasma.top();
        }
    }

    public static void scalSerie(List<Tape> input, Tape out) throws IOException {
        if (input.isEmpty()) {
            throw new IllegalArgumentException("there must be at least one input tape");
        }
        List<ZczytywaczSerii> in = input.stream()
                .map(ZczytywaczSerii::new)
                .collect(Collectors.toList());
        while (in.stream().anyMatch(ZczytywaczSerii::hasNext)) {
            ZczytywaczSerii which = in.stream()
                    .reduce((x, y) -> compareNullable(x.current(), y.current(), NULLS_ARE_BIGGER) < 0 ? x : y)
                    .get();
            out.receive(which.current());
            which.next();
        }
    }

    public static void poczatkowaDystrybucja(Tape in, Tape out1, Tape out2) throws IOException {
        in.reset();
        out1.rewrite();
        out2.rewrite();
        final Iterator<Tape> dest_it = Fibonacci.fibonacciAlternatorStream(out1, out2).iterator();
        Tape dest = dest_it.next();
        long before, after;
        while (in.top() != null) {
            do {
                before = dest.runCount();
                scalSerie(Arrays.asList(in), dest);
                after = dest.runCount();
            } while (before == after);
            dest = dest_it.next();
        }
        // uzupełnij dummiesami
        do {
            dest.addDummyRuns(1);
        } while (dest == dest_it.next());
    }

    public static Tape kolejneDystrybucje(Tape in1, Tape in2, Tape out) throws IOException {
        in1.reset();
        in2.reset();
        List<Tape> tapes = Arrays.asList(in1, in2, out);
        final int destination_index = 2;
        while (true) {
            System.out.println(tapes.get(0).runCount());
            System.out.println(tapes.get(1).runCount());
            System.out.println(tapes.get(2).runCount());
            System.out.println();
            tapes.get(destination_index).rewrite();
            List<Tape> inputs = tapes.subList(0, destination_index);
            do {
                scalSerie(inputs, tapes.get(destination_index));
            } while (inputs.stream().allMatch(x -> x.top() != null));
            if (inputs.stream().allMatch(x -> x.top() == null)) {
                return tapes.get(destination_index);
            }
            int finished_index = tapes.indexOf(inputs.stream().filter(x -> x.top() == null).findFirst().get());
            Collections.swap(tapes, finished_index, destination_index);
            tapes.get(finished_index).reset();
        }
    }

    public static File createFileFromKeyboardInput(File f) throws IOException {
        System.out.println("Please input lines in sequence. EOF or empty line terminates input");
        Scanner s = new Scanner(System.in);
        Tape t = new Tape(f, 8192);
        try {
            t.rewrite();
            while (s.hasNextLine()) {
                final String line = s.nextLine();
                if (line.equals("")) {
                    break;
                }
                if (line.length() <= 30) {
                    t.receive(new TapeSentinel<>(line));
                } else {
                    System.err.println("!!! Invalid input length: please input less than 30 chars - this line will be ignored");
                }
            }
        } finally {
            t.close();
        }
        return f;
    }

    public static void main(String[] args) throws Exception {
        List<String> arguments = Arrays.asList(args);
        File input_file, output_file;
        Iterator<String> argsiter = arguments.iterator();
        switch (argsiter.next()) {
            case "K":
                input_file = createFileFromKeyboardInput(new File("key.whatever"));
                output_file = new File(argsiter.next());
                break;
            case "F":
                input_file = new File(argsiter.next());
                output_file = new File(argsiter.next());
                break;
            case "R":
                input_file = generateRandomFile(new File("random.whatever"), Long.valueOf(argsiter.next()));
                output_file = new File(argsiter.next());
                break;
            default:
                return;
        }
        int bufor = argsiter.hasNext() ? Integer.valueOf(argsiter.next()) : 4096;

        final Tape a = new Tape(input_file, bufor);
        final Tape b;
        {
            File f = new File("bbb.whatever");
            f.createNewFile();
            b = new Tape(f, bufor);
        }
        final Tape c = new Tape(output_file, bufor);
        final Tape d = new Tape(new File("ddd.whatever"), bufor);
        sortujNaturalne(a, b, c, d);
    }
}
