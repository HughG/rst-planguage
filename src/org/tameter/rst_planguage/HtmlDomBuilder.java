package org.tameter.rst_planguage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Copyright (c) 2016 Hugh Greene (githugh@tameter.org).
 */
class HtmlDomBuilder {
    static final String HTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

    private String name;
    private List<HtmlDomBuilder> children = new LinkedList<>();
    private Map<String, String> attributes = new HashMap<>();
    private String text;

    private HtmlDomBuilder(String name) {
        this.name = name;
    }

    HtmlDomBuilder with(HtmlDomBuilder... children) {
        Collections.addAll(this.children, children);
        return this;
    }

    HtmlDomBuilder with(Supplier<HtmlDomBuilder>... childSuppliers) {
        for (Supplier<HtmlDomBuilder> child : childSuppliers) {
            this.children.add(child.get());
        }
        return this;
    }

    HtmlDomBuilder apply(Consumer<HtmlDomBuilder> childConsumer) {
        childConsumer.accept(this);
        return this;
    }

    HtmlDomBuilder withText(String text) {
        this.text = text;
        return this;
    }

    HtmlDomBuilder withAttribute(String name, String value) {
        this.attributes.put(name, value);
        return this;
    }

    HtmlDomBuilder withClass(String value) {
        return withAttribute("class", value);
    }

    Element asElement(Document doc) {
        final Element element = doc.createElementNS(HTML_NAMESPACE, name);
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            element.setAttribute(attribute.getKey(), attribute.getValue());
        }
        if (text != null) {
            element.setTextContent(text);
        }
        for (HtmlDomBuilder child : children) {
            element.appendChild(child.asElement(doc));
        }
        return element;
    }

    // Script

    static HtmlDomBuilder script() {
        return new HtmlDomBuilder("script");
    }

    // Anchors

    static HtmlDomBuilder a() {
        return new HtmlDomBuilder("a");
    }

    // Tables

    static HtmlDomBuilder table() {
        return new HtmlDomBuilder("table");
    }

    static HtmlDomBuilder colgroup() {
        return new HtmlDomBuilder("colgroup");
    }

    static HtmlDomBuilder col() {
        return new HtmlDomBuilder("col");
    }

    static HtmlDomBuilder thead() {
        return new HtmlDomBuilder("thead");
    }

    static HtmlDomBuilder tbody() {
        return new HtmlDomBuilder("tbody");
    }

    static HtmlDomBuilder tr() {
        return new HtmlDomBuilder("tr");
    }

    static HtmlDomBuilder th() {
        return new HtmlDomBuilder("th");
    }

    static HtmlDomBuilder td() {
        return new HtmlDomBuilder("td");
    }
}
