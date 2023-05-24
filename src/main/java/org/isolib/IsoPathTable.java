package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

class IsoPathTable extends ArrayList<IsoPathTableEntry> {
    private boolean isLittleEndian;
    private long offset;
    private int length;

    public IsoPathTable(RandomAccessFile iso, long off, int len, boolean isLE) throws IOException {
        offset = off;
        length = length;
        isLittleEndian = isLE;



        iso.seek(offset);

        while(iso.getFilePointer() < offset + length)
            add(new IsoPathTableEntry(iso, isLittleEndian));
    }

    public IsoPathTable(RandomAccessFile iso, long off,  int len) throws IOException {
        this(iso, off, len, false);
    }
}
