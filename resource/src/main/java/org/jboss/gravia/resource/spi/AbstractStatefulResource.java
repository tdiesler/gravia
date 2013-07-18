package org.jboss.gravia.resource.spi;

import java.util.List;

import org.jboss.gravia.resource.Attachable;
import org.jboss.gravia.resource.AttachmentKey;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.resource.StatefulResource;
import org.jboss.gravia.resource.Wiring;

/**
 * An abstract {@link StatefulResource}
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public class AbstractStatefulResource implements StatefulResource {

    private static AttachmentKey<Wiring> WIRING_KEY = AttachmentKey.create(Wiring.class);
    
    private final Resource resource;
    private Attachable attachments; 

    public AbstractStatefulResource(Resource resource) {
        if (resource == null)
            throw new IllegalArgumentException("Null resource");
        this.resource = resource;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T adapt(Class<T> type) {
        T result = null;
        if (type.isAssignableFrom(getClass())) {
            result = (T) this;
        }
        return result;
    }
    
    @Override
    public ResourceIdentity getIdentity() {
        return resource.getIdentity();
    }

    @Override
    public Capability getIdentityCapability() {
        return resource.getIdentityCapability();
    }

    @Override
    public List<Capability> getCapabilities(String namespace) {
        return resource.getCapabilities(namespace);
    }

    @Override
    public List<Requirement> getRequirements(String namespace) {
        return resource.getRequirements(namespace);
    }

    public Wiring getWiring() {
        return getAttachment(WIRING_KEY);
    }

    public void addWiring(Wiring wiring) {
        putAttachment(WIRING_KEY, wiring);
    }

    public Wiring removeWiring() {
        return removeAttachment(WIRING_KEY);
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
}
