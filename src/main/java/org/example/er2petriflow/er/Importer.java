package org.example.er2petriflow.er;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.er.json.Diagram;
import org.example.er2petriflow.er.json.Shape;
import org.example.er2petriflow.generated.er.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Importer {

    private Diagram imported;
    private ERDiagram result;
    private Map<Integer, Shape> shapeMap;

    public Optional<ERDiagram> importDiagram(InputStream jsonFile) {
        try {
            Diagram imported = unmarshall(jsonFile);
            return convert(imported);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    protected Diagram unmarshall(InputStream jsonFile) throws IOException {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(jsonFile, Diagram.class);
    }

    protected Optional<ERDiagram> convert(Diagram imported) {
        this.imported = imported;
        mapShapes();
        result = new ERDiagram();
//        parseEntities();
        return Optional.of(result);
    }

    protected void mapShapes() {
        shapeMap = new HashMap<>();
        for (Shape s : imported.getShapes()) {
            shapeMap.put(s.getDetails().getId(), s);
        }
    }

//    protected void extractNodeData() {
//        GraphType graph = null;
//        for (Object obj : imported.getGraphOrData()) {
//            if (obj instanceof GraphType) {
//                graph = (GraphType) obj;
//                break;
//            }
//        }
//        if (graph == null) {
//            return;
//        }
//        for (Object obj : graph.getDataOrNodeOrEdge()) {
//            if (obj instanceof NodeType) {
//                parseNode((NodeType) obj);
//            }
//        }
//    }
//
//    protected void parseNode(NodeType node) {
//        for (Object obj : node.getDataOrPort()) {
//            if (obj instanceof DataType) {
//                DataType data = (DataType) obj;
//                if (data.getKey().equals(keys.get(Key.POTENTIAL_COMPACT_ENTITY))) {
//                    parsePotentialCompactEntity(data);
//                }
//            }
//        }
//    }
//
//    protected void parsePotentialCompactEntity(DataType data) {
//        for (Object obj : data.getContent()) {
//            if (obj instanceof ElementNSImpl) {
//                ElementNSImpl test = (ElementNSImpl) obj;
//            }
//        }
//    }

}
