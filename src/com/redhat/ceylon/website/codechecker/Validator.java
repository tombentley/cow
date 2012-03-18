package com.redhat.ceylon.website.codechecker;

import java.io.File;

/**
 * Validates some kind of source code
 * @author tom
 */
public interface Validator {

    /**
     * Validates some kind of source
     * @param file The file the source was extracted from
     * @param startLine The start line within file that the source was extracted from
     * @param source The source itself.
     * @return true if the source was considered valid
     */
    public boolean isValid(File file, int startLine, String source);
    
}
