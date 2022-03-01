package org.example.er2petriflow.er;

import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.generated.er.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Importer {

    private GraphmlType imported;
    private Map<Key, String> keys;
    private ERDiagram result;

    public Optional<ERDiagram> importDiagram(InputStream xmlFile) {
        try {
            GraphmlType imported = unmarshall(xmlFile);
            return convert(imported);
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

    protected Optional<ERDiagram> convert(GraphmlType imported) {
        this.imported = imported;
        mapKeys();
        if (!allKeysMapped()) {
            return Optional.empty();
        }
        result = new ERDiagram();
        extractNodeData();
        return Optional.of(result);
    }

    protected void mapKeys() {
        keys = new HashMap<>();
        for (KeyType xmlKey : imported.getKey()) {
            String name = xmlKey.getAttrName();
            for (Key key : Key.values()) {
                if (name.equals(key.getAttributeName())) {
                    keys.put(key, xmlKey.getId());
                    break;
                }
            }
        }
    }

    protected boolean allKeysMapped() {
        for (Key key : Key.values()) {
            if (!keys.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    protected void extractNodeData() {
        GraphType graph = null;
        for (Object obj : imported.getGraphOrData()) {
            if (obj instanceof GraphType) {
                graph = (GraphType) obj;
                break;
            }
        }
        if (graph == null) {
            return;
        }
        for (Object obj : graph.getDataOrNodeOrEdge()) {
            if (obj instanceof NodeType) {
                parseNode((NodeType) obj);
            }
        }
    }

    protected void parseNode(NodeType node) {
        for (Object obj : node.getDataOrPort()) {
            if (obj instanceof DataType) {
                DataType data = (DataType) obj;
                if (data.getKey().equals(keys.get(Key.POTENTIAL_COMPACT_ENTITY))) {
                    parsePotentialCompactEntity(data);
                }
            }
        }
    }

    protected void parsePotentialCompactEntity(DataType data) {
        for (Object obj : data.getContent()) {
            if (obj instanceof ElementNSImpl) {
                ElementNSImpl test = (ElementNSImpl) obj;
            }
        }
    }

}
