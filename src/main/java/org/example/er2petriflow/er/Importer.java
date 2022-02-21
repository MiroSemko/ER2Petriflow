package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.generated.er.GraphmlType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.Optional;

public class Importer {

    public Optional<ERDiagram> importDiagram(InputStream xmlFile) {
        try {
            GraphmlType imported = unmarshall(xmlFile);
            return Optional.of(convert(imported));
        } catch (JAXBException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    protected GraphmlType unmarshall(InputStream xmlFile) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(GraphmlType.class);
        JAXBElement<GraphmlType> element = (JAXBElement<GraphmlType>) context.createUnmarshaller().unmarshal(xmlFile);
        return element.getValue();
    }

    protected ERDiagram convert(GraphmlType imported) {
        return new ERDiagram();
    }

}
