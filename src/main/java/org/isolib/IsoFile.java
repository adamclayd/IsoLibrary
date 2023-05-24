package org.isolib;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class IsoFile {
    private IsoFileEntry entry;
    private IsoFileEntry parent;
    private IsoPrimaryVolumeDescriptor descriptor;
    private Iso iso;

    public IsoFile(String path, Iso iso) throws IOException {
        this.iso = iso;
        descriptor = iso.getPrimaryVolumeDescriptor();

        RandomAccessFile raf = iso.getRAF();

        if(path == null || path.isEmpty() || path.compareTo("/") == 0) {
            entry = descriptor.getRootDirectoryRecord();
            parent = null;
        }
        else {
            IsoFileEntry ent = descriptor.getRootDirectoryRecord();
            IsoFileEntry prev = null;

            path = path.startsWith("/") ? path.substring(1) : path;
            path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
            String[] parts = path.contains("/") ? path.split("/") : new String[] { path };

            for(int i = 0; i < parts.length && !ent.isBlankEntry(); i++) {
                prev = ent;

                raf.seek(((long)ent.getSector() * (long)descriptor.getSectorSize()) + 68);
                IsoFileEntry tmp = new IsoFileEntry(raf);
                while(tmp.getName().compareTo(parts[i]) != 0 && !tmp.isBlankEntry()) {
                    tmp = new IsoFileEntry(raf);
                }

                ent = tmp;
            }

            if(!ent.isBlankEntry()) {
                entry = ent;
                parent = prev;
            }
            else {
                entry = parent = null;
            }
        }
    }

    private IsoFile(IsoFileEntry ent, IsoFileEntry par, Iso iso) throws IOException {
        entry = ent;
        parent = par;
        this.iso = iso;
        descriptor = iso.getPrimaryVolumeDescriptor();
    }

    public boolean isDirectory() {
        boolean r = false;
        if(entry != null)
            r = entry.isDirectory();

        return r;
    }

    public boolean exists() {
        return entry != null;
    }

    public IsoFile[] listFiles() throws IOException {
        if(!exists())
            throw new FileNotFoundException();

        if(!isDirectory())
            throw new IOException("Not a directory");

        RandomAccessFile raf = iso.getRAF();

        ArrayList<IsoFile> dirs = new ArrayList<>();
        ArrayList<IsoFile> files = new ArrayList<>();

        raf.seek(((long)entry.getSector() * (long)descriptor.getSectorSize()) + 68);

        IsoFileEntry tmp = null;
        while(!(tmp = new IsoFileEntry(raf)).isBlankEntry()) {

            if(tmp.isDirectory())
                dirs.add(new IsoFile(tmp, entry, iso));
            else
                files.add(new IsoFile(tmp, entry, iso));
        }

        IsoFile[] r = new IsoFile[dirs.size() + files.size()];
        Collections.sort(dirs, (a, b) -> { return a.getName().compareTo(b.getName()); });
        Collections.sort(files, (a, b) -> { return a.getName().compareTo(b.getName()); });

        IsoFile[] dirArr = new IsoFile[dirs.size()];
        IsoFile[] fileArr = new IsoFile[files.size()];
        dirs.toArray(dirArr);
        files.toArray(fileArr);

        System.arraycopy(dirArr, 0, r, 0, dirArr.length);
        System.arraycopy(fileArr, 0, r, dirArr.length, fileArr.length);

        return r;
    }

    public String getName() {
        return entry.getName();
    }

    public IsoFile getParent() throws IOException {
        if(parent == null)
            return null;

        int rootSector = descriptor.getRootDirectoryRecord().getSector();

        if(parent.getSector() == rootSector)
            return new IsoFile(descriptor.getRootDirectoryRecord(), null, iso);

        RandomAccessFile raf =  iso.getRAF();

        raf.seek(((long)parent.getSector() * (long)descriptor.getSectorSize()) + 34);

        IsoFileEntry tmp = new IsoFileEntry(raf);
        int sector = tmp.getSector();

        IsoFileEntry par = null;

        if(sector == rootSector) {
            par = descriptor.getRootDirectoryRecord();
        }
        else {
            raf.seek((long)sector * (long)descriptor.getSectorSize());

            tmp = new IsoFileEntry(raf);

            while(tmp.getSector() != sector && !tmp.isBlankEntry()) {
                tmp = new IsoFileEntry(raf);
            }

            if(tmp.isBlankEntry())
                throw new IOException("Cannot get the parent of the file entry");

            par = tmp;
        }

        return new IsoFile(parent, par, iso);
    }

    public String getPath() throws IOException {
        String path = getName();

        for(IsoFile f = getParent(); f != null; f = f.getParent()) {
            path = f.getName() + "/" + path;
        }

        path = path.isEmpty() ? "/" : path;

        return path;
    }

    public long getLength() throws IOException {
        return isDirectory() ? getDirectorySize(this) : entry.getDataLength();
    }

    private long getDirectorySize(IsoFile dir) throws IOException {
        IsoFile[] list = dir.listFiles();

        long r = 0;
        for(int i = 0; i < list.length; i++) {
            if(list[i].isDirectory())
                r += getDirectorySize(list[i]);
            else
                r += list[i].getLength();
        }

        return r;
    }

    public long getDataOffset() {
        return (long)entry.getSector() *  (long)descriptor.getSectorSize();
    }

    public IsoRandomAccessFile getRAF() throws IOException {
        return new IsoRandomAccessFile(iso, this);
    }

    public IsoFileInputStream getInputStream() throws IOException {
        return new IsoFileInputStream(getRAF());
    }

    public IsoDirectoryEntry getDirectoryEntry() throws IOException {
        if (!isDirectory())
            throw new IOException("Not a directory");

        long offset = (long) entry.getSector() * (long) descriptor.getSectorSize();

        return new IsoDirectoryEntry(iso, offset);
    }
}
