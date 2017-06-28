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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageFileModifier {

    public static final String FILE_NAME = "C:\\Jenkins\\ConfigurationFile.txt";
    //public static final String FILE_NAME = "C:\\Jenkins\\pmd-bin-5.8.0-SNAPSHOT\\pmd-bin-5.8.0-SNAPSHOT\\Sample\\package.xml";
    static List<String> stringList = new ArrayList<String>();

    public static void main(String[] args) throws IOException {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);
        clearTheFile(propertiesMap);

        try {
            FileInputStream fstream = new FileInputStream(propertiesMap.get("ClassesTextFilepath"));
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String className;
            while ((className = br.readLine()) != null)   {
                if(className != null && (!className.equals(""))){
                    stringList.add(className);
                }
            }
            br.close();

            File fXmlFile = new File(propertiesMap.get("PackageXMLFilePath"));
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

    private static void createMapOfProperties(FileReader fileReader, Map<String, String> propertiesMap) throws IOException {
        BufferedReader bufferedReader = null;
        String sCurrentLine;

        bufferedReader = new BufferedReader(fileReader);

        while ((sCurrentLine = bufferedReader.readLine()) != null) {
            sCurrentLine= sCurrentLine.replaceAll("\\s+","");
            String[] split = sCurrentLine.split("=");
            propertiesMap.put(split[0], split[1]);

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
            System.out.println("Class Name is "+stringList.get(i));
            version.setTextContent(stringList.get(i));
            parentNode.appendChild(version);

        }
        version = document.createElement("name");
        version.setTextContent("ApexClass");
        parentNode.appendChild(version);
    }

    private static void clearTheFile(Map<String, String> propertiesMap) throws IOException {
        FileWriter fwOb = new FileWriter(propertiesMap.get("PackageXMLFilePath"), false);
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();
    }



}

