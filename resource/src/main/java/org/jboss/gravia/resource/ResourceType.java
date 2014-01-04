/*
 * #%L
 * Gravia Resource
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
package org.jboss.gravia.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/**
 * Open MBean constants for a resource.
 *
 * @author thomas.diesler@jboss.com
 * @since 03-Jan-2014
 */
public final class ResourceType {

    public static final String TYPE_NAME = "ResourceType";
    public static final String ITEM_IDENTITY = "identity";
    public static final String ITEM_CAPABILITIES = "capabilities";
    public static final String ITEM_REQUIREMENTS = "requirements";

    private final CompositeType compositeType;

    public ResourceType(Resource res) throws OpenDataException {
        String[] itemNames = getItemNames(res);
        compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, itemNames, itemNames, getItemTypes(res));
    }

    public CompositeType getCompositeType() {
        return compositeType;
    }

    public CompositeData getCompositeData(Resource res) throws OpenDataException {
        String identity = res.getIdentity().toString();
        List<Capability> caps = res.getCapabilities(null);
        List<Requirement> reqs = res.getRequirements(null);
        List<Object> items = new ArrayList<Object>();
        items.add(identity);
        items.add(new CapabilitiesType(caps).getCompositeData(caps));
        if (reqs.size() > 0) {
            items.add(new RequirementsType(reqs).getCompositeData(reqs));
        }
        Object[] itemValues = items.toArray(new Object[items.size()]);
        return new CompositeDataSupport(compositeType, getItemNames(res), itemValues);
    }

    public static String[] getItemNames(Resource res) {
        List<String> itemNames = new ArrayList<String>();
        itemNames.add(ITEM_IDENTITY);
        itemNames.add(ITEM_CAPABILITIES);
        List<Requirement> reqs = res.getRequirements(null);
        if (reqs.size() > 0) {
            itemNames.add(ITEM_REQUIREMENTS);
        }
        return itemNames.toArray(new String[itemNames.size()]);
    }

    public static OpenType<?>[] getItemTypes(Resource res) throws OpenDataException {
        List<OpenType<?>> itemTypes = new ArrayList<OpenType<?>>();
        itemTypes.add(SimpleType.STRING);

        // Capabilities
        List<Capability> caps = res.getCapabilities(null);
        CompositeType capsType = new CapabilitiesType(caps).getCompositeType();
        itemTypes.add(capsType);

        // Requirements are optional
        List<Requirement> reqs = res.getRequirements(null);
        if (reqs.size() > 0) {
            CompositeType reqsType = new RequirementsType(reqs).getCompositeType();
            itemTypes.add(reqsType);
        }
        return itemTypes.toArray(new OpenType<?>[itemTypes.size()]);
    }

    public static final class CapabilitiesType {

        public static final String TYPE_NAME = "CapabilitiesType";
        public static final String ITEM_NAME = "capabilities";

        private final CompositeType compositeType;

        public CapabilitiesType(List<Capability> caps) throws OpenDataException {
            String[] itemNames = getItemNames(caps);
            compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, itemNames, itemNames, getItemTypes(caps));
        }

        public CompositeType getCompositeType() {
            return compositeType;
        }

        public CompositeData getCompositeData(List<Capability> caps) throws OpenDataException {
            String[] itemNames = getItemNames(caps);
            Object[] itemValues = new Object[caps.size()];
            for(int index = 0; index < caps.size(); index++) {
                Capability cap = caps.get(index);
                itemValues[index] = new CapabilityType().getCompositeData(cap);
            }
            return new CompositeDataSupport(compositeType, itemNames, itemValues);
        }

        public static String[] getItemNames(List<Capability> caps) {
            List<String> itemNames = new ArrayList<String>();
            for(int index = 0; index < caps.size(); index++) {
                itemNames.add("cap#" + index);
            }
            return itemNames.toArray(new String[itemNames.size()]);
        }

        public static OpenType<?>[] getItemTypes(List<Capability> caps) throws OpenDataException {
            OpenType<?>[] itemTypes = new OpenType<?>[caps.size()];
            Arrays.fill(itemTypes, new CapabilityType().getCompositeType());
            return itemTypes;
        }
    }

    public static final class CapabilityType {

        public static final String TYPE_NAME = "CapabilityType";
        public static final String ITEM_NAMESPACE = "namespace";
        public static final String ITEM_ATTRIBUTES = "attributes";
        public static final String ITEM_DIRECTIVES = "directives";

        private final CompositeType compositeType;

        public CapabilityType() throws OpenDataException {
            String[] itemNames = getItemNames();
            compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, itemNames, itemNames, getItemTypes());
        }

        public CompositeType getCompositeType() {
            return compositeType;
        }

        public CompositeData getCompositeData(Capability cap) throws OpenDataException {
            String namespace = cap.getNamespace();
            TabularData attsData = new AttributesType().getTabularData(cap.getAttributes());
            TabularData dirsData = new DirectivesType().getTabularData(cap.getDirectives());
            Object[] itemValues = new Object[] { namespace, attsData, dirsData };
            return new CompositeDataSupport(compositeType, getItemNames(), itemValues);
        }

        public static String[] getItemNames() {
            return new String[] { ITEM_NAMESPACE, ITEM_ATTRIBUTES, ITEM_DIRECTIVES };
        }

        public static OpenType<?>[] getItemTypes() throws OpenDataException {
            TabularType attsType = new AttributesType().getTabularType();
            TabularType dirsType = new DirectivesType().getTabularType();
            return new OpenType<?>[] { SimpleType.STRING, attsType, dirsType };
        }
    }

    public static final class RequirementsType {

        public static final String TYPE_NAME = "RequirementsType";
        public static final String ITEM_NAME = "requirements";

        private final CompositeType compositeType;

        public RequirementsType(List<Requirement> reqs) throws OpenDataException {
            String[] itemNames = getItemNames(reqs);
            compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, itemNames, itemNames, getItemTypes(reqs));
        }

        public CompositeType getCompositeType() {
            return compositeType;
        }

        public CompositeData getCompositeData(List<Requirement> requirements) throws OpenDataException {
            String[] itemNames = getItemNames(requirements);
            Object[] itemValues = new Object[requirements.size()];
            for(int index = 0; index < requirements.size(); index++) {
                Requirement req = requirements.get(index);
                itemValues[index] = new RequirementType().getCompositeData(req);
            }
            return new CompositeDataSupport(compositeType, itemNames, itemValues);
        }

        public static String[] getItemNames(List<Requirement> reqs) {
            List<String> itemNames = new ArrayList<String>();
            for(int index = 0; index < reqs.size(); index++) {
                itemNames.add("req#" + index);
            }
            return itemNames.toArray(new String[itemNames.size()]);
        }

        public static OpenType<?>[] getItemTypes(List<Requirement> reqs) throws OpenDataException {
            OpenType<?>[] itemTypes = new OpenType<?>[reqs.size()];
            Arrays.fill(itemTypes, new RequirementType().getCompositeType());
            return itemTypes;
        }
    }

    public static final class RequirementType {

        public static final String TYPE_NAME = "RequirementType";
        public static final String ITEM_NAMESPACE = "namespace";
        public static final String ITEM_ATTRIBUTES = "attributes";
        public static final String ITEM_DIRECTIVES = "directives";

        private final CompositeType compositeType;

        public RequirementType() throws OpenDataException {
            String[] itemNames = getItemNames();
            compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, itemNames, itemNames, getItemTypes());
        }

        public CompositeType getCompositeType() {
            return compositeType;
        }

        public CompositeData getCompositeData(Requirement req) throws OpenDataException {
            String namespace = req.getNamespace();
            TabularData attsData = new AttributesType().getTabularData(req.getAttributes());
            TabularData dirsData = new DirectivesType().getTabularData(req.getDirectives());
            Object[] itemValues = new Object[] { namespace, attsData, dirsData };
            return new CompositeDataSupport(compositeType, getItemNames(), itemValues);
        }

        public static String[] getItemNames() {
            return new String[] { ITEM_NAMESPACE, ITEM_ATTRIBUTES, ITEM_DIRECTIVES };
        }

        public static OpenType<?>[] getItemTypes() throws OpenDataException {
            TabularType attsType = new AttributesType().getTabularType();
            TabularType dirsType = new DirectivesType().getTabularType();
            return new OpenType<?>[] { SimpleType.STRING, attsType, dirsType };
        }
    }

    public static final class AttributesType {

        public static final String TYPE_NAME = "AttributesType";
        public static final String ITEM_NAME = "attributes";

        private final TabularType tabularType;

        public AttributesType() throws OpenDataException {
            tabularType = new TabularType(TYPE_NAME, TYPE_NAME, getRowType(), getIndexNames());
        }

        public TabularType getTabularType() {
            return tabularType;
        }

        public TabularData getTabularData(Map<String, Object> attributes) throws OpenDataException {
            TabularDataSupport tabularData = new TabularDataSupport(tabularType);
            for (Entry<String, Object> entry : attributes.entrySet()) {
                String[] itemNames = new String[] { AttributeType.ITEM_KEY, AttributeType.ITEM_VALUE };
                Object[] itemValues = new Object[] { entry.getKey(), entry.getValue().toString() };
                CompositeData data = new CompositeDataSupport(new AttributeType().getCompositeType(), itemNames, itemValues);
                tabularData.put(data);
            }
            return tabularData;
        }

        public static String[] getIndexNames() {
            return new String[] { AttributeType.ITEM_KEY, AttributeType.ITEM_VALUE };
        }

        public static CompositeType getRowType() throws OpenDataException {
            return new AttributeType().getCompositeType();
        }
    }

    public static final class AttributeType {

        public static final String TYPE_NAME = "AttributeType";
        public static final String ITEM_KEY = "key";
        public static final String ITEM_VALUE = "value";

        private final CompositeType compositeType;

        public AttributeType() throws OpenDataException {
            compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
        }

        public CompositeType getCompositeType() {
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

        private final TabularType tabularType;

        public DirectivesType() throws OpenDataException {
            tabularType = new TabularType(TYPE_NAME, TYPE_NAME, getRowType(), getIndexNames());
        }

        public TabularType getTabularType() {
            return tabularType;
        }

        public TabularData getTabularData(Map<String, String> directives) throws OpenDataException {
            TabularDataSupport tabularData = new TabularDataSupport(tabularType);
            for (Entry<String, String> entry : directives.entrySet()) {
                String[] itemNames = getIndexNames();
                Object[] itemValues = new Object[] { entry.getKey(), entry.getValue() };
                CompositeData data = new CompositeDataSupport(new DirectiveType().getCompositeType(), itemNames, itemValues);
                tabularData.put(data);
            }
            return tabularData;
        }

        public static String[] getIndexNames() {
            return new String[] { DirectiveType.ITEM_KEY, DirectiveType.ITEM_VALUE };
        }

        public static CompositeType getRowType() throws OpenDataException {
            return new DirectiveType().getCompositeType();
        }
    }

    public static final class DirectiveType {

        public static final String TYPE_NAME = "DirectiveType";
        public static final String ITEM_KEY = "key";
        public static final String ITEM_VALUE = "value";

        private final CompositeType compositeType;

        public DirectiveType() throws OpenDataException {
            compositeType = new CompositeType(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
        }

        public CompositeType getCompositeType() {
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
