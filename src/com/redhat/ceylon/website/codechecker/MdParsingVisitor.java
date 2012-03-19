package com.redhat.ceylon.website.codechecker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * It's not really a Markdown parser, but it understands enough to 
 * extract code blocks and any annotation comments before and after them.
 * 
 * @author tom
 */
public class MdParsingVisitor implements FileVisitor {

    public static interface Handler {
        public void htmlComment(String comment);
        public void codeBlock(int startLine, String block);
        public void text();
        public void startDocument(File file);
        public void endDocument(File file);
    }
    private int startLine;
    
    private LineNumberReader reader;
    private String line;
    
    private final StringBuilder commentSb = new StringBuilder();
    private final StringBuilder codeBlockSb = new StringBuilder();
    private boolean inComment;

    private final Handler handler;
    
    public MdParsingVisitor(Handler handler) {
        this.handler = handler;
    }
    
    public void parse(File file) throws IOException {
        reader = new LineNumberReader(new FileReader(file));
        try {
            startLine = -1;
            commentSb.setLength(0);
            codeBlockSb.setLength(0);
            inComment = false;
            
            boolean inTag = false;
            String tag = null;
            handler.startDocument(file);
            try {
                parseLoop(inTag, tag);
            } finally {
                handler.endDocument(file);
            }
        } finally {
            reader.close();
        }
        
    }

    private void parseLoop(boolean inTag, String tag)
            throws IOException {
        // XXX This is horrible
        line = reader.readLine();
        while (line != null) {
            /* Block level HTML elements, like
             * <table>
             *     blah
             * </table>
             */
            if (inTag) {
                Matcher matcher = Pattern.compile("^ {0,3}\\< */ *" + Pattern.quote(tag) + "[\\s>].*").matcher(line);
                if (matcher.matches()) {
                    inTag = false;
                } else {
                    line = reader.readLine();
                    break;
                }
            }
            Matcher matcher = Pattern.compile("^ {0,3}\\< *([a-zA-Z0-9_:-]+).*").matcher(line);
            if (matcher.matches()) {
                // HTML tag, so skip to matching close tag
                tag = matcher.group(1);
                inTag = true;
                handler.text();
                break;
            }
            
            if (!inComment && (line.startsWith("    ")
                    || line.startsWith("\t"))) {
                codeBlockLine();
            } else if (!line.trim().isEmpty()) {
                // ^^ a definite non-code block line 
                // (could have two indented code blocks separated by a blank line 
                // (zero spaces) -- need to treat that as a single code block
                if (codeBlockSb.length() > 0) {
                    // the end of a code block
                    endCodeBlock();
                } 
                int ii = 0;
                if (inComment) {
                    for (; ii < line.length(); ii++) {
                        char ch = line.charAt(ii);
                        if (atCommentEnd(ii, ch)) {
                            ii+=3;
                            finishComment();   
                            break;
                        } else {
                            appendToComment(ch);    
                        }
                    }
                }
                
                outer: for (; ii < line.length(); ii++) {
                    char ch = line.charAt(ii);
                    if (atCommentStart(ii, ch)) {
                        ii += 4;
                        inComment = true;
                        for (; ii < line.length(); ii++) {
                            ch = line.charAt(ii);
                            if (atCommentEnd(ii, ch)) {
                                ii+=3;
                                finishComment();
                                continue outer;
                            } else {
                                appendToComment(ch);    
                            }
                        }
                    } else if (!Character.isWhitespace(ch)) {
                        handler.text();
                    }
                }
                
                if (inComment) {
                    commentSb.append('\n');
                }
            }
            line = reader.readLine();
        }
    }

    private boolean atCommentStart(int ii, char ch) {
        return ch == '<'
                && ii+3 < line.length()
                && line.charAt(ii+1) == '!'
                && line.charAt(ii+2) == '-'
                && line.charAt(ii+3) == '-';
    }

    private boolean atCommentEnd(int ii, char ch) {
        return ch == '-'
                && ii+2 < line.length()
                && line.charAt(ii+1) == '-'
                && line.charAt(ii+2) == '>';
    }

    private void appendToComment(char ch) {
        commentSb.append(ch);
    }

    private void finishComment() {
        String comment = commentSb.toString();
        handler.htmlComment(comment);
        commentSb.setLength(0);
        inComment = false;
    }
    
    private String trimRight(String string) {
        return string.replaceAll("\\s*\\z", "");
    }
    
    private void endCodeBlock() {
        String source = trimRight(codeBlockSb.toString());
        codeBlockSb.setLength(0);
        if (!source.isEmpty()) {
            handler.codeBlock(startLine, source);
        }
        startLine = -1;
    }


    private void codeBlockLine() {
        codeBlockSb.append(line).append('\n');
        if (startLine == -1) {
            startLine = reader.getLineNumber();
        }
    }
    
    public static void main(String[] args) throws Exception {
        File reference = new File("/home/tom/ceylon/ceylon-lang.org/documentation/1.0/reference");
        new MdParsingVisitor(new MdCodeBlockHandler(System.out, true)).parse(
                new File(reference, "/operator/multiply-assign.md")
        );
    }

    @Override
    public void preWalk(File root) {
        // Nothing to do
    }

    @Override
    public void postWalk(File root) {
        // Nothing to do
    }

    @Override
    public void visit(File file) throws IOException {
        parse(file);        
    }
    
}
