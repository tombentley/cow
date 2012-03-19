package com.redhat.ceylon.website.codechecker;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String PROGNAME = "cow";
    
    private List<File> roots;

    private boolean verbose = false;
    
    private int visitedFiles = 0;
    
    public void setRoots(List<File> roots) {
        this.roots = roots;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public void run() throws Exception {
        PrintStream out = System.out;
        DirectoryWalker dirWalker = new DirectoryWalker();
        MdCodeBlockHandler handler = new MdCodeBlockHandler(out, verbose);
        MdParsingVisitor visitor = new MdParsingVisitor(handler);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean visit = pathname.getName().endsWith(".md");
                if (visit) {
                    visitedFiles++;
                }
                return visit;
            }
        };
        dirWalker.addVisitor(new FilteredFileVisitor(filter, visitor));
        for (File root : roots) {
            dirWalker.walk(root);
        }
        
        printStats(out, handler);
        
    }

    private void printStats(PrintStream out, MdCodeBlockHandler handler) {
        out.println("# files visited:      "+ visitedFiles);
        if (verbose) {
            int good = handler.getNumGood();
            int bad = handler.getNumBad();
            int ignored = handler.getNumIgnored();
            int total = good + bad + ignored;
            out.println("# ignored code blocks:"+ ignored);
            out.println("# valid code blocks:  "+ good);
            out.println("# invalid code blocks:" + bad);
            out.println("total # code blocks:  "+ total);
        }
    }
    
    public static void main(String[] args) {
        
        boolean eoo = false;
        List<File> roots = new ArrayList<File>(1);
        boolean verbose = false;
        
        for (int ii = 0; ii < args.length; ii++) {
            String arg = args[ii];
            if (!eoo
                    && arg.startsWith("-")) {
                if ("-h".equals(arg)
                        || "--help".equals(arg)) {
                    usage(System.out);
                    System.exit(0);
                } else if ("--verbose".equals(arg)) {
                    verbose = true;
                } else if ("--".equals(arg)) {
                    eoo = true;
                } else { // unrecognised option
                    usage(System.out);
                    System.exit(1);
                }
            } else {
                roots.add(new File(arg));
            }
        }
        
        if (roots.isEmpty()) {
            roots.add(new File("."));
        }
        
        Main main = new Main();
        main.setRoots(roots);
        main.setVerbose(verbose);
        try {
            main.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static void usage(PrintStream out) {
        out.println(PROGNAME + " - Checks markdown code blocks");
        out.println();
        out.println(PROGNAME + " [--verbose] [<file>...]");
        out.println(PROGNAME + " [-h | --help]");
        out.println();
        out.println("With --verbose prints information about ignored and valid code blocks and summary statistics.");
    }
    
}
