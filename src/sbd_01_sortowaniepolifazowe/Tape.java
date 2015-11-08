/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbd_01_sortowaniepolifazowe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import static sbd_01_sortowaniepolifazowe.SBD_01_SortowaniePolifazowe.NULLS_ARE_BIGGER;
import static sbd_01_sortowaniepolifazowe.SBD_01_SortowaniePolifazowe.compareNullable;

public class Tape implements AutoCloseable {

    private long dummy_runs = 0;
    private long normal_runs = 0;
    private TapeSentinel<String> top_element = null;
    private final File file;
    private CountedInputStream cin = null;
    private CountedOutputStream cout = null;
    private InputStream in = null;
    private OutputStream out = null;
    private long total_reads = 0;
    private long total_writes = 0;
    private final int buffer_size;

    public TapeSentinel<String> top() {
        return top_element;
    }

    public long writeCount() {
        return total_writes;
    }

    public long readCount() {
        return total_reads;
    }

    public long runCount() {
        return normal_runs + dummy_runs;
    }

    public Tape(File file, int buffer_size) {
        this.file = Objects.requireNonNull(file);
        this.buffer_size = buffer_size;
    }

    public void addDummyRuns(long count) {
        out = Objects.requireNonNull(out);
        dummy_runs += count;
    }

    public void receive(TapeSentinel<String> element) throws IOException {
        out = Objects.requireNonNull(out);
        element = Objects.requireNonNull(element);
        if (!element.isSentinel()) {
            byte[] bytes = element.getValue().getBytes(Charset.forName("ISO-8859-2"));
            out.write(Arrays.copyOf(bytes, 30));
            if (compareNullable(top_element, element, NULLS_ARE_BIGGER) > 0) {
                normal_runs++;
            }
            top_element = element;
        }
    }

    public TapeSentinel<String> retrieve() throws IOException {
        in = Objects.requireNonNull(in);
        if (dummy_runs == 0) {
            byte[] bytes = new byte[30];
            int count = in.read(bytes);
            assert count == 30 || count == -1;
            TapeSentinel<String> element = count != -1
                    ? new TapeSentinel<>(new String(bytes, Charset.forName("ISO-8859-2")))
                    : null;
            if (compareNullable(top_element, element, NULLS_ARE_BIGGER) > 0) {
                normal_runs++;
            }
            top_element = element;
        } else {
            top_element = new TapeSentinel<>(dummy_runs);
            --dummy_runs;
        }
        return top_element;
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
            total_reads += cin.readCount();
            cin = null;
        }
        if (out != null) {
            out.close();
            total_writes += cout.writeCount();
            cout = null;
        }
        in = null;
        out = null;
    }

    public void rewrite() throws IOException {
        close();
        dummy_runs = 0;
        normal_runs = 0;
        cout = new CountedOutputStream(new FileOutputStream(file));
        if (buffer_size != 0) {
            out = new BufferedOutputStream(cout, buffer_size);
        }
        else {
            out = cout;
        }
    }

    public void reset() throws IOException {
        close();
        cin = new CountedInputStream(new FileInputStream(file));
        if (buffer_size != 0) {
            in = new BufferedInputStream(cin, buffer_size);
        } else {
            in = cin;
        }
        this.retrieve();
    }

    public static void main(String[] args) {
        try {
            File f = new File("test1.whatever");
            try (Tape a = new Tape(f, 8192)) {
                a.rewrite();
                a.receive(new TapeSentinel<>("zzz"));
                a.receive(new TapeSentinel<>("yyy"));
                a.receive(new TapeSentinel<>("xxx"));
                a.receive(new TapeSentinel<>("www"));
                System.out.println(a.normal_runs == 4);
            }
            try (Tape a = new Tape(f, 8192)) {
                a.reset();
                for (int i = 0; i < 5; ++i) {
                    a.retrieve();
                }
                System.out.println(a.normal_runs == 4);
            }
            try (Tape a = new Tape(f, 8192)) {
                a.rewrite();
                a.receive(new TapeSentinel<>("zzz"));
                a.receive(new TapeSentinel<>("yyy"));
                a.receive(new TapeSentinel<>("xxx"));
                a.receive(new TapeSentinel<>("www"));
                a.addDummyRuns(5);
                System.out.println(a.normal_runs == 4);
                a.reset();
                for (TapeSentinel<String> s = a.retrieve(); s != null; s = a.retrieve()) {
                    System.out.println(s);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Tape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
