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
import java.util.*;

public class PackageFileModifier {

    public static final String FILE_NAME = "C:\\JenkinsPOC\\Jenkins\\ConfigurationFile.txt";
    //public static final String FILE_NAME = "C:\\Jenkins\\pmd-bin-5.8.0-SNAPSHOT\\pmd-bin-5.8.0-SNAPSHOT\\Sample\\package.xml";
    static List<String> classList = new ArrayList<String>();
    static List<String> pageList = new ArrayList<String>();
    static List<String> triggerList = new ArrayList<String>();
    static List<String> componentList = new ArrayList<String>();

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
                    String name = className.split("\\.")[0];
                    String extension = className.split("\\.")[1];
                    if(extension.equals("cls")){
                        classList.add(name);
                    }
                    if(extension.equals("page")){
                        pageList.add(name);
                    }
                    if(extension.equals("trigger")){
                        triggerList.add(name);
                    }
                    if(extension.equals("component")){
                        componentList.add(name);
                    }
                }
            }

            classList = removeDuplicates(classList);
            triggerList = removeDuplicates(triggerList);
            pageList = removeDuplicates(pageList);
            componentList = removeDuplicates(componentList);

            br.close();

            File fXmlFile = new File(propertiesMap.get("PackageXMLFilePath"));
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            Element rootElement = doc.createElementNS("http://soap.sforce.com/2006/04/metadata", "Package");
            doc.appendChild(rootElement);
            rootElement.appendChild(createVersionNode(doc));
            if(!classList.isEmpty()){
                Node typeNode = createTypeNode(doc);
                rootElement.appendChild(typeNode);
                createChildNodesForType(doc, typeNode, classList, "CLASS");
            }
            if(!triggerList.isEmpty()){
                Node typeNode = createTypeNode(doc);
                rootElement.appendChild(typeNode);
                createChildNodesForType(doc, typeNode, triggerList, "TRIGGER");
            }
            if(!pageList.isEmpty()){
                Node typeNode = createTypeNode(doc);
                rootElement.appendChild(typeNode);
                createChildNodesForType(doc, typeNode, pageList, "PAGE");
            }
            if(!componentList.isEmpty()){
                Node typeNode = createTypeNode(doc);
                rootElement.appendChild(typeNode);
                createChildNodesForType(doc, typeNode, componentList, "COMPONENT");
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

    private static List<String> removeDuplicates(List<String> list) {
        Set<String> strings = new HashSet<String>();
        List<String> removedDuplicates = new ArrayList<String>();
        for(String s : list){
            strings.add(s);
        }
        removedDuplicates.addAll(strings);

        return removedDuplicates;
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

    private static void createChildNodesForType(Document document, Node parentNode, List<String> stringList, String component) {
        if(component.equals("CLASS")) {
            Element version = null;
            for (int i = 0; i < stringList.size(); i++) {
                version = document.createElement("members");
                System.out.println("Class Name is " + stringList.get(i));
                version.setTextContent(stringList.get(i));
                parentNode.appendChild(version);

            }
            version = document.createElement("name");
            version.setTextContent("ApexClass");
            parentNode.appendChild(version);
        }
        if(component.equals("TRIGGER")) {
            Element version = null;
            for (int i = 0; i < stringList.size(); i++) {
                version = document.createElement("members");
                System.out.println("Class Name is " + stringList.get(i));
                version.setTextContent(stringList.get(i));
                parentNode.appendChild(version);

            }
            version = document.createElement("name");
            version.setTextContent("ApexTrigger");
            parentNode.appendChild(version);
        }
        if(component.equals("PAGE")) {
            Element version = null;
            for (int i = 0; i < stringList.size(); i++) {
                version = document.createElement("members");
                System.out.println("Class Name is " + stringList.get(i));
                version.setTextContent(stringList.get(i));
                parentNode.appendChild(version);

            }
            version = document.createElement("name");
            version.setTextContent("ApexPage");
            parentNode.appendChild(version);
        }
        if(component.equals("COMPONENT")) {
            Element version = null;
            for (int i = 0; i < stringList.size(); i++) {
                version = document.createElement("members");
                System.out.println("Class Name is " + stringList.get(i));
                version.setTextContent(stringList.get(i));
                parentNode.appendChild(version);

            }
            version = document.createElement("name");
            version.setTextContent("ApexComponent");
            parentNode.appendChild(version);
        }
    }

    private static void clearTheFile(Map<String, String> propertiesMap) throws IOException {
        FileWriter fwOb = new FileWriter(propertiesMap.get("PackageXMLFilePath"), false);
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();
    }



}

