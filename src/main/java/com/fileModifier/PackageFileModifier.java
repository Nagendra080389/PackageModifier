package com.fileModifier;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PackageFileModifier {

    public static final String FILE_NAME = "C:\\Jenkins\\pmd-bin-5.8.0-SNAPSHOT\\pmd-bin-5.8.0-SNAPSHOT\\Sample\\package.xml";
    static List<String> stringList = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        clearTheFile();

        try {
            FileInputStream fstream = new FileInputStream("C:\\Jenkins\\SalesForceClasses.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String className;
            while ((className = br.readLine()) != null)   {
                if(className != null && (!className.equals(""))){
                    stringList.add(className);
                }
            }
            br.close();

            File fXmlFile = new File(FILE_NAME);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            Element rootElement = doc.createElementNS("http://soap.sforce.com/2006/04/metadata", "Package");
            doc.appendChild(rootElement);
            Node typeNode = createTypeNode(doc);
            rootElement.appendChild(typeNode);
            rootElement.appendChild(createVersionNode(doc));
            if(!stringList.isEmpty()){
                createChildNodesForType(doc, typeNode, stringList);
            }

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fXmlFile.getPath()));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Node createVersionNode(Document doc) {
        Element node = doc.createElement("version");
        node.appendChild(
                doc.createTextNode("38.0"));
        return node;
    }

    private static Node createTypeNode(Document doc) {
        return doc.createElement("types");
    }

    private static void createChildNodesForType(Document document, Node parentNode, List<String> stringList) {
        Element version = null;
        for (int i = 0; i < stringList.size(); i++) {
            version = document.createElement("members");
            version.setTextContent(stringList.get(i));
            parentNode.appendChild(version);

        }
        version = document.createElement("name");
        version.setTextContent("ApexClass");
        parentNode.appendChild(version);
    }

    private static void clearTheFile() throws IOException {
        FileWriter fwOb = new FileWriter(FILE_NAME, false);
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();
    }



}

