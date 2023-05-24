package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;

class IsoFileEntry {
    private byte recordLength;
    private byte extendedRecordLength;
    private int sector;
    private int dataLength;
    private byte[] dateTime;
    private byte flags;
    private byte unitSize;
    private byte gapSize;
    private short volumeSequenceNumber;
    private byte fileIdLength;
    private String fileId;
    private byte[] systemUse;


    public IsoFileEntry(RandomAccessFile iso) throws IOException {
        recordLength = iso.readByte();
        extendedRecordLength = iso.readByte();

        iso.skipBytes(4);
        sector = iso.readInt();

        iso.skipBytes(4);
        dataLength = iso.readInt();

        dateTime = new byte[7];
        iso.read(dateTime);

        flags = iso.readByte();

        unitSize = iso.readByte();
        gapSize = iso.readByte();

        iso.skipBytes(2);
        volumeSequenceNumber = iso.readShort();

        fileIdLength = iso.readByte();

        byte[] buffer = new byte[(int)fileIdLength];
        iso.read(buffer);
        fileId = new String(buffer);
        buffer = null;

        int padding = (int)fileIdLength % 2 == 0 ? 1 : 0;

        if(padding == 1)
            iso.skipBytes(1);

        int leftOver = recordLength - (33 + fileIdLength + padding);
        leftOver = leftOver < 0 ? 0 : leftOver;
        systemUse = new byte[leftOver];
        iso.read(systemUse);
    }


    public int getSector() {

        return sector;
    }

    public byte getRecordLength() {

        return recordLength;
    }

    public int getDataLength() {
        return dataLength;
    }

    public short getVolumeSequenceNumber() {
        return volumeSequenceNumber;
    }

    public boolean isDirectory() {
        return ((flags >> 1) & 1) == 1;
    }

    public boolean isHidden() {
        return ((flags >> 0) & 1) == 1;
    }

    public String getName() {
        String r = fileId.trim().strip();
        if(r.endsWith(".;1"))
            r = r.substring(0, r.length() - 3);
        else if (r.endsWith(";1"))
            r = r.substring(0, r.length() - 2);

        return r;
    }

    public String getFileId() {
        return fileId;
    }

    public boolean isBlankEntry() {

        return recordLength <= 0;
    }

    public byte[] getSystemUse() {
        return systemUse;
    }

    public void setRecordLength(byte v) {
        recordLength = v;
    }

    public void setSector(int v) {
        sector = v;
    }

    public void setDataLength(int v) {
        dataLength = v;
    }

    public void setDateTime(byte[] v) {
        dateTime = v;
    }

    public void setIsHidden(boolean v) {
        if(v)
            flags |= 1 << 0;
        else
            flags &= ~(1 << 0);
    }

    public void setIsDirectory(boolean v) {
        if(v)
            flags |= 1 << 1;
        else
            flags &= ~(1 << 1);
    }

    public void rename(String fname) {
        String name = fname;

        if(fileId.endsWith(".;1"))
            name += ".;1";
        else if(fileId.endsWith(";1"))
            name += ";1";

        fileId = name;
        fileIdLength = (byte)name.length();
        recordLength = (byte)(33 + name.length() + (name.length() % 2 == 0 ? 1 : 0));
        systemUse = new byte[0];
    }

    public byte[] getBytes() {
        RandomAccessBytes rab = new RandomAccessBytes(recordLength);

        rab.write(recordLength);
        rab.write(extendedRecordLength);
        rab.write(Integer.reverseBytes(sector));
        rab.write(sector);
        rab.write(Integer.reverseBytes(dataLength));
        rab.write(dataLength);
        rab.write(dateTime);
        rab.write(flags);
        rab.write(unitSize);
        rab.write(gapSize);
        rab.write(Short.reverseBytes(volumeSequenceNumber));
        rab.write(volumeSequenceNumber);
        rab.write(fileIdLength);
        rab.write(fileId.getBytes());

        if(fileId.length() % 2 == 0)
            rab.write((byte)0);

        if(systemUse.length > 0)
            rab.write(systemUse);

        return rab.getBytes();
    }
}
