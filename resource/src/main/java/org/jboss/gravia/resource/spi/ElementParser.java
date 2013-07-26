/*
 * #%L
 * JBossOSGi Resolver Metadata
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

import java.util.ArrayList;
import java.util.List;

/**
 * ElementParser.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 07-Nov-2012
 */
public final class ElementParser {

	// Hide ctor
    private ElementParser() {
	}

    public static List<String> parseDelimitedString(String value, char delim) {
        return parseDelimitedString(value, delim, true);
    }
    
	/**
     * Parses delimited string and returns an array containing the tokens. This parser obeys quotes, so the delimiter character
     * will be ignored if it is inside of a quote. This method assumes that the quote character is not included in the set of
     * delimiter characters.
     *
     * @param value the delimited string to parse.
     * @param delim the characters delimiting the tokens.
     * @param trim whether to trim the parts.
     * @return an array of string tokens or null if there were no tokens.
     **/
    public static List<String> parseDelimitedString(String value, char delim, boolean trim) {
        if (value == null)
            value = "";

        List<String> list = new ArrayList<String>();

        int CHAR = 1;
        int DELIMITER = 2;
        int STARTQUOTE = 4;
        int ENDQUOTE = 8;

        StringBuilder sb = new StringBuilder();

        int expecting = (CHAR | DELIMITER | STARTQUOTE);

        for (int i = 0; i < value.length(); i++) {
            char p = i > 0 ? value.charAt(i - 1) : 0;
            char c = value.charAt(i);

            boolean isDelimiter = (delim == c) && (p != '\\');
            boolean isQuote = ((c == '"') || (c == '\'')) && (p != '\\');
            
            if (isDelimiter && ((expecting & DELIMITER) > 0)) {
                addPart(list, sb, trim);
                sb.delete(0, sb.length());
                expecting = (CHAR | DELIMITER | STARTQUOTE);
            } else if (isQuote && ((expecting & STARTQUOTE) > 0)) {
                sb.append(c);
                expecting = CHAR | ENDQUOTE;
            } else if (isQuote && ((expecting & ENDQUOTE) > 0)) {
                sb.append(c);
                expecting = (CHAR | STARTQUOTE | DELIMITER);
            } else if ((expecting & CHAR) > 0) {
                sb.append(c);
            } else {
                throw new IllegalArgumentException("Invalid delimited string [" + value + "] for delimiter: '" + delim + "'");
            }
        }

        if (sb.length() > 0) {
            addPart(list, sb, trim);
        }

        return list;
    }

    private static void addPart(List<String> list, StringBuilder sb, boolean trim) {
        list.add(trim ? sb.toString().trim() : sb.toString());
    }

}
