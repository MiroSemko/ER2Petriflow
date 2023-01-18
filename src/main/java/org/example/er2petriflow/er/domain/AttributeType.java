package org.example.er2petriflow.er.domain;

import lombok.Getter;
import org.example.er2petriflow.generated.petriflow.DataType;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public enum AttributeType {
    TEXT(DataType.TEXT, "text", "string", "varchar"),
    NUMBER(DataType.NUMBER, "number", "int", "integer", "float", "double", "decimal"),
    DATE(DataType.DATE, "date");

    @Getter
    private final DataType mapping;
    private final Set<String> synonyms;

    AttributeType(DataType mapping, String... synonyms) {
        this.mapping = mapping;
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
