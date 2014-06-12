/*
 * #%L
 * Gravia :: Resource
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.resource.spi;

import static org.jboss.gravia.resource.ContentNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.gravia.resource.MavenCoordinates;
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
        Boolean,
        Double,
        Float,
        Integer,
        Long,
        Maven,
        String,
        URL,
        Version,
        VersionRange
    }

    /**
     * Read attribute values according to
     * 132.5.6 Attribute Element
     */
    public static AttributeValue readAttributeValue(String typespec, String valstr) {
        return readAttributeValue(null,  typespec, valstr);
    }

    public static AttributeValue readAttributeValue(String keystr, String typespec, String valstr) {
        boolean listType = false;
        if (typespec != null && typespec.startsWith("List<") && typespec.endsWith(">")) {
            typespec = typespec.substring(5, typespec.length() - 1);
            listType = true;
        }

        Type type;
        if (typespec != null) {
            type = Type.valueOf(typespec);
        } else if (CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE.equals(keystr)) {
            type = Type.Maven;
        } else {
            type = Type.String;
        }

        // Whitespace around the list and around commas must be trimmed
        valstr = valstr.trim();

        Object value;
        switch (type) {
            case Boolean:
                if (listType) {
                    List<Boolean> list = new ArrayList<>();
                    for (String val : split(valstr)) {
                        list.add(Boolean.parseBoolean(val.trim()));
                    }
                    value = list;
                } else {
                    value = Boolean.parseBoolean(valstr);
                }
                break;
            case Double:
                if (listType) {
                    List<Double> list = new ArrayList<>();
                    for (String val : split(valstr)) {
                        list.add(Double.parseDouble(val.trim()));
                    }
                    value = list;
                } else {
                    value = Double.parseDouble(valstr);
                }
                break;
            case Float:
                if (listType) {
                    List<Float> list = new ArrayList<>();
                    for (String val : split(valstr)) {
                        list.add(Float.parseFloat(val.trim()));
                    }
                    value = list;
                } else {
                    value = Float.parseFloat(valstr);
                }
                break;
            case Integer:
                if (listType) {
                    List<Integer> list = new ArrayList<>();
                    for (String val : split(valstr)) {
                        list.add(Integer.parseInt(val.trim()));
                    }
                    value = list;
                } else {
                    value = Integer.parseInt(valstr);
                }
                break;
            case Long:
                if (listType) {
                    List<Long> list = new ArrayList<>();
                    for (String val : split(valstr)) {
                        list.add(Long.parseLong(val.trim()));
                    }
                    value = list;
                } else {
                    value = Long.parseLong(valstr);
                }
                break;
            case Maven:
                if (listType) {
                    List<MavenCoordinates> list = new ArrayList<>();
                    for (String val : split(valstr)) {
                        list.add(MavenCoordinates.parse(val.trim()));
                    }
                    value = list;
                } else {
                    value = MavenCoordinates.parse(valstr);
                }
                break;
            case String:
                if (listType) {
                    List<String> list = new ArrayList<>();
                    for (String val : split(valstr)) {
                        list.add(val.trim());
                    }
                    value = list;
                } else {
                    value = valstr;
                }
                break;
            case URL:
                if (listType) {
                    List<URL> list = new ArrayList<>();
                    for (String val : split(valstr)) {
                        list.add(toURL(val.trim()));
                    }
                    value = list;
                } else {
                    value = toURL(valstr);
                }
                break;
            case Version:
                if (listType) {
                    List<Version> list = new ArrayList<>();
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
                    List<VersionRange> list = new ArrayList<>();
                    for (String val : split(valstr)) {
                        list.add(new VersionRange(val.trim()));
                    }
                    value = list;
                } else {
                    value = new VersionRange(valstr);
                }
                break;
            default:
                value = valstr;
                break;
        }
        return new AttributeValue(type, value);
    }

    private static URL toURL(String urlspec) {
        try {
            return new URL(urlspec);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
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
            Type type;
            if (MavenCoordinates.class == valueType) {
                type = Type.Maven;
            } else {
                String simpleName = valueType.getSimpleName();
                type = Type.valueOf(simpleName);
            }
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
            if (this == obj) return true;
            if (!(obj instanceof AttributeValue)) return false;
            AttributeValue other = (AttributeValue) obj;
            return type.equals(other.type) && value.equals(other.value);
        }

        @Override
        public String toString() {
            return toExternalForm();
        }
    }
}
