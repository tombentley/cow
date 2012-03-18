package com.redhat.ceylon.website.codechecker;

import java.io.File;
import java.io.IOException;

public interface FileVisitor {
    
    public void preWalk(File root);
    
    public void postWalk(File root);
    
    public void visit(File file) throws IOException;
}