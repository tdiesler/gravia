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
package org.jboss.gravia.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * Open MBean support for a resource.
 *
 * @author thomas.diesler@jboss.com
 * @since 03-Jan-2014
 */
public final class CompositeDataResourceType {

    public static final String TYPE_NAME = "ResourceType";
    public static final String ITEM_IDENTITY = "identity";
    public static final String ITEM_CAPABILITIES = "capabilities";
    public static final String ITEM_REQUIREMENTS = "requirements";

    private static final CompositeType compositeType;
    static {
        try {
            compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
        } catch (OpenDataException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static CompositeType getCompositeType() {
        return compositeType;
    }

    public CompositeData getCompositeData(Resource res) throws OpenDataException {
        String identity = res.getIdentity().toString();
        List<Capability> caps = res.getCapabilities(null);
        List<Requirement> reqs = res.getRequirements(null);
        List<Object> items = new ArrayList<Object>();
        items.add(identity);
        items.add(new CapabilitiesType().getCompositeData(caps));
        items.add(new RequirementsType().getCompositeData(reqs));
        Object[] itemValues = items.toArray(new Object[items.size()]);
        return new CompositeDataSupport(compositeType, getItemNames(), itemValues);
    }

    public static String[] getItemNames() {
        return  new String[] { ITEM_IDENTITY, ITEM_CAPABILITIES, ITEM_REQUIREMENTS };
    }

    public static OpenType<?>[] getItemTypes() throws OpenDataException {
        List<OpenType<?>> itemTypes = new ArrayList<OpenType<?>>();
        itemTypes.add(SimpleType.STRING);
        itemTypes.add(CapabilitiesType.getArrayType());
        itemTypes.add(RequirementsType.getArrayType());
        return itemTypes.toArray(new OpenType<?>[itemTypes.size()]);
    }

    public static final class CapabilitiesType {

        public static final String TYPE_NAME = "CapabilitiesType";
        public static final String ITEM_NAME = "capabilities";

        private static final ArrayType<CompositeType> arrayType;
        static {
            try {
                arrayType = new ArrayType<CompositeType>(1, CapabilityType.getCompositeType());
            } catch (OpenDataException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public static ArrayType<CompositeType> getArrayType() {
            return arrayType;
        }

        public CompositeData[] getCompositeData(List<Capability> caps) throws OpenDataException {
            CompositeData[] itemValues = new CompositeData[caps.size()];
            for(int index = 0; index < caps.size(); index++) {
                Capability cap = caps.get(index);
                itemValues[index] = new CapabilityType().getCompositeData(cap);
            }
            return itemValues;
        }
    }

    public static final class CapabilityType {

        public static final String TYPE_NAME = "CapabilityType";
        public static final String ITEM_NAMESPACE = "namespace";
        public static final String ITEM_ATTRIBUTES = "attributes";
        public static final String ITEM_DIRECTIVES = "directives";

        private static final CompositeType compositeType;
        static {
            try {
                compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
            } catch (OpenDataException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public static CompositeType getCompositeType() {
            return compositeType;
        }

        public CompositeData getCompositeData(Capability cap) throws OpenDataException {
            String namespace = cap.getNamespace();
            CompositeData[] attsData = new AttributesType().getCompositeData(cap.getAttributes());
            CompositeData[] dirsData = new DirectivesType().getCompositeData(cap.getDirectives());
            Object[] itemValues = new Object[] { namespace, attsData, dirsData };
            return new CompositeDataSupport(compositeType, getItemNames(), itemValues);
        }

        public static String[] getItemNames() {
            return new String[] { ITEM_NAMESPACE, ITEM_ATTRIBUTES, ITEM_DIRECTIVES };
        }

        public static OpenType<?>[] getItemTypes() throws OpenDataException {
            ArrayType<CompositeType> attsType = AttributesType.getArrayType();
            ArrayType<CompositeType> dirsType = DirectivesType.getArrayType();
            return new OpenType<?>[] { SimpleType.STRING, attsType, dirsType };
        }
    }

    public static final class RequirementsType {

        public static final String TYPE_NAME = "RequirementsType";
        public static final String ITEM_NAME = "requirements";

        private static final ArrayType<CompositeType> arrayType;
        static {
            try {
                arrayType = new ArrayType<CompositeType>(1, RequirementType.getCompositeType());
            } catch (OpenDataException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public static ArrayType<CompositeType> getArrayType() {
            return arrayType;
        }

        public CompositeData[] getCompositeData(List<Requirement> requirements) throws OpenDataException {
            CompositeData[] itemValues = new CompositeData[requirements.size()];
            for(int index = 0; index < requirements.size(); index++) {
                Requirement req = requirements.get(index);
                itemValues[index] = new RequirementType().getCompositeData(req);
            }
            return itemValues;
        }
    }

    public static final class RequirementType {

        public static final String TYPE_NAME = "RequirementType";
        public static final String ITEM_NAMESPACE = "namespace";
        public static final String ITEM_ATTRIBUTES = "attributes";
        public static final String ITEM_DIRECTIVES = "directives";

        private static final CompositeType compositeType;
        static {
            try {
                compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
            } catch (OpenDataException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public static CompositeType getCompositeType() {
            return compositeType;
        }

        public CompositeData getCompositeData(Requirement req) throws OpenDataException {
            String namespace = req.getNamespace();
            CompositeData[] attsData = new AttributesType().getCompositeData(req.getAttributes());
            CompositeData[] dirsData = new DirectivesType().getCompositeData(req.getDirectives());
            Object[] itemValues = new Object[] { namespace, attsData, dirsData };
            return new CompositeDataSupport(compositeType, getItemNames(), itemValues);
        }

        public static String[] getItemNames() {
            return new String[] { ITEM_NAMESPACE, ITEM_ATTRIBUTES, ITEM_DIRECTIVES };
        }

        public static OpenType<?>[] getItemTypes() throws OpenDataException {
            ArrayType<CompositeType> attsType = AttributesType.getArrayType();
            ArrayType<CompositeType> dirsType = DirectivesType.getArrayType();
            return new OpenType<?>[] { SimpleType.STRING, attsType, dirsType };
        }
    }

    public static final class AttributesType {

        public static final String TYPE_NAME = "AttributesType";
        public static final String ITEM_NAME = "attributes";

        private static final ArrayType<CompositeType> arrayType;
        static {
            try {
                arrayType = new ArrayType<CompositeType>(1, getRowType());
            } catch (OpenDataException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public static ArrayType<CompositeType> getArrayType() {
            return arrayType;
        }

        public CompositeData[] getCompositeData(Map<String, Object> attributes) throws OpenDataException {
            CompositeData[] dataArr = new CompositeData[attributes.size()];
            String[] itemNames = new String[] { AttributeType.ITEM_KEY, AttributeType.ITEM_VALUE };
            int index = 0;
            for (Entry<String, Object> entry : attributes.entrySet()) {
                Object[] itemValues = new Object[] { entry.getKey(), entry.getValue().toString() };
                CompositeData data = new CompositeDataSupport(AttributeType.getArrayType(), itemNames, itemValues);
                dataArr[index++] = data;
            }
            return dataArr;
        }

        public static String[] getIndexNames() {
            return new String[] { AttributeType.ITEM_KEY, AttributeType.ITEM_VALUE };
        }

        public static CompositeType getRowType() throws OpenDataException {
            return AttributeType.getArrayType();
        }
    }

    public static final class AttributeType {

        public static final String TYPE_NAME = "AttributeType";
        public static final String ITEM_KEY = "key";
        public static final String ITEM_VALUE = "value";

        private static final CompositeType compositeType;
        static {
            try {
                compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
            } catch (OpenDataException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public static CompositeType getArrayType() {
            return compositeType;
        }

        public static String[] getItemNames() {
            return new String[] { ITEM_KEY, ITEM_VALUE };
        }

        public static OpenType<?>[] getItemTypes() {
            return new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING };
        }
    }

    public static final class DirectivesType {

        public static final String TYPE_NAME = "DirectivesType";
        public static final String ITEM_NAME = "directives";

        private static final ArrayType<CompositeType> arrayType;
        static {
            try {
                arrayType = new ArrayType<CompositeType>(1, getRowType());
            } catch (OpenDataException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public static ArrayType<CompositeType> getArrayType() {
            return arrayType;
        }

        public CompositeData[] getCompositeData(Map<String, String> directives) throws OpenDataException {
            CompositeData[] dataArr = new CompositeData[directives.size()];
            String[] itemNames = new String[] { AttributeType.ITEM_KEY, AttributeType.ITEM_VALUE };
            int index = 0;
            for (Entry<String, String> entry : directives.entrySet()) {
                Object[] itemValues = new Object[] { entry.getKey(), entry.getValue() };
                CompositeData data = new CompositeDataSupport(DirectiveType.getCompositeType(), itemNames, itemValues);
                dataArr[index++] = data;
            }
            return dataArr;
        }

        public static String[] getIndexNames() {
            return new String[] { DirectiveType.ITEM_KEY, DirectiveType.ITEM_VALUE };
        }

        public static CompositeType getRowType() throws OpenDataException {
            return DirectiveType.getCompositeType();
        }
    }

    public static final class DirectiveType {

        public static final String TYPE_NAME = "DirectiveType";
        public static final String ITEM_KEY = "key";
        public static final String ITEM_VALUE = "value";

        private static final CompositeType compositeType;
        static {
            try {
                compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
            } catch (OpenDataException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public static CompositeType getCompositeType() {
            return compositeType;
        }

        public static String[] getItemNames() {
            return new String[] { ITEM_KEY, ITEM_VALUE };
        }

        public static OpenType<?>[] getItemTypes() {
            return new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING };
        }
    }
}
