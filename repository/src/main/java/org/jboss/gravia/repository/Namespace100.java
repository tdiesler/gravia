/*
 * #%L
 * JBossOSGi Repository
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
package org.jboss.gravia.repository;

import java.util.HashMap;
import java.util.Map;


/**
 * Constants related to namespace
 *
 * http://www.osgi.org/xmlns/repository/v1.0.0
 *
 * @author Thomas.Diesler@jboss.com
 * @since 14-May-2012
 */
public interface Namespace100 {

    String REPOSITORY_NAMESPACE = "http://www.osgi.org/xmlns/repository/v1.0.0";

    enum Attribute {
        UNKNOWN(null),
        NAME("name"),
        NAMESPACE("namespace"),
        INCREMENT("increment"),
        VALUE("value"),
        TYPE("type"),
        ;
        private final String name;

        Attribute(final String name) {
            this.name = name;
        }

        public String getLocalName() {
            return name;
        }

        private static final Map<String, Attribute> MAP;

        static {
            final Map<String, Attribute> map = new HashMap<String, Attribute>();
            for (Attribute element : values()) {
                final String name = element.getLocalName();
                if (name != null) map.put(name, element);
            }
            MAP = map;
        }

        public static Attribute forName(String localName) {
            final Attribute element = MAP.get(localName);
            return element == null ? UNKNOWN : element;
        }

        public String toString() {
            return getLocalName();
        }
    }

    enum Element {
        UNKNOWN(null),
        ATTRIBUTE("attribute"),
        CAPABILITY("capability"),
        DIRECTIVE("directive"),
        REQUIREMENT("requirement"),
        REPOSITORY("repository"),
        RESOURCE("resource"),
        ;

        private final String name;

        Element(final String name) {
            this.name = name;
        }

        public String getLocalName() {
            return name;
        }

        private static final Map<String, Element> MAP;

        static {
            final Map<String, Element> map = new HashMap<String, Element>();
            for (Element element : values()) {
                final String name = element.getLocalName();
                if (name != null) map.put(name, element);
            }
            MAP = map;
        }

        public static Element forName(String localName) {
            final Element element = MAP.get(localName);
            return element == null ? UNKNOWN : element;
        }
    }
}
