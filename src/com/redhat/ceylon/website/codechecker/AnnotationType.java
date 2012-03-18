package com.redhat.ceylon.website.codechecker;

import java.util.HashMap;

public enum AnnotationType {

    LANG("lang", false),
    CAT("cat", true),
    ID("id", false),
    IMPLICIT_ID("implicit-id", false),
    CAT_ID("cat-id", true),
    NO_CHECK("no-check", false);
    
    private static HashMap<String, AnnotationType> INSTANCES = new HashMap<String, AnnotationType>(10);
    static {
        for (AnnotationType type : AnnotationType.values()) {
            INSTANCES.put(type.key, type);
        }
    }
    
    private final String key;
    private boolean multiValue;
    
    private AnnotationType(String key, boolean multiValue) {
        this.key = key;
        this.multiValue = multiValue;
    }
    
    public static AnnotationType fromKey(String key) {
        return INSTANCES.get(key);
    }

    public String getKey() {
        return key;
    }

    public boolean isMultiValue() {
        return multiValue;
    } 
}
