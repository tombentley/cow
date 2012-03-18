package com.redhat.ceylon.website.codechecker;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        
        DirectoryWalker dirWalker = new DirectoryWalker();
        MdCodeBlockHandler handler = new MdCodeBlockHandler(System.out);
        MdParsingVisitor visitor = new MdParsingVisitor(handler);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".md");
            }
        };
        dirWalker.addVisitor(new FilteredFileVisitor(filter, visitor));
        dirWalker.walk(new File("/home/tom/ceylon/ceylon-lang.org/documentation/1.0/reference"));
        
        int good = handler.getNumGood();
        int bad = handler.getNumBad();
        int ignored = handler.getNumIgnored();
        int total = good + bad + ignored;
        System.out.println("# ignored code blocks:"+ ignored);
        System.out.println("# valid code blocks:  "+ good);
        System.out.println("# invalid code blocks:" + bad);
        System.out.println("total # code blocks:  "+ total);
    }
    
}
