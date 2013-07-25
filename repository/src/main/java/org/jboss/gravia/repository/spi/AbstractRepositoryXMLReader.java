/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.spi.AttributeValueHandler;
import org.jboss.gravia.resource.spi.AttributeValueHandler.AttributeValue;

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
        if (inputStream == null)
            throw new IllegalArgumentException("Null inputStream");
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
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                Element element = Element.forName(reader.getLocalName());
                switch (element) {
                    case RESOURCE:
                        return readResourceElement(reader);
                    default:
                        continue;
                }
            }
        } catch (XMLStreamException ex) {
            throw new IllegalStateException("Cannot read resource element: " + reader.getLocation(), ex);
        }
        return null;
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (XMLStreamException ex) {
            // ignore
        }
    }

    private Resource readResourceElement(XMLStreamReader reader) throws XMLStreamException {
        ResourceBuilder builder = createResourceBuilder();
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case CAPABILITY:
                    readCapabilityElement(reader, builder);
                    break;
                case REQUIREMENT:
                    readRequirementElement(reader, builder);
                    break;
                default:
                    continue;
            }
        }
        return builder.getResource();
    }

    private void readCapabilityElement(XMLStreamReader reader, ResourceBuilder builder) throws XMLStreamException {
        String namespace = reader.getAttributeValue(null, Attribute.NAMESPACE.toString());
        Map<String, Object> atts = new HashMap<String, Object>();
        Map<String, String> dirs = new HashMap<String, String>();
        readAttributesAndDirectives(reader, atts, dirs);
        builder.addCapability(namespace, atts, dirs);
    }

    private void readRequirementElement(XMLStreamReader reader, ResourceBuilder builder) throws XMLStreamException {
        String namespace = reader.getAttributeValue(null, Attribute.NAMESPACE.toString());
        Map<String, Object> atts = new HashMap<String, Object>();
        Map<String, String> dirs = new HashMap<String, String>();
        readAttributesAndDirectives(reader, atts, dirs);
        builder.addRequirement(namespace, atts, dirs);
    }

    private void readAttributesAndDirectives(XMLStreamReader reader, Map<String, Object> atts, Map<String, String> dirs) throws XMLStreamException {
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case ATTRIBUTE:
                    readAttributeElement(reader, atts);
                    break;
                case DIRECTIVE:
                    readDirectiveElement(reader, dirs);
                    break;
                default:
                    continue;
            }
        }
    }

    private void readAttributeElement(XMLStreamReader reader, Map<String, Object> attributes) throws XMLStreamException {
        String name = reader.getAttributeValue(null, Attribute.NAME.toString());
        String valstr = reader.getAttributeValue(null, Attribute.VALUE.toString());
        String typespec = reader.getAttributeValue(null, Attribute.TYPE.toString());
        AttributeValue value = AttributeValueHandler.readAttributeValue(typespec, valstr);
        attributes.put(name, value.getValue());
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT)
            ;
    }

    private void readDirectiveElement(XMLStreamReader reader, Map<String, String> directives) throws XMLStreamException {
        String name = reader.getAttributeValue(null, Attribute.NAME.toString());
        String value = reader.getAttributeValue(null, Attribute.VALUE.toString());
        directives.put(name, value);
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
        }
    }
}
