package org.tameter.rst_planguage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.tameter.rst_planguage.HtmlDomBuilder.*;

public class Main {

    public static void main(String[] args) {
        try {
            File rawHtml = new File(args[0]);
            File outHtml = new File(args[1]);
            Document doc = load(rawHtml);
            addSummary(doc);
            save(outHtml, doc);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static Document load(File rawHtml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        builder.setEntityResolver(new InternalEntityResolver());
        return builder.parse(rawHtml);
    }

    private static void save(File outHtml, Document doc) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        // Use "html" output method, otherwise empty elements become empty tags, which confuses Firefox.
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        Source source = new DOMSource(doc);
        StreamResult result = new StreamResult(outHtml);
        transformer.transform(source, result);
    }

    private static void addSummary(Document doc) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new DocumentNamespaceContext(doc));
        XPathExpression summaryNodeExpr = xPath.compile("id('summary')");
        Element summaryNode = (Element) summaryNodeExpr.evaluate(doc, XPathConstants.NODE);

        List<String> summaryFields = getSummaryFields(xPath, summaryNode);

        // Now collect the contents of the corresponding nodes in the document.
        XPathExpression frSectionNodeExpr = xPath.compile(
                "//*[contains(concat(' ', normalize-space(@class), ' '), ' section ')][starts-with(@id, 'fr-')]"
        );
        NodeList frSectionNodes = (NodeList) frSectionNodeExpr.evaluate(doc, XPathConstants.NODESET);

        Element table = table().withClass("summary").apply(t -> {
            t.with(colgroup().with(
                    col().withAttribute("span", "1")
                            .withAttribute("class", "summary-col-tag")
            ));
            t.with(tr().apply(row -> {
                row.with(th().withText("Tag"));
                for (String summaryField : summaryFields) {
                    row.with(th().withText(summaryField));
                }
            }));
            int sectionCount = frSectionNodes.getLength();
            for (int sectionIndex = 0; sectionIndex < sectionCount; sectionIndex++) {
                Element sectionNode = (Element) frSectionNodes.item(sectionIndex);
                Element sectionTitleNode = getFirstChildElement(sectionNode);
                // Add a soft break after each '.' in the title, to allow wrapping in the summary table.
                String sectionTag = sectionTitleNode.getTextContent().replaceAll("\\.", ".\u200b");
                String sectionId = sectionNode.getAttribute("id");
                Map<String, String> fields = getFields(xPath, sectionNode);
                t.with(tr().apply(row -> {
                    row.with(td().with(a().withAttribute("href", sectionId).withText(sectionTag)));
                    for (String summaryField : summaryFields) {
                        row.with(td().withText(fields.get(summaryField)));
                    }
                }));
            }
        }).asElement(doc);

        summaryNode.appendChild(table);
    }

    private static List<String> getSummaryFields(XPath xPath, Element summaryNode) {
        // Collect the names of fields we care about from the Summary section, stripping off the trailing ':'.
        XPathExpression summaryFieldNameNodeExpr;
        NodeList summaryFieldNameNodes;
        try {
            summaryFieldNameNodeExpr = xPath.compile(
                    ".//th[contains(concat(' ', normalize-space(@class), ' '), ' field-name ')]"
            );
            summaryFieldNameNodes = (NodeList) summaryFieldNameNodeExpr.evaluate(summaryNode, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        int length = summaryFieldNameNodes.getLength();
        List<String> summaryFields = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            String rawText = summaryFieldNameNodes.item(i).getTextContent();
            summaryFields.add(stripLastCharacter(rawText));
        }
        return summaryFields;
    }

    private static Map<String, String> getFields(XPath xPath, Element sectionNode) {
        // Collect the field names and values we from the section, stripping off the trailing ':' from the field names.
        XPathExpression sectionFieldNodesExpr;
        NodeList sectionFieldNodes;
        try {
            sectionFieldNodesExpr = xPath.compile(
                    ".//tr[contains(concat(' ', normalize-space(@class), ' '), ' field ')]"
            );
            sectionFieldNodes = (NodeList) sectionFieldNodesExpr.evaluate(sectionNode, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        int length = sectionFieldNodes.getLength();
        Map<String, String> sectionFields = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            Node fieldNameNode = sectionFieldNodes.item(i).getFirstChild();
            Node fieldBodyNode = fieldNameNode.getNextSibling();
            String fieldName = stripLastCharacter(fieldNameNode.getTextContent());
            String fieldBody = fieldBodyNode.getTextContent();
            sectionFields.put(fieldName, fieldBody);
        }
        return sectionFields;
    }

    private static Element getFirstChildElement(Element sectionNode) {
        Node firstChild = sectionNode.getFirstChild();
        while (firstChild != null && firstChild.getNodeType() != Node.ELEMENT_NODE) {
            firstChild = firstChild.getNextSibling();
        }
        if (firstChild == null) {
            throw new IllegalArgumentException(
                    String.format("Could not find first element child of section %s", sectionNode.getAttribute("id"))
            );
        }
        return (Element) firstChild;
    }

    private static String stripLastCharacter(String s) {
        return s.substring(0, s.length() - 1);
    }
}
