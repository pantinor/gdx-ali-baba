
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import org.testng.annotations.Test;

public class TmxXmlTest {

    //@Test
    public void regenerateObjectIds() throws Exception {
        File inputFile = new File("src/main/resources/assets/data/ali-baba.tmx");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        NodeList objectNodes = doc.getElementsByTagName("object");
        int newId = 1;

        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element objectElement = (Element) objectNodes.item(i);
            objectElement.setAttribute("id", String.valueOf(newId++));
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("your-map-updated.tmx"));
        transformer.transform(source, result);

        System.out.println("Object IDs successfully regenerated.");
    }

}
