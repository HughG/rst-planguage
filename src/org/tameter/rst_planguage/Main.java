package org.tameter.rst_planguage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import java.util.List;

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

        // Collect the names of fields we care about from the Summary section, stripping off the trailing ':'.
        XPathExpression summaryFieldNameNodeExpr = xPath.compile(
                ".//th[contains(concat(' ', normalize-space(@class), ' '), ' field-name ')]"
        );
        NodeList summaryFieldNameNodes = (NodeList) summaryFieldNameNodeExpr.evaluate(summaryNode, XPathConstants.NODESET);
        int length = summaryFieldNameNodes.getLength();
        List<String> summaryFields = new ArrayList<>(length);
        System.out.printf("Got %s fields", length);
        for (int i = 0; i < length; i++) {
            String rawText = summaryFieldNameNodes.item(i).getTextContent();
            summaryFields.add(rawText.substring(0, rawText.length() - 1));
        }


        Element table = table().withClass("summary").with(
                tr().apply(b -> {
                    b.with(th().withText("Tag"));
                    for (int i = 0; i < length; i++) {
                        b.with(th().withText(summaryFields.get(i)));
                        System.out.printf("Added summary field '%s'", summaryFields.get(i));
                    }
                })
        ).asElement(doc);
        summaryNode.appendChild(table);
    }
}
