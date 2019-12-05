/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.errors;

/**
 * A parser exception indicating something went wrong while parsing the 
 * backend security configuration file.
 * 
 * @author Ross Wayland
 */
public final class BackendSecurityParserException
        extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a BackendSecurityParserException.
     * 
     * @param message
     *        An informative message explaining what happened and (possibly) how
     *        to fix it.
     */
    public BackendSecurityParserException(String message) {
        super(message);
    }

    public BackendSecurityParserException(String message, Throwable cause) {
        super(message,  cause);
    }

    public BackendSecurityParserException(String bundleName,
                                          String code,
                                          String[] values,
                                          String[] details,
                                          Throwable cause) {
        super( code,  cause);
    }
}
