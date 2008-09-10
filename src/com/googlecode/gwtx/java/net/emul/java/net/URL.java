/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * This file is based on code from the Apache Harmony Project.
 * http://svn.apache.org/repos/asf/harmony/enhanced/classlib/trunk/modules/luni/src/main/java/java/net/URL.java
 */

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * An instance of class URL specifies the location of a resource on the world
 * wide web as specified by RFC 1738.
 */
public final class URL implements java.io.Serializable {

    private int hashCode;

    /**
     * The receiver's filename.
     *
     * @serial the file of this URL
     *
     */
    private String file;

    /**
     * The receiver's protocol identifier.
     *
     * @serial the protocol of this URL (http, file)
     *
     */
    private String protocol = null;

    /**
     * The receiver's host name.
     *
     * @serial the host of this URL
     *
     */
    private String host;

    /**
     * The receiver's port number.
     *
     * @serial the port of this URL
     *
     */
    private int port = -1;

    /**
     * The receiver's authority.
     *
     * @serial the authority of this URL
     *
     */
    private String authority = null;

    /**
     * The receiver's userInfo.
     */
    private transient String userInfo = null;

    /**
     * The receiver's path.
     */
    private transient String path = null;

    /**
     * The receiver's query.
     */
    private transient String query = null;

    /**
     * The receiver's reference.
     *
     * @serial the reference of this URL
     *
     */
    private String ref = null;

    /**
     * Cache for storing protocol handler
     */
    private static HashMap<String, URLStreamHandler> streamHandlers = new HashMap<String, URLStreamHandler>();

    /**
     * The URL Stream (protocol) Handler
     */
    transient URLStreamHandler strmHandler;

    /**
     * The factory responsible for producing URL Stream (protocol) Handler
     */
    private static URLStreamHandlerFactory streamHandlerFactory;

    /**
     * Sets the URL Stream (protocol) handler factory. This method can be
     * invoked only once during an application's lifetime.
     * <p>
     * A security check is performed to verify that the current Policy allows
     * the stream handler factory to be set.
     *
     * @param streamFactory
     *            URLStreamHandlerFactory The factory to use for finding stream
     *            handlers.
     */
    public static synchronized void setURLStreamHandlerFactory(
            URLStreamHandlerFactory streamFactory) {
        if (streamHandlerFactory != null) {
            throw new Error("Attempt to set factory more than once."); //$NON-NLS-1$
        }
        streamHandlers.clear();
        streamHandlerFactory = streamFactory;
    }

    /**
     * Constructs a new URL instance by parsing the specification.
     *
     * @param spec
     *            java.lang.String a URL specification.
     *
     * @throws MalformedURLException
     *             if the spec could not be parsed as an URL.
     */
    public URL(String spec) throws MalformedURLException {
        this((URL) null, spec, (URLStreamHandler) null);
    }

    /**
     * Constructs a new URL by parsing the specification given by
     * <code>spec</code> and using the context provided by
     * <code>context</code>.
     * <p>
     * The protocol of the specification is obtained by parsing the
     * <code> spec </code> string.
     * <p>
     * If the <code>spec</code> does not specify a protocol:
     * <ul>
     * <li>If the context is <code>null</code>, then a
     * <code>MalformedURLException</code>.</li>
     * <li>If the context is not <code>null</code>, then the protocol is
     * obtained from the context.</li>
     * </ul>
     * If the <code>spec</code> does specify a protocol:
     * <ul>
     * <li>If the context is <code>null</code>, or specifies a different
     * protocol than the spec, the context is ignored.</li>
     * <li>If the context is not <code>null</code> and specifies the same
     * protocol as the specification, the properties of the new <code>URL</code>
     * are obtained from the context.</li>
     * </ul>
     *
     * @param context
     *            java.net.URL URL to use as context.
     * @param spec
     *            java.lang.String a URL specification.
     *
     * @throws MalformedURLException
     *             if the spec could not be parsed as an URL.
     */
    public URL(URL context, String spec) throws MalformedURLException {
        this(context, spec, (URLStreamHandler) null);
    }

    /**
     * Constructs a new URL by parsing the specification given by
     * <code>spec</code> and using the context provided by
     * <code>context</code>.
     * <p>
     * If the handler argument is non-null, a security check is made to verify
     * that user-defined protocol handlers can be specified.
     * <p>
     * The protocol of the specification is obtained by parsing the
     * <code> spec </code> string.
     * <p>
     * If the <code>spec</code> does not specify a protocol:
     * <ul>
     * <li>If the context is <code>null</code>, then a
     * <code>MalformedURLException</code>.</li>
     * <li>If the context is not <code>null</code>, then the protocol is
     * obtained from the context.</li>
     * </ul>
     * If the <code>spec</code> does specify a protocol:
     * <ul>
     * <li>If the context is <code>null</code>, or specifies a different
     * protocol than the spec, the context is ignored.</li>
     * <li>If the context is not <code>null</code> and specifies the same
     * protocol as the specification, the properties of the new <code>URL</code>
     * are obtained from the context.</li>
     * </ul>
     *
     * @param context
     *            java.net.URL URL to use as context.
     * @param spec
     *            java.lang.String a URL specification.
     * @param handler
     *            java.net.URLStreamHandler a URLStreamHandler.
     *
     * @throws MalformedURLException
     *             if the spec could not be parsed as an URL
     */
    public URL(URL context, String spec, URLStreamHandler handler)
            throws MalformedURLException {
        if (handler != null) {
            strmHandler = handler;
        }

        if (spec == null) {
            throw new MalformedURLException();
        }
        spec = spec.trim();

        // The spec includes a protocol if it includes a colon character
        // before the first occurrence of a slash character. Note that,
        // "protocol" is the field which holds this URLs protocol.
        int index;
        try {
            index = spec.indexOf(':');
        } catch (NullPointerException e) {
            throw new MalformedURLException(e.toString());
        }
        int startIPv6Addr = spec.indexOf('[');
        if (index >= 0) {
            if ((startIPv6Addr == -1) || (index < startIPv6Addr)) {
                protocol = spec.substring(0, index);
                // According to RFC 2396 scheme part should match
                // the following expression:
                // alpha *( alpha | digit | "+" | "-" | "." )
                char c = protocol.charAt(0);
                boolean valid = ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
                for (int i = 1; valid && (i < protocol.length()); i++) {
                    c = protocol.charAt(i);
                    valid = ('a' <= c && c <= 'z') ||
                            ('A' <= c && c <= 'Z') ||
                            ('0' <= c && c <= '9') ||
                            (c == '+') ||
                            (c == '-') ||
                            (c == '.');
                }
                if (!valid) {
                    protocol = null;
                    index = -1;
                } else {
                    // Ignore case in protocol names.
                    // Scheme is defined by ASCII characters.
                    protocol = Util.toASCIILowerCase(protocol);
                }
            }
        }

        if (protocol != null) {
            // If the context was specified, and it had the same protocol
            // as the spec, then fill in the receiver's slots from the values
            // in the context but still allow them to be over-ridden later
            // by the values in the spec.
            if (context != null && protocol.equals(context.getProtocol())) {
                String cPath = context.getPath();
                if (cPath != null && cPath.startsWith("/")) { //$NON-NLS-1$
                    set(protocol, context.getHost(), context.getPort(), context
                            .getAuthority(), context.getUserInfo(), cPath,
                            context.getQuery(), null);
                }
                if (strmHandler == null) {
                    strmHandler = context.strmHandler;
                }
            }
        } else {
            // If the spec did not include a protocol, then the context
            // *must* be specified. Fill in the receiver's slots from the
            // values in the context, but still allow them to be over-ridden
            // by the values in the ("relative") spec.
            if (context == null) {
                throw new MalformedURLException("Protocol not found: " + spec); //$NON-NLS-1$
            }
            set(context.getProtocol(), context.getHost(), context.getPort(),
                    context.getAuthority(), context.getUserInfo(), context
                            .getPath(), context.getQuery(), null);
            if (strmHandler == null) {
                strmHandler = context.strmHandler;
            }
        }

        // If the stream handler has not been determined, set it
        // to the default for the specified protocol.
        if (strmHandler == null) {
            setupStreamHandler();
            if (strmHandler == null) {
                throw new MalformedURLException("Unknown protocol: " + protocol); //$NON-NLS-1$
            }
        }

        // Let the handler parse the URL. If the handler throws
        // any exception, throw MalformedURLException instead.
        //
        // Note: We want "index" to be the index of the start of the scheme
        // specific part of the URL. At this point, it will be either
        // -1 or the index of the colon after the protocol, so we
        // increment it to point at either character 0 or the character
        // after the colon.
        try {
            strmHandler.parseURL(this, spec, ++index, spec.length());
        } catch (Exception e) {
            throw new MalformedURLException(e.toString());
        }

        if (port < -1) {
            throw new MalformedURLException("Port out of range: " + port); //$NON-NLS-1$
        }
    }

    /**
     * Constructs a new URL instance using the arguments provided.
     *
     * @param protocol
     *            String the protocol for the URL.
     * @param host
     *            String the name of the host.
     * @param file
     *            the name of the resource.
     *
     * @throws MalformedURLException
     *             if the parameters do not represent a valid URL.
     */
    public URL(String protocol, String host, String file)
            throws MalformedURLException {
        this(protocol, host, -1, file, (URLStreamHandler) null);
    }

    /**
     * Constructs a new URL instance using the arguments provided.
     *
     * @param protocol
     *            String the protocol for the URL.
     * @param host
     *            String the name of the host.
     * @param port
     *            int the port number.
     * @param file
     *            String the name of the resource.
     *
     * @throws MalformedURLException
     *             if the parameters do not represent a valid URL.
     */
    public URL(String protocol, String host, int port, String file)
            throws MalformedURLException {
        this(protocol, host, port, file, (URLStreamHandler) null);
    }

    /**
     * Constructs a new URL instance using the arguments provided.
     *
     * If the handler argument is non-null, a security check is made to verify
     * that user-defined protocol handlers can be specified.
     *
     * @param protocol
     *            the protocol for the URL.
     * @param host
     *            the name of the host.
     * @param port
     *            the port number.
     * @param file
     *            the name of the resource.
     * @param handler
     *            the stream handler that this URL uses.
     *
     * @throws MalformedURLException
     *             if the parameters do not represent an URL.
     */
    public URL(String protocol, String host, int port, String file,
            URLStreamHandler handler) throws MalformedURLException {
        if (port < -1) {
            throw new MalformedURLException("Port out of range: " + port); //$NON-NLS-1$
        }

        if (host != null && host.indexOf(":") != -1 && host.charAt(0) != '[') { //$NON-NLS-1$
            host = "[" + host + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (protocol == null) {
            throw new NullPointerException("Unknown protocol: null"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        this.protocol = protocol;
        this.host = host;
        this.port = port;

        // Set the fields from the arguments. Handle the case where the
        // passed in "file" includes both a file and a reference part.
        int index = -1;
        index = file.indexOf("#", file.lastIndexOf("/")); //$NON-NLS-1$ //$NON-NLS-2$
        if (index >= 0) {
            this.file = file.substring(0, index);
            ref = file.substring(index + 1);
        } else {
            this.file = file;
        }
        fixURL(false);

        // Set the stream handler for the URL either to the handler
        // argument if it was specified, or to the default for the
        // receiver's protocol if the handler was null.
        if (handler == null) {
            setupStreamHandler();
            if (strmHandler == null) {
                throw new MalformedURLException("Unknown protocol: " + protocol); //$NON-NLS-1$
            }
        } else {
            strmHandler = handler;
        }
    }

    void fixURL(boolean fixHost) {
        int index;
        if (host != null && host.length() > 0) {
            authority = host;
            if (port != -1) {
                authority = authority + ":" + port; //$NON-NLS-1$
            }
        }
        if (fixHost) {
            if (host != null && (index = host.lastIndexOf('@')) > -1) {
                userInfo = host.substring(0, index);
                host = host.substring(index + 1);
            } else {
                userInfo = null;
            }
        }
        if (file != null && (index = file.indexOf('?')) > -1) {
            query = file.substring(index + 1);
            path = file.substring(0, index);
        } else {
            query = null;
            path = file;
        }
    }

    /**
     * Sets the properties of this URL using the provided arguments. This method
     * is used both within this class and by the <code>URLStreamHandler</code>
     * code.
     *
     * @param protocol
     *            the new protocol.
     * @param host
     *            the new host name.
     * @param port
     *            the new port number.
     * @param file
     *            the new file component.
     * @param ref
     *            the new reference.
     *
     * @see URL
     * @see URLStreamHandler
     */
    protected void set(String protocol, String host, int port, String file,
            String ref) {
        if (this.protocol == null) {
            this.protocol = protocol;
        }
        this.host = host;
        this.file = file;
        this.port = port;
        this.ref = ref;
        hashCode = 0;
        fixURL(true);
    }

    /**
     * Compares the argument to the receiver, and answers true if they represent
     * the same URL. Two URLs are equal if they have the same file, host, port,
     * protocol, and reference components.
     *
     * @param o
     *            the object to compare with this URL.
     * @return <code>true</code> if the object is the same as this URL,
     *         <code>false</code> otherwise.
     *
     * @see #hashCode()
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        return strmHandler.equals(this, (URL) o);
    }

    /**
     * Answers true if the receiver and the argument refer to the same file. All
     * components except the reference are compared.
     *
     * @param otherURL
     *            URL to compare against.
     * @return true if the same resource, false otherwise
     */
    public boolean sameFile(URL otherURL) {
        return strmHandler.sameFile(this, otherURL);
    }

    /**
     * Answers a hash code for this URL object.
     *
     * @return the hashcode for hashtable indexing
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = strmHandler.hashCode(this);
        }
        return hashCode;
    }

    /**
     * Sets the receiver's stream handler to one which is appropriate for its
     * protocol. Throws a MalformedURLException if no reasonable handler is
     * available.
     * <p>
     * Note that this will overwrite any existing stream handler with the new
     * one. Senders must check if the strmHandler is null before calling the
     * method if they do not want this behavior (a speed optimization).
     */

    void setupStreamHandler() {
     // TODO? load up http stream handlers
    }


    /**
     * Creates a URI related with this URL
     *
     * @return a URI related to this URL
     * @throws URISyntaxException
     *             if this URL cannot format into URI
     */
    public URI toURI() throws URISyntaxException {
        return new URI(toExternalForm());
    }

    /**
     * Answers a string containing a concise, human-readable description of the
     * receiver.
     *
     * @return a printable representation for the receiver.
     */
    @Override
    public String toString() {
        return toExternalForm();
    }

    /**
     * Create and return the String representation of this URL.
     *
     * @return the external representation of this URL.
     *
     * @see #toString()
     * @see URL
     * @see URLStreamHandler#toExternalForm(URL)
     */
    public String toExternalForm() {
        if (strmHandler == null) {
            return "unknown protocol(" + protocol + ")://" + host + file; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return strmHandler.toExternalForm(this);
    }


    /**
     * Answers the file component of this URL.
     *
     * @return the receiver's file.
     */
    public String getFile() {
        return file;
    }

    /**
     * Answers the host component of this URL.
     *
     * @return the receiver's host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Answers the port component of this URL.
     *
     * @return the receiver's port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Answers the protocol component of this URL.
     *
     * @return the receiver's protocol.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Answers the reference component of this URL.
     *
     * @return the receiver's reference component.
     */
    public String getRef() {
        return ref;
    }

    /**
     * Answers the query component of this URL.
     *
     * @return the receiver's query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Answers the path component of this URL.
     *
     * @return the receiver's path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Answers the user info component of this URL.
     *
     * @return the receiver's user info.
     */
    public String getUserInfo() {
        return userInfo;
    }

    /**
     * Answers the authority component of this URL.
     *
     * @return the receiver's authority.
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Sets the properties of this URL using the provided arguments. This method
     * is used both within this class and by the <code>URLStreamHandler</code>
     * code.
     *
     * @param protocol
     *            the new protocol.
     * @param host
     *            the new host name.
     * @param port
     *            the new port number.
     * @param authority
     *            the new authority.
     * @param userInfo
     *            the new user info.
     * @param path
     *            the new path component.
     * @param query
     *            the new query.
     * @param ref
     *            the new reference.
     *
     * @see URL
     * @see URLStreamHandler
     */
    protected void set(String protocol, String host, int port,
            String authority, String userInfo, String path, String query,
            String ref) {
        String file = path;
        if (query != null && !query.equals("")) { //$NON-NLS-1$
            if (file != null) {
                file = file + "?" + query; //$NON-NLS-1$
            } else {
                file = "?" + query; //$NON-NLS-1$
            }
        }
        set(protocol, host, port, file, ref);
        this.authority = authority;
        this.userInfo = userInfo;
        this.path = path;
        this.query = query;
    }

    /**
     * Returns the default port for this URL as defined by the URLStreamHandler.
     *
     * @return the default port for this URL
     *
     * @see URLStreamHandler#getDefaultPort
     */
    public int getDefaultPort() {
        return strmHandler.getDefaultPort();
    }
}

/*
 * from http://svn.apache.org/repos/asf/harmony/enhanced/classlib/trunk/modules/luni/src/main/java/org/apache/harmony/luni/util/Util.java
 */

final class Util {

    public static String toASCIILowerCase(String s) {
        int len = s.length();
        StringBuilder buffer = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ('A' <= c && c <= 'Z') {
                buffer.append((char) (c + ('a' - 'A')));
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

}