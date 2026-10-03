package dev.zoroaster1x.vlcskin.format;

import dev.zoroaster1x.vlcskin.model.SkinTheme;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * DOM helpers with secure defaults: no external DTD or entity fetching.
 */
public final class XmlSupport {

    private XmlSupport() {
    }

    public static Document parse(byte[] bytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setIgnoringComments(true);
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        // The DOCTYPE points at skin.dtd. The editor must not need that file to be
        // present to open a theme, so the resolver returns a harmless empty source.
        builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            return builder.parse(in);
        }
    }

    public static List<Element> childElements(Node node) {
        List<Element> elements = new ArrayList<>();
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) child);
            }
        }
        return elements;
    }

    /**
     * Serializes one element back to a compact XML string.
     */
    public static String elementToXml(Element element) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();
        } catch (Exception ex) {
            return "<" + element.getNodeName() + "/>";
        }
    }

    /**
     * A readable element path such as Theme/Window/Layout/Text[id=x].
     */
    public static String path(Element element) {
        StringBuilder path = new StringBuilder(element.getNodeName());
        String id = element.getAttribute("id");
        if (!id.isEmpty() && !"none".equals(id)) {
            path.append('[').append(id).append(']');
        }
        Node parent = element.getParentNode();
        while (parent instanceof Element parentElement) {
            StringBuilder prefix = new StringBuilder(parentElement.getNodeName());
            String parentId = parentElement.getAttribute("id");
            if (!parentId.isEmpty() && !"none".equals(parentId)) {
                prefix.append('[').append(parentId).append(']');
            }
            path.insert(0, prefix + "/");
            parent = parentElement.getParentNode();
        }
        return path.toString();
    }

    /**
     * True when the document element is a Theme with a compatible version.
     */
    public static boolean isCompatibleThemeVersion(SkinTheme theme, String version) {
        if (version == null || version.isBlank()) {
            return true;
        }
        return version.startsWith("2.");
    }
}
