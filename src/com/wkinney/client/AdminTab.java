/**
 * 
 */
package com.wkinney.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.TreeImages;

/**
 * @author wkinney
 * 
 */
public abstract class AdminTab extends Composite {


    final static SVNAdminServiceAsync svnAdminService = (SVNAdminServiceAsync) GWT.create(SVNAdminService.class);
    
    static {
        ServiceDefTarget endpoint = (ServiceDefTarget) svnAdminService;
        final String moduleRelativeURL = GWT.getModuleBaseURL() + "svn-web-admin";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
    }
    
    public static SVNAdminServiceAsync getAdminService() {
        return svnAdminService;       
    }
    
    /**
     * An image provider to make available images to Sinks.
     */
    public interface Images extends ImageBundle, TreeImages {
        AbstractImagePrototype svnAdminLogo();
    }

    /**
     * Encapsulated information about a sink. Each sink is expected to have a
     * static <code>init()</code> method that will be called by the kitchen
     * sink on startup.
     */
    public abstract static class AdminTabInfo {
        private AdminTab instance;
        private String name, description;

        public AdminTabInfo(String name, String desc) {
            this.name = name;
            this.description = desc;
        }

        public abstract AdminTab createInstance();

        public String getColor() {
            return "#2a8ebf";
        }

        public String getDescription() {
            return description;
        }

        public final AdminTab getInstance() {
            if (instance != null) {
                return instance;
            }
            return (instance = createInstance());
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Called just before this sink is hidden.
     */
    public void onHide() {
    }

    /**
     * Called just after this sink is shown.
     */
    public void onShow() {
    }
}
