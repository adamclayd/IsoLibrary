package org.isolib;

public enum IsoVolumeDescriptorType {
    BOOT_RECORD(0),
    PRIMARY(1),
    SET_TERMINATOR(255);
    private int number;
    public int toInt() {
        return number;
    }
    IsoVolumeDescriptorType(int i) {
        number = i;
    }
}
