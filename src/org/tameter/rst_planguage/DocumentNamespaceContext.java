package org.tameter.rst_planguage;

import org.w3c.dom.Document;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

/**
 * Copyright (c) 2016 Hugh Greene (githugh@tameter.org).
 */
public class DocumentNamespaceContext implements NamespaceContext {
    private final Document doc;

    public DocumentNamespaceContext(Document doc) {
        this.doc = doc;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return doc.lookupNamespaceURI(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return doc.lookupPrefix(namespaceURI);
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        System.out.println("Asked for prefixes for " + namespaceURI);
        throw new NotImplementedException();
    }
}
