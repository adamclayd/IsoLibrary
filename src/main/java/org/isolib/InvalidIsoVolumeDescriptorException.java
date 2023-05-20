package org.isolib;

import java.io.IOException;

public class InvalidIsoVolumeDescriptorException extends IOException {
    public InvalidIsoVolumeDescriptorException() {
        super();
    }

    public InvalidIsoVolumeDescriptorException(String msg) {
        super(msg);
    }
}
