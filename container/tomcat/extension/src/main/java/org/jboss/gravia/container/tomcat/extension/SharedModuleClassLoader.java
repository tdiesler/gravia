package org.jboss.gravia.container.tomcat.extension;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentCapability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.utils.NotNullException;

/**
 * The shared module {@link ClassLoader}.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 13-Jan-2014
 */
public final class SharedModuleClassLoader extends ClassLoader {

    private final static List<URL> urls = new ArrayList<URL>();

    SharedModuleClassLoader(ClassLoader parent) {
        super(getExtendedParent(parent));
    }

    private static ClassLoader getExtendedParent(ClassLoader parent) {
        synchronized (urls) {
            if (urls.isEmpty()) {
                return parent;
            } else {
                URL[] urlarr = urls.toArray(new URL[urls.size()]);
                return new URLClassLoader(urlarr, parent);
            }
        }
    }

    public static void addSharedModule(Resource resource) {
        NotNullException.assertValue(resource, "resource");

        List<Capability> ccaps = resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
        if (ccaps.isEmpty())
            throw new IllegalArgumentException("Cannot obtain content capability from: " + resource);

        ContentCapability ccap = (ContentCapability) ccaps.get(0);
        URL contentURL = ccap.getContentURL();
        if (contentURL == null)
            throw new IllegalArgumentException("Cannot obtain content URL from: " + ccap);

        synchronized (urls) {
            urls.add(contentURL);
        }
    }
}