package com.redhat.ceylon.website.codechecker;

import java.io.File;
import java.io.PrintStream;

import com.redhat.ceylon.website.codechecker.MdParsingVisitor.Handler;

public class MdPrinter implements Handler {

    private final PrintStream out = System.out;
    private boolean lastWasText = false;
    
    @Override
    public void htmlComment(String comment) {
        out.println("COMMENT: " +  comment);
        lastWasText = false;
    }

    @Override
    public void codeBlock(int startLine, String block) {
        out.println("CODE BLOCK: " + startLine + ":"+ block);
        lastWasText = false;
    }

    @Override
    public void text() {
        if (!lastWasText) {
            out.println("TEXT");
        }
        lastWasText = true;
    }

    @Override
    public void startDocument(File file) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void endDocument(File file) {
        // TODO Auto-generated method stub
        
    }

}
