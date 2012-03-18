package com.redhat.ceylon.website.codechecker;

public class Annotation {
    private final AnnotationType type;
    private final String value;
    public Annotation(AnnotationType key, String value) {
        super();
        this.type = key;
        this.value = value;
    }
    public AnnotationType getType() {
        return type;
    }
    public String getValue() {
        return value;
    }
    public String toString() {
        return type + ":" + value;
    }
    
}