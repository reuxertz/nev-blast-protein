/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BLAST;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author golds_000
 */
public class ParseBlast {
    BlastOutput Parse() throws JAXBException, SAXException, FileNotFoundException{
        
        JAXBContext jc = JAXBContext.newInstance(BlastOutput.class);
        Unmarshaller u = jc.createUnmarshaller();

        XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        xmlreader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlreader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        xmlreader.setEntityResolver(new EntityResolver() {
             public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                  String file = null;
                  if (systemId.contains("NCBI_BlastOutput.dtd")) {
                       file = "NCBI_BlastOutput.dtd";
                  }
                  if (systemId.contains("NCBI_Entity.mod.dtd")) {
                       file = "NCBI_Entity.mod.dtd";
                  }
                  if (systemId.contains("NCBI_BlastOutput.mod.dtd")) {
                       file = "NCBI_BlastOutput.mod.dtd";
                  }
                  return new InputSource(BlastOutput.class.getResourceAsStream(file));
            }
        });
        InputSource input = new InputSource(new FileReader(new File("blast-xml-output.xml")));
        Source source = new SAXSource(xmlreader, input);
        return (BlastOutput) u.unmarshal(source);
        
    }//end default constructor
}//end class
