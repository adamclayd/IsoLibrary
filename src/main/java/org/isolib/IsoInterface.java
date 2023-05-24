package org.isolib;

import java.io.File;
import java.io.IOException;

public class IsoInterface {
    private Iso iso;
    private File file;

    public IsoInterface(File isoFile) throws IOException {
        file = isoFile;
        iso = new Iso(isoFile);
    }

    public IsoInterface(String isoFilePath) throws IOException {
        this(new File(isoFilePath));
    }

    public IsoFile getRoot() throws IOException {
        return new IsoFile("/", iso);
    }

    public IsoFile getFile(String isoFilePath) throws IOException {
        return new IsoFile(isoFilePath, iso);
    }

    public String getFilename() {
        return file.getName();
    }
}
