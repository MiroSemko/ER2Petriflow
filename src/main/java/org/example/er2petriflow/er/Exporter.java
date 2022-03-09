package org.example.er2petriflow.er;

import org.example.er2petriflow.generated.petriflow.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Exporter {

    public void exportToZip(FileOutputStream outputStream, List<Document> petriflows) throws JAXBException, IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        JAXBContext context = JAXBContext.newInstance(Document.class);
        Marshaller marshaller = context.createMarshaller();
        for (Document document : petriflows) {
            ZipEntry entry = new ZipEntry(document.getId() + ".xml");
            zipOutputStream.putNextEntry(entry);
            marshaller.marshal(document, zipOutputStream);
        }
        zipOutputStream.close();
    }

}
