package org.isolib;

import java.io.*;

/**
 * Allows you to access a stored file in an ISO as a random access file.
 * Reading is all that is supported currently
 */

public class IsoRandomAccessFile implements DataInput, Closeable {
    private final long begining;
    private long length;

    private RandomAccessFile iso;

    private IsoRandomAccessFile(Iso iso, long offset, long len) throws IOException {
        begining = offset;
        length = len;

        this.iso = iso.getRAF();

        this.iso.seek(begining);
    }

    public IsoRandomAccessFile(Iso iso, IsoFile isoFile) throws IOException {
        this(iso, isoFile.getDataOffset(), isoFile.getLength());
    }

    public long getFilePointer() throws IOException {
        ensureOpen();

        return iso.getFilePointer() - begining;
    }

    public void seek(long position) throws IOException {
        ensureOpen();

        if(position < 0 || position >= length)
            throw new IndexOutOfBoundsException("Argument position is out of bounds");

        iso.seek(begining + position);
    }

    public int read() throws IOException {
        ensureOpen();

        int c = -1;
        if(getFilePointer() + 1 <= length)
            c = (int)iso.read();

        return c;
    }

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

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        int n = 0;
        do {
            int count = read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        } while (n < len);
    }

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

    public boolean readBoolean() throws IOException {
        int ch = read();

        if(ch < 0)
            throw new EOFException();

        return (ch != 0);
    }

    public byte readByte() throws IOException {
        int ch = read();

        if(ch < 0)
            throw new EOFException();

        return (byte)ch;
    }

    public int readUnsignedByte() throws IOException {
        int ch = read();

        if(ch < 0)
            throw new EOFException();

        return ch;
    }

    public short readShort() throws IOException {
        int ch1 = read();
        int ch2 = read();

        if((ch1 | ch2) < 0)
            throw new EOFException();

        return (short)((ch1 << 8) + (ch2 << 0));
    }

    public int readUnsignedShort() throws IOException {
        int ch1 = read();
        int ch2 = read();

        if((ch1 | ch2) < 0)
            throw new EOFException();

        return (ch1 << 8) + (ch2 << 0);
    }

    public char readChar() throws IOException {
        int ch1 = read();
        int ch2 = read();

        if((ch1 | ch2) < 0)
            throw new EOFException();

        return (char)((ch1 << 8) + (ch2 << 0));
    }

    public int readInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();

        if((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public long readLong() throws IOException {
        return ((long)readInt() << 32) + (readInt() & 0xFFFFFFFFL);
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public String readLine() throws IOException {
        StringBuilder in = new StringBuilder();
        int c = -1;
        boolean eol = false;

        while(!eol) {
            switch (c = read()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long cur = getFilePointer();
                    if(read() != '\n')
                        seek(cur);

                    break;
                default:
                    in.append((char)c);
                    break;
            }
        }

        if ((c == -1) && (in.length() == 0)) {
            return null;
        }
        return in.toString();
    }

    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    public void close() throws IOException {
        iso = null;
    }

    private void ensureOpen() throws IOException {
        if(iso == null)
            throw new IOException("Iso is closed");
    }
}
