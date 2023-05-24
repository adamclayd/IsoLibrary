package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;

class IsoPathTableEntry {
    private byte length;
    private byte numSectorsInExtRecord;
    private int sector;
    private short parentRecordNumber;
    private String name;

    public IsoPathTableEntry(RandomAccessFile iso, boolean isLittleEndian) throws IOException {

        length = iso.readByte();
        numSectorsInExtRecord = iso.readByte();

        if(isLittleEndian) {
            sector = Integer.reverseBytes(iso.readInt());
            parentRecordNumber = Short.reverseBytes(iso.readShort());
        }
        else {
            sector = iso.readInt();
            parentRecordNumber = iso.readShort();
        }

        byte[] buff = new byte[(int)length];
        iso.read(buff);
        name = new String(buff);

        if(length % 2 != 0)
            iso.seek(iso.getFilePointer() + 1);
    }

    public int getSector() {
        return sector;
    }

    public int getParentRecordNumber() {
        return parentRecordNumber;
    }

    public String getName() {
        return name;
    }

    public byte getNumSectorsInExtRecord() {
        return numSectorsInExtRecord;
    }
}
