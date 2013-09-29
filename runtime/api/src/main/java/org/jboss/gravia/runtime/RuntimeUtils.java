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
package org.jboss.gravia.runtime;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public class RuntimeUtils {
    /**
     * FrameworkUtil objects may not be constructed.
     */
    private RuntimeUtils() {
        // private empty constructor to prevent construction
    }

    /**
     * Creates a {@code Filter} object. This {@code Filter} object may be used
     * to match a {@code ServiceReference} object or a {@code Dictionary}
     * object.
     * 
     * <p>
     * If the filter cannot be parsed, an {@link InvalidSyntaxException} will be
     * thrown with a human readable message where the filter became unparsable.
     * 
     * <p>
     * This method returns a Filter implementation which may not perform as well
     * as the framework implementation-specific Filter implementation returned
     * by {@link ModuleContext#createFilter(String)}.
     * 
     * @param filter The filter string.
     * @return A {@code Filter} object encapsulating the filter string.
     * @If {@code filter} contains an invalid
     *         filter string that cannot be parsed.
     * @throws NullPointerException If {@code filter} is null.
     * 
     * @see Filter
     */
    public static Filter createFilter(String filter) {
        return FilterImpl.newInstance(filter);
    }

    /**
     * RFC 1960-based Filter. Filter objects can be created by calling the
     * constructor with the desired filter string. A Filter object can be called
     * numerous times to determine if the match argument matches the filter
     * string that was used to create the Filter object.
     * 
     * <p>
     * The syntax of a filter string is the string representation of LDAP search
     * filters as defined in RFC 1960: <i>A String Representation of LDAP Search
     * Filters</i> (available at http://www.ietf.org/rfc/rfc1960.txt). It should
     * be noted that RFC 2254: <i>A String Representation of LDAP Search
     * Filters</i> (available at http://www.ietf.org/rfc/rfc2254.txt) supersedes
     * RFC 1960 but only adds extensible matching and is not applicable for this
     * API.
     * 
     * <p>
     * The string representation of an LDAP search filter is defined by the
     * following grammar. It uses a prefix format.
     * 
     * <pre>
     *   &lt;filter&gt; ::= '(' &lt;filtercomp&gt; ')'
     *   &lt;filtercomp&gt; ::= &lt;and&gt; | &lt;or&gt; | &lt;not&gt; | &lt;item&gt;
     *   &lt;and&gt; ::= '&amp;' &lt;filterlist&gt;
     *   &lt;or&gt; ::= '|' &lt;filterlist&gt;
     *   &lt;not&gt; ::= '!' &lt;filter&gt;
     *   &lt;filterlist&gt; ::= &lt;filter&gt; | &lt;filter&gt; &lt;filterlist&gt;
     *   &lt;item&gt; ::= &lt;simple&gt; | &lt;present&gt; | &lt;substring&gt;
     *   &lt;simple&gt; ::= &lt;attr&gt; &lt;filtertype&gt; &lt;value&gt;
     *   &lt;filtertype&gt; ::= &lt;equal&gt; | &lt;approx&gt; | &lt;greater&gt; | &lt;less&gt;
     *   &lt;equal&gt; ::= '='
     *   &lt;approx&gt; ::= '&tilde;='
     *   &lt;greater&gt; ::= '&gt;='
     *   &lt;less&gt; ::= '&lt;='
     *   &lt;present&gt; ::= &lt;attr&gt; '=*'
     *   &lt;substring&gt; ::= &lt;attr&gt; '=' &lt;initial&gt; &lt;any&gt; &lt;final&gt;
     *   &lt;initial&gt; ::= NULL | &lt;value&gt;
     *   &lt;any&gt; ::= '*' &lt;starval&gt;
     *   &lt;starval&gt; ::= NULL | &lt;value&gt; '*' &lt;starval&gt;
     *   &lt;final&gt; ::= NULL | &lt;value&gt;
     * </pre>
     * 
     * {@code &lt;attr&gt;} is a string representing an attribute, or key, in
     * the properties objects of the registered services. Attribute names are
     * not case sensitive; that is cn and CN both refer to the same attribute.
     * {@code &lt;value&gt;} is a string representing the value, or part of one,
     * of a key in the properties objects of the registered services. If a
     * {@code &lt;value&gt;} must contain one of the characters ' {@code *}' or
     * '{@code (}' or '{@code )}', these characters should be escaped by
     * preceding them with the backslash '{@code \}' character. Note that
     * although both the {@code &lt;substring&gt;} and {@code &lt;present&gt;}
     * productions can produce the {@code 'attr=*'} construct, this construct is
     * used only to denote a presence filter.
     * 
     * <p>
     * Examples of LDAP filters are:
     * 
     * <pre>
     *   &quot;(cn=Babs Jensen)&quot;
     *   &quot;(!(cn=Tim Howes))&quot;
     *   &quot;(&amp;(&quot; + Constants.OBJECTCLASS + &quot;=Person)(|(sn=Jensen)(cn=Babs J*)))&quot;
     *   &quot;(o=univ*of*mich*)&quot;
     * </pre>
     * 
     * <p>
     * The approximate match ({@code ~=}) is implementation specific but should
     * at least ignore case and white space differences. Optional are codes like
     * soundex or other smart "closeness" comparisons.
     * 
     * <p>
     * Comparison of values is not straightforward. Strings are compared
     * differently than numbers and it is possible for a key to have multiple
     * values. Note that that keys in the match argument must always be strings.
     * The comparison is defined by the object type of the key's value. The
     * following rules apply for comparison:
     * 
     * <blockquote>
     * <TABLE BORDER=0>
     * <TR>
     * <TD><b>Property Value Type </b></TD>
     * <TD><b>Comparison Type</b></TD>
     * </TR>
     * <TR>
     * <TD>String</TD>
     * <TD>String comparison</TD>
     * </TR>
     * <TR valign=top>
     * <TD>Integer, Long, Float, Double, Byte, Short, BigInteger, BigDecimal</TD>
     * <TD>numerical comparison</TD>
     * </TR>
     * <TR>
     * <TD>Character</TD>
     * <TD>character comparison</TD>
     * </TR>
     * <TR>
     * <TD>Boolean</TD>
     * <TD>equality comparisons only</TD>
     * </TR>
     * <TR>
     * <TD>[] (array)</TD>
     * <TD>recursively applied to values</TD>
     * </TR>
     * <TR>
     * <TD>Collection</TD>
     * <TD>recursively applied to values</TD>
     * </TR>
     * </TABLE>
     * Note: arrays of primitives are also supported. </blockquote>
     * 
     * A filter matches a key that has multiple values if it matches at least
     * one of those values. For example,
     * 
     * <pre>
     * Dictionary d = new Hashtable();
     * d.put(&quot;cn&quot;, new String[] {&quot;a&quot;, &quot;b&quot;, &quot;c&quot;});
     * </pre>
     * 
     * d will match {@code (cn=a)} and also {@code (cn=b)}
     * 
     * <p>
     * A filter component that references a key having an unrecognizable data
     * type will evaluate to {@code false} .
     */
    static private final class FilterImpl implements Filter {
        /* filter operators */
        private static final int	EQUAL		= 1;
        private static final int	APPROX		= 2;
        private static final int	GREATER		= 3;
        private static final int	LESS		= 4;
        private static final int	PRESENT		= 5;
        private static final int	SUBSTRING	= 6;
        private static final int	AND			= 7;
        private static final int	OR			= 8;
        private static final int	NOT			= 9;

        /** filter operation */
        private final int			op;
        /** filter attribute or null if operation AND, OR or NOT */
        private final String		attr;
        /** filter operands */
        private final Object		value;

        /* normalized filter string for Filter object */
        private transient String	filterString;

        /**
         * Constructs a {@link FilterImpl} object. This filter object may be
         * used to match a {@link ServiceReference} or a Dictionary.
         * 
         * <p>
         * If the filter cannot be parsed, an {@link InvalidSyntaxException}
         * will be thrown with a human readable message where the filter became
         * unparsable.
         * 
         * @param filterString the filter string.
         * @If the filter parameter contains an
         *         invalid filter string that cannot be parsed.
         */
        static FilterImpl newInstance(String filterString) {
            return new Parser(filterString).parse();
        }

        FilterImpl(int operation, String attr, Object value) {
            this.op = operation;
            this.attr = attr;
            this.value = value;
            filterString = null;
        }

        /**
         * Filter using a service's properties.
         * <p>
         * This {@code Filter} is executed using the keys and values of the
         * referenced service's properties. The keys are looked up in a case
         * insensitive manner.
         * 
         * @param reference The reference to the service whose properties are
         *        used in the match.
         * @return {@code true} if the service's properties match this
         *         {@code Filter}; {@code false} otherwise.
         */
        @Override
        public boolean match(ServiceReference<?> reference) {
            return matches(new ServiceReferenceMap(reference));
        }

        /**
         * Filter using a {@code Dictionary} with case insensitive key lookup.
         * This {@code Filter} is executed using the specified
         * {@code Dictionary}'s keys and values. The keys are looked up in a
         * case insensitive manner.
         * 
         * @param dictionary The {@code Dictionary} whose key/value pairs are
         *        used in the match.
         * @return {@code true} if the {@code Dictionary}'s values match this
         *         filter; {@code false} otherwise.
         * @throws IllegalArgumentException If {@code dictionary} contains case
         *         variants of the same key name.
         */
        @Override
        public boolean match(Dictionary<String, ?> dictionary) {
            return matches(new CaseInsensitiveMap(dictionary));
        }

        /**
         * Filter using a {@code Dictionary}. This {@code Filter} is executed
         * using the specified {@code Dictionary}'s keys and values. The keys
         * are looked up in a normal manner respecting case.
         * 
         * @param dictionary The {@code Dictionary} whose key/value pairs are
         *        used in the match.
         * @return {@code true} if the {@code Dictionary}'s values match this
         *         filter; {@code false} otherwise.
         * @since 1.3
         */
        @Override
        public boolean matchCase(Dictionary<String, ?> dictionary) {
            switch (op) {
                case AND : {
                    FilterImpl[] filters = (FilterImpl[]) value;
                    for (FilterImpl f : filters) {
                        if (!f.matchCase(dictionary)) {
                            return false;
                        }
                    }
                    return true;
                }

                case OR : {
                    FilterImpl[] filters = (FilterImpl[]) value;
                    for (FilterImpl f : filters) {
                        if (f.matchCase(dictionary)) {
                            return true;
                        }
                    }
                    return false;
                }

                case NOT : {
                    FilterImpl filter = (FilterImpl) value;
                    return !filter.matchCase(dictionary);
                }

                case SUBSTRING :
                case EQUAL :
                case GREATER :
                case LESS :
                case APPROX : {
                    Object prop = (dictionary == null) ? null : dictionary.get(attr);
                    return compare(op, prop, value);
                }

                case PRESENT : {
                    Object prop = (dictionary == null) ? null : dictionary.get(attr);
                    return prop != null;
                }
            }

            return false;
        }

        /**
         * Filter using a {@code Map}. This {@code Filter} is executed using the
         * specified {@code Map}'s keys and values. The keys are looked up in a
         * normal manner respecting case.
         * 
         * @param map The {@code Map} whose key/value pairs are used in the
         *        match. Maps with {@code null} key or values are not supported.
         *        A {@code null} value is considered not present to the filter.
         * @return {@code true} if the {@code Map}'s values match this filter;
         *         {@code false} otherwise.
         * @since 1.6
         */
        @Override
        public boolean matches(Map<String, ?> map) {
            switch (op) {
                case AND : {
                    FilterImpl[] filters = (FilterImpl[]) value;
                    for (FilterImpl f : filters) {
                        if (!f.matches(map)) {
                            return false;
                        }
                    }
                    return true;
                }

                case OR : {
                    FilterImpl[] filters = (FilterImpl[]) value;
                    for (FilterImpl f : filters) {
                        if (f.matches(map)) {
                            return true;
                        }
                    }
                    return false;
                }

                case NOT : {
                    FilterImpl filter = (FilterImpl) value;
                    return !filter.matches(map);
                }

                case SUBSTRING :
                case EQUAL :
                case GREATER :
                case LESS :
                case APPROX : {
                    Object prop = (map == null) ? null : map.get(attr);
                    return compare(op, prop, value);
                }

                case PRESENT : {
                    Object prop = (map == null) ? null : map.get(attr);
                    return prop != null;
                }
            }

            return false;
        }

        /**
         * Returns this {@code Filter}'s filter string.
         * <p>
         * The filter string is normalized by removing whitespace which does not
         * affect the meaning of the filter.
         * 
         * @return This {@code Filter}'s filter string.
         */
        @Override
        public String toString() {
            String result = filterString;
            if (result == null) {
                filterString = result = normalize().toString();
            }
            return result;
        }

        /**
         * Returns this {@code Filter}'s normalized filter string.
         * <p>
         * The filter string is normalized by removing whitespace which does not
         * affect the meaning of the filter.
         * 
         * @return This {@code Filter}'s filter string.
         */
        private StringBuffer normalize() {
            StringBuffer sb = new StringBuffer();
            sb.append('(');

            switch (op) {
                case AND : {
                    sb.append('&');

                    FilterImpl[] filters = (FilterImpl[]) value;
                    for (FilterImpl f : filters) {
                        sb.append(f.normalize());
                    }

                    break;
                }

                case OR : {
                    sb.append('|');

                    FilterImpl[] filters = (FilterImpl[]) value;
                    for (FilterImpl f : filters) {
                        sb.append(f.normalize());
                    }

                    break;
                }

                case NOT : {
                    sb.append('!');
                    FilterImpl filter = (FilterImpl) value;
                    sb.append(filter.normalize());

                    break;
                }

                case SUBSTRING : {
                    sb.append(attr);
                    sb.append('=');

                    String[] substrings = (String[]) value;

                    for (String substr : substrings) {
                        if (substr == null) /* * */{
                            sb.append('*');
                        } else /* xxx */{
                            sb.append(encodeValue(substr));
                        }
                    }

                    break;
                }
                case EQUAL : {
                    sb.append(attr);
                    sb.append('=');
                    sb.append(encodeValue((String) value));

                    break;
                }
                case GREATER : {
                    sb.append(attr);
                    sb.append(">=");
                    sb.append(encodeValue((String) value));

                    break;
                }
                case LESS : {
                    sb.append(attr);
                    sb.append("<=");
                    sb.append(encodeValue((String) value));

                    break;
                }
                case APPROX : {
                    sb.append(attr);
                    sb.append("~=");
                    sb.append(encodeValue(approxString((String) value)));

                    break;
                }

                case PRESENT : {
                    sb.append(attr);
                    sb.append("=*");

                    break;
                }
            }

            sb.append(')');

            return sb;
        }

        /**
         * Compares this {@code Filter} to another {@code Filter}.
         * 
         * <p>
         * This implementation returns the result of calling
         * {@code this.toString().equals(obj.toString()}.
         * 
         * @param obj The object to compare against this {@code Filter}.
         * @return If the other object is a {@code Filter} object, then returns
         *         the result of calling
         *         {@code this.toString().equals(obj.toString()}; {@code false}
         *         otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof Filter)) {
                return false;
            }

            return this.toString().equals(obj.toString());
        }

        /**
         * Returns the hashCode for this {@code Filter}.
         * 
         * <p>
         * This implementation returns the result of calling
         * {@code this.toString().hashCode()}.
         * 
         * @return The hashCode of this {@code Filter}.
         */
        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        /**
         * Encode the value string such that '(', '*', ')' and '\' are escaped.
         * 
         * @param value unencoded value string.
         * @return encoded value string.
         */
        private static String encodeValue(String value) {
            boolean encoded = false;
            int inlen = value.length();
            int outlen = inlen << 1; /* inlen 2 */

            char[] output = new char[outlen];
            value.getChars(0, inlen, output, inlen);

            int cursor = 0;
            for (int i = inlen; i < outlen; i++) {
                char c = output[i];

                switch (c) {
                    case '(' :
                    case '*' :
                    case ')' :
                    case '\\' : {
                        output[cursor] = '\\';
                        cursor++;
                        encoded = true;

                        break;
                    }
                }

                output[cursor] = c;
                cursor++;
            }

            return encoded ? new String(output, 0, cursor) : value;
        }

        private boolean compare(int operation, Object value1, Object value2) {
            if (value1 == null) {
                return false;
            }
            if (value1 instanceof String) {
                return compare_String(operation, (String) value1, value2);
            }

            Class<?> clazz = value1.getClass();
            if (clazz.isArray()) {
                Class<?> type = clazz.getComponentType();
                if (type.isPrimitive()) {
                    return compare_PrimitiveArray(operation, type, value1, value2);
                }
                return compare_ObjectArray(operation, (Object[]) value1, value2);
            }
            if (value1 instanceof Collection<?>) {
                return compare_Collection(operation, (Collection<?>) value1, value2);
            }
            if (value1 instanceof Integer) {
                return compare_Integer(operation, ((Integer) value1).intValue(), value2);
            }
            if (value1 instanceof Long) {
                return compare_Long(operation, ((Long) value1).longValue(), value2);
            }
            if (value1 instanceof Byte) {
                return compare_Byte(operation, ((Byte) value1).byteValue(), value2);
            }
            if (value1 instanceof Short) {
                return compare_Short(operation, ((Short) value1).shortValue(), value2);
            }
            if (value1 instanceof Character) {
                return compare_Character(operation, ((Character) value1).charValue(), value2);
            }
            if (value1 instanceof Float) {
                return compare_Float(operation, ((Float) value1).floatValue(), value2);
            }
            if (value1 instanceof Double) {
                return compare_Double(operation, ((Double) value1).doubleValue(), value2);
            }
            if (value1 instanceof Boolean) {
                return compare_Boolean(operation, ((Boolean) value1).booleanValue(), value2);
            }
            if (value1 instanceof Comparable<?>) {
                @SuppressWarnings("unchecked")
                Comparable<Object> comparable = (Comparable<Object>) value1;
                return compare_Comparable(operation, comparable, value2);
            }
            return compare_Unknown(operation, value1, value2);
        }

        private boolean compare_Collection(int operation, Collection<?> collection, Object value2) {
            for (Object value1 : collection) {
                if (compare(operation, value1, value2)) {
                    return true;
                }
            }
            return false;
        }

        private boolean compare_ObjectArray(int operation, Object[] array, Object value2) {
            for (Object value1 : array) {
                if (compare(operation, value1, value2)) {
                    return true;
                }
            }
            return false;
        }

        private boolean compare_PrimitiveArray(int operation, Class<?> type, Object primarray, Object value2) {
            if (Integer.TYPE.isAssignableFrom(type)) {
                int[] array = (int[]) primarray;
                for (int value1 : array) {
                    if (compare_Integer(operation, value1, value2)) {
                        return true;
                    }
                }
                return false;
            }
            if (Long.TYPE.isAssignableFrom(type)) {
                long[] array = (long[]) primarray;
                for (long value1 : array) {
                    if (compare_Long(operation, value1, value2)) {
                        return true;
                    }
                }
                return false;
            }
            if (Byte.TYPE.isAssignableFrom(type)) {
                byte[] array = (byte[]) primarray;
                for (byte value1 : array) {
                    if (compare_Byte(operation, value1, value2)) {
                        return true;
                    }
                }
                return false;
            }
            if (Short.TYPE.isAssignableFrom(type)) {
                short[] array = (short[]) primarray;
                for (short value1 : array) {
                    if (compare_Short(operation, value1, value2)) {
                        return true;
                    }
                }
                return false;
            }
            if (Character.TYPE.isAssignableFrom(type)) {
                char[] array = (char[]) primarray;
                for (char value1 : array) {
                    if (compare_Character(operation, value1, value2)) {
                        return true;
                    }
                }
                return false;
            }
            if (Float.TYPE.isAssignableFrom(type)) {
                float[] array = (float[]) primarray;
                for (float value1 : array) {
                    if (compare_Float(operation, value1, value2)) {
                        return true;
                    }
                }
                return false;
            }
            if (Double.TYPE.isAssignableFrom(type)) {
                double[] array = (double[]) primarray;
                for (double value1 : array) {
                    if (compare_Double(operation, value1, value2)) {
                        return true;
                    }
                }
                return false;
            }
            if (Boolean.TYPE.isAssignableFrom(type)) {
                boolean[] array = (boolean[]) primarray;
                for (boolean value1 : array) {
                    if (compare_Boolean(operation, value1, value2)) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        private boolean compare_String(int operation, String string, Object value2) {
            switch (operation) {
                case SUBSTRING : {
                    String[] substrings = (String[]) value2;
                    int pos = 0;
                    for (int i = 0, size = substrings.length; i < size; i++) {
                        String substr = substrings[i];

                        if (i + 1 < size) /* if this is not that last substr */{
                            if (substr == null) /* * */{
                                String substr2 = substrings[i + 1];

                                if (substr2 == null) /* ** */
                                    continue; /* ignore first star */
                                /* xxx */
                                int index = string.indexOf(substr2, pos);
                                if (index == -1) {
                                    return false;
                                }

                                pos = index + substr2.length();
                                if (i + 2 < size) // if there are more
                                    // substrings, increment
                                    // over the string we just
                                    // matched; otherwise need
                                    // to do the last substr
                                    // check
                                    i++;
                            } else /* xxx */{
                                int len = substr.length();
                                if (string.regionMatches(pos, substr, 0, len)) {
                                    pos += len;
                                } else {
                                    return false;
                                }
                            }
                        } else /* last substr */{
                            if (substr == null) /* * */{
                                return true;
                            }
                            /* xxx */
                            return string.endsWith(substr);
                        }
                    }

                    return true;
                }
                case EQUAL : {
                    return string.equals(value2);
                }
                case APPROX : {
                    string = approxString(string);
                    String string2 = approxString((String) value2);

                    return string.equalsIgnoreCase(string2);
                }
                case GREATER : {
                    return string.compareTo((String) value2) >= 0;
                }
                case LESS : {
                    return string.compareTo((String) value2) <= 0;
                }
            }
            return false;
        }

        private boolean compare_Integer(int operation, int intval, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            int intval2;
            try {
                intval2 = Integer.parseInt(((String) value2).trim());
            } catch (IllegalArgumentException e) {
                return false;
            }
            switch (operation) {
                case APPROX :
                case EQUAL : {
                    return intval == intval2;
                }
                case GREATER : {
                    return intval >= intval2;
                }
                case LESS : {
                    return intval <= intval2;
                }
            }
            return false;
        }

        private boolean compare_Long(int operation, long longval, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            long longval2;
            try {
                longval2 = Long.parseLong(((String) value2).trim());
            } catch (IllegalArgumentException e) {
                return false;
            }

            switch (operation) {
                case APPROX :
                case EQUAL : {
                    return longval == longval2;
                }
                case GREATER : {
                    return longval >= longval2;
                }
                case LESS : {
                    return longval <= longval2;
                }
            }
            return false;
        }

        private boolean compare_Byte(int operation, byte byteval, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            byte byteval2;
            try {
                byteval2 = Byte.parseByte(((String) value2).trim());
            } catch (IllegalArgumentException e) {
                return false;
            }

            switch (operation) {
                case APPROX :
                case EQUAL : {
                    return byteval == byteval2;
                }
                case GREATER : {
                    return byteval >= byteval2;
                }
                case LESS : {
                    return byteval <= byteval2;
                }
            }
            return false;
        }

        private boolean compare_Short(int operation, short shortval, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            short shortval2;
            try {
                shortval2 = Short.parseShort(((String) value2).trim());
            } catch (IllegalArgumentException e) {
                return false;
            }

            switch (operation) {
                case APPROX :
                case EQUAL : {
                    return shortval == shortval2;
                }
                case GREATER : {
                    return shortval >= shortval2;
                }
                case LESS : {
                    return shortval <= shortval2;
                }
            }
            return false;
        }

        private boolean compare_Character(int operation, char charval, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            char charval2;
            try {
                charval2 = ((String) value2).charAt(0);
            } catch (IndexOutOfBoundsException e) {
                return false;
            }

            switch (operation) {
                case EQUAL : {
                    return charval == charval2;
                }
                case APPROX : {
                    return (charval == charval2) || (Character.toUpperCase(charval) == Character.toUpperCase(charval2)) || (Character.toLowerCase(charval) == Character.toLowerCase(charval2));
                }
                case GREATER : {
                    return charval >= charval2;
                }
                case LESS : {
                    return charval <= charval2;
                }
            }
            return false;
        }

        private boolean compare_Boolean(int operation, boolean boolval, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            boolean boolval2 = Boolean.valueOf(((String) value2).trim()).booleanValue();
            switch (operation) {
                case APPROX :
                case EQUAL :
                case GREATER :
                case LESS : {
                    return boolval == boolval2;
                }
            }
            return false;
        }

        private boolean compare_Float(int operation, float floatval, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            float floatval2;
            try {
                floatval2 = Float.parseFloat(((String) value2).trim());
            } catch (IllegalArgumentException e) {
                return false;
            }

            switch (operation) {
                case APPROX :
                case EQUAL : {
                    return Float.compare(floatval, floatval2) == 0;
                }
                case GREATER : {
                    return Float.compare(floatval, floatval2) >= 0;
                }
                case LESS : {
                    return Float.compare(floatval, floatval2) <= 0;
                }
            }
            return false;
        }

        private boolean compare_Double(int operation, double doubleval, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            double doubleval2;
            try {
                doubleval2 = Double.parseDouble(((String) value2).trim());
            } catch (IllegalArgumentException e) {
                return false;
            }

            switch (operation) {
                case APPROX :
                case EQUAL : {
                    return Double.compare(doubleval, doubleval2) == 0;
                }
                case GREATER : {
                    return Double.compare(doubleval, doubleval2) >= 0;
                }
                case LESS : {
                    return Double.compare(doubleval, doubleval2) <= 0;
                }
            }
            return false;
        }

        private static Object valueOf(Class<?> target, String value2) {
            do {
                Method method;
                try {
                    method = target.getMethod("valueOf", String.class);
                } catch (NoSuchMethodException e) {
                    break;
                }
                if (Modifier.isStatic(method.getModifiers()) && target.isAssignableFrom(method.getReturnType())) {
                    setAccessible(method);
                    try {
                        return method.invoke(null, value2.trim());
                    } catch (IllegalAccessException e) {
                        return null;
                    } catch (InvocationTargetException e) {
                        return null;
                    }
                }
            } while (false);

            do {
                Constructor<?> constructor;
                try {
                    constructor = target.getConstructor(String.class);
                } catch (NoSuchMethodException e) {
                    break;
                }
                setAccessible(constructor);
                try {
                    return constructor.newInstance(value2.trim());
                } catch (IllegalAccessException e) {
                    return null;
                } catch (InvocationTargetException e) {
                    return null;
                } catch (InstantiationException e) {
                    return null;
                }
            } while (false);

            return null;
        }

        private static void setAccessible(AccessibleObject accessible) {
            if (!accessible.isAccessible()) {
                AccessController.doPrivileged(new SetAccessibleAction(accessible));
            }
        }

        private boolean compare_Comparable(int operation, Comparable<Object> value1, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            value2 = valueOf(value1.getClass(), (String) value2);
            if (value2 == null) {
                return false;
            }
            try {
                switch (operation) {
                    case APPROX :
                    case EQUAL : {
                        return value1.compareTo(value2) == 0;
                    }
                    case GREATER : {
                        return value1.compareTo(value2) >= 0;
                    }
                    case LESS : {
                        return value1.compareTo(value2) <= 0;
                    }
                }
            } catch (Exception e) {
                // if the compareTo method throws an exception; return false
                return false;
            }
            return false;
        }

        private boolean compare_Unknown(int operation, Object value1, Object value2) {
            if (operation == SUBSTRING) {
                return false;
            }
            value2 = valueOf(value1.getClass(), (String) value2);
            if (value2 == null) {
                return false;
            }
            try {
                switch (operation) {
                    case APPROX :
                    case EQUAL :
                    case GREATER :
                    case LESS : {
                        return value1.equals(value2);
                    }
                }
            } catch (Exception e) {
                // if the equals method throws an exception; return false
                return false;
            }
            return false;
        }

        /**
         * Map a string for an APPROX (~=) comparison.
         * 
         * This implementation removes white spaces. This is the minimum
         * implementation allowed by the OSGi spec.
         * 
         * @param input Input string.
         * @return String ready for APPROX comparison.
         */
        private static String approxString(String input) {
            boolean changed = false;
            char[] output = input.toCharArray();
            int cursor = 0;
            for (char c : output) {
                if (Character.isWhitespace(c)) {
                    changed = true;
                    continue;
                }

                output[cursor] = c;
                cursor++;
            }

            return changed ? new String(output, 0, cursor) : input;
        }

        /**
         * Parser class for OSGi filter strings. This class parses the complete
         * filter string and builds a tree of Filter objects rooted at the
         * parent.
         */
        static private final class Parser {
            private final String	filterstring;
            private final char[]	filterChars;
            private int				pos;

            Parser(String filterstring) {
                this.filterstring = filterstring;
                filterChars = filterstring.toCharArray();
                pos = 0;
            }

            FilterImpl parse() {
                FilterImpl filter;
                try {
                    filter = parse_filter();
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Filter ended abruptly: " + filterstring, e);
                }

                if (pos != filterChars.length) {
                    throw new IllegalArgumentException("Extraneous trailing characters in: " + filterstring);
                }
                return filter;
            }

            private FilterImpl parse_filter() {
                FilterImpl filter;
                skipWhiteSpace();

                if (filterChars[pos] != '(') {
                    throw new IllegalArgumentException("Missing '(' in: " + filterstring);
                }

                pos++;

                filter = parse_filtercomp();

                skipWhiteSpace();

                if (filterChars[pos] != ')') {
                    throw new IllegalArgumentException("Missing ')' in: " + filterstring);
                }

                pos++;

                skipWhiteSpace();

                return filter;
            }

            private FilterImpl parse_filtercomp() {
                skipWhiteSpace();

                char c = filterChars[pos];

                switch (c) {
                    case '&' : {
                        pos++;
                        return parse_and();
                    }
                    case '|' : {
                        pos++;
                        return parse_or();
                    }
                    case '!' : {
                        pos++;
                        return parse_not();
                    }
                }
                return parse_item();
            }

            private FilterImpl parse_and() {
                int lookahead = pos;
                skipWhiteSpace();

                if (filterChars[pos] != '(') {
                    pos = lookahead - 1;
                    return parse_item();
                }

                List<FilterImpl> operands = new ArrayList<FilterImpl>(10);

                while (filterChars[pos] == '(') {
                    FilterImpl child = parse_filter();
                    operands.add(child);
                }

                return new FilterImpl(FilterImpl.AND, null, operands.toArray(new FilterImpl[operands.size()]));
            }

            private FilterImpl parse_or() {
                int lookahead = pos;
                skipWhiteSpace();

                if (filterChars[pos] != '(') {
                    pos = lookahead - 1;
                    return parse_item();
                }

                List<FilterImpl> operands = new ArrayList<FilterImpl>(10);

                while (filterChars[pos] == '(') {
                    FilterImpl child = parse_filter();
                    operands.add(child);
                }

                return new FilterImpl(FilterImpl.OR, null, operands.toArray(new FilterImpl[operands.size()]));
            }

            private FilterImpl parse_not() {
                int lookahead = pos;
                skipWhiteSpace();

                if (filterChars[pos] != '(') {
                    pos = lookahead - 1;
                    return parse_item();
                }

                FilterImpl child = parse_filter();

                return new FilterImpl(FilterImpl.NOT, null, child);
            }

            private FilterImpl parse_item() {
                String attr = parse_attr();

                skipWhiteSpace();

                switch (filterChars[pos]) {
                    case '~' : {
                        if (filterChars[pos + 1] == '=') {
                            pos += 2;
                            return new FilterImpl(FilterImpl.APPROX, attr, parse_value());
                        }
                        break;
                    }
                    case '>' : {
                        if (filterChars[pos + 1] == '=') {
                            pos += 2;
                            return new FilterImpl(FilterImpl.GREATER, attr, parse_value());
                        }
                        break;
                    }
                    case '<' : {
                        if (filterChars[pos + 1] == '=') {
                            pos += 2;
                            return new FilterImpl(FilterImpl.LESS, attr, parse_value());
                        }
                        break;
                    }
                    case '=' : {
                        if (filterChars[pos + 1] == '*') {
                            int oldpos = pos;
                            pos += 2;
                            skipWhiteSpace();
                            if (filterChars[pos] == ')') {
                                return new FilterImpl(FilterImpl.PRESENT, attr, null);
                            }
                            pos = oldpos;
                        }

                        pos++;
                        Object string = parse_substring();

                        if (string instanceof String) {
                            return new FilterImpl(FilterImpl.EQUAL, attr, string);
                        }
                        return new FilterImpl(FilterImpl.SUBSTRING, attr, string);
                    }
                }

                throw new IllegalArgumentException("Invalid operator in: " + filterstring);
            }

            private String parse_attr() {
                skipWhiteSpace();

                int begin = pos;
                int end = pos;

                char c = filterChars[pos];

                while (c != '~' && c != '<' && c != '>' && c != '=' && c != '(' && c != ')') {
                    pos++;

                    if (!Character.isWhitespace(c)) {
                        end = pos;
                    }

                    c = filterChars[pos];
                }

                int length = end - begin;

                if (length == 0) {
                    throw new IllegalArgumentException("Missing attr in: " + filterstring);
                }

                return new String(filterChars, begin, length);
            }

            private String parse_value() {
                StringBuffer sb = new StringBuffer(filterChars.length - pos);

                parseloop: while (true) {
                    char c = filterChars[pos];

                    switch (c) {
                        case ')' : {
                            break parseloop;
                        }

                        case '(' : {
                            throw new IllegalArgumentException("Invalid value in: " + filterstring);
                        }

                        case '\\' : {
                            pos++;
                            c = filterChars[pos];
                            /* fall through into default */
                        }

                        default : {
                            sb.append(c);
                            pos++;
                            break;
                        }
                    }
                }

                if (sb.length() == 0) {
                    throw new IllegalArgumentException("Missing value: " + filterstring);
                }

                return sb.toString();
            }

            private Object parse_substring() {
                StringBuffer sb = new StringBuffer(filterChars.length - pos);

                List<String> operands = new ArrayList<String>(10);

                parseloop: while (true) {
                    char c = filterChars[pos];

                    switch (c) {
                        case ')' : {
                            if (sb.length() > 0) {
                                operands.add(sb.toString());
                            }

                            break parseloop;
                        }

                        case '(' : {
                            throw new IllegalArgumentException("Invalid value: " + filterstring);
                        }

                        case '*' : {
                            if (sb.length() > 0) {
                                operands.add(sb.toString());
                            }

                            sb.setLength(0);

                            operands.add(null);
                            pos++;

                            break;
                        }

                        case '\\' : {
                            pos++;
                            c = filterChars[pos];
                            /* fall through into default */
                        }

                        default : {
                            sb.append(c);
                            pos++;
                            break;
                        }
                    }
                }

                int size = operands.size();

                if (size == 0) {
                    return "";
                }

                if (size == 1) {
                    Object single = operands.get(0);

                    if (single != null) {
                        return single;
                    }
                }

                return operands.toArray(new String[size]);
            }

            private void skipWhiteSpace() {
                for (int length = filterChars.length; (pos < length) && Character.isWhitespace(filterChars[pos]);) {
                    pos++;
                }
            }
        }
    }

    /**
     * This Map is used for case-insensitive key lookup during filter
     * evaluation. This Map implementation only supports the get operation using
     * a String key as no other operations are used by the Filter
     * implementation.
     */
    static private final class CaseInsensitiveMap extends AbstractMap<String, Object> implements Map<String, Object> {
        private final Dictionary<String, ?>	dictionary;
        private final String[]				keys;

        /**
         * Create a case insensitive map from the specified dictionary.
         * 
         * @param dictionary
         * @throws IllegalArgumentException If {@code dictionary} contains case
         *         variants of the same key name.
         */
        CaseInsensitiveMap(Dictionary<String, ?> dictionary) {
            if (dictionary == null) {
                this.dictionary = null;
                this.keys = new String[0];
                return;
            }
            this.dictionary = dictionary;
            List<String> keyList = new ArrayList<String>(dictionary.size());
            for (Enumeration<?> e = dictionary.keys(); e.hasMoreElements();) {
                Object k = e.nextElement();
                if (k instanceof String) {
                    String key = (String) k;
                    for (String i : keyList) {
                        if (key.equalsIgnoreCase(i)) {
                            throw new IllegalArgumentException();
                        }
                    }
                    keyList.add(key);
                }
            }
            this.keys = keyList.toArray(new String[keyList.size()]);
        }

        @Override
        public Object get(Object o) {
            String k = (String) o;
            for (String key : keys) {
                if (key.equalsIgnoreCase(k)) {
                    return dictionary.get(key);
                }
            }
            return null;
        }

        @Override
        public Set<java.util.Map.Entry<String, Object>> entrySet() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * This Map is used for key lookup from a ServiceReference during filter
     * evaluation. This Map implementation only supports the get operation using
     * a String key as no other operations are used by the Filter
     * implementation.
     */
    static private final class ServiceReferenceMap extends AbstractMap<String, Object> implements Map<String, Object> {
        private final ServiceReference<?>	reference;

        ServiceReferenceMap(ServiceReference<?> reference) {
            this.reference = reference;
        }

        @Override
        public Object get(Object key) {
            if (reference == null) {
                return null;
            }
            return reference.getProperty((String) key);
        }

        @Override
        public Set<java.util.Map.Entry<String, Object>> entrySet() {
            throw new UnsupportedOperationException();
        }
    }

    static private final class SetAccessibleAction implements PrivilegedAction<Object> {
        private final AccessibleObject	accessible;

        SetAccessibleAction(AccessibleObject accessible) {
            this.accessible = accessible;
        }

        @Override
        public Object run() {
            accessible.setAccessible(true);
            return null;
        }
    }
}
