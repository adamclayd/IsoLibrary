package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class IsoVolumeDescriptor {

    private final String STANDARD_ID = "CD001";
    private byte type;
    private String standardId;
    private byte version;
    private byte[] bootSystemId;
    private byte[] bootId;
    private byte[] bootSystemUse;
    private byte[] systemId;
    private byte[] volumeId;
    private int numSectors;
    private short volumeSetSize;
    private short volumeSequenceNumber;
    private short sectorSize;
    private int pathTableLength;
    private int firstLittleEndianPathTableNumber;
    private int secondLittleEndianPathTableNumber;
    private int firstBigEndianPathTableNumber;
    private int secondBigEndianPathTableNumber;
    private byte[] rootDirectoryRecord;
    private byte[] volumeSetId;
    private byte[] publisherId;
    private byte[] dataPreparerId;
    private byte[] applicationId;
    private byte[] copyrightFileId;
    private byte[] abstractFileId;
    private byte[] bibliographicalFileId;
    private byte[] createdAt;
    private byte[] modifiedAt;
    private byte[] expiresAt;
    private byte[] effectiveAt;
    private byte fileStructureVersion;
    private byte[] applicationUsed;
    private byte[] reserved;

    ArrayList<IsoPathTableEntry> pathTable;


    public IsoVolumeDescriptor(RandomAccessFile iso) throws IOException {
        type = iso.readByte();

        byte[] buffer = new byte[5];
        iso.read(buffer);
        standardId = new String(buffer);
        buffer = null;

        if (standardId.compareTo(STANDARD_ID) != 0)
            throw new InvalidIsoVolumeDescriptorException(standardId + " is not a valid iso Volume descriptor");


        version = iso.readByte();


        if ((int) type == 0 || (int) type == 255) {
            systemId = null;
            volumeId = null;
            numSectors = 0;
            volumeSetSize = 0;
            volumeSequenceNumber = 0;
            sectorSize = 0;
            pathTableLength = 0;
            firstLittleEndianPathTableNumber = 0;
            secondLittleEndianPathTableNumber = 0;
            firstBigEndianPathTableNumber = 0;
            secondBigEndianPathTableNumber = 0;
            rootDirectoryRecord = null;
            volumeSetId = null;
            publisherId = null;
            dataPreparerId = null;
            applicationId = null;
            copyrightFileId = null;
            abstractFileId = null;
            bibliographicalFileId = null;
            createdAt = null;
            modifiedAt = null;
            expiresAt = null;
            effectiveAt = null;
            fileStructureVersion = (byte) 0;
            applicationUsed = null;
            reserved = null;
        } else if ((int) type == 0) {
            bootSystemId = new byte[32];
            iso.read(bootSystemId);

            bootId = new byte[32];
            iso.read(bootId);

            bootSystemUse = new byte[1977];
            iso.read(bootSystemUse);
        } else {

            iso.seek(iso.getFilePointer() + 1);

            systemId = new byte[32];
            iso.read(systemId);

            volumeId = new byte[32];
            iso.read(volumeId);

            iso.seek(iso.getFilePointer() + 12);
            numSectors = iso.readInt();

            iso.seek(iso.getFilePointer() + 34);
            volumeSetSize = iso.readShort();

            iso.seek(iso.getFilePointer() + 2);
            volumeSequenceNumber = iso.readShort();

            iso.seek(iso.getFilePointer() + 2);
            sectorSize = iso.readShort();

            iso.seek(iso.getFilePointer() + 4);
            pathTableLength = iso.readInt();

            firstLittleEndianPathTableNumber = Integer.reverseBytes(iso.readInt());
            secondLittleEndianPathTableNumber = Integer.reverseBytes(iso.readInt());
            firstBigEndianPathTableNumber = iso.readInt();
            secondBigEndianPathTableNumber = iso.readInt();

            rootDirectoryRecord = new byte[34];
            iso.read(rootDirectoryRecord);

            volumeSetId = new byte[128];
            iso.read(volumeSetId);

            publisherId = new byte[128];
            iso.read(publisherId);

            dataPreparerId = new byte[128];
            iso.read(dataPreparerId);

            applicationId = new byte[128];
            iso.read(applicationId);

            copyrightFileId = new byte[37];
            iso.read(copyrightFileId);

            abstractFileId = new byte[37];
            iso.read(abstractFileId);

            bibliographicalFileId = new byte[37];
            iso.read(bibliographicalFileId);

            createdAt = new byte[17];
            iso.read(createdAt);

            modifiedAt = new byte[17];
            iso.read(modifiedAt);

            expiresAt = new byte[17];
            iso.read(expiresAt);

            effectiveAt = new byte[17];
            iso.read(effectiveAt);

            fileStructureVersion = iso.readByte();

            iso.seek(iso.getFilePointer() + 1);

            applicationUsed = new byte[512];
            iso.read(applicationUsed);

            reserved = new byte[653];
            iso.read(reserved);

            long pathTableOffset = iso.getFilePointer();

            while (iso.getFilePointer() < (pathTableOffset + pathTableLength)) {
                IsoPathTableEntry entry = new IsoPathTableEntry(iso);
                pathTable.add(entry);
            }
        }

    }

    public IsoFileEntry getRootDirectoryRecord() throws IOException {
        return new IsoFileEntry(rootDirectoryRecord);
    }

    public int getSectorSize() {
        return sectorSize;
    }

    public int getDescriptorType() {
        return (int)type;
    }
}
