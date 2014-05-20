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

import static org.jboss.gravia.resource.ContentNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;

import org.jboss.gravia.resource.CompositeDataResourceType.AttributeType;
import org.jboss.gravia.resource.CompositeDataResourceType.CapabilityType;
import org.jboss.gravia.resource.CompositeDataResourceType.DirectiveType;
import org.jboss.gravia.resource.CompositeDataResourceType.RequirementType;


/**
 * A {@link Resource} builder for {@link CompositeData}.
 *
 * @author thomas.diesler@jboss.com
 * @since 03-Jan-2014
 *
 * @NotThreadSafe
 */
public final class CompositeDataResourceBuilder extends DefaultResourceBuilder {

    public CompositeDataResourceBuilder(CompositeData resData) {
        CompositeData[] capsData = (CompositeData[]) resData.get(CompositeDataResourceType.ITEM_CAPABILITIES);
        for(CompositeData capData : capsData) {
            String namespace = (String) capData.get(CapabilityType.ITEM_NAMESPACE);
            CompositeData[] attsData = (CompositeData[]) capData.get(CapabilityType.ITEM_ATTRIBUTES);
            Map<String, Object> atts = getAttributes(attsData);
            CompositeData[] dirsData = (CompositeData[]) capData.get(CapabilityType.ITEM_DIRECTIVES);
            Map<String, String> dirs = getDirectives(dirsData);
            addCapability(namespace, atts, dirs);
        }
        CompositeData[] reqsData = (CompositeData[]) resData.get(CompositeDataResourceType.ITEM_REQUIREMENTS);
        for(CompositeData reqData : reqsData) {
            String namespace = (String) reqData.get(CapabilityType.ITEM_NAMESPACE);
            CompositeData[] attsData = (CompositeData[]) reqData.get(RequirementType.ITEM_ATTRIBUTES);
            Map<String, Object> atts = getAttributes(attsData);
            CompositeData[] dirsData = (CompositeData[]) reqData.get(RequirementType.ITEM_DIRECTIVES);
            Map<String, String> dirs = getDirectives(dirsData);
            addRequirement(namespace, atts, dirs);
        }
    }

    private Map<String, Object> getAttributes(CompositeData[] attsData) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for(CompositeData attData : attsData) {
            String key = (String) attData.get(AttributeType.ITEM_KEY);
            Object value = attData.get(AttributeType.ITEM_VALUE);
            if (CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE.equals(key)) {
                value = MavenCoordinates.parse((String) value);
            }
            result.put(key, value);
        }
        return result;
    }

    private Map<String, String> getDirectives(CompositeData[] dirsData) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for(CompositeData dirData : dirsData) {
            String key = (String) dirData.get(DirectiveType.ITEM_KEY);
            String value = (String) dirData.get(DirectiveType.ITEM_VALUE);
            result.put(key, value);
        }
        return result;
    }
}
