package com.redhat.ceylon.website.codechecker;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class FilteredFileVisitor implements FileVisitor {

    private final FileFilter filter;
    
    private final FileVisitor visitor;

    public FilteredFileVisitor(FileFilter filter, FileVisitor fileVisitor) {
        super();
        this.filter = filter;
        this.visitor = fileVisitor;
    }

    @Override
    public void visit(File file) throws IOException {
        if (filter.accept(file)) {
            visitor.visit(file);
        }
    }

    @Override
    public void preWalk(File root) {
        visitor.preWalk(root);
    }

    @Override
    public void postWalk(File root) {
        visitor.postWalk(root);
    }
    
}
