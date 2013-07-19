package org.jboss.gravia.resource;

import java.util.Map;

/**
 * A {@link Requirement} builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public interface RequirementBuilder {

    Map<String, Object> getAttributes();

    Map<String, String> getDirectives();

    Requirement getRequirement();

}