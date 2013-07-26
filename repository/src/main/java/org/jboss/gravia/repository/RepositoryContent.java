/*
 * #%L
 * Gravia Repository
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
package org.jboss.gravia.repository;

import java.io.InputStream;

import org.jboss.gravia.resource.Resource;

/**
 * An accessor for the default content of a resource.
 *
 * All {@link Resource} objects which represent non-abstract resources in a
 * {@link Repository} must be adaptable to this interface.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2012
 */
public interface RepositoryContent {

    /**
     * Returns a new input stream to the default format of this resource.
     *
     * @return A new input stream for associated resource.
     */
    InputStream getContent();
}
