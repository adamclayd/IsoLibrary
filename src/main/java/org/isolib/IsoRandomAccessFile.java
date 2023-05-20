package org.isolib;

import java.io.*;

/**
 * Allows you to access a stored file in an ISO as a random access file.
 * Reading is all that is supported currently
 */

public class IsoRandomAccessFile extends RandomAccessFile {
    private final long begining;
    private long length;

    /**
     * Opens the file in the given ISO for reading
     * @param isoPath Path to the iso file
     * @param offset Offset to the data of the file in the iso
     * @param len The size of the file in the iso
     * @throws IOException
     */
    public IsoRandomAccessFile(String isoPath, long offset, long len) throws IOException {
        super(isoPath, "r");

        begining = offset;
        length = len;

        super.seek(begining);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFilePointer() throws IOException {
        return super.getFilePointer() - begining;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seek(long position) throws IOException {
        if(position < 0 || position >= length)
            throw new IndexOutOfBoundsException("Argument position is out of bounds");

        super.seek(begining + position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        int c = -1;
        if(getFilePointer() + 1 <= length)
            c = (int)super.read();

        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException {
        int c = 0;
        if(getFilePointer() + b.length <= length) {
            c = read(b);
        }
        else {
            c = -1;
            for(int i = 0; i < (int)(length - getFilePointer()); i++)
                b[i] = (byte)read();

        }

        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if(off < 0 || off >= b.length)
            throw new IndexOutOfBoundsException("Argument off is outside the bounds of argument b");
        else if(len < 0)
            throw new IllegalArgumentException("Argument len cannot be less than 0");

        int c = 0;

        if(getFilePointer() + len <= length && off + len <= b.length) {
            c = read(b, off, len);
        }
        else {
            byte bb = (byte)read();
            c++;
            for(int i = off; bb >= 0 && i < b.length; i++, c++) {
                b[i] = bb;
                bb = (byte)read();
            }

            if(bb < 0)
                c = -1;
        }

        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int skipBytes(int n) throws IOException {
        if(n <= 0)
            return 0;

        long pos = getFilePointer();
        long newpos = pos + n;

        if(newpos > length)
            newpos = length;

        seek(newpos);

        return (int)(newpos - pos);
    }

    /**
     * Unimplemented method write
     */
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Unimplement method write
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        throw new IOException("Iso file no opened for writing");
    }
}
