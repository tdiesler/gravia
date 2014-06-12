/*
 * #%L
 * Gravia :: Repository
 * %%
 * Copyright (C) 2012 - 2014 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.jboss.gravia.repository.spi;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.gravia.repository.Namespace100;
import org.jboss.gravia.repository.Namespace100.Attribute;
import org.jboss.gravia.repository.Namespace100.Element;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.spi.AttributeValueHandler;
import org.jboss.gravia.resource.spi.AttributeValueHandler.AttributeValue;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.jboss.gravia.utils.IllegalStateAssertion;

/**
 * Read repository contnet from XML.
 *
 * @author thomas.diesler@jboss.com
 * @since 21-May-2012
 */
public abstract class AbstractRepositoryXMLReader implements RepositoryReader {

    private final Map<String, String> attributes = new HashMap<String, String>();
    private final XMLStreamReader reader;

    public AbstractRepositoryXMLReader(InputStream inputStream) {
        IllegalArgumentAssertion.assertNotNull(inputStream, "inputStream");
        reader = createXMLStreamReader(inputStream);
        try {
            reader.require(START_DOCUMENT, null, null);
            reader.nextTag();
            reader.require(START_ELEMENT, Namespace100.REPOSITORY_NAMESPACE, Element.REPOSITORY.getLocalName());
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot read resource element: " + reader.getLocation(), ex);
        }
    }

    protected abstract XMLStreamReader createXMLStreamReader(InputStream inputSteam);

    protected abstract ResourceBuilder createResourceBuilder();

    @Override
    public Map<String, String> getRepositoryAttributes() {
        return attributes;
    }

    @Override
    public Resource nextResource() {
        try {
            return nextResource(reader, createResourceBuilder());
        } catch (XMLStreamException ex) {
            throw new IllegalStateException("Cannot read resource element: " + reader.getLocation(), ex);
        }
    }

    public static Resource nextResource(XMLStreamReader reader, ResourceBuilder builder) throws XMLStreamException {

        if (!reader.hasNext() || reader.nextTag() == END_ELEMENT)
            return null;

        Element element = Element.forName(reader.getLocalName());
        IllegalStateAssertion.assertEquals(Element.RESOURCE, element, "Expected resource element, but got: " + element);

        while (reader.hasNext() && reader.nextTag() == START_ELEMENT) {
            element = Element.forName(reader.getLocalName());
            switch (element) {
                case CAPABILITY:
                    readCapability(reader, builder);
                    break;
                case REQUIREMENT:
                    readRequirement(reader, builder);
                    break;
                default:
                    continue;
            }
        }
        return builder.getResource();
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (XMLStreamException ex) {
            // ignore
        }
    }

    private static Capability readCapability(XMLStreamReader reader, ResourceBuilder builder) throws XMLStreamException {
        String namespace = reader.getAttributeValue(null, Attribute.NAMESPACE.toString());
        Map<String, Object> atts = new HashMap<String, Object>();
        Map<String, String> dirs = new HashMap<String, String>();
        readAttributesAndDirectives(reader, atts, dirs);
        return builder.addCapability(namespace, atts, dirs);
    }

    private static Requirement readRequirement(XMLStreamReader reader, ResourceBuilder builder) throws XMLStreamException {
        String namespace = reader.getAttributeValue(null, Attribute.NAMESPACE.toString());
        Map<String, Object> atts = new HashMap<String, Object>();
        Map<String, String> dirs = new HashMap<String, String>();
        readAttributesAndDirectives(reader, atts, dirs);
        return builder.addRequirement(namespace, atts, dirs);
    }

    public static void readAttributesAndDirectives(XMLStreamReader reader, Map<String, Object> atts, Map<String, String> dirs) throws XMLStreamException {
        while (reader.hasNext() && reader.nextTag() == START_ELEMENT) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case ATTRIBUTE:
                    readAttributeElement(reader, atts);
                    break;
                case DIRECTIVE:
                    readDirectiveElement(reader, dirs);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported element: " + reader.getLocalName());
            }
        }
    }

    private static void readAttributeElement(XMLStreamReader reader, Map<String, Object> attributes) throws XMLStreamException {
        String name = reader.getAttributeValue(null, Attribute.NAME.toString());
        String valstr = reader.getAttributeValue(null, Attribute.VALUE.toString());
        String typespec = reader.getAttributeValue(null, Attribute.TYPE.toString());
        AttributeValue value = AttributeValueHandler.readAttributeValue(name, typespec, valstr);
        attributes.put(name, value.getValue());
        assertEndElement(reader);
    }

    private static void readDirectiveElement(XMLStreamReader reader, Map<String, String> directives) throws XMLStreamException {
        String name = reader.getAttributeValue(null, Attribute.NAME.toString());
        String value = reader.getAttributeValue(null, Attribute.VALUE.toString());
        directives.put(name, value);
        assertEndElement(reader);
    }

    public static void assertEndElement(XMLStreamReader reader) throws XMLStreamException {
        IllegalStateAssertion.assertEquals(END_ELEMENT, reader.nextTag(), "End element expected, but was: " + reader.getLocalName());
    }
}
