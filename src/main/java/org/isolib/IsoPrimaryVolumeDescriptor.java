package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;

class IsoPrimaryVolumeDescriptor extends IsoVolumeDescriptor {
    private final String STANDARD_ID = "CD001";
    private byte type;
    private String standardId;
    private byte version;
    private byte[] systemId;
    private byte[] volumeId;
    private int numSectors;
    private short volumeSetSize;
    private short volumeSequenceNumber;
    private short sectorSize;
    private int pathTableLength;
    private int firstLittleEndianPathTableSector;
    private int secondLittleEndianPathTableSector;
    private int firstBigEndianPathTableSector;
    private int secondBigEndianPathTableSector;
    private IsoFileEntry rootDirectoryRecord;
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

    IsoPathTable pathTable;
    IsoPathTable optionalTable;


    public IsoPrimaryVolumeDescriptor(RandomAccessFile iso) throws IOException {
        super(iso);

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

        firstLittleEndianPathTableSector = Integer.reverseBytes(iso.readInt());
        secondLittleEndianPathTableSector = Integer.reverseBytes(iso.readInt());
        firstBigEndianPathTableSector = iso.readInt();
        secondBigEndianPathTableSector = iso.readInt();
        rootDirectoryRecord = new IsoFileEntry(iso);

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

        long curr = iso.getFilePointer();
        pathTable = new IsoPathTable(iso, firstBigEndianPathTableSector * sectorSize, pathTableLength);

        if(secondBigEndianPathTableSector != 0) {
            optionalTable = new IsoPathTable(iso, secondBigEndianPathTableSector * sectorSize, pathTableLength);
        }
        else {
            optionalTable = null;
        }

        iso.seek(curr);
    }

    public IsoFileEntry getRootDirectoryRecord() throws IOException {
        return rootDirectoryRecord;
    }

    public short getVolumeSequenceNumber() {
        return volumeSequenceNumber;
    }

    public long getVolumeLength() {
        return (long)numSectors * (long)sectorSize;
    }

    public int getSectorSize() {
        return sectorSize;
    }

    public IsoPathTable getPathTable() {
        return pathTable;
    }

    public IsoPathTable getOptionalTable() {
        return optionalTable;
    }
}