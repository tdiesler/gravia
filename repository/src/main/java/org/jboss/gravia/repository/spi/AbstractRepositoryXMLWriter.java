/*
 * #%L
 * JBossOSGi Repository: API
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.jboss.gravia.repository.spi;

import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.gravia.repository.Namespace100;
import org.jboss.gravia.repository.RepositoryStorageException;
import org.jboss.gravia.repository.RepositoryWriter;
import org.jboss.gravia.repository.Namespace100.Attribute;
import org.jboss.gravia.repository.Namespace100.Element;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.spi.AttributeValueHandler;
import org.jboss.gravia.resource.spi.AttributeValueHandler.AttributeValue;


/**
 * Write repository contnet to XML.
 *
 * @author thomas.diesler@jboss.com
 * @since 21-May-2012
 */
public abstract class AbstractRepositoryXMLWriter implements RepositoryWriter {

    private final XMLStreamWriter writer;

    public AbstractRepositoryXMLWriter(OutputStream outputStream) {
        if (outputStream == null)
            throw new IllegalArgumentException("Null outputStream");
        writer = createXMLStreamWriter(outputStream);
    }

    protected abstract XMLStreamWriter createXMLStreamWriter(OutputStream outputStream);

    @Override
    public void writeRepositoryElement(Map<String, String> attributes) {
        try {
            writer.writeStartDocument();
            writer.setDefaultNamespace(Namespace100.REPOSITORY_NAMESPACE);
            writer.writeStartElement(Element.REPOSITORY.getLocalName());
            writer.writeDefaultNamespace(Namespace100.REPOSITORY_NAMESPACE);
            for (Entry<String, String> entry : attributes.entrySet()) {
                writer.writeAttribute(entry.getKey(), entry.getValue());
            }
        } catch (XMLStreamException ex) {
            throw new RepositoryStorageException("Cannot write repository element", ex);
        }
    }

    @Override
    public void writeResource(Resource resource) {
        try {
            writer.writeStartElement(Element.RESOURCE.getLocalName());
            for (Capability cap : resource.getCapabilities(null)) {
                writer.writeStartElement(Element.CAPABILITY.getLocalName());
                writer.writeAttribute(Attribute.NAMESPACE.getLocalName(), cap.getNamespace());
                writeAttributes(cap.getAttributes());
                writeDirectives(cap.getDirectives());
                writer.writeEndElement();
            }
            for (Requirement req : resource.getRequirements(null)) {
                writer.writeStartElement(Element.REQUIREMENT.getLocalName());
                writer.writeAttribute(Attribute.NAMESPACE.getLocalName(), req.getNamespace());
                writeAttributes(req.getAttributes());
                writeDirectives(req.getDirectives());
                writer.writeEndElement();
            }
            writer.writeEndElement();
        } catch (XMLStreamException ex) {
            throw new IllegalStateException("Cannot initialize repository writer", ex);
        }
    }

    @Override
    public void close() {
        try {
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        } catch (XMLStreamException ex) {
            throw new RepositoryStorageException("Cannot write repository element", ex);
        }
    }

    private void writeAttributes(Map<String, Object> attributes) throws XMLStreamException {
        for (Entry<String, Object> entry : attributes.entrySet()) {
            AttributeValue attval = AttributeValue.create(entry.getValue());
            writer.writeStartElement(Element.ATTRIBUTE.getLocalName());
            writer.writeAttribute(Attribute.NAME.getLocalName(), entry.getKey());
            if (attval.isListType()) {
                writer.writeAttribute(Attribute.VALUE.getLocalName(), attval.getValueString());
                writer.writeAttribute(Attribute.TYPE.getLocalName(), "List<" + attval.getType() + ">");
            } else {
                writer.writeAttribute(Attribute.VALUE.getLocalName(), attval.getValueString());
                if (attval.getType() != AttributeValueHandler.Type.String) {
                    writer.writeAttribute(Attribute.TYPE.getLocalName(), attval.getType().toString());
                }
            }
            writer.writeEndElement();
        }
    }

    private void writeDirectives(Map<String, String> directives) throws XMLStreamException {
        for (Entry<String, String> entry : directives.entrySet()) {
            writer.writeStartElement(Element.DIRECTIVE.getLocalName());
            writer.writeAttribute(Attribute.NAME.getLocalName(), entry.getKey());
            writer.writeAttribute(Attribute.VALUE.getLocalName(), entry.getValue());
            writer.writeEndElement();
        }
    }
}
