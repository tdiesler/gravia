/*
 * #%L
 * JBossOSGi Repository: API
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
package org.jboss.gravia.resource.spi;

import java.util.ArrayList;
import java.util.List;

import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;

/**
 * A handler for attribute values.
 *
 * @author thomas.diesler@jboss.com
 * @since 06-Jul-2012
 */
public final class AttributeValueHandler {

    public static enum Type {
        String,
        Version,
        VersionRange,
        Long,
        Double
    }

    /**
     * Read attribute values according to
     * 132.5.6 Attribute Element
     */
    public static AttributeValue readAttributeValue(String typespec, String valstr) {
        boolean listType = false;
        if (typespec != null && typespec.startsWith("List<") && typespec.endsWith(">")) {
            typespec = typespec.substring(5, typespec.length() - 1);
            listType = true;
        }
        Type type = typespec != null ? Type.valueOf(typespec) : Type.String;

        // Whitespace around the list and around commas must be trimmed
        valstr = valstr.trim();

        Object value;
        switch (type) {
            case String:
                if (listType) {
                    List<String> list = new ArrayList<String>();
                    for (String val : split(valstr)) {
                        list.add(val.trim());
                    }
                    value = list;
                } else {
                    value = valstr;
                }
                break;
            case Version:
                if (listType) {
                    List<Version> list = new ArrayList<Version>();
                    for (String val : split(valstr)) {
                        list.add(Version.parseVersion(val.trim()));
                    }
                    value = list;
                } else {
                    value = Version.parseVersion(valstr);
                }
                break;
            case VersionRange:
                if (listType) {
                    List<VersionRange> list = new ArrayList<VersionRange>();
                    for (String val : split(valstr)) {
                        list.add(new VersionRange(val.trim()));
                    }
                    value = list;
                } else {
                    value = new VersionRange(valstr);
                }
                break;
            case Long:
                if (listType) {
                    List<Long> list = new ArrayList<Long>();
                    for (String val : split(valstr)) {
                        list.add(Long.parseLong(val.trim()));
                    }
                    value = list;
                } else {
                    value = Long.parseLong(valstr);
                }
                break;
            case Double:
                if (listType) {
                    List<Double> list = new ArrayList<Double>();
                    for (String val : split(valstr)) {
                        list.add(Double.parseDouble(val.trim()));
                    }
                    value = list;
                } else {
                    value = Double.parseDouble(valstr);
                }
                break;
            default:
                value = valstr;
                break;
        }
        return new AttributeValue(type, value);
    }

    private static List<String> split(String valstr) {
        boolean escape = false;
        StringBuffer tok = new StringBuffer();
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < valstr.length(); i++) {
            char ch = valstr.charAt(i);
            if (ch == '\\' && !escape) {
                escape = true;
                continue;
            }
            if (escape && ch != '\\' && ch != ',' && ch != '"') {
                tok.append('\\');
                escape = false;
            }
            if (ch == ',' && !escape) {
                result.add(tok.toString());
                tok = new StringBuffer();
                escape = false;
                continue;
            }
            tok.append(ch);
            escape = false;
        }
        if (tok.length() > 0) {
            result.add(tok.toString());
        }
        return result;
    }

    public static class AttributeValue {
        private final Type type;
        private final Object value;
        private final boolean listType;

        public static AttributeValue parse(String external) {
            String typespec = external.substring(external.indexOf("type=") + 5, external.indexOf(','));
            String valuestr = external.substring(external.indexOf("value=") + 6, external.length() - 1);
            return AttributeValueHandler.readAttributeValue(typespec, valuestr);
        }

        public static AttributeValue create(Object value) {
            Class<?> valueType = value.getClass();
            boolean listType = List.class.isAssignableFrom(valueType);
            if (listType) {
                List<?> list = (List<?>) value;
                if (!list.isEmpty()) {
                    // Use the type of the first element in the list
                    valueType = list.get(0).getClass();
                } else {
                    // For an empty list it is not possible to infer it's component type
                    valueType = String.class;
                }
            }
            Type type = Type.valueOf(valueType.getSimpleName());
            return new AttributeValue(type, value);
        }

        private AttributeValue(Type type, Object value) {
            assert type != null : "Null type";
            assert value != null : "Null value";
            this.type = type;
            this.value = value;
            Class<? extends Object> valueClass = value.getClass();
            this.listType = List.class.isAssignableFrom(valueClass);
        }

        public Type getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }

        public String getValueString() {
            StringBuffer result = new StringBuffer();
            if (listType) {
                for (Object val : (List<?>) value) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(escape(val));
                }
            } else {
                result.append(value);
            }
            return result.toString();
        }

        private String escape(Object val) {
            String valstr = val.toString();
            if (type != Type.String)
                return valstr;

            StringBuffer result = new StringBuffer();
            for (int i = 0; i < valstr.length(); i++) {
                char ch = valstr.charAt(i);
                if (ch == '\\' || ch == ',' || ch == '"') {
                    result.append("\\" + ch);
                } else {
                    result.append(ch);
                }
            }
            return result.toString();
        }

        public boolean isListType() {
            return listType;
        }

        public String toExternalForm() {
            String typespec = listType ? "List<" + type + ">" : "" + type;
            String valstr = value.toString();
            if (listType) {
                valstr = valstr.substring(1, valstr.length() -1);
            }
            return "[type=" + typespec + ", value=" + valstr + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AttributeValue other = (AttributeValue) obj;
            if (type != other.type)
                return false;
            return value.equals(other.value);
        }

        @Override
        public String toString() {
            return toExternalForm();
        }
    }
}
