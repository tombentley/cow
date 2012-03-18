package com.redhat.ceylon.website.codechecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class AnnotationList {

    /*
     * Using `cat` to prepend some code to an example
     *  
     *     <!-- cat: CODE -->
     *         // CODE-BLOCK
     *     
     * Using `cat` to append some code to an example
     * 
     *         // CODE-BLOCK
     *     <!-- cat: CODE -->
     *     
     * Using `id` to name some code for use by `cat-id:` in an 
     * example(s) later in the same document
     * 
     *     <!-- id:m -->
     *         // CODE-BLOCK
     * 
     * Using `implicit-id` to declare some code for use by `cat-id:` in an 
     * example(s) later in the same document
     * 
     *     <!-- implicit-id:m: CODE -->
     *
     * Using `cat-id:` to prepend code named with `id:` or `implicit-id:` 
     * earlier in the same document
     * 
     *     <!-- cat-id:m -->
     *         // CODE
     *         
     * Using `no-check` to prevent checking all together
     * 
     *     <!-- no-check -->
     *         // UNCHECKED CODE
     *     
     * Use `error-line:` to indicate that the first error is expected on the
     * given line:
     * 
     *     <!-- error-line: 1 -->
     *         // BAD CODE
     */
    
    private final LinkedList<Annotation> annotationList = new LinkedList<Annotation>();
    private final HashMap<AnnotationType, List<String>> multiAnnosByKey = new HashMap<AnnotationType, List<String>>();
    private final HashMap<AnnotationType, Annotation> singleAnnosByKey = new HashMap<AnnotationType, Annotation>();

    public AnnotationList(List<String> comments) {
        for (String comment : comments) {
            Annotation keyValue = parseAnnotation(comment);
            if (keyValue == null
                    || keyValue.getType() == null) {
                // comment was not an annotation
                continue;
            }
            annotationList.add(keyValue);
            if (keyValue.getType().isMultiValue()) {
                List<String> list = multiAnnosByKey.get(keyValue.getType());
                if (list == null) {
                    list = new ArrayList<String>(1);
                    multiAnnosByKey.put(keyValue.getType(),  list);
                }
                list.add(keyValue.getValue());
            } else {
                Annotation oldKeyValue = singleAnnosByKey.put(keyValue.getType(), keyValue);
                if (oldKeyValue != null) {
                    throw new RuntimeException();
                }
            }
        }
    }

    static Annotation parseAnnotation(String str) {
        String[] split = keyValue(str);
        if (split == null
                || split.length != 2) {
            return null;
        }
        AnnotationType type = AnnotationType.fromKey(split[0].trim());
        Annotation annotation = null;
        if (type != null) {
            annotation = new Annotation(type, split[1]);
        }
        return annotation;
    }

    static String[] keyValue(String comment) {
        String trimmed = comment.trim();
        String[] split = trimmed.split(":", 2);
        return split;
    }
    
    String getAnnoValue(AnnotationType type, boolean trimmed) {
        if (type.isMultiValue()) {
            throw new RuntimeException();
        }
        String result = null;
        Annotation keyValue = singleAnnosByKey.get(type);
        if (keyValue != null) {
            result = keyValue.getValue();
            if (trimmed) {
                result = result.trim();
            }
        }
        
        return result;
    }
    
    private List<String> getAnnoValues(AnnotationType type) {
        if (!type.isMultiValue()) {
            throw new RuntimeException();
        }
        return multiAnnosByKey.get(type);
    }
    
    public String getCheck() {
        String value = getAnnoValue(AnnotationType.CHECK, true);
        if (value != null) {
            String[] keyValue = keyValue(value);
            return keyValue[0];
        }
        return null;
    }

    public String getLang() {
        return getAnnoValue(AnnotationType.LANG, true);
    }
   
    @Override
    public String toString() {
        return "Annotations [annos=" + annotationList + "]";
    }

    public Iterable<Annotation> getAnnos() {
        return Collections.unmodifiableList(annotationList);
    }
    
}