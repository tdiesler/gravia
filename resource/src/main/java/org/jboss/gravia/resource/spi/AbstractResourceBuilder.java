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

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.VersionRange;
import org.jboss.gravia.resource.spi.AttributeValueHandler.AttributeValue;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.jboss.gravia.utils.MavenUtils;

/**
 * An abstract {@link Resource} builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 *
 * @NotThreadSafe
 */
public abstract class AbstractResourceBuilder implements ResourceBuilder {

    private AbstractResource resource;

    protected abstract AbstractResource createResource();

    protected abstract AbstractCapability createCapability(AbstractResource resource, String namespace, Map<String, Object> attributes, Map<String, String> directives);

    protected abstract AbstractRequirement createRequirement(AbstractResource resource, String namespace, Map<String, Object> attributes, Map<String, String> directives);

    @Override
    public Capability addIdentityCapability(ResourceIdentity identity) {
        IllegalArgumentAssertion.assertNotNull(identity, "identity");
        return addIdentityCapability(identity.getSymbolicName(), identity.getVersion());
    }

    @Override
    public Capability addIdentityCapability(String symbolicName, String version) {
        IllegalArgumentAssertion.assertNotNull(symbolicName, "symbolicName");
        return addIdentityCapability(symbolicName, version != null ? Version.parseVersion(version) : null, null, null);
    }

    @Override
    public Capability addIdentityCapability(String symbolicName, Version version) {
        IllegalArgumentAssertion.assertNotNull(symbolicName, "symbolicName");
        return addIdentityCapability(symbolicName, version, null, null);
    }

    public Capability addIdentityCapability(MavenCoordinates mavenid) {
        Capability icap = addIdentityCapability(MavenUtils.getSymbolicName(mavenid), MavenUtils.getVersion(mavenid), null, null);
        icap.getAttributes().put(ContentNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE, mavenid);
        return icap;
    }

    @Override
    public Capability addIdentityCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs) {
        IllegalArgumentAssertion.assertNotNull(symbolicName, "symbolicName");
        Capability icap = addCapability(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
        if (version != null) {
            icap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);
        }
        if (atts != null) {
            icap.getAttributes().putAll(atts);
        }
        if (dirs != null) {
            icap.getDirectives().putAll(dirs);
        }
        return icap;
    }

    @Override
    public Capability addContentCapability(InputStream content) {
        IllegalArgumentAssertion.assertNotNull(content, "content");
        Map<String, Object> atts = Collections.singletonMap(ContentNamespace.CAPABILITY_STREAM_ATTRIBUTE, (Object)content);
        return addCapability(ContentNamespace.CONTENT_NAMESPACE, atts, null);
    }

    @Override
    public Capability addContentCapability(InputStream content, Map<String, Object> atts, Map<String, String> dirs) {
        IllegalArgumentAssertion.assertNotNull(content, "content");
        Map<String, Object> exatts = new LinkedHashMap<String, Object>();
        if (atts != null) {
            exatts.putAll(atts);
        }
        exatts.put(ContentNamespace.CAPABILITY_STREAM_ATTRIBUTE, content);
        return addCapability(ContentNamespace.CONTENT_NAMESPACE, exatts, dirs);
    }

    @Override
    public Capability addContentCapability(URL contentURL) {
        IllegalArgumentAssertion.assertNotNull(contentURL, "contentURL");
        Map<String, Object> atts = Collections.singletonMap(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, (Object)contentURL);
        return addCapability(ContentNamespace.CONTENT_NAMESPACE, atts, null);
    }

    @Override
    public Capability addContentCapability(URL contentURL, Map<String, Object> atts, Map<String, String> dirs) {
        IllegalArgumentAssertion.assertNotNull(contentURL, "contentURL");
        Map<String, Object> exatts = new LinkedHashMap<String, Object>();
        if (atts != null) {
            exatts.putAll(atts);
        }
        exatts.put(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, contentURL);
        return addCapability(ContentNamespace.CONTENT_NAMESPACE, exatts, dirs);
    }

    @Override
    public Capability addCapability(String namespace, String nsvalue) {
        IllegalArgumentAssertion.assertNotNull(namespace, "namespace");
        IllegalArgumentAssertion.assertNotNull(nsvalue, "nsvalue");
        return addCapability(namespace, Collections.singletonMap(namespace, (Object)nsvalue), null);
    }

    @Override
    public Capability addCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        IllegalArgumentAssertion.assertNotNull(namespace, "namespace");
        AbstractResource resource = getResourceInternal();
        AbstractCapability cap = createCapability(resource, namespace, mutableAttributes(atts), mutableDirectives(dirs));
        resource.addCapability(cap);
        return cap;
    }

    @Override
    public Requirement addIdentityRequirement(String symbolicName) {
        IllegalArgumentAssertion.assertNotNull(symbolicName, "symbolicName");
        return addIdentityRequirement(symbolicName, null, null,  null);
    }

    @Override
    public Requirement addIdentityRequirement(String symbolicName, String range) {
        IllegalArgumentAssertion.assertNotNull(symbolicName, "symbolicName");
        return addIdentityRequirement(symbolicName, range != null ? new VersionRange(range) : null, null,  null);
    }

    @Override
    public Requirement addIdentityRequirement(String symbolicName, VersionRange range) {
        IllegalArgumentAssertion.assertNotNull(symbolicName, "symbolicName");
        return addIdentityRequirement(symbolicName, range, null,  null);
    }

    @Override
    public Requirement addIdentityRequirement(String symbolicName, VersionRange range, Map<String, Object> atts, Map<String, String> dirs) {
        IllegalArgumentAssertion.assertNotNull(symbolicName, "symbolicName");
        Requirement ireq = addRequirement(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
        if (range != null) {
            ireq.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, range);
        }
        if (atts != null) {
            ireq.getAttributes().putAll(atts);
        }
        if (dirs != null) {
            ireq.getDirectives().putAll(dirs);
        }
        return ireq;
    }

    @Override
    public Requirement addRequirement(String namespace, String nsvalue) {
        IllegalArgumentAssertion.assertNotNull(namespace, "namespace");
        IllegalArgumentAssertion.assertNotNull(nsvalue, "nsvalue");
        return addRequirement(namespace, Collections.singletonMap(namespace, (Object)nsvalue), null);
    }

    @Override
    public Requirement addRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs) {
        IllegalArgumentAssertion.assertNotNull(namespace, "namespace");
        AbstractResource resource = getResourceInternal();
        AbstractRequirement req = createRequirement(resource, namespace, mutableAttributes(atts), mutableDirectives(dirs));
        resource.addRequirement(req);
        return req;
    }

    @Override
    public boolean isValid() {
        AbstractResource resource = getResourceInternal();
        return resource.isValid();
    }

    @Override
    public Resource getCurrentResource() {
        AbstractResource resource = getResourceInternal();
        resource.assertMutable();
        return resource;
    }

    @Override
    public Resource getResource() {
        AbstractResource resource = getResourceInternal();
        resource.validate();
        resource.setMutable(false);
        return resource;
    }

    protected String parseParameterizedValue(String line, Map<String, Object> atts, Map<String, String> dirs) {
        IllegalArgumentAssertion.assertNotNull(line, "line");
        String mainvalue = null;
        for (String part : ElementParser.parseDelimitedString(line, ';', true)) {
            if (part.indexOf(":=") > 0) {
                int index = part.indexOf(":=");
                String key = part.substring(0, index);
                String value = unquote(part.substring(index + 2));
                dirs.put(key.trim(), value);
            } else if (part.indexOf('=') > 0) {
                int index = part.indexOf('=');
                String keystr = part.substring(0, index);
                Object value = getAttributeValue(keystr, part.substring(index + 1));
                atts.put(getAttributeKey(keystr), value);
            } else if (mainvalue == null) {
                mainvalue = part;
            } else {
                throw new IllegalArgumentException("Cannot parse: " + line);
            }
        }
        return mainvalue;
    }

    private String getAttributeKey(String keystr) {
        String[] parts = keystr.split(":");
        return parts[0].trim();
    }

    private Object getAttributeValue(String key, String valstr) {
        String[] parts = key.split(":");
        if (parts.length == 1) {
            return unquote(valstr);
        }
        String typespec = parts[1].trim();
        if (typespec.startsWith("List")) {
            parts = typespec.split("[<>]");
            typespec = "List<" + (parts.length > 1 ? parts[1].trim() : "String") + ">";
        }
        AttributeValue attval = AttributeValueHandler.readAttributeValue(key, typespec, unquote(valstr));
        return attval.getValue();
    }

    private String unquote(String value) {
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private Map<String, Object> mutableAttributes(Map<String, Object> atts) {
        return new LinkedHashMap<String, Object>(atts != null ? atts : new LinkedHashMap<String, Object>());
    }

    private Map<String, String> mutableDirectives(Map<String, String> dirs) {
        return new LinkedHashMap<String, String>(dirs != null ? dirs : new LinkedHashMap<String, String>());
    }

    private AbstractResource getResourceInternal() {
        if (resource == null) {
            resource = createResource();
        }
        return resource;
    }

}
