package com.redhat.ceylon.website.codechecker;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.ceylon.website.codechecker.MdParsingVisitor.Handler;

public class MdCodeBlockHandler implements Handler {

    private int numGood = 0;
    private int numBad = 0;

    private final Map<String, Validator> validators;
    
    private final List<String> comments = new ArrayList<String>(3);
    private Map<String, String> ids = new HashMap<String, String>();
    private File file;
    private AnnotationList preAnnos;
    private int startLine;
    private String block;
    private int numIgnored;
    private final PrintStream out;
    
    public MdCodeBlockHandler(PrintStream out) {
        this.out = out;
        validators = new HashMap<String, Validator>(3);
        Validator validator = new CeylonValidator(out);
        validators.put(null, validator);
        validators.put("", validator);
        validators.put("ceylon", validator);
    }
    
    public int getNumGood() {
        return numGood;
    }

    public int getNumBad() {
        return numBad;
    }

    public int getNumIgnored() {
        return numIgnored;
    }

    @Override
    public void htmlComment(String comment) {
        comments.add(comment);
        Annotation annotation = AnnotationList.parseAnnotation(comment);
        if (annotation != null && annotation.getType() == AnnotationType.IMPLICIT_ID) {
            String implicitId = annotation.getValue();
            String[] keyValue = AnnotationList.keyValue(implicitId);
            String id = keyValue[0].trim();
            String code = keyValue[1].trim();
            ids.put(id, code);
        }
    }

    private void validateBuffered() {
        AnnotationList postAnnos = new AnnotationList(comments);
        if (preAnnos == null) {
            return;
        }
        Validator validator = getValidator(preAnnos.getLang());
        if (validator != null) {
            String check = preAnnos.getCheck();
            if ("none".equals(check)) {
                numIgnored++;
                out.println("Ignoring " + file + ":" + startLine);
            } else {
                out.println("Validating " + file + ":" + startLine);
                String preparedSource = prepareSource(this.preAnnos, this.block, postAnnos);
                if (validator.isValid(file, startLine, preparedSource, check)) {
                    numGood++;
                } else {
                    numBad++;
                }
            }
        }
        preAnnos = null;
    }
    
    @Override
    public void codeBlock(int startLine, String block) {
        preAnnos = new AnnotationList(comments);
        comments.clear();
        this.startLine = startLine;
        this.block = block;
    }

    @Override
    public void text() {
        validateBuffered();
        comments.clear();
    }

    @Override
    public void startDocument(File file) {
        ids.clear();
        comments.clear();
        this.file = file;
    }
    
    @Override
    public void endDocument(File file) {
        validateBuffered();
        ids.clear();
        comments.clear();
    }
    
    public String prepareSource(AnnotationList preAnnos, String source, AnnotationList postAnnos) {
        // If this code block had an <!-- id:NAME --> annotation, save it for later use
        String id = preAnnos.getAnnoValue(AnnotationType.ID, true);
        if (id != null) {
            ids.put(id, source);
        }
        
        StringBuilder sb = new StringBuilder();
        catToSource(preAnnos, sb);
        sb.append(source).append('\n');
        catToSource(postAnnos, sb);
        
        
        return sb.toString();
    }

    private void catToSource(AnnotationList annotations, StringBuilder sb) {
        for (Annotation annotation : annotations.getAnnos()) {
            AnnotationType type = annotation.getType();
            switch (type) {
            case CAT:
                sb.append(annotation.getValue()).append(" // cat\n");
                break;
            case CAT_ID:
                String referred = ids.get(annotation.getValue().trim());
                if (referred == null) {
                    throw new RuntimeException("Couldn't find id: "+annotation.getValue());
                }
                sb.append(referred).append(" // cat-id\n");
                break;
            default:
                break;
            }
        }
    }

    private Validator getValidator(String lang) {
        return validators.get(lang);
    }

}
