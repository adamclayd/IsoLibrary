package org.isolib;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

class IsoDirectoryEntry extends ArrayList<IsoFileEntry> {
    private long offset;
    private Iso iso;

    public IsoDirectoryEntry(Iso iso, long beginning) throws IOException {
        this.iso = iso;
        offset = beginning;
        RandomAccessFile raf = iso.getRAF();
        raf.seek(offset);

        IsoFileEntry tmp;
        while(!(tmp = new IsoFileEntry(raf)).isBlankEntry()) {
            add(tmp);
            tmp = new IsoFileEntry(raf);
        }
    }

    public void write() throws IOException {
        RandomAccessBytes rab = new RandomAccessBytes(iso.getPrimaryVolumeDescriptor().getSectorSize());
        RandomAccessFile raf = iso.getRAF();
        raf.seek(offset);
        int length = 0;
        for(IsoFileEntry entry: this) {
            length += entry.getRecordLength();
            rab.write(entry.getBytes());
        }

        byte[] bytes = rab.getBytes();

        for(int i = length; i < bytes.length; i++)
            bytes[i] = 0;

        raf.write(bytes);
    }
}
