package org.example.er2petriflow;

import org.example.er2petriflow.generated.er.GraphmlType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

public class ER2PetriflowCLI {

    public static void main(String[] args) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(GraphmlType.class);
        JAXBElement<GraphmlType> element = (JAXBElement) context.createUnmarshaller().unmarshal(ER2PetriflowCLI.class.getResourceAsStream("/test_graph.graphml"));
        GraphmlType graph = element.getValue();
        assert graph != null;
    }

}
