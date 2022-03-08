package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.Attribute;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.domain.Entity;
import org.example.er2petriflow.generated.petriflow.Data;
import org.example.er2petriflow.generated.petriflow.Document;
import org.example.er2petriflow.generated.petriflow.I18NStringType;
import org.example.er2petriflow.generated.petriflow.Role;

import java.util.List;
import java.util.stream.Collectors;

public class Converter {

    public static final String SYSTEM_ROLE_ID = "system";
    public static final String SYSTEM_ROLE_TITLE = "System";

    protected Role systemRole;

    public List<Document> convertToPetriflows(ERDiagram diagram) {
        List<Document> result = convertEntities(diagram.getEntities());
        return result;
    }

    protected List<Document> convertEntities(List<Entity> entities) {
        return entities.stream().map(this::convertEntity).collect(Collectors.toList());
    }

    protected Document convertEntity(Entity entity) {
        Document result = new Document();

        result.setDefaultRole(true);
        result.setAnonymousRole(false);
        result.setVersion("1.0.0");
        result.setInitials("ENT");

        entity.setProcessIdentifier(entity.getProcessIdentifier());

        result.setId(entity.getProcessIdentifier());
        result.setTitle(i18nWithDefaultValue(entity.getName()));

        convertAttributes(entity, result);
        createSystemRole(result);

        return result;
    }

    protected void convertAttributes(Entity entity, Document result) {
        result.getData().addAll(
                entity.getAttributes().stream().map(this::convertAttribute).collect(Collectors.toList())
        );
    }

    protected Data convertAttribute(Attribute attribute) {
        Data result = new Data();

        result.setId(attribute.getVariableIdentifier());
        result.setTitle(i18nWithDefaultValue(attribute.getName()));
        result.setType(attribute.getType().getMapping());

        return result;
    }

    protected void createSystemRole(Document petriflow) {
        this.systemRole = new Role();
        systemRole.setId(SYSTEM_ROLE_ID);
        systemRole.setTitle(i18nWithDefaultValue(SYSTEM_ROLE_TITLE));
        petriflow.getRole().add(systemRole);
    }

    protected static I18NStringType i18nWithDefaultValue(String defaultValue) {
        I18NStringType result = new I18NStringType();
        result.setValue(defaultValue);
        return result;
    }
}