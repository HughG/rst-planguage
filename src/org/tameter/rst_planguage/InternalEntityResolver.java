package org.tameter.rst_planguage;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright (c) 2016 Hugh Greene (githugh@tameter.org).
 */
class InternalEntityResolver implements EntityResolver {
    private final ClassLoader classLoader = InternalEntityResolver.class.getClassLoader();

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String[] systemIdParts = systemId.split("/");
        final String dtd = "dtd/" + systemIdParts[systemIdParts.length - 1];
        InputStream dtdFileStream = classLoader.getResourceAsStream(dtd);
        //System.out.printf("Loading external entity '%s' / '%s'%n", publicId, systemId);
        return new InputSource(dtdFileStream);
    }
}
