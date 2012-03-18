package com.redhat.ceylon.website.codechecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DirectoryWalker {

    private List<FileVisitor> visitors = new ArrayList<FileVisitor>();

    private Comparator<File> order = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    
    /**
     * Walks the directory hierarchy rooted at the given file.
     * @param root
     * @throws IOException
     */
    public void walk(File root) throws IOException {
        if (!root.exists()) {
            throw new RuntimeException("Root doesn't exist");
        }
        for (FileVisitor visitor : visitors) {
            visitor.preWalk(root);
        }
        walkRecursively(root);
        for (FileVisitor visitor : visitors) {
            visitor.postWalk(root);
        }
    }
    
    /**
     * Adds a visitor to the walker
     * @param visitor
     */
    public void addVisitor(FileVisitor visitor) {
        visitors.add(visitor);
    }

    private void walkRecursively(File file) throws IOException {
        for (FileVisitor visitor : visitors) {
            visitor.visit(file);
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (order != null) {
                Arrays.sort(files, order);
            }
            for (File child : files) {
                walkRecursively(child);
            }
        }
    }
    
}
