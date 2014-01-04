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
@SuppressWarnings("serial")
public final class ResourceType extends CompositeType {

    public static final String TYPE_NAME = "ResourceType";
    public static final String ITEM_IDENTITY = "identity";
    public static final String ITEM_CAPABILITIES = "capabilities";
    public static final String ITEM_REQUIREMENTS = "requirements";

    private final Resource resource;

    public ResourceType(Resource resource) throws OpenDataException {
        super(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes(resource));
        this.resource = resource;
    }

    public CompositeData getCompositeData() throws OpenDataException {
        CapabilitiesType capsType = (CapabilitiesType) getType(ITEM_CAPABILITIES);
        RequirementsType reqsType = (RequirementsType) getType(ITEM_REQUIREMENTS);
        String identity = resource.getIdentity().toString();
        CompositeData capsData = capsType.getCompositeData();
        CompositeData reqsData = reqsType.getCompositeData();
        Object[] itemValues = new Object[] { identity, capsData, reqsData };
        return new CompositeDataSupport(this, getItemNames(), itemValues);
    }

    private static String[] getItemNames() {
        return new String[] { ITEM_IDENTITY, ITEM_CAPABILITIES, ITEM_REQUIREMENTS };
    }

    private static OpenType<?>[] getItemTypes(Resource res) throws OpenDataException {
        List<Capability> caps = res.getCapabilities(null);
        List<Requirement> reqs = res.getRequirements(null);
        return new OpenType<?>[] { SimpleType.STRING, new CapabilitiesType(caps), new RequirementsType(reqs) };
    }

    public static final class CapabilitiesType extends CompositeType {

        public static final String TYPE_NAME = "CapabilitiesType";
        public static final String ITEM_NAME = "capabilities";

        private final List<Capability> capabilities;

        public CapabilitiesType(List<Capability> caps) throws OpenDataException {
            super(TYPE_NAME, TYPE_NAME, getItemNames(caps), getItemNames(caps), getItemTypes(caps));
            this.capabilities = caps;
        }

        public CompositeData getCompositeData() throws OpenDataException {
            String[] itemNames = getItemNames(capabilities);
            Object[] itemValues = new Object[capabilities.size()];
            for(int index = 0; index < capabilities.size(); index++) {
                CapabilityType captype = (CapabilityType) getType(itemNames[index]);
                itemValues[index] = captype.getCompositeData();
            }
            return new CompositeDataSupport(this, itemNames, itemValues);
        }

        private static String[] getItemNames(List<Capability> caps) {
            List<String> itemNames = new ArrayList<String>();
            for(int index = 0; index < caps.size(); index++) {
                itemNames.add("cap#" + index);
            }
            return itemNames.toArray(new String[itemNames.size()]);
        }

        private static OpenType<?>[] getItemTypes(List<Capability> caps) throws OpenDataException {
            List<OpenType<?>> itemTypes = new ArrayList<OpenType<?>>();
            for (Capability cap : caps) {
                itemTypes.add(new CapabilityType(cap));
            }
            return itemTypes.toArray(new OpenType<?>[itemTypes.size()]);
        }
    }

    public static final class CapabilityType extends CompositeType {

        public static final String TYPE_NAME = "CapabilityType";
        public static final String ITEM_NAMESPACE = "namespace";
        public static final String ITEM_ATTRIBUTES = "attributes";
        public static final String ITEM_DIRECTIVES = "directives";

        private final Capability capability;

        public CapabilityType(Capability capability) throws OpenDataException {
            super(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes(capability));
            this.capability = capability;
        }

        public CompositeData getCompositeData() throws OpenDataException {
            AttributesType attsType = (AttributesType) getType(ITEM_ATTRIBUTES);
            DirectivesType dirsType = (DirectivesType) getType(ITEM_DIRECTIVES);
            String namespace = capability.getNamespace();
            TabularData attsData = attsType.getTabularData();
            TabularData dirsData = dirsType.getTabularData();
            Object[] itemValues = new Object[] { namespace, attsData, dirsData };
            return new CompositeDataSupport(this, getItemNames(), itemValues);
        }

        private static String[] getItemNames() {
            return new String[] { ITEM_NAMESPACE, ITEM_ATTRIBUTES, ITEM_DIRECTIVES };
        }

        private static OpenType<?>[] getItemTypes(Capability cap) throws OpenDataException {
            return new OpenType<?>[] { SimpleType.STRING, new AttributesType(cap.getAttributes()), new DirectivesType(cap.getDirectives()) };
        }
    }

    public static final class RequirementsType extends CompositeType {

        public static final String TYPE_NAME = "RequirementsType";
        public static final String ITEM_NAME = "requirements";

        private final List<Requirement> requirements;

        public RequirementsType(List<Requirement> reqs) throws OpenDataException {
            super(TYPE_NAME, TYPE_NAME, getItemNames(reqs), getItemNames(reqs), getItemTypes(reqs));
            this.requirements = reqs;
        }

        public CompositeData getCompositeData() throws OpenDataException {
            String[] itemNames = getItemNames(requirements);
            Object[] itemValues = new Object[requirements.size()];
            for(int index = 0; index < requirements.size(); index++) {
                RequirementType reqtype = (RequirementType) getType(itemNames[index]);
                itemValues[index] = reqtype.getCompositeData();
            }
            return new CompositeDataSupport(this, itemNames, itemValues);
        }

        private static String[] getItemNames(List<Requirement> reqs) {
            List<String> itemNames = new ArrayList<String>();
            for(int index = 0; index < reqs.size(); index++) {
                itemNames.add("req#" + index);
            }
            return itemNames.toArray(new String[itemNames.size()]);
        }

        private static OpenType<?>[] getItemTypes(List<Requirement> reqs) throws OpenDataException {
            List<OpenType<?>> itemTypes = new ArrayList<OpenType<?>>();
            for (Requirement req : reqs) {
                itemTypes.add(new RequirementType(req));
            }
            return itemTypes.toArray(new OpenType<?>[itemTypes.size()]);
        }
    }

    public static final class RequirementType extends CompositeType {

        public static final String TYPE_NAME = "RequirementType";
        public static final String ITEM_NAMESPACE = "namespace";
        public static final String ITEM_ATTRIBUTES = "attributes";
        public static final String ITEM_DIRECTIVES = "directives";

        private final Requirement requirement;

        public RequirementType(Requirement req) throws OpenDataException {
            super(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes(req));
            this.requirement = req;
        }

        public CompositeData getCompositeData() throws OpenDataException {
            AttributesType attsType = (AttributesType) getType(ITEM_ATTRIBUTES);
            DirectivesType dirsType = (DirectivesType) getType(ITEM_DIRECTIVES);
            String namespace = requirement.getNamespace();
            TabularData attsData = attsType.getTabularData();
            TabularData dirsData = dirsType.getTabularData();
            Object[] itemValues = new Object[] { namespace, attsData, dirsData };
            return new CompositeDataSupport(this, getItemNames(), itemValues);
        }

        private static String[] getItemNames() {
            return new String[] { ITEM_NAMESPACE, ITEM_ATTRIBUTES, ITEM_DIRECTIVES };
        }

        private static OpenType<?>[] getItemTypes(Requirement req) throws OpenDataException {
            return new OpenType<?>[] { SimpleType.STRING, new AttributesType(req.getAttributes()), new DirectivesType(req.getDirectives()) };
        }
    }

    public static final class AttributesType extends TabularType {

        public static final String TYPE_NAME = "AttributesType";
        public static final String ITEM_NAME = "attributes";

        private final Map<String, Object> attributes;

        public AttributesType(Map<String, Object> attributes) throws OpenDataException {
            super(TYPE_NAME, TYPE_NAME, new AttributeType(), getIndexNames(attributes));
            this.attributes = attributes;
        }

        public TabularData getTabularData() throws OpenDataException {
            TabularDataSupport tabularData = new TabularDataSupport(this);
            for (Entry<String, Object> entry : attributes.entrySet()) {
                String[] itemNames = new String[] { AttributeType.ITEM_KEY, AttributeType.ITEM_VALUE };
                Object[] itemValues = new Object[] { entry.getKey(), entry.getValue().toString() };
                CompositeData data = new CompositeDataSupport(new AttributeType(), itemNames, itemValues);
                tabularData.put(data);
            }
            return tabularData;
        }

        private static String[] getIndexNames(Map<String, Object> attributes) {
            return new String[] { AttributeType.ITEM_KEY, AttributeType.ITEM_VALUE };
        }
    }

    public static final class AttributeType extends CompositeType {

        public static final String TYPE_NAME = "AttributeType";
        public static final String ITEM_KEY = "key";
        public static final String ITEM_VALUE = "value";

        public AttributeType() throws OpenDataException {
            super(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
        }

        private static String[] getItemNames() {
            return new String[] { ITEM_KEY, ITEM_VALUE };
        }

        private static OpenType<?>[] getItemTypes() {
            return new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING };
        }
    }

    public static final class DirectivesType extends TabularType {

        public static final String TYPE_NAME = "DirectivesType";
        public static final String ITEM_NAME = "directives";

        private final Map<String, String> directives;

        public DirectivesType(Map<String, String> directives) throws OpenDataException {
            super(TYPE_NAME, TYPE_NAME, new DirectiveType(), getIndexNames(directives));
            this.directives = directives;
        }

        public TabularData getTabularData() throws OpenDataException {
            TabularDataSupport tabularData = new TabularDataSupport(this);
            for (Entry<String, String> entry : directives.entrySet()) {
                String[] itemNames = new String[] { DirectiveType.ITEM_KEY, DirectiveType.ITEM_VALUE };
                Object[] itemValues = new Object[] { entry.getKey(), entry.getValue() };
                CompositeData data = new CompositeDataSupport(new DirectiveType(), itemNames, itemValues);
                tabularData.put(data);
            }
            return tabularData;
        }

        private static String[] getIndexNames(Map<String, String> directives) {
            return new String[] { DirectiveType.ITEM_KEY, DirectiveType.ITEM_VALUE };
        }
    }

    public static final class DirectiveType extends CompositeType {

        public static final String TYPE_NAME = "DirectiveType";
        public static final String ITEM_KEY = "key";
        public static final String ITEM_VALUE = "value";

        public DirectiveType() throws OpenDataException {
            super(TYPE_NAME, TYPE_NAME, getItemNames(), getItemNames(), getItemTypes());
        }

        private static String[] getItemNames() {
            return new String[] { ITEM_KEY, ITEM_VALUE };
        }

        private static OpenType<?>[] getItemTypes() {
            return new OpenType<?>[] { SimpleType.STRING, SimpleType.STRING };
        }
    }
}
