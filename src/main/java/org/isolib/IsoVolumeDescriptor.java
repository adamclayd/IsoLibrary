package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;

abstract class IsoVolumeDescriptor {
    protected byte type;
    protected String standardId;
    protected byte version;

    public IsoVolumeDescriptor(RandomAccessFile iso) throws IOException {
        type = iso.readByte();

        byte[] buffer = new byte[5];
        iso.read(buffer);
        standardId = new String(buffer);
        buffer = null;

        version = iso.readByte();
    }
}
