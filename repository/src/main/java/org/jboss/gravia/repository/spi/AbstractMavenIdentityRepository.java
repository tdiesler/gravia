/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
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
package org.jboss.gravia.repository.spi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.gravia.repository.ContentNamespace;
import org.jboss.gravia.repository.MavenCoordinates;
import org.jboss.gravia.repository.MavenIdentityRepository;
import org.jboss.gravia.repository.MavenResourceBuilder;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.util.DefaultPropertiesProvider;

/**
 * A simple {@link Repository} that delegates to a maven repositories.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public abstract class AbstractMavenIdentityRepository extends AbstractRepository implements MavenIdentityRepository {

    private final List<URL> baserepos;

    /** The configuration for the {@link AbstractMavenIdentityRepository} */
    public interface Configuration {

        /** The default JBoss Nexus repository: http://repository.jboss.org/nexus/content/groups/public */
        String JBOSS_NEXUS_BASE = "http://repository.jboss.org/nexus/content/groups/public";

        /** The default Maven Central repository: http://repo1.maven.org/maven2 */
        String MAVEN_CENTRAL_BASE = "http://repo1.maven.org/maven2";

        /** Get the list of configured base URLs */
        List<URL> getBaseURLs();
    }

    public AbstractMavenIdentityRepository() {
        this(new DefaultPropertiesProvider());
    }

    public AbstractMavenIdentityRepository(PropertiesProvider propertyProvider) {
        Configuration configuration = getConfiguration(propertyProvider);
        baserepos = Collections.unmodifiableList(configuration.getBaseURLs());
    }

    /**
     * Get the default configuration which delegates to
     *
     * #1 The local maven repository at ~/.m2/repository
     * #2 The default JBoss Nexus repository
     * #3 The default Maven Central repository
     */
    protected Configuration getConfiguration(final PropertiesProvider propertyProvider) {
        return new Configuration() {
            @Override
            public List<URL> getBaseURLs() {
                List<URL> result = new ArrayList<URL>();
                String property = (String) propertyProvider.getProperty(PROPERTY_MAVEN_REPOSITORY_BASE_URLS);
                if (property == null) {
                    property = "";
                    String userhome = SecurityActions.getSystemProperty("user.home", "");
                    File localrepo = new File(userhome + File.separator + ".m2" + File.separator + "repository");
                    if (localrepo.isDirectory()) {
                        property += localrepo.toURI().toString() + ",";
                    }
                    property += JBOSS_NEXUS_BASE + ",";
                    property += MAVEN_CENTRAL_BASE;
                }
                for (String urlspec : property.split(",")) {
                    result.add(getBaseURL(urlspec));
                }
                return Collections.unmodifiableList(result);
            }
        };
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {

        String attval = (String) req.getAttribute(IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE);
        if (attval == null)
            return Collections.emptyList();

        MavenCoordinates mavenid = MavenCoordinates.parse(attval);
        Resource resource = findMavenResource(mavenid);
        if (resource == null)
            return Collections.emptyList();

        return Collections.singleton(resource.getIdentityCapability());
    }

    @Override
    public Resource findMavenResource(MavenCoordinates mavenid) {
        LOGGER.info("Find maven providers for: {}", mavenid);

        URL contentURL = null;
        for (URL baseURL : baserepos) {
            URL url = mavenid.getArtifactURL(baseURL);
            try {
                url.openStream().close();
                contentURL = url;
                break;
            } catch (IOException e) {
                LOGGER.debug("Cannot access input stream for: {}", url);
            }
        }

        Resource result = null;
        if (contentURL != null) {
            MavenResourceBuilder builder = new MavenResourceBuilder();
            builder.addIdentityCapability(mavenid);
            Capability ccap = builder.addCapability(ContentNamespace.CONTENT_NAMESPACE, null, null);
            ccap.getAttributes().put(ContentNamespace.CAPABILITY_URL_ATTRIBUTE, contentURL.toExternalForm());
            LOGGER.debug("Found maven resource: {}", result = builder.getResource());
        }

        return result;
    }

    private static URL getBaseURL(String urlspec) {
        try {
            return new URL(urlspec);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid repository base: " + urlspec);
        }
    }
}
