package org.isolib;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class IsoFileInputStream extends InputStream implements Closeable {
    private IsoRandomAccessFile raf;


    public IsoFileInputStream(IsoRandomAccessFile raf) throws IOException {
        this.raf = raf;
    }

    @Override
    public int read() throws IOException {
        if(raf == null)
            throw new IOException("The file is closed");

        return raf.read();
    }

    @Override
    public void close() throws IOException {
        raf = null;
    }

}
