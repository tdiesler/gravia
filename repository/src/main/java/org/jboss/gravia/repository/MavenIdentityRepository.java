package org.jboss.gravia.repository;

import org.jboss.gravia.resource.Resource;

public interface MavenIdentityRepository extends Repository {

    Resource findMavenResource(MavenCoordinates mavenid);

}