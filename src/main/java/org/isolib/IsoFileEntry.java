package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IsoFileEntry {
    private long offset;

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

    public IsoFileEntry(RandomAccessFile iso, long offset) throws IOException {

        this.offset = offset;

        iso.seek(offset);

        readEntry(iso);
    }

    public IsoFileEntry(RandomAccessFile iso) throws IOException {
        offset = -1;
        readEntry(iso);
    }

    public IsoFileEntry(byte[] bytes) throws IOException {
        if(bytes.length < 33)
            throw new IllegalArgumentException("Array argument bytes has to have a length of at least 33");

        offset = -1;

        int position = 0;

        recordLength = bytes[position++];
        extendedRecordLength = bytes[position++];

        position += 4;
        sector = (((int)bytes[position++] & 0xff) << 24) | (((int)bytes[position++] & 0xff) << 16) | (((int)bytes[position++] << 8) & 0xff) | (((int)bytes[position++] & 0xff) << 0);

        position += 4;
        dataLength = (((int)bytes[position++] & 0xff) << 24) | (((int)bytes[position++] & 0xff) << 16) | (((int)bytes[position++] << 8) & 0xff) | (((int)bytes[position++] & 0xff) << 0);

        dateTime = new byte[7];

        for(int i = 0; i < 7; i++)
            dateTime[i] = bytes[position++];

        flags = bytes[position++];
        unitSize = bytes[position++];
        gapSize = bytes[position++];

        position += 2;

        volumeSequenceNumber = (short)((bytes[position++] << 8) | ((bytes[position++]) & 0xff));
        fileIdLength = bytes[position++];

        byte[] buff = new byte[(int)fileIdLength];
        for(int i = 0; i < fileIdLength; i++)
            buff[i] = bytes[position++];

        fileId = new String(buff);
        buff = null;

        int padding = (int)fileIdLength % 2 == 0 ? 1 : 0;

        if(padding == 1)
            position++;

        int leftOver = recordLength - (33 + fileIdLength + padding);
        leftOver = leftOver < 0 ? 0 : leftOver;
        systemUse = new byte[leftOver];
        for(int i = 0; i < leftOver; i++)
            systemUse[i] = bytes[position++];
    }

    private void readEntry(RandomAccessFile iso) throws IOException {
        recordLength = iso.readByte();
        extendedRecordLength = iso.readByte();

        iso.seek(iso.getFilePointer() + 4);
        sector = iso.readInt();

        iso.seek(iso.getFilePointer() + 4);
        dataLength = iso.readInt();

        dateTime = new byte[7];
        iso.read(dateTime);

        flags = iso.readByte();

        unitSize = iso.readByte();
        gapSize = iso.readByte();

        iso.seek(iso.getFilePointer() + 2);
        volumeSequenceNumber = iso.readShort();

        fileIdLength = iso.readByte();

        byte[] buffer = new byte[(int)fileIdLength];
        iso.read(buffer);
        fileId = new String(buffer);
        buffer = null;

        int padding = (int)fileIdLength % 2 == 0 ? 1 : 0;

        if(padding == 1)
            iso.seek(iso.getFilePointer() + 1);

        int leftOver = recordLength - (33 + fileIdLength + padding);
        leftOver = leftOver < 0 ? 0 : leftOver;
        systemUse = new byte[leftOver];
        iso.read(systemUse);
    }


    public long getDataOffset(int sectorSize) {
        return (long)sectorSize * (long)sector;
    }

    public int getDataLength() {
        return dataLength;
    }

    public boolean isDirectory() {
        return ((flags >> 1) & 1) == 1;
    }

    public boolean isHidden() {
        return ((flags >> 0) & 1) == 1;
    }

    public String getFileId() {
        return fileId.trim().strip();
    }

    public boolean isBlankEntry() {
        return recordLength <= 0;
    }
}
