/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbd_01_sortowaniepolifazowe;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author milleniumbug
 */
public class CountedOutputStream extends FilterOutputStream {

    private long write_count;
    
    @Override
    public void write(int i) throws IOException {
        write_count++;
        out.write(i);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        write_count++;
        out.write(b, off, len);
    }

    public CountedOutputStream(OutputStream os) {
        super(os);
        this.write_count = 0;
    }
    
    public long writeCount()
    {
        return write_count;
    }
}
