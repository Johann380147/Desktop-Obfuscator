package com.sim.application.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlParser {
    private final File file;
    private final Document document;
    private static List<Document> stashedDocuments = new ArrayList<>();

    public XmlParser(File file) throws IOException, SAXException, ParserConfigurationException {
        this.file = file;
        this.document = parse();
    }

    private Document parse() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        Element root = document.getDocumentElement();
        root.normalize();
        return document;
    }

    public static Document parse(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        Element root = document.getDocumentElement();
        root.normalize();
        return document;
    }

    public List<Element> findElementsContainingText(String searchText) {
        Element root = document.getDocumentElement();
        List<Element> elementList = new ArrayList<>();
        iterateElements(root, searchText, elementList);
        return elementList;
    }

    private void iterateElements(Element element, String searchText, List<Element> elementList) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String textContent = child.getTextContent();
                if (searchText.equals(textContent)) {
                    elementList.add(child);
                }
                iterateElements(child, searchText, elementList);
            }
        }
    }

    public static List<Element> findElementsContainingText(Element root, String searchText) {
        List<Element> elementList = new ArrayList<>();
        NodeList nodeList = root.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String textContent = element.getTextContent();
                if (searchText.equals(textContent)) {
                    elementList.add(element);
                }
            }
        }
        return elementList;
    }

    public static void replaceText(Element node, String replacementText) {
        node.setTextContent(replacementText);
    }

    public boolean saveFile(String downloadLocation) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult streamResult =  new StreamResult(new File(downloadLocation));
            transformer.transform(source, streamResult);
            return true;
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveFile(Document document, String downloadLocation) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult streamResult =  new StreamResult(new File(downloadLocation));
            transformer.transform(source, streamResult);
            return true;
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Document getDocument() {
        return document;
    }

    public void stashDocument() {
        stashedDocuments.add(document);
    }

    public static void stashDocument(Document document) {
        stashedDocuments.add(document);
    }

    public static List<Document> getStashedDocuments() {
        return stashedDocuments;
    }
}
