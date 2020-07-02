package sample;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;

public class XMLWriter {
    private String outputFilePath;
    private XMLStreamReader reader;
    private XMLEventFactory eventFactory;
    private XMLStreamWriter writer;


    public XMLWriter(String outputFilePath) {
        this.outputFilePath = outputFilePath;

    }

    public void writeFile() {
    }

/*    public void writeFile(XMLStreamReader reader) throws XMLStreamException, IOException {
        this.reader = reader;
        this.eventFactory = XMLEventFactory.newInstance();
        try {
        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileWriter(outputFilePath));
        while (reader.hasNext()) {
            XMLEvent event = reader.get;
            writer.;
        }
        writer.flush();
        writer.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}*/
}
