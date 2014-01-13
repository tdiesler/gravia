/*
 * #%L
 * Gravia :: Runtime :: API
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
package org.jboss.gravia.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A utility class for IO operations.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-Sep-2010
 */
public final class IOUtils {

    // Hide ctor
    private IOUtils() {
    }

    public static long copyStream(InputStream input, OutputStream output) throws IOException {
        int len = 0;
        long total = 0;
        byte[] buf = new byte[4096];
        while ((len = input.read(buf)) >= 0) {
            output.write(buf, 0, len);
            total += len;
        }
        input.close();
        output.close();
        return total;
    }
}