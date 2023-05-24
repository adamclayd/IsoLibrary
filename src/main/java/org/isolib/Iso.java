package org.isolib;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

class Iso {
    private final String ISO_ID = "CD001";
    private RandomAccessFile iso = null;
    private IsoPrimaryVolumeDescriptor primaryVolumeDescriptor = null;


    public Iso(File isoFile) throws IOException {
        iso = new RandomAccessFile(isoFile, "r");

        iso.seek(0x8001);

        byte[] buffer = new byte[5];
        iso.read(buffer);
        String identifier = new String(buffer);
        if(identifier.compareTo(ISO_ID) != 0)
            throw new IOException("Invalid iso file");

        iso.seek(0x8000);

        int type;
        while((type = iso.read()) != IsoVolumeDescriptorType.SET_TERMINATOR.toInt()) {
            iso.seek(iso.getFilePointer() - 1);
            if(type == IsoVolumeDescriptorType.PRIMARY.toInt())
                primaryVolumeDescriptor = new IsoPrimaryVolumeDescriptor(iso);
            else
                iso.skipBytes(2048);
        }
    }

    public Iso(String isoPath) throws IOException {
        this(new File(isoPath));
    }

    public RandomAccessFile getRAF() {
        return iso;
    }

    public IsoPrimaryVolumeDescriptor getPrimaryVolumeDescriptor() {
        return primaryVolumeDescriptor;
    }

    private long afterLastDirectoryOffset() {
        int sectorSize = primaryVolumeDescriptor.getSectorSize();
        IsoPathTable ptable = primaryVolumeDescriptor.getPathTable();

        long max = 0;
        for(IsoPathTableEntry entry: ptable)
            max = max < entry.getSector() ? entry.getSector() : max;

        return (max * sectorSize) + sectorSize;
    }

    private long afterLastFileOffset() throws IOException {
        return iso.length() - (150 * primaryVolumeDescriptor.getSectorSize());
    }

    private ArrayList<IsoDirectoryEntry> getDirs(IsoFile dir) throws IOException {
        ArrayList<IsoDirectoryEntry> entries = new ArrayList<>();
        entries.add(dir.getDirectoryEntry());
        getDirs(entries, dir);
        return entries;
    }
    private void getDirs(ArrayList<IsoDirectoryEntry> entries, IsoFile dir) throws IOException {
        IsoFile[] files = dir.listFiles();
        for(IsoFile f: files) {
            if(f.isDirectory()) {
                entries.add(f.getDirectoryEntry());
                getDirs(entries, f);
            }
        }
    }

    private ArrayList<IsoFile> getFiles(IsoFile dir) throws IOException {
        ArrayList<IsoFile> files = new ArrayList<>();
        getFiles(files, dir);
        return files;
    }

    private void getFiles(ArrayList<IsoFile> files, IsoFile dir) throws IOException {
        IsoFile[] iFiles = dir.listFiles();
        for(IsoFile f : iFiles) {
            if(f.isDirectory())
                getFiles(files, f);
            else
                files.add(f);
        }
    }

    private int maxFileOffsetIndex(ArrayList<IsoFile> files) {
        int max = 0;

        for(int i = 1; i < files.size(); i++) {
            if(files.get(max).getDataOffset() < files.get(i).getDataOffset())
                max = i;
        }

        return max;
    }

    private int maxDirSectorIndex(ArrayList<IsoDirectoryEntry> entries) {
        int max = 0;

        for(int i = 1; i < entries.size(); i++) {
            if(entries.get(max).get(0).getSector() < entries.get(i).get(0).getSector())
                max = i;
        }

        return max;
    }

    public void removeFile(IsoFile file) throws IOException {
        if (file.isDirectory())
            throw new IOException("removeFile() cannot remove a directory");

        int sectorSize = primaryVolumeDescriptor.getSectorSize();

        int sectors = (int)(Math.ceil((double)file.getLength() / sectorSize));
        ArrayList<IsoFile> allFiles = getFiles(new IsoFile("/", this));

        for(IsoFile f : allFiles) {
            if(f.getDataOffset() >  file.getDataOffset()) {
                IsoDirectoryEntry entry = f.getParent().getDirectoryEntry();

                for(IsoFileEntry fe : entry) {
                    if(fe.getSector() == (int)(f.getDataOffset() / sectorSize)) {
                        fe.setSector(fe.getSector() - sectors);
                    }
                }

                entry.write();
            }
        }


        IsoDirectoryEntry entry = file.getParent().getDirectoryEntry();

        int sector = (int) (file.getDataOffset() / sectorSize);

        for(int i = 0; i < entry.size(); i++) {
            if(entry.get(i).getSector() == sector)
                entry.remove(i);
        }

        entry.write();


        long dest = file.getDataOffset();
        long src = dest + ((long)Math.ceil((double)file.getLength() / sectorSize) * sectorSize);

        byte[] buffer = new byte[sectorSize];

        while(src < iso.length()) {
            iso.seek(src);
            iso.read(buffer);
            iso.seek(dest);
            iso.write(buffer);

            dest += sectorSize;
            src += sectorSize;
        }

        iso.setLength(iso.length() - ((long)Math.ceil((double)file.getLength() / sectorSize) * sectorSize));
    }

    public void removeDirectory(IsoFile dir) throws IOException {
        if(!dir.isDirectory())
            throw new IOException("removeDirectory() cannot remove a file");

        int sectorSize = primaryVolumeDescriptor.getSectorSize();

        IsoDirectoryEntry parent = dir.getParent().getDirectoryEntry();
        int sector = (int)(dir.getDataOffset() / sectorSize);

        for(int i = 0; i < parent.size(); i++) {
            if(parent.get(i).getSector() == sector)
                parent.remove(i);
        }

        parent.write();

        ArrayList<IsoDirectoryEntry> dirs = getDirs(dir);
        ArrayList<IsoFile> files = getFiles(dir);

        while(files.size() > 0) {
            int max = maxFileOffsetIndex(files);
            removeFile(files.get(max));
            files.remove(max);
        }

        while(dirs.size() > 0) {
            int max = maxDirSectorIndex(dirs);
            rmdir(dirs.get(max));
            dirs.remove(max);
        }
    }

    private void rmdir(IsoDirectoryEntry dir) throws IOException {
        int sectorSize = primaryVolumeDescriptor.getSectorSize();
        ArrayList<IsoDirectoryEntry> dirs = getDirs(new IsoFile("/", this));

        for(IsoDirectoryEntry d : dirs) {
            if(d.get(0).getSector() > dir.get(0).getSector()) {
                d.get(0).setSector(d.get(0).getSector() - 1);
                d.write();
            }

            if(d.get(1).getSector() > dir.get(0).getSector()) {
                d.get(1).setSector(d.get(1).getSector() - 1);
                d.write();
            }
        }

        long dest = dir.get(0).getSector() * sectorSize;
        long src = dest + sectorSize;

        byte[] buffer = new byte[sectorSize];

        while(src < iso.length()) {
            iso.seek(src);
            iso.read(buffer);
            iso.seek(dest);
            iso.write(buffer);

            dest += sectorSize;
            src += sectorSize;
        }

        iso.setLength(iso.length() - sectorSize);
    }

}
