package org.tameter.rst_planguage;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2016 Hugh Greene (githugh@tameter.org).
 */
class XPathUtil {
    private XPath xPath;
    private Map<String, XPathExpression> expressionCache = new HashMap<>();

    XPathUtil(XPath xPath) {
        this.xPath = xPath;
    }

    Node getNode(Node context, String xPathExpression) throws XPathExpressionException {
        return (Node) getExpression(xPathExpression).evaluate(context, XPathConstants.NODE);
    }

    NodeList getNodeList(Node context, String xPathExpression) throws XPathExpressionException {
        return (NodeList) getExpression(xPathExpression).evaluate(context, XPathConstants.NODESET);
    }

    private XPathExpression getExpression(String expressionString) throws XPathExpressionException {
        if (!expressionCache.containsKey(expressionString)) {
            expressionCache.put(expressionString, xPath.compile(expressionString));
        }
        return expressionCache.get(expressionString);
    }
}
