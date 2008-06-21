/**
 *
 */
package com.wkinney.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author wkinney
 *
 */
public class SVNAdminServiceProperties {


    private static Properties PROPERTIES;

    private static final String PROP_FILE_NAME = "svnwebadmin.properties";
    private static final String PROP_NAME_USER_DIR_ACCESS_FILE = "svnwebadmin.userdirectoryaccessfile";
    private static final String PROP_NAME_HTPASSWD_SCRIPT = "svnwebadmin.htpasswd.script";
    private static final String PROP_NAME_HTPASSWD_USERAUTHFILE = "svnwebadmin.htpasswd.userauthfile";
    private static final String PROP_NAME_HTPASSWD_FLAGS = "svnwebadmin.htpasswd.flags";

    private static String userDirectoryAccessFile;
    private static String htpasswdScript;
    private static String htpasswdUserAuthFile;
    private static String htpasswdFlags;


    static {
        synchronized (SVNAdminServiceProperties.class) {

            loadProperties();
        }

    }


    private static void loadProperties() {

        try {
            PROPERTIES = loadPropertiesAsResource(PROP_FILE_NAME);

        } catch (Exception e) {
            System.out.println("Error loading properties file: " + PROP_FILE_NAME + ", reason : " + e.getMessage());
            return;
        }

        userDirectoryAccessFile = (String) PROPERTIES.get(PROP_NAME_USER_DIR_ACCESS_FILE);
        if (userDirectoryAccessFile == null) {
            throw new RuntimeException("could not load property value for name: " + PROP_NAME_USER_DIR_ACCESS_FILE);
        }

        htpasswdScript = (String) PROPERTIES.get(PROP_NAME_HTPASSWD_SCRIPT);
        if (htpasswdScript == null) {
            throw new RuntimeException("could not load property value for name: " + PROP_NAME_HTPASSWD_SCRIPT);
        }

        htpasswdUserAuthFile = (String) PROPERTIES.get(PROP_NAME_HTPASSWD_USERAUTHFILE);
        if (htpasswdUserAuthFile == null) {
            throw new RuntimeException("could not load property value for name: " + PROP_NAME_HTPASSWD_USERAUTHFILE);
        }

        htpasswdFlags = (String) PROPERTIES.get(PROP_NAME_HTPASSWD_FLAGS);
        if (htpasswdFlags == null) {
            throw new RuntimeException("could not load property value for name: " + PROP_NAME_HTPASSWD_FLAGS);
        }

    }

    private static Properties loadPropertiesAsResource(String propertiesFileName) throws IOException {

        final Properties props = new Properties();
        final Thread currThread = Thread.currentThread();
        final ClassLoader myLoader = currThread.getContextClassLoader();
        InputStream is = null;

        try {
            is = myLoader.getResourceAsStream(propertiesFileName);
            if (is == null) {
                is = SVNAdminServiceProperties.class.getResourceAsStream(propertiesFileName);
                if (is == null) {
                    // TODO try loading from package level
                    throw new RuntimeException("could not find properties file in the current context");
                }
            }
            props.load(is);

        } finally {

            try {
                is.close();
                is = null;
            } catch (final Exception ex) { /* ignore */
            }
        }
        return props;
    }



    /**
     * @return the userDirectoryAccessFile
     */
    public static String getUserDirectoryAccessFile() {
        return userDirectoryAccessFile;
    }

    /**
     * @return the htpasswdScript
     */
    public static String getHtpasswdScript() {
        return htpasswdScript;
    }

    /**
     * @return the htpasswdUserAuthFile
     */
    public static String getHtpasswdUserAuthFile() {
        return htpasswdUserAuthFile;
    }

    /**
     * @return the htpasswdFlags
     */
    public static String getHtpasswdFlags() {
        return htpasswdFlags;
    }


}
