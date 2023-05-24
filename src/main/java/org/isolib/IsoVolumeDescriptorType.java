package org.isolib;

enum IsoVolumeDescriptorType {
    BOOT_RECORD(0),
    PRIMARY(1),
    SET_TERMINATOR(255);
    private final int number;
    public int toInt() {
        return number;
    }

    IsoVolumeDescriptorType(int n) {
        number = n;
    }
}

