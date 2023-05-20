package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IsoPathTableEntry {
    private long location;
    private byte length;
    private byte numSectorsInExtRecord;
    private int sectorNumber;
    private short parentRecordNumber;
    private String name;

    public IsoPathTableEntry(RandomAccessFile iso) throws IOException {

        location = iso.getFilePointer();

        length = iso.readByte();
        numSectorsInExtRecord = iso.readByte();

        sectorNumber = Integer.reverseBytes(iso.readInt());
        parentRecordNumber = Short.reverseBytes(iso.readShort());

        byte[] buff = new byte[(int)length];
        iso.read(buff);
        name = new String(buff);

        if(length % 2 != 0)
            iso.seek(iso.getFilePointer() + 1);
    }

    public int getSectorNumber() {
        return sectorNumber;
    }

    public int getParentRecordNumber() {
        return parentRecordNumber;
    }

    public String getName() {
        String ret = name;
        if(name.compareTo("\0") == 0)
            ret = "/";

        return ret;
    }
}
