package org.jboss.gravia.repository.spi;
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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helpers for repository content
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2012
 */
public final class RepositoryContentHelper {

    public static final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";
    
    // Hide ctor
    private RepositoryContentHelper() {
    }

    /**
     * Get the digest for a given input stream using the default algorithm
     */
    public static String getDigest(InputStream input) throws IOException, NoSuchAlgorithmException {
        return getDigest(input, DEFAULT_DIGEST_ALGORITHM);
    }
    
    /**
     * Get the digest for a given input stream and algorithm
     */
    public static String getDigest(InputStream input, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        try {
            int nread = 0;
            byte[] dataBytes = new byte[1024];
            while ((nread = input.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        } finally {
            input.close();
        }
        StringBuilder builder = new StringBuilder();
        for (byte b : md.digest()) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
