package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;

class IsoBootVolumeDescriptor extends IsoVolumeDescriptor {
    private byte[] systemBootId;
    private byte[] bootId;
    private byte[] bootSystemUse;

    public IsoBootVolumeDescriptor(RandomAccessFile iso) throws IOException {
        super(iso);

        systemBootId = new byte[32];
        iso.read(systemBootId);

        bootId = new byte[32];
        iso.read(bootId);

        bootSystemUse = new byte[1977];
        iso.read(bootSystemUse);
    }
}
