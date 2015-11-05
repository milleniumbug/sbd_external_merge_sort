/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbd_01_sortowaniepolifazowe;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountedInputStream extends FilterInputStream
{
    private long read_count;
    
    public CountedInputStream(InputStream is)
    {
        super(is);
        this.read_count = 0;
    }
    
    public long readCount()
    {
        return read_count;
    }
    
    @Override
    public int read() throws IOException {
        read_count++;
        return super.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        read_count++;
        return super.read(b, off, len);
    }
}
