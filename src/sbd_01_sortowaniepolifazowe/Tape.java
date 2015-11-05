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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import static sbd_01_sortowaniepolifazowe.SBD_01_SortowaniePolifazowe.NULLS_ARE_BIGGER;
import static sbd_01_sortowaniepolifazowe.SBD_01_SortowaniePolifazowe.NULLS_ARE_SMALLER;
import static sbd_01_sortowaniepolifazowe.SBD_01_SortowaniePolifazowe.compare;

public class Tape implements AutoCloseable {

    private int dummy_runs = 0;
    private int normal_runs = 0;
    private String top_element = null;
    private final File file;
    private CountedInputStream in = null;
    private CountedOutputStream out = null;
    private long total_reads = 0;
    private long total_writes = 0;
    private final int buffer_size;

    public String top() {
        return top_element;
    }

    public long writeCount() {
        return total_writes;
    }

    public long readCount() {
        return total_reads;
    }

    public Tape(File file, int buffer_size) {
        this.file = Objects.requireNonNull(file);
        this.buffer_size = buffer_size;
    }

    public void receive(String element) throws IOException {
        out = Objects.requireNonNull(out);
        byte[] bytes = element.getBytes(Charset.forName("ISO-8859-2"));
        out.write(Arrays.copyOf(bytes, 30));
        if (compare(top_element, element, NULLS_ARE_SMALLER) > 0) {
            normal_runs++;
        }
        top_element = element;
    }

    public String retrieve() throws IOException {
        in = Objects.requireNonNull(in);
        byte[] bytes = new byte[30];
        int count = in.read(bytes);
        assert count == 30 || count == -1;
        String element = count != -1 ? new String(bytes, Charset.forName("ISO-8859-2")) : null;
        if (compare(top_element, element, NULLS_ARE_BIGGER) > 0) {
            normal_runs++;
        }
        top_element = element;
        return top_element;
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
            total_reads += in.readCount();
        }
        if (out != null) {
            out.close();
            total_writes += out.writeCount();
        }
        in = null;
        out = null;
    }

    public void rewrite() throws IOException {
        close();
        dummy_runs = 0;
        normal_runs = 0;
        OutputStream os = new FileOutputStream(file);
        if (buffer_size != 0) {
            os = new BufferedOutputStream(os, buffer_size);
        }
        out = new CountedOutputStream(os);
    }

    public void reset() throws IOException {
        close();
        InputStream is = new FileInputStream(file);
        if (buffer_size != 0) {
            is = new BufferedInputStream(is, buffer_size);
        }
        in = new CountedInputStream(is);
    }
}
