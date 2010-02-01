package com.wkinney.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.wkinney.client.AdminTab.AdminTabInfo;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 * @author wkinney
 * 
 */
public class SVNWebAdmin implements EntryPoint, HistoryListener  {
    
    private static final AdminTab.Images images = (AdminTab.Images) GWT.create(AdminTab.Images.class);

    protected AdminTabList list = new AdminTabList(images);
    private AdminTabInfo curInfo;
    private AdminTab curAdminTab;
    private HTML description = new HTML();
    
    final private VerticalPanel panel = new VerticalPanel();
    
    public SVNWebAdmin() {
        
    }
    /**
     * Adds all sinks to the list. Note that this does not create actual instances
     * of all sinks yet (they are created on-demand). This can make a significant
     * difference in startup time.
     */
    protected void loadSinks() {
      list.addSink(Users.init(images));
      list.addSink(Groups.init(images));
      list.addSink(Projects.init(images));
    }

    private void showUsers() {
      show(list.find("Users"), false);
    }
    
    public void onHistoryChanged(String token) {
        // Find the AdminTabInfo associated with the history context. If one is
        // found, show it (It may not be found, for example, when the user mis-
        // types a URL, or on startup, when the first context will be "").
        AdminTabInfo info = list.find(token);
        if (info == null) {
            showUsers();
          return;
        }
        show(info, false);
      }
    
    public void show(AdminTabInfo info, boolean affectHistory) {
        // Don't bother re-displaying the existing sink. This can be an issue
        // in practice, because when the history context is set, our
        // onHistoryChanged() handler will attempt to show the currently-visible
        // sink.
        if (info == curInfo) {
          return;
        }
        curInfo = info;

        // Remove the old sink from the display area.
        if (curAdminTab != null) {
          curAdminTab.onHide();
          panel.remove(curAdminTab);
        }

        // Get the new sink instance, and display its description in the
        // sink list.
        curAdminTab = info.getInstance();
        list.setSinkSelection(info.getName());
        description.setHTML(info.getDescription());

        // If affectHistory is set, create a new item on the history stack. This
        // will ultimately result in onHistoryChanged() being called. It will call
        // show() again, but nothing will happen because it will request the exact
        // same sink we're already showing.
        if (affectHistory) {
          History.newItem(info.getName());
        }

        // Change the description background color.
        DOM.setStyleAttribute(description.getElement(), "backgroundColor",
            info.getColor());

        // Display the new sink.
        curAdminTab.setVisible(false);
        panel.add(curAdminTab);
        panel.setCellHorizontalAlignment(curAdminTab, VerticalPanel.ALIGN_CENTER);
        curAdminTab.setVisible(true);
        curAdminTab.onShow();
      }
    
    public void onModuleLoad() {

       
        
        // Load all the sinks.
        loadSinks();

        panel.add(list);
        panel.add(description);
        panel.setWidth("100%");

        description.setStyleName("ks-Info");

        History.addHistoryListener(this);
        RootPanel.get().add(panel);

        // Show the initial screen.
        String initToken = History.getToken();
        if (initToken.length() > 0) {
          onHistoryChanged(initToken);
        } else {
          showUsers();
        }
      
        

    }



}
