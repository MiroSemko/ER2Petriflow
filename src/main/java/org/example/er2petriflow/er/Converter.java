package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.generated.petriflow.Document;
import org.example.er2petriflow.generated.petriflow.I18NStringType;

import java.util.ArrayList;
import java.util.List;

public class Converter {

    public List<Document> convertToPetriflows(ERDiagram diagram) {
        List<Document> result = convertEntities(diagram.getEntities());
        return result;
    }

    protected List<Document> convertEntities(List<Entity> entities) {
        List<Document> result = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            result.add(convertEntity(entities.get(i), i));
        }
        return result;
    }

    protected Document convertEntity(Entity entity, int index) {
        Document result = new Document();

        result.setDefaultRole(true);
        result.setAnonymousRole(false);
        result.setVersion("1.0.0");
        result.setInitials("ENT" + index);

        entity.setProcessIdentifier("Entity_" + index);

        result.setId(entity.getProcessIdentifier());
        result.setTitle(i18nWithDefaultValue(entity.getName()));

        return result;
    }

    protected static I18NStringType i18nWithDefaultValue(String defaultValue) {
        I18NStringType result = new I18NStringType();
        result.setValue(defaultValue);
        return result;
    }
}