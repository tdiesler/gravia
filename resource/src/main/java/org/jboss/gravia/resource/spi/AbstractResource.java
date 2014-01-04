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
package org.jboss.gravia.resource.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.ResourceType;
import org.jboss.gravia.resource.Version;
import org.jboss.gravia.resource.Wiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of a {@link Resource}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public abstract class AbstractResource implements Resource, Serializable {

    public static final Logger LOGGER = LoggerFactory.getLogger(Resource.class.getPackage().getName());

    private static final long serialVersionUID = -3787048558260649200L;

    private final List<AbstractCapability> capabilities = new ArrayList<AbstractCapability>();
    private final List<AbstractRequirement> requirements = new ArrayList<AbstractRequirement>();
    private final AtomicBoolean mutable = new AtomicBoolean(true);
    private Capability identityCapability;
    private ResourceIdentity identity;

    private transient Attachable attachments;
    private transient Wiring wiring;

    void addCapability(AbstractCapability cap) {
        synchronized (capabilities) {
            assertMutable();
            capabilities.add(cap);
        }
    }

    void addRequirement(AbstractRequirement req) {
        synchronized (requirements) {
            assertMutable();
            requirements.add(req);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T adapt(Class<T> type) {
        T result = null;
        if (type.isAssignableFrom(getClass())) {
            result = (T) this;
        } else if (type.isAssignableFrom(CompositeData.class)) {
            result = (T) getCompositeData();
        }
        return result;
    }

    private CompositeData getCompositeData() {
        CompositeData compositeData;
        try {
            ResourceType resourceType = new ResourceType(this);
            compositeData = resourceType.getCompositeData(this);
        } catch (OpenDataException ex) {
            throw new IllegalStateException("Cannot construct composite data for: " + this, ex);
        }
        return compositeData;
    }

    @Override
    public Capability getIdentityCapability() {
        if (identityCapability == null) {
            List<Capability> icaps = getCapabilities(IdentityNamespace.IDENTITY_NAMESPACE);
            if (icaps.size() > 1)
                throw new IllegalStateException("Multiple identity capabilities");
            if (icaps.size() < 1)
                throw new IllegalStateException("No identity capability");
            Capability icap = icaps.get(0);
            Object version = icap.getAttribute(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            if (!(version instanceof Version)) {
                version = version == null ? Version.emptyVersion : Version.parseVersion(version.toString());
                icap.getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);
            }
            identityCapability = icap;
        }
        return identityCapability;
    }

    @Override
    public ResourceIdentity getIdentity() {
        if (identity == null) {
            Capability icap = getIdentityCapability();
            String symbolicName = (String) icap.getAttribute(IdentityNamespace.IDENTITY_NAMESPACE);
            Version version = (Version) icap.getAttribute(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            identity = ResourceIdentity.create(symbolicName, version);
        }
        return identity;
    }

    protected void setMutable(boolean flag) {
        mutable.set(flag);
    }

    protected boolean isMutable() {
        return mutable.get();
    }

    void assertMutable() {
        if (!isMutable())
            throw new IllegalStateException("Invalid access to immutable resource");
    }

    void assertImmutable() {
        if (isMutable())
            throw new IllegalStateException("Invalid access to mutable resource");
    }

    @Override
    public List<Capability> getCapabilities(String namespace) {
        List<Capability> result = new ArrayList<Capability>();
        synchronized (capabilities) {
            for (Capability cap : capabilities) {
                if (namespace == null || namespace.equals(cap.getNamespace())) {
                    result.add(cap);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<Requirement> getRequirements(String namespace) {
        List<Requirement> result = new ArrayList<Requirement>();
        synchronized (requirements) {
            for (Requirement req : requirements) {
                if (namespace == null || namespace.equals(req.getNamespace())) {
                    result.add(req);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Wiring getWiring() {
        return wiring;
    }

    public void setWiring(Wiring wiring) {
        this.wiring = wiring;
    }

    @Override
    public <T> T putAttachment(AttachmentKey<T> key, T value) {
        return getAttachmentsInternal().putAttachment(key, value);
    }

    @Override
    public <T> T getAttachment(AttachmentKey<T> key) {
        return getAttachmentsInternal().getAttachment(key);
    }

    @Override
    public <T> T removeAttachment(AttachmentKey<T> key) {
        return getAttachmentsInternal().getAttachment(key);
    }

    private Attachable getAttachmentsInternal() {
        if (attachments == null) {
            attachments = new AttachableSupport();
        }
        return attachments;
    }

    boolean isValid() {
        try {
            validate();
            return true;
        } catch (RuntimeException rte) {
            return false;
        }
    }

    void validate() {

        // Make sure we have an identity
        getIdentity();

        // Validate the capabilities
        for (Capability cap : getCapabilities(null)) {
            ((AbstractCapability) cap).validate();
        }

        // Validate the requirements
        for (Requirement req : getRequirements(null)) {
            ((AbstractRequirement) req).validate();
        }
    }

    protected String getSimpleTypeName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        ResourceIdentity id = identity;
        String idstr = (id != null ? id.getSymbolicName() + ":" + id.getVersion() : "anonymous");
        return getSimpleTypeName() + "[" + idstr + "]";
    }
}
