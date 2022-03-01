package org.example.er2petriflow.er.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public enum AttributeType {
    TEXT("text", "string", "varchar"),
    NUMBER("number", "int", "integer", "float", "double");


    private final Set<String> synonyms;

    AttributeType(String... synonyms) {
        this.synonyms = new HashSet<>(List.of(synonyms));
    }

    public static AttributeType resolve(String type) {
        String lowercase = type.toLowerCase(Locale.ROOT);
        for (AttributeType attributeType : AttributeType.values()) {
            if (attributeType.synonyms.contains(lowercase)) {
                return attributeType;
            }
        }
        return null;
    }
}
