package org.isolib;

import java.io.IOException;
import java.io.OutputStream;

class RandomAccessBytes {
    byte bytes[];
    int position;

    public RandomAccessBytes(int byteSize) {
        bytes = new byte[byteSize];
        position = 0;
    }

    public RandomAccessBytes(byte[] b) {
        bytes = b;
        position = 0;
    }

    public void skipBytes(int n) {
        if(position + n < 0)
            position = 0;
        else if(position + n > bytes.length)
            position = bytes.length;
        else
            position += n;
    }

    public void seek(int n) {
        if(n < 0)
            position = 0;
        else if(n > bytes.length)
            position = bytes.length;
        else
            position = n;
    }

    public int read() {
        int c = -1;
        if(position < bytes.length) {
            c = bytes[position];
            position++;
        }

        return c;
    }

    public byte readByte() {
        int ch = read();

        if(ch == -1)
            throw new IndexOutOfBoundsException();

        return (byte)ch;
    }

    public void read(byte[] b) {
        for(int i = 0; i < b.length; i++) {
            int ch = read();

            if(ch == -1)
                throw new IndexOutOfBoundsException();

            b[i] = (byte)ch;
        }
    }

    public int readInt() {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();

        if((ch1 | ch2 | ch3 | ch4) == -1)
            throw new IndexOutOfBoundsException();

        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
    }

    public short readShort() {
        int ch1 = read();
        int ch2 = read();

        if((ch1 | ch2) == -1)
            throw new IndexOutOfBoundsException();

        return (short)((ch1 << 8) + (ch2 << 0));
    }

    public void write(byte b) {
        if(position  >= bytes.length)
            throw new IndexOutOfBoundsException();

        bytes[position] = b;
        position++;
    }

    public void write(byte[] b) {
        for(int i = 0; i < b.length;  i++)
            write(b[i]);
    }

    public void write(int i) {
        byte[] b = new byte[4];

        b[0] = (byte)(i >> 24);
        b[1] = (byte)((i << 8) >> 24);
        b[2] = (byte)((i << 16) >> 24);
        b[3] = (byte)((i << 24) >> 24);

        write(b);
    }

    public void write(short s) {
        byte[] b = new byte[2];

        b[0] = (byte)(s >> 8);
        b[1] = (byte)((s << 8) >> 8);

        write(b);
    }

    public byte[] getBytes() {
        return bytes;
    }
}
