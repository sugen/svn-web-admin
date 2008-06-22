package com.wkinney.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Position;
import com.gwtext.client.core.SortDir;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.GroupingStore;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.SortState;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.MessageBoxConfig;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.WaitConfig;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.Hidden;
import com.gwtext.client.widgets.form.Label;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.ValidationException;
import com.gwtext.client.widgets.form.Validator;
import com.gwtext.client.widgets.grid.BaseColumnConfig;
import com.gwtext.client.widgets.grid.CheckboxColumnConfig;
import com.gwtext.client.widgets.grid.CheckboxSelectionModel;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;
import com.gwtext.client.widgets.grid.GridPanel;
import com.gwtext.client.widgets.grid.GroupingView;
import com.gwtext.client.widgets.grid.event.GridRowListenerAdapter;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.ColumnLayout;
import com.gwtext.client.widgets.layout.ColumnLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 * @author wkinney
 * 
 */
public class SVNWebAdmin implements EntryPoint {

    final private Set groupSet = new HashSet();
    final private Set userSet = new HashSet();

    private static final int totalToLoad = 3;
    private static int loaded = 0;

    private void finishedLoading(final String loadedModule) {

        loaded++;
        if (loaded == totalToLoad) {
            MessageBox.hide();
        } else if (loaded < totalToLoad) {
            MessageBox.updateProgress((int)(((float) loaded / totalToLoad) * 100), "Loading item " + loaded + " of " + totalToLoad +"... ");
        }
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        final SVNAdminServiceAsync svnAdminService = (SVNAdminServiceAsync) GWT.create(SVNAdminService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) svnAdminService;
        
        /**
         * Uncomment this for dev mode (GWT Shell tomcat)
         * 
         */
        //String moduleRelativeURL = GWT.getModuleBaseURL() + "svn-web-admin";
        final String moduleRelativeURL = GWT.getModuleBaseURL() + "svn-web-admin";
        endpoint.setServiceEntryPoint(moduleRelativeURL);

        // 1st Panel - User
     //   final RecordDef userRecordDef = new RecordDef(new FieldDef[] { new StringFieldDef("user") });
       // final Store userStore = new Store(userRecordDef);
       // userStore.load();


        // 2nd Panel - Group
        final RecordDef groupingRecordDef = new RecordDef(new FieldDef[] { new StringFieldDef("group"), new StringFieldDef("user") });
        final GroupingStore groupingStore = new GroupingStore(groupingRecordDef);
        groupingStore.setSortInfo(new SortState("group", SortDir.ASC));
        groupingStore.setGroupField("group");

        final RecordDef userDDLRecordDef = new RecordDef(new FieldDef[]{
                new StringFieldDef("uservalue"),
                new StringFieldDef("user")});

        final Store userDDLStore = new Store(userDDLRecordDef);

        final RecordDef groupDDLRecordDef = new RecordDef(new FieldDef[]{
                new StringFieldDef("groupvalue"),
                new StringFieldDef("group")});

        final Store groupDDLStore = new Store(groupDDLRecordDef);

        // 3rd Panel
        final RecordDef ppRecordDef = new RecordDef(new FieldDef[] { new StringFieldDef("projectPath") });
        final Store ppStore = new Store(ppRecordDef);


        // Show progress bar
        MessageBox.show(new MessageBoxConfig() {
             {
                 setTitle("Please wait...");
                 setMsg("Initializing...");
                 setWidth(240);
                 setProgress(true);
                 setClosable(false);

             }
         });

        populateUsersList(svnAdminService, userDDLStore, userDDLRecordDef);
        populateGroupsPanel(svnAdminService, groupingStore, userDDLStore, groupDDLStore, groupingRecordDef, userDDLRecordDef, groupDDLRecordDef);
        populateProjectPathPanel(svnAdminService, ppStore, ppRecordDef);



        // ----------------------
        // 1st Tab - "Users"

        final Panel usersPanel = createUsersTab(svnAdminService, userDDLStore, groupingStore, groupDDLStore);

        // ----------------------
        // 2nd Tab - "Groups"

        final Panel groupsPanel = createGroupsTab(svnAdminService, groupingStore, userDDLStore, groupDDLStore);

        // ----------------------
        // 3rd Tab - "Projects"

        final Panel projectsPanel = createProjectsTab(svnAdminService, ppStore, ppRecordDef, groupDDLStore, groupDDLRecordDef);


        // --------------------
        // Tab Panels

        TabPanel tabs = new TabPanel();
        tabs.setWidth(500);
        tabs.setHeight(400);
        tabs.setPaddings(8);
        tabs.add(usersPanel);
        tabs.add(groupsPanel);
        tabs.add(projectsPanel);
        tabs.setActiveItem(0);

        // Wrapper for TabPanel
        Panel verticalPanel = new Panel();
        verticalPanel.setLayout(new FitLayout());
        verticalPanel.setBorder(false);
        verticalPanel.add(tabs);


        // Assume that the host HTML has elements defined whose
        // IDs are "slot1", "slot2". In a real app, you probably would not want
        // to hard-code IDs. Instead, you could, for example, search for all
        // elements with a particular CSS class and replace them with widgets.
        //
        // Viewport vp = new Viewport(verticalPanel);

        RootPanel.get("slot1").add(verticalPanel);

        // RootPanel.get("left1").add(save);
        // RootPanel.get("left2").add(load);
        // RootPanel.get("left3").add(response);

    }

    /**
     * Wrapper
     *
     * @param svnAdminService
     * @param store
     * @param userStore
     * @param groupStore
     */
    private void populateGroupsPanel(final SVNAdminServiceAsync svnAdminService, final Store store, final Store userStore,
            final Store groupStore) {
        final RecordDef recordDef = new RecordDef(new FieldDef[] { new StringFieldDef("group"), new StringFieldDef("user") });

        // Right panel, user drop down
        final RecordDef userRecordDef = new RecordDef(new FieldDef[]{
                new StringFieldDef("uservalue"),
                new StringFieldDef("user")});

        // Right panel, group drop down
        final RecordDef groupRecordDef = new RecordDef(new FieldDef[]{
                new StringFieldDef("groupvalue"),
                new StringFieldDef("group")});

        populateGroupsPanel(svnAdminService, store, userStore, groupStore, recordDef, userRecordDef, groupRecordDef);
    }

    private void populateGroupsPanel(final SVNAdminServiceAsync svnAdminService, final Store store, final Store userStore,
            final Store groupStore, final RecordDef recordDef, final RecordDef userRecordDef, final RecordDef groupRecordDef) {

        AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {

                store.removeAll();
                userStore.removeAll();
                groupStore.removeAll();

                Map groupMembersMap = (Map) result;

                groupSet.add("*");

                for (Iterator it = groupMembersMap.keySet().iterator(); it.hasNext();) {
                    String groupName = (String) it.next();

                    groupSet.add(groupName);

                    Collection memberList = (Collection) groupMembersMap.get(groupName);
                    for (Iterator it2 = memberList.iterator(); it2.hasNext();) {
                        String member = (String) it2.next();

                        userSet.add(member);

                        store.add(recordDef.createRecord(new Object[] { groupName, member }));
                    }

                }

                store.load();
                store.sort("group", SortDir.ASC);

                for (Iterator i = userSet.iterator(); i.hasNext();) {
                    String user = (String) i.next();
                    userStore.add(userRecordDef.createRecord(new Object[] { user, user }));
                }
                userStore.load();
                userStore.sort("user", SortDir.ASC);

                for (Iterator i = groupSet.iterator(); i.hasNext();) {
                    String group = (String) i.next();
                    // skip * as it only applies to project access
                    if (!group.equals("*")) {
                        groupStore.add(groupRecordDef.createRecord(new Object[] { group, group }));
                    }

                }
                groupStore.load();
                groupStore.sort("group", SortDir.ASC);

                Timer timer = new Timer() {
                    public void run() {
                        finishedLoading("Group List");
                    }
                };
                timer.schedule(1000);

            }

            public void onFailure(Throwable caught) {
               // GWT.log("Fail! " + System.currentTimeMillis());
                caught.printStackTrace();
            }
        };

        svnAdminService.getGroupMembersMap(callback);

    }

    private Panel createGroupsTab(final SVNAdminServiceAsync svnAdminService, final GroupingStore store,
            final Store userStore, final Store groupStore) {

        // Right panel, user drop down
        userStore.setSortInfo(new SortState("user", SortDir.ASC));

        // Right panel, group drop down
        groupStore.setSortInfo(new SortState("group", SortDir.ASC));

   //     populateGroupsPanel(svnAdminService, store, userStore, groupStore, recordDef, userRecordDef, groupRecordDef);

        final CheckboxSelectionModel cbSelectionModel = new CheckboxSelectionModel();

        final FormPanel addMembershipFormPanel = new FormPanel(Position.RIGHT);
        addMembershipFormPanel.setFrame(true);
        addMembershipFormPanel.setTitle("Add Membership");
       // addMembershipFormPanel.setHeight(200);
        addMembershipFormPanel.setLabelWidth(75);
        addMembershipFormPanel.setIconCls("user-add-icon");
        addMembershipFormPanel.setVisible(false);

        final ComboBox userCB = new ComboBox();
        final ComboBox groupCB = new ComboBox();

        Button removeButton = new Button("Remove Checked Membership(s)", new ButtonListenerAdapter() {
            public void onClick(final Button button, EventObject e) {
                final Record[] records = cbSelectionModel.getSelections();

                if (records.length == 0) {
                    return;
                }

                String users = null;
                for (int i = 0; i < records.length; i++) {
                    Record record = records[i];
                    if (users == null) {
                        users = record.getAsString("user");
                    } else {
                        users +=  ", " + record.getAsString("user");
                    }

                }
                //GWT.log("memberships selected to remove :" + users);

                MessageBox.confirm("Confirm", "Remove membership for the following user(s) \"" + users + "\"?", new MessageBox.ConfirmCallback() {
                    public void execute(String btnID) {
                       // GWT.log("Button Click : " + Format.format("You clicked the {0} button", btnID));
                        if (btnID.equalsIgnoreCase("Yes")) {

                            addMembershipFormPanel.setVisible(false);

                         // Show progress bar
                            MessageBox.show(new MessageBoxConfig() {
                                {
                                    setMsg("Removing membership(s), please wait...");
                                    setProgressText("Removing...");
                                    setWidth(300);
                                    setWait(true);
                                    setWaitConfig(new WaitConfig() {
                                        {
                                            setInterval(200);
                                        }
                                    });
                                    setAnimEl(button.getId());
                                }
                            });

                           // delete user async call
                            removeMembership(svnAdminService, store, userStore, groupStore, records);


                        }

                    }
                });

            }
        });



        Button addMembershipButton = new Button("Add Membership", new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {
                userCB.setValue("");
                groupCB.setValue("");

                addMembershipFormPanel.setVisible(true);
                userCB.focus();
               // GWT.log("Add Membership button clicked");
            }
        });

        BaseColumnConfig[] columns = new BaseColumnConfig[] {
                new CheckboxColumnConfig(cbSelectionModel),
                new ColumnConfig("Group", "group", 160, true, null, "group"),
                new ColumnConfig("User", "user", 160, true, null, "user")};

        ColumnModel columnModel = new ColumnModel(columns);

        final GridPanel groupedUsersPanel = new GridPanel();
        groupedUsersPanel.setTopToolbar(addMembershipButton);
        groupedUsersPanel.setStore(store);
        groupedUsersPanel.setColumnModel(columnModel);
        groupedUsersPanel.setSelectionModel(cbSelectionModel);
        groupedUsersPanel.setFrame(true);
        groupedUsersPanel.setStripeRows(true);
        groupedUsersPanel.setAutoExpandColumn("users");
        groupedUsersPanel.setTitle("Groups and Users");
        groupedUsersPanel.hideColumn("group");
        groupedUsersPanel.hideColumnHeader();


        groupedUsersPanel.addButton(removeButton);
        groupedUsersPanel.setButtonAlign(Position.LEFT);

        GroupingView gridView = new GroupingView();
        gridView.setForceFit(true);
        gridView.setGroupTextTpl("{text} ({[values.rs.length]} {[values.rs.length > 1 ? \"Members\" : \"Member\"]})");


        groupedUsersPanel.setView(gridView);
        groupedUsersPanel.setFrame(true);
      //  grid.setWidth(520);
        groupedUsersPanel.setHeight(400);
       // grid.setCollapsible(true);
      //  grid.setAnimCollapse(false);
        groupedUsersPanel.setTitle("Group Membership");
        groupedUsersPanel.setIconCls("user-suit-icon");

     // Right Panel

        // set reader and error reader
       // formPanel.setReader(reader);
        //formPanel.setErrorReader(errorReader);


        Label label = new Label();
        label.setHtml("<p>Use this form to add a new group membership for a user.</p>");
       // label.setCls("simple-form-label");
        addMembershipFormPanel.add(label);

        Label label2 = new Label();
        label2.setHtml("<p>To add a user to a new group, enter the new group name.</p>");
        label2.setHeight(20);
       // label.setCls("simple-form-label");
        addMembershipFormPanel.add(label2);

        final class UserCBValidator implements Validator {
            public boolean validate(String value) throws ValidationException {
                if (value == null || value.trim().equals("")) {
                    return false;
                }
                if (!userSet.contains(value)) {
                    return false;
                }
                return true;
            }

        }
        final class GroupCBValidator implements Validator {
            public boolean validate(String value) throws ValidationException {
                if (value == null || value.trim().equals("")) {
                    return false;
                }
                // can enter a new group name
                return true;
            }

        }

        userCB.setFieldLabel("User");
        userCB.setHiddenName("user");
        userCB.setStore(userStore);
        userCB.setDisplayField("uservalue");
        userCB.setTypeAhead(true);
        userCB.setMode(ComboBox.LOCAL);
        userCB.setTriggerAction(ComboBox.ALL);
        userCB.setEmptyText("Select a user...");
        userCB.setSelectOnFocus(true);
        userCB.setWidth(190);
        userCB.setValidator(new UserCBValidator());



        groupCB.setFieldLabel("Group");
        groupCB.setHiddenName("group");
        groupCB.setStore(groupStore);
        groupCB.setDisplayField("groupvalue");
        groupCB.setTypeAhead(true);
        groupCB.setMode(ComboBox.LOCAL);
        groupCB.setTriggerAction(ComboBox.ALL);
        groupCB.setEmptyText("Select a group...");
        groupCB.setSelectOnFocus(true);
        groupCB.setWidth(190);
        groupCB.setValidator(new GroupCBValidator());

        addMembershipFormPanel.add(userCB);

        addMembershipFormPanel.add(groupCB);

        final Button submitBtn = new Button("Save", new ButtonListenerAdapter() {
            public void onClick(final Button button, EventObject e) {

                if (userCB.getValueAsString().trim().equals("") ||
                        groupCB.getValueAsString().trim().equals("")) {
                    return;
                }

                if (userCB.isValid() && groupCB.isValid()) {

                    final String group = groupCB.getValueAsString();
                    final String user = userCB.getValueAsString();

                   // GWT.log("membership to add: " + user + " to group: " + group);

                    MessageBox.confirm("Confirm", "Add membership to group \"" + group + "\" for user \"" + user + "\"?", new MessageBox.ConfirmCallback() {
                        public void execute(String btnID) {
                        //    GWT.log("Button Click : " + Format.format("You clicked the {0} button", btnID));
                            if (btnID.equalsIgnoreCase("Yes")) {


                             // Show progress bar
                                MessageBox.show(new MessageBoxConfig() {
                                    {
                                        setMsg("Adding membership, please wait...");
                                        setProgressText("Adding...");
                                        setWidth(300);
                                        setWait(true);
                                        setWaitConfig(new WaitConfig() {
                                            {
                                                setInterval(200);
                                            }
                                        });
                                        setAnimEl(button.getId());
                                    }
                                });

                               // delete user async call
                                addMembership(svnAdminService, store, userStore, groupStore, addMembershipFormPanel, group, user);


                            }

                        }
                    });

                } else {
                    MessageBox.alert("Error", "One or more of the required fields are not valid.");
                }

            }
        });
        final Button addMemberCancelButton = new Button("Cancel", new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {
                addMembershipFormPanel.setVisible(false);
                userCB.setValue("");
                groupCB.setValue("");
            }
        });

        addMembershipFormPanel.addButton(submitBtn);
        addMembershipFormPanel.addButton(addMemberCancelButton);

     // Join Left and Right
        final Panel groupsLeftPanel = new Panel();
        groupsLeftPanel.setLayout(new FitLayout());
        groupsLeftPanel.add(groupedUsersPanel);
        groupsLeftPanel.setBorder(false);
        groupsLeftPanel.setPaddings(4);

        final Panel groupsRightPanel = new Panel();
        groupsRightPanel.setLayout(new FitLayout());
        groupsRightPanel.add(addMembershipFormPanel);
        groupsRightPanel.setBorder(false);
        groupsRightPanel.setPaddings(4);

        Panel groupsPanel = new Panel();
        groupsPanel.setTitle("Groups");
        groupsPanel.setBorder(false);
        groupsPanel.setLayout(new FitLayout());
        Panel groupsWrapperPanel = new Panel();
        groupsWrapperPanel.setBorder(false);

        groupsWrapperPanel.setLayout(new ColumnLayout());
        groupsWrapperPanel.add(groupsLeftPanel, new ColumnLayoutData(.50));
        groupsWrapperPanel.add(groupsRightPanel, new ColumnLayoutData(.50));
        // projectsWrapperPanel.add(new Panel("hello", 200, 300), new AnchorLayoutData("20% 100%"));
        // projectsWrapperPanel.add(new Panel("hello2", 300, 200), new AnchorLayoutData("70% 100%"));
        groupsPanel.add(groupsWrapperPanel);

        return groupsPanel;


    }

   private void populateUsersList(final SVNAdminServiceAsync svnAdminService, final Store store) {
       final RecordDef userDDLRecordDef = new RecordDef(new FieldDef[]{
               new StringFieldDef("uservalue"),
               new StringFieldDef("user")});
       populateUsersList(svnAdminService, store, userDDLRecordDef);
   }


   private void removeMembership(final SVNAdminServiceAsync svnAdminService, final GroupingStore store, final Store userStore,
           final Store groupStore, final Record[] records) {


       Set memberships = new HashSet();
       Set uniqueUserSet = new HashSet();
       for (int i = 0; i < records.length; i++) {
           String group = records[i].getAsString("group");
           String user = records[i].getAsString("user");

           uniqueUserSet.add(user);

           memberships.add(new Membership(group, user));


       }

       String prettyPrintUniqueUsers = null;
       for (Iterator i = uniqueUserSet.iterator(); i.hasNext();) {
           String user = (String) i.next();
           if (prettyPrintUniqueUsers == null) {
               prettyPrintUniqueUsers = user;
           } else {
               prettyPrintUniqueUsers += ", " + user;
           }
       }
       final String uniqueUsers = prettyPrintUniqueUsers;

       AsyncCallback callback = new AsyncCallback() {
           public void onSuccess(Object result) {

               MessageBox.hide();
               MessageBox.alert("Success", "User membership(s) successfully removed.");

               populateGroupsPanel(svnAdminService, store, userStore, groupStore);


           }

           public void onFailure(Throwable e) {
               MessageBox.hide();
               MessageBox.alert("Error", "There was an error in removing memberships for users: " + uniqueUsers);
              // GWT.log("Fail! " + System.currentTimeMillis());
               e.printStackTrace();
           }
       };


       svnAdminService.removeMembership(memberships, callback);

   }


   private void removeProjectAccess(final SVNAdminServiceAsync svnAdminService, final String projectPath, final Record[] removeRecords,
           final GridPanel accessGrid, final Store accessStore, final RecordDef accessRecordDef) {

       // pretty print removed groups
       String accessRemoved = null;
       for (int i = 0; i < removeRecords.length; i++) {
           String group = removeRecords[i].getAsString("group");
           if (accessRemoved == null) {
               accessRemoved = group;
           } else {
               accessRemoved += ", " + group;
           }
       }

       // populate helper Set of all records
       Record[] allRecords = accessStore.getRecords();
       // <Record>
       Set allRecordSet = new HashSet();
       for (int i = 0; i < allRecords.length; i++) {
           allRecordSet.add(allRecords[i]);
       }

       // Populate helper Set of all groups that we are removing
       // <String>
       Set removeRecordSet = new HashSet();
       for (int i = 0; i < removeRecords.length; i++) {
           removeRecordSet.add(removeRecords[i].getAsString("group"));
       }


       Set toRemoveSet = new HashSet();
       // iterate through all records, checkt to see if it exists in remove set, if it does, add it to set for remove
       for (Iterator i = allRecordSet.iterator(); i.hasNext(); ) {
           Record r = (Record) i.next();
           if (removeRecordSet.contains(r.getAsString("group"))) {
               toRemoveSet.add(r);
           }
       }

       // remove records
       allRecordSet.removeAll(toRemoveSet);

       for (Iterator it = allRecordSet.iterator(); it.hasNext(); ) {
          // GWT.log("updated value: " + it.next());
       }

     //<String, String>
       Map updatedAccessMap = new HashMap();

       // create map of access for async call
       String group = null;
       String access = null;
       for (Iterator i = allRecordSet.iterator(); i.hasNext();) {
           Record r = (Record) i.next();

           group = r.getAsString("group");
           access = r.getAsString("accessType");

           updatedAccessMap.put(group, access);
       }


       updateProjectAccess(svnAdminService, projectPath, updatedAccessMap, accessRemoved, accessGrid, accessStore, accessRecordDef);


   }


   private void updateProjectAccess(final SVNAdminServiceAsync svnAdminService, final String projectPath,
           final Map updatedAccessMap, final String updateDetails, final GridPanel accessGrid, final Store accessStore,
           final RecordDef accessRecordDef) {

       AsyncCallback callback = new AsyncCallback() {
           public void onSuccess(Object result) {

               Timer t = new Timer() {
                   public void run() {
                       MessageBox.hide();
                       MessageBox.alert("Success", "Project access successfully updated.");

                       populateAccessGridPanel(svnAdminService, accessGrid, accessStore, projectPath, accessRecordDef);
                   }
               };
               t.schedule(500);



           }

           public void onFailure(Throwable e) {
               MessageBox.hide();
               MessageBox.alert("Error", "There was an error in updating project access for groups: " + updateDetails);
               //GWT.log("Fail! " + System.currentTimeMillis());
               e.printStackTrace();
           }
       };


       svnAdminService.updateProjectAccess(projectPath, updatedAccessMap, callback);

   }


   private void addProjectAccess(final SVNAdminServiceAsync svnAdminService, final Window addGroupAccessWindow, final String projectPath,
           final String groupName, final String accessType, final GridPanel accessGrid, final Store accessStore,
           final RecordDef accessRecordDef) {

       AsyncCallback callback = new AsyncCallback() {
           public void onSuccess(Object result) {

               Timer t = new Timer() {
                   public void run() {
                       addGroupAccessWindow.hide();
                       MessageBox.alert("Success", "Project access successfully added.");

                       populateAccessGridPanel(svnAdminService, accessGrid, accessStore, projectPath, accessRecordDef);

                   }
               };
               t.schedule(500);


           }

           public void onFailure(Throwable e) {
               MessageBox.hide();
               MessageBox.alert("Error", "There was an error in adding project access: " + accessType + " for group: " + groupName);
              // GWT.log("Fail! " + System.currentTimeMillis());
               e.printStackTrace();
           }
       };


       svnAdminService.addProjectAccess(projectPath, groupName, accessType, callback);

   }

   private void removeProjectAccess(final SVNAdminServiceAsync svnAdminService, final String projectPath, final String groupName,
           final String accessType, final GridPanel accessGrid, final Store accessStore,
           final RecordDef accessRecordDef) {

       AsyncCallback callback = new AsyncCallback() {
           public void onSuccess(Object result) {

               MessageBox.alert("Success", "Project access successfully removed.");

               populateAccessGridPanel(svnAdminService, accessGrid, accessStore, projectPath, accessRecordDef);


           }

           public void onFailure(Throwable e) {
               MessageBox.hide();
               MessageBox.alert("Error", "There was an error in removing project access: " + accessType + " for group: " + groupName);
              // GWT.log("Fail! " + System.currentTimeMillis());
               e.printStackTrace();
           }
       };


       svnAdminService.removeProjectAccess(projectPath, groupName, accessType, callback);

   }

   private void addMembership(final SVNAdminServiceAsync svnAdminService, final GroupingStore store, final Store userStore,
           final Store groupStore, final FormPanel addMembershipFormPanel, final String groupName, final String userName) {

       Collection userSet = new HashSet(1);
       userSet.add(userName);

       AsyncCallback callback = new AsyncCallback() {
           public void onSuccess(Object result) {


               Timer t = new Timer() {

                   public void run() {
                        addMembershipFormPanel.setVisible(false);

                        MessageBox.hide();
                        MessageBox.alert("Success", "User membership successfully created.");

                        populateGroupsPanel(svnAdminService, store, userStore, groupStore);

                   }
               };
               t.schedule(500);

           }

           public void onFailure(Throwable e) {
               MessageBox.hide();
               MessageBox.alert("Error", "There was an error in creating the membership for user: " + userName);
              // GWT.log("Fail! " + System.currentTimeMillis());
               e.printStackTrace();
           }
       };


       svnAdminService.addMembership(groupName, userSet, callback);

   }

   private void populateUsersList(final SVNAdminServiceAsync svnAdminService, final Store userStore, final RecordDef userRecordDef) {


       AsyncCallback userListCallback = new AsyncCallback() {
           public void onSuccess(Object result) {

               userSet.clear();
               userStore.removeAll();

               String[] users = (String[]) result;

               for (int i = 0; i < users.length; i++) {
                   userSet.add(users[i]);
                   userStore.add(userRecordDef.createRecord(new Object[] {users[i], users[i]}));
               }

               userStore.load();
               userStore.sort("user", SortDir.ASC);

               Timer timer = new Timer() {
                   public void run() {
                       finishedLoading("User List");
                   }
               };
               timer.schedule(2000);



           }

           public void onFailure(Throwable caught) {
             //  GWT.log("Fail! " + System.currentTimeMillis());
               caught.printStackTrace();
           }
       };

       svnAdminService.getUserList(userListCallback);
   }

   private Panel createUsersTab(final SVNAdminServiceAsync svnAdminService, final Store userStore, final Store groupingStore,
           final Store groupStore) {

       // Wrapper panel for later onClick Edit or Add
        final Panel userEditOrAddPanel = new Panel();
        userEditOrAddPanel.setBorder(false);
        userEditOrAddPanel.setHeight(350);
        userEditOrAddPanel.setWidth(450);

        // left side list of users
        final GridPanel userGrid = new GridPanel();

        userGrid.setStore(userStore);

        //populateUsersList(svnAdminService, userRecordDef, userStore);

        ColumnConfig[] columns = new ColumnConfig[] { new ColumnConfig("User", "user", 220, true, null, "user") };

        ColumnModel columnModel = new ColumnModel(columns);
        userGrid.setColumnModel(columnModel);

        userGrid.setFrame(false);
        userGrid.setStripeRows(true);
        userGrid.setAutoExpandColumn("user");
        userGrid.setAutoHeight(true);
       // userGrid.setHeight(350);
        userGrid.setWidth(220);
        // userGrid.setTitle("Users");

        userGrid.addGridRowListener(new GridRowListenerAdapter() {
            public void onRowClick(GridPanel grid, int rowIndex, EventObject e) {
                userEditOrAddPanel.clear();
                userEditOrAddPanel.hide();
                Store store = grid.getStore();
                Record record = store.getAt(rowIndex);
                String user = record.getAsString("user");
               // String user = (String) memberList.get(rowIndex);
               // GWT.log("Clicked on user: " + user);
                setUserEditPanel(svnAdminService, user, userEditOrAddPanel, userStore, groupingStore, groupStore);
                userEditOrAddPanel.doLayout(true);
                userEditOrAddPanel.show();
            }
        });



        Button addUser = new Button("Add User", new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {
               // GWT.log("Add user button clicked");
                userEditOrAddPanel.clear();
                userEditOrAddPanel.hide();
                setUserAddPanel(svnAdminService, userEditOrAddPanel, userStore);
                userEditOrAddPanel.doLayout(true);
                userEditOrAddPanel.show();
            }
        });
        addUser.setIconCls("user-add-icon");

        userGrid.addButton(addUser);

        
        final Panel usersLeftPanel = new Panel();
        usersLeftPanel.setLayout(new FitLayout());
        usersLeftPanel.add(userGrid);
        usersLeftPanel.setBorder(false);
        usersLeftPanel.setPaddings(4);

        final Panel usersRightPanel = new Panel();
        usersRightPanel.setLayout(new FitLayout());
        usersRightPanel.add(userEditOrAddPanel);
        usersRightPanel.setBorder(false);
        usersRightPanel.setPaddings(4);
       // projectsRightPanel.setVisible(false);

        
        final Panel usersPanel = new Panel();
        usersPanel.setTitle("Users");
        usersPanel.setBorder(false);
        usersPanel.setLayout(new FitLayout());
        Panel usersWrapperPanel = new Panel();
        usersWrapperPanel.setBorder(false);

        usersWrapperPanel.setLayout(new ColumnLayout());
        usersWrapperPanel.add(usersLeftPanel, new ColumnLayoutData(.30));
        usersWrapperPanel.add(usersRightPanel, new ColumnLayoutData(.70));
        usersPanel.add(usersWrapperPanel);

        return usersPanel;

    }

   private void setUserEditPanel(final SVNAdminServiceAsync svnAdminService, String user, final Panel wrapperPanel, final Store userStore,
           final Store groupingStore, final Store groupStore) {

        final FormPanel formPanel = new FormPanel();
        formPanel.setFrame(true);
        formPanel.setTitle("Edit User");
        formPanel.setWidth(400);
        formPanel.setIconCls("user-edit-icon");
        formPanel.setLabelWidth(120);
        formPanel.setUrl("save-form.php");


        Label label = new Label();
        label.setHtml("<p>Use this form to update a user's Username, Password or to Delete them.</p>");
        label.setCls("simple-form-label");
        label.setWidth(375);
        label.setHeight(20);

        formPanel.add(label);

        final TextField username = new TextField("Username", "username", 230, user);
        username.setAllowBlank(false);
        formPanel.add(username);

        final Hidden usernameHidden = new Hidden("usernameHidden", user);
        formPanel.add(usernameHidden);

        final TextField password = new TextField("New Password", "password", 230);
        final TextField passwordConfirm = new TextField("Confirm Password", "passwordConfirm", 230);

        final class SecondPasswordValidator implements Validator {
            public boolean validate(String value) throws ValidationException {
                if(value == null || value.trim().equals("")) {
               //     GWT.log("password is blank");
                    return false;
                }
//               assumes this validator is set to the second password object
                if (password.getValueAsString() == null || password.getValueAsString().trim().equals("")) {
              //      GWT.log("passwordConfirm is blank");
                    return false;
                }
                // make sure they equal each other
                if (!value.trim().equals(password.getValueAsString().trim())) {
             //       GWT.log("passwords do not equal");
                    return false;
                }
                return true;
            }

        }


        password.setAllowBlank(false);
        password.setPassword(true);
        formPanel.add(password);

        passwordConfirm.setAllowBlank(false);
        passwordConfirm.setPassword(true);
        passwordConfirm.setValidator(new SecondPasswordValidator());
        formPanel.add(passwordConfirm);


        Button save = new Button("Save", new ButtonListenerAdapter() {
            public void onClick(final Button button, EventObject e) {

                if (username.validate() && password.validate() && passwordConfirm.validate()) {
                    // Show progress bar
                     MessageBox.show(new MessageBoxConfig() {
                         {
                             setMsg("Updating user, please wait...");
                             setProgressText("Saving...");
                             setWidth(300);
                             setWait(true);
                             setWaitConfig(new WaitConfig() {
                                 {
                                     setInterval(200);
                                 }
                             });
                             setAnimEl(button.getId());
                         }
                     });

                    // edit user async call
                     editUser(svnAdminService, wrapperPanel, userStore, usernameHidden.getValueAsString(),
                             username.getValueAsString(), password.getValueAsString(), groupingStore, groupStore);

                } else {
                    MessageBox.alert("Error", "One or more of the required fields are not valid.");
                }

           }
         });
        formPanel.addButton(save);

        Button cancel = new Button("Cancel", new ButtonListenerAdapter() {
              public void onClick(Button button, EventObject e) {
                  wrapperPanel.clear();
            }
        });
        formPanel.addButton(cancel);

        Button delete = new Button("Delete", new ButtonListenerAdapter() {
              public void onClick(final Button button, EventObject e) {
                MessageBox.confirm("Confirm", "Delete the user \"" + username.getValueAsString() + "\"?", new MessageBox.ConfirmCallback() {
                    public void execute(String btnID) {
                    //    GWT.log("Button Click : " + Format.format("You clicked the {0} button", btnID));
                        if (btnID.equalsIgnoreCase("Yes")) {

                         // Show progress bar
                            MessageBox.show(new MessageBoxConfig() {
                                {
                                    setMsg("Deleting user, please wait...");
                                    setProgressText("Deleting...");
                                    setWidth(300);
                                    setWait(true);
                                    setWaitConfig(new WaitConfig() {
                                        {
                                            setInterval(200);
                                        }
                                    });
                                    setAnimEl(button.getId());
                                }
                            });

                           // delete user async call
                            deleteUser(svnAdminService, wrapperPanel, userStore, username.getValueAsString(), groupingStore, groupStore);


                        }

                    }
                });
            }
        });

        delete.setIconCls("user-delete-icon");

        formPanel.addButton(delete);

        wrapperPanel.add(formPanel);

    }


    private void setUserAddPanel(final SVNAdminServiceAsync svnAdminService, final Panel wrapperPanel, final Store userStore) {

        final FormPanel formPanel = new FormPanel();
        formPanel.setFrame(true);
        formPanel.setTitle("Add User");
        formPanel.setIconCls("user-add-icon");
        formPanel.setWidth(400);
        formPanel.setLabelWidth(120);
        formPanel.setUrl("save-form.php");

        Label label = new Label();
        label.setHtml("<p>Use this form to add a User.</p>");
        label.setCls("simple-form-label");
        label.setWidth(375);
        label.setHeight(20);

        formPanel.add(label);

        final TextField username = new TextField("Username", "username", 230);
        username.setAllowBlank(false);
        formPanel.add(username);

        final TextField password = new TextField("Password", "password", 230);
        final TextField passwordConfirm = new TextField("Confirm Password", "passwordConfirm", 230);

        final class SecondPasswordValidator implements Validator {
            public boolean validate(String value) throws ValidationException {
                if(value == null || value.trim().equals("")) {
                  //  GWT.log("password is blank");
                    return false;
                }
//               assumes this validator is set to the second password object
                if (password.getValueAsString() == null || password.getValueAsString().trim().equals("")) {
                 //   GWT.log("passwordConfirm is blank");
                    return false;
                }
                // make sure they equal each other
                if (!value.trim().equals(password.getValueAsString().trim())) {
                //    GWT.log("passwords do not equal");
                    return false;
                }
                return true;
            }

        }


        password.setAllowBlank(false);
        password.setPassword(true);
        formPanel.add(password);


        passwordConfirm.setAllowBlank(false);
        passwordConfirm.setPassword(true);
        passwordConfirm.setValidator(new SecondPasswordValidator());
        formPanel.add(passwordConfirm);

        Button save = new Button("Save", new ButtonListenerAdapter() {
            public void onClick(final Button button, EventObject e) {

                if (username.validate() && password.validate() && passwordConfirm.validate()) {
                    // Show progress bar
                     MessageBox.show(new MessageBoxConfig() {
                         {
                             setMsg("Adding user, please wait...");
                             setProgressText("Saving...");
                             setWidth(300);
                             setWait(true);
                             setWaitConfig(new WaitConfig() {
                                 {
                                     setInterval(200);
                                 }
                             });
                             setAnimEl(button.getId());
                         }
                     });

                    // add user async call
                     addUser(svnAdminService, wrapperPanel, userStore, username.getValueAsString(), password.getValueAsString());

                } else {
                    MessageBox.alert("Error", "One or more of the required fields are not valid.");
                }

          }
        });
        formPanel.addButton(save);

        Button cancel = new Button("Cancel", new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {
                wrapperPanel.clear();
          }
        });
        formPanel.addButton(cancel);

        wrapperPanel.add(formPanel);

    }


    private Panel createProjectsTab(final SVNAdminServiceAsync svnAdminService, final Store ppStore, final RecordDef ppRecordDef,
            final Store groupDDLStore, final RecordDef groupRecordDef) {


        //  Project Path Grid Panel (Left Side)
        final GridPanel ppGrid = new GridPanel();
        ppStore.load();
        ppGrid.setStore(ppStore);



        // Access Grid Panel (Right Side)
        final GridPanel accessGrid = new GridPanel();
        final RecordDef accessRecordDef = new RecordDef(new FieldDef[] { new StringFieldDef("group"), new StringFieldDef("accessType")});
        final Store accessStore = new Store(accessRecordDef);
        accessGrid.setVisible(false);
        accessStore.load();


        final Button addProjectButton = new Button("Add Project", new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {

                MessageBox.prompt("Add Project", "Enter the project path", new MessageBox.PromptCallback() {
                    public void execute(String btnID, String text) {
                   //    GWT.log("Button Click : " +
                   //            Format.format("You clicked the {0} button and " +
                   //                    "entered the text {1}", btnID, text));
                       if (btnID.equalsIgnoreCase("OK")) {
                           if (text != null && text.trim().length() > 0 && text.startsWith("/")) {
                               addProject(svnAdminService, ppStore, text.trim(), ppRecordDef);
                           } else {
                               MessageBox.alert("Error", "One or more of the required fields are not valid.");
                           }



                       }
                    }
                });

                //GWT.log("Add Project button clicked");
            }
        });
        ppGrid.setTopToolbar(addProjectButton);

        final CheckboxSelectionModel ppCBSelectionModel = new CheckboxSelectionModel();

        final Button ppRemoveButton = new Button("Remove Checked Project(s)", new ButtonListenerAdapter() {
            public void onClick(final Button button, EventObject e) {
                final Record[] records = ppCBSelectionModel.getSelections();

                if (records.length == 0) {
                    return;
                }

                final Collection ppList = new HashSet(records.length);

                String pp = null;
                for (int i = 0; i < records.length; i++) {
                    Record record = records[i];
                    if (pp == null) {
                        pp = record.getAsString("projectPath");
                    } else {
                        pp +=  ", " + record.getAsString("projectPath");
                    }
                    ppList.add(record.getAsString("projectPath"));

                }
              //  GWT.log("projectss selected to remove :" + pp);

                MessageBox.confirm("Confirm", "Remove the following project(s) \"" + pp + "\"?", new MessageBox.ConfirmCallback() {
                    public void execute(String btnID) {
                    //    GWT.log("Button Click : " + Format.format("You clicked the {0} button", btnID));
                        if (btnID.equalsIgnoreCase("Yes")) {

                            //addMembershipFormPanel.setVisible(false);

                         // Show progress bar
                            MessageBox.show(new MessageBoxConfig() {
                                {
                                    setMsg("Removing projects(s), please wait...");
                                    setProgressText("Removing...");
                                    setWidth(300);
                                    setWait(true);
                                    setWaitConfig(new WaitConfig() {
                                        {
                                            setInterval(200);
                                        }
                                    });
                                    setAnimEl(button.getId());
                                }
                            });

                           // delete pp async call
                            removeProject(svnAdminService, accessGrid, ppStore, ppList, ppRecordDef);


                        }

                    }
                });

            }
        });

        ppGrid.addButton(ppRemoveButton);
        ppGrid.setButtonAlign(Position.LEFT);


        ColumnConfig groupCol = new ColumnConfig("Group", "group", 220, true, null, "group");
        ColumnConfig atCol = new ColumnConfig("Access Type", "accessType", 130, true, null, "accessType");

        final CheckboxSelectionModel accessGridCBSelectionModel = new CheckboxSelectionModel();
        CheckboxColumnConfig checkboxColumnConfig = new CheckboxColumnConfig(accessGridCBSelectionModel);

        BaseColumnConfig[] accessColumnConfigs = {
                checkboxColumnConfig, groupCol, atCol};

        ColumnModel accessColumnModel = new ColumnModel(accessColumnConfigs);
        accessColumnModel.setDefaultSortable(true);


        final Button accessGridRemoveButton = new Button("Remove Checked Group(s)", new ButtonListenerAdapter() {
            public void onClick(final Button button, EventObject e) {
                final Record[] records = accessGridCBSelectionModel.getSelections();

                if (records.length == 0) {
                    return;
                }
                final Record projectPathRecord = ppGrid.getSelectionModel().getSelected();

                if (projectPathRecord == null || projectPathRecord.getAsString("projectPath")  == null) {
                    return;
                }

                final String projectPath = projectPathRecord.getAsString("projectPath");


                String groups = null;
                for (int i = 0; i < records.length; i++) {
                    Record record = records[i];
                    if (groups == null) {
                        groups = record.getAsString("group");
                    } else {
                        groups +=  ", " + record.getAsString("group");
                    }

                }
               // GWT.log("group access selected to remove :" + groups);

                MessageBox.confirm("Confirm", "Remove project access for the following group(s) \"" + groups + "\"?", new MessageBox.ConfirmCallback() {
                    public void execute(String btnID) {
                        if (btnID.equalsIgnoreCase("Yes")) {



                         // Show progress bar
                            MessageBox.show(new MessageBoxConfig() {
                                {
                                    setMsg("Removing project access, please wait...");
                                    setProgressText("Removing...");
                                    setWidth(300);
                                    setWait(true);
                                    setWaitConfig(new WaitConfig() {
                                        {
                                            setInterval(200);
                                        }
                                    });
                                    setAnimEl(button.getId());
                                }
                            });

                           // update project access async call
                            removeProjectAccess(svnAdminService, projectPath, records, accessGrid, accessStore,
                                    accessRecordDef);


                        }

                    }
                });

            }
        });


        final String[] atValues = new String[] { "r", "rw", "w" };

        final SimpleStore atStore = new SimpleStore("accessType", atValues);
        atStore.load();

        final ComboBox groupCB = new ComboBox();
        final ComboBox atCB = new ComboBox();


        final class LimitedGroupCBValidator implements Validator {
            public boolean validate(String value) throws ValidationException {
                if (value == null || value.trim().equals("")) {
                    return false;
                }
               // allow any value
               // if (!value.trim().equals("*") && !groupSet.contains(value)) {
               //    return false;
               // }
                return true;
            }

        }
        final class ATCBValidator implements Validator {
            public boolean validate(String value) throws ValidationException {
                if (value == null || value.trim().equals("")) {
                    return false;
                }
                for (int i = 0; i < atValues.length; i++ ) {
                    if (atValues[i].equals(value)) {
                        return true;
                    }
                }
                return false;
            }

        }

        //TODO groupDDLStore.add *

        groupCB.setFieldLabel("Group");
        groupCB.setHiddenName("group");
        groupCB.setStore(groupDDLStore);
        groupCB.setDisplayField("groupvalue");
        groupCB.setTypeAhead(true);
        groupCB.setMode(ComboBox.LOCAL);
        groupCB.setTriggerAction(ComboBox.ALL);
        groupCB.setEmptyText("Select a group...");
        groupCB.setSelectOnFocus(true);
        groupCB.setWidth(190);
        groupCB.setValidator(new LimitedGroupCBValidator());

        atCB.setFieldLabel("Access Type");

        atCB.setHiddenName("accessType");
        atCB.setStore(atStore);
        atCB.setDisplayField("accessType");
        atCB.setTypeAhead(true);
        atCB.setMode(ComboBox.LOCAL);
        atCB.setTriggerAction(ComboBox.ALL);
        atCB.setEmptyText("Select access type...");
        atCB.setSelectOnFocus(true);
        atCB.setWidth(190);
        atCB.setValidator(new ATCBValidator());

        final Window addGroupAccessWindow = new Window();
        addGroupAccessWindow.setTitle("Add Group Access");
        addGroupAccessWindow.setWidth(300);
        addGroupAccessWindow.setHeight(150);
        addGroupAccessWindow.setLayout(new FitLayout());
        addGroupAccessWindow.setPaddings(4);
        
        final Button addGroupAccessButton = new Button("Save", new ButtonListenerAdapter() {
            public void onClick(final Button button, EventObject e) {
                if (atCB.getValueAsString().trim().equals("") ||
                        groupCB.getValueAsString().trim().equals("")) {
                    return;
                }

                final Record projectPathRecord = ppGrid.getSelectionModel().getSelected();

                if (projectPathRecord == null || projectPathRecord.getAsString("projectPath")  == null) {
                    return;
                }

                final String projectPath = projectPathRecord.getAsString("projectPath");


                if (atCB.isValid() && groupCB.isValid()) {

                    final String group = groupCB.getValueAsString();
                    final String accessType = atCB.getValueAsString();

                    //GWT.log("adding " + accessType + " access for group: " + group);

                    MessageBox.confirm("Confirm", "Add \"" + accessType + "\" access for group \"" + group + "\"?", new MessageBox.ConfirmCallback() {
                        public void execute(String btnID) {
                            if (btnID.equalsIgnoreCase("Yes")) {

                             // Show progress bar
                                MessageBox.show(new MessageBoxConfig() {
                                    {
                                        setMsg("Adding group access, please wait...");
                                        setProgressText("Adding...");
                                        setWidth(300);
                                        setWait(true);
                                        setWaitConfig(new WaitConfig() {
                                            {
                                                setInterval(200);
                                            }
                                        });
                                        setAnimEl(button.getId());
                                    }
                                });

                               // add access async call
                                addProjectAccess(svnAdminService, addGroupAccessWindow, projectPath, group, accessType, accessGrid, accessStore, accessRecordDef);


                            }

                        }
                    });

                } else {
                    MessageBox.alert("Error", "One or more of the required fields are not valid.");
                }
            }
        });

        final Button addAccessCancelButton = new Button("Cancel", new ButtonListenerAdapter() {
            public void onClick(Button button, EventObject e) {
                addGroupAccessWindow.hide();
                groupCB.setValue("");
                atCB.setValue("");
            }
        });



        addGroupAccessWindow.setButtonAlign(Position.CENTER);
        addGroupAccessWindow.addButton(addGroupAccessButton);
        addGroupAccessWindow.addButton(addAccessCancelButton);

        addGroupAccessWindow.setCloseAction(Window.CLOSE);
        addGroupAccessWindow.setPlain(true);

        FormPanel addGroupAccessFormPanel = new FormPanel();
        //strips all Ext styling for the component
        addGroupAccessFormPanel.setBaseCls("x-plain");
        addGroupAccessFormPanel.setLabelWidth(55);
       // addGroupAccessFormPanel.setUrl("save-form.php");

        addGroupAccessFormPanel.setWidth(300);
        addGroupAccessFormPanel.setHeight(300);



        addGroupAccessFormPanel.setLabelWidth(80);
        addGroupAccessFormPanel.add(groupCB, new AnchorLayoutData("100%"));
        addGroupAccessFormPanel.add(atCB, new AnchorLayoutData("100%"));

        addGroupAccessWindow.add(addGroupAccessFormPanel);

        Toolbar toolbar = new Toolbar();
        ToolbarButton button = new ToolbarButton("Add Group", new ButtonListenerAdapter() {

            public void onClick(com.gwtext.client.widgets.Button button, EventObject e) {

                groupCB.setValue("");
                atCB.setValue("");

                addGroupAccessWindow.show();

//                Record group = accessRecordDef.createRecord(new Object[] { "", "r" });
//                accessGrid.getStore().insert(0, group);
//                accessGrid.getStore().load();

            }
        });
       // button.setDisabled(false);


        toolbar.addButton(button);



        accessGrid.addButton(accessGridRemoveButton);
        accessGrid.setButtonAlign(Position.LEFT);
        accessGrid.setStore(accessStore);
        accessGrid.setAutoHeight(true);
        accessGrid.setColumnModel(accessColumnModel);
        accessGrid.setSelectionModel(accessGridCBSelectionModel);
        accessGrid.setTopToolbar(toolbar);
        accessGrid.setTitle("Group Access");
        accessGrid.setFrame(true);

        CheckboxColumnConfig ppCheckboxColumnConfig = new CheckboxColumnConfig(ppCBSelectionModel);
        BaseColumnConfig[] ppColumnConfigs = {
                ppCheckboxColumnConfig,  new ColumnConfig("Project Path", "projectPath", 200, true, null, "projectPath")};

        ColumnModel ppColumnModel = new ColumnModel(ppColumnConfigs);
        ppGrid.setColumnModel(ppColumnModel);
        ppGrid.setSelectionModel(ppCBSelectionModel);

        ppGrid.setAutoHeight(true);
        ppGrid.setFrame(true);
        ppGrid.setStripeRows(true);
        ppGrid.setAutoExpandColumn("projectPath");
        ppGrid.setTitle("Project Paths");
        ppGrid.setHideColumnHeader(true);


       // populateAccessGridPanel(svnAdminService, accessGrid, accessStore, null, recordDef);


        ppGrid.addGridRowListener(new GridRowListenerAdapter() {
            public void onRowClick(GridPanel grid, int rowIndex, EventObject e) {
                Record r = grid.getStore().getAt(rowIndex);
                String path = r.getAsString("projectPath");
               // GWT.log("path clicked: " + path);
                populateAccessGridPanel(svnAdminService, accessGrid, accessStore, path, accessRecordDef);

            }
        });

        // layout of pp and acess panels

        final Panel projectsLeftPanel = new Panel();
        projectsLeftPanel.setLayout(new FitLayout());
        projectsLeftPanel.add(ppGrid);
        projectsLeftPanel.setBorder(false);
        projectsLeftPanel.setPaddings(4);

        final Panel projectsRightPanel = new Panel();
        projectsRightPanel.setLayout(new FitLayout());
        projectsRightPanel.add(accessGrid);
        projectsRightPanel.setBorder(false);
        projectsRightPanel.setPaddings(4);
       // projectsRightPanel.setVisible(false);


        Panel projectsWrapperPanel = new Panel();
        projectsWrapperPanel.setBorder(false);

        projectsWrapperPanel.setLayout(new ColumnLayout());
        projectsWrapperPanel.add(projectsLeftPanel, new ColumnLayoutData(.30));
        projectsWrapperPanel.add(projectsRightPanel, new ColumnLayoutData(.70));

        Panel projectsPanel = new Panel();
        //projectsPanel.setHeight(450);
        projectsPanel.setTitle("Projects");
        projectsPanel.setBorder(false);
        projectsPanel.setLayout(new FitLayout());
        projectsPanel.add(projectsWrapperPanel);


        return projectsPanel;

    }

    private void removeProject(final SVNAdminServiceAsync svnAdminService, final Panel accessGrid, final Store ppStore, final Collection projectPath,
            final RecordDef ppRecordDef) {

        String prettyPrintPathList = null;

        for(Iterator i = projectPath.iterator(); i.hasNext();) {
            String pp = (String) i.next();
            if (prettyPrintPathList == null) {
                prettyPrintPathList = pp;
            } else {
                prettyPrintPathList += ", " + pp;
            }
        }

        final String ppOutput = prettyPrintPathList;

        AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {

               // MessageBox.hide();
                Timer t = new Timer() {
                    public void run() {
                        MessageBox.alert("Success", "Project(s): " + ppOutput + " successfully removed.");
                        ppStore.load();

                        accessGrid.hide();

                        populateProjectPathPanel(svnAdminService, ppStore, ppRecordDef);
                    }

                };
                t.schedule(500);




            }

            public void onFailure(Throwable e) {
                MessageBox.hide();
                MessageBox.alert("Error", "There was an error in removing project: " + projectPath);
            //    GWT.log("Fail! " + System.currentTimeMillis());
                e.printStackTrace();
            }
        };

        svnAdminService.removeProject(projectPath, callback);

    }

    private void addProject(final SVNAdminServiceAsync svnAdminService, final Store ppStore, final String projectPath,
            final RecordDef ppRecordDef) {


        AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {

                MessageBox.hide();
                MessageBox.alert("Success", "Project: " + projectPath + " successfully added.");

                populateProjectPathPanel(svnAdminService, ppStore, ppRecordDef);

                ppStore.load();

            }

            public void onFailure(Throwable e) {
                MessageBox.hide();
                MessageBox.alert("Error", "There was an error in adding project: " + projectPath);
               // GWT.log("Fail! " + System.currentTimeMillis());
                e.printStackTrace();
            }
        };

        svnAdminService.addProject(projectPath, callback);

    }


    private void populateProjectPathPanel(final SVNAdminServiceAsync svnAdminService, final Store ppStore, final RecordDef ppRecordDef) {

        AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                ppStore.removeAll();

                Map projectAccessMap = (Map) result;

                for (Iterator it = projectAccessMap.keySet().iterator(); it.hasNext();) {
                    String projectPath = (String) it.next();
                    ppStore.add(ppRecordDef.createRecord(new Object[] {projectPath}));
                }

                Timer timer = new Timer() {
                    public void run() {
                        finishedLoading("Project Path List");
                    }
                };
                timer.schedule(2000);


            }

            public void onFailure(Throwable caught) {
              //  GWT.log("Fail! " + System.currentTimeMillis());
                caught.printStackTrace();
            }
        };
        svnAdminService.getProjectAccessMap(callback);
    }

    private void populateAccessGridPanel(final SVNAdminServiceAsync svnAdminService, final GridPanel accessGrid,
            final Store accessStore, final String projectPath, final RecordDef accessRecordDef) {

        AsyncCallback accessRightsCallback = new AsyncCallback() {
            public void onSuccess(Object result) {

                accessStore.removeAll();

                accessGrid.setTitle("Group Access - " + projectPath);

                Map projectAccessMap = (Map) result;

                for (Iterator it = projectAccessMap.keySet().iterator(); it.hasNext();) {
                    String serverProjectPath = (String) it.next();
                    // find the project path's current saved access details
                    if (serverProjectPath.equalsIgnoreCase(projectPath)) {
                        Collection accessList = (Collection) projectAccessMap.get(serverProjectPath);
                        Object[][] accessObject = parseProjectRightsString(accessList);
                        for (int i = 0; i < accessObject.length; i++) {
                            accessStore.add(accessRecordDef.createRecord(accessObject[i]));
                        }
                        // break outer for loop of all project access data
                        break;
                    }


                }
                accessStore.load();
                accessStore.sort("group", SortDir.ASC);

                accessGrid.setVisible(true);

            }

            public void onFailure(Throwable caught) {
           //     GWT.log("Fail! " + System.currentTimeMillis());
                caught.printStackTrace();
            }
        };
        svnAdminService.getProjectAccessMap(accessRightsCallback);


    }



    private void addUser(final SVNAdminServiceAsync svnAdminService, final Panel wrapperPanel, final Store userStore,
            final String username, String password) {

        AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {

                Timer t = new Timer() {
                    public void run() {
                        MessageBox.hide();
                        MessageBox.alert("Success", "User: " + username + " successfully added.");

                        populateUsersList(svnAdminService, userStore);

                        userStore.reload();

                        wrapperPanel.clear();

                    }
                };
                t.schedule(500);

            }

            public void onFailure(Throwable e) {
                MessageBox.hide();
                MessageBox.alert("Error", "There was an error in adding the user: " + username);
           //     GWT.log("Fail! " + System.currentTimeMillis());
                e.printStackTrace();
            }
        };

        svnAdminService.addUser(username, password, callback);



    }

    private void editUser(final SVNAdminServiceAsync svnAdminService, final Panel wrapperPanel, final Store userStore,
            String oldUser, final String username, String password, final Store groupingStore, final Store groupStore) {

        AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {

                Timer t = new Timer() {
                    public void run() {

                        MessageBox.alert("Success", "User: " + username + " successfully updated.");

                        populateUsersList(svnAdminService, userStore);

                        populateGroupsPanel(svnAdminService, groupingStore, userStore, groupStore);

                        wrapperPanel.clear();



                    }
                };
                t.schedule(500);

            }

            public void onFailure(Throwable e) {
                MessageBox.hide();
                MessageBox.alert("Error", "There was an error in updating the user: " + username);
           //     GWT.log("Fail! " + System.currentTimeMillis());
                e.printStackTrace();
            }
        };

        svnAdminService.editUser(oldUser, username, password, callback);



    }

    private void deleteUser(final SVNAdminServiceAsync svnAdminService, final Panel wrapperPanel, final Store userStore,
            final String username, final Store groupingStore, final Store groupStore) {

        AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {

                Timer t = new Timer() {
                    public void run() {
                        MessageBox.hide();
                        MessageBox.alert("Success", "User: " + username + " successfully deleted.");

                        populateUsersList(svnAdminService, userStore);

                        userStore.load();

                        wrapperPanel.clear();

                        populateGroupsPanel(svnAdminService, groupingStore, userStore, groupStore);
                    }
                };
                t.schedule(500);



            }

            public void onFailure(Throwable e) {
                MessageBox.hide();
                MessageBox.alert("Error", "There was an error in deleting the user: " + username);
             //   GWT.log("Fail! " + System.currentTimeMillis());
                e.printStackTrace();
            }
        };

        svnAdminService.deleteUser(username, callback);


    }


    /**
     * Converts from
     * * = rw
     * >>>
     * Object[][] (for drop downs)
     *
     * @param lineList
     * @return
     */
    private Object[][] parseProjectRightsString(Collection lineList) {

        if (lineList == null || lineList.size() == 0) {
            return new Object[][]{};
        }

        Map tempMap = new HashMap();

        for (Iterator i = lineList.iterator(); i.hasNext();) {
            String line = (String) i.next();

            String owner = null;
            // @ == group reference
            if (line.startsWith("@")) {
                owner = line.substring(1, line.indexOf('=')).trim();
            } else {
                owner = line.substring(0, line.indexOf('=')).trim();
            }

            String access = null;
            access = line.substring(line.indexOf('=')+1, line.length());
            access = access.trim();

            tempMap.put(owner, access);


        }
        Object[][] projectAccess = new Object[tempMap.keySet().size()][];

        int j = 0;
        for (Iterator i = tempMap.keySet().iterator(); i.hasNext();) {
            String owner = (String) i.next();
            String access = (String) tempMap.get(owner);
            // GWT.log("owner: " + owner + ", access: " + access);
            projectAccess[j] =  new Object[]{owner, access};

            j++;

        }


        return projectAccess;
    }


    /**
     * Unused
     *
     * @param treeItem
     * @param jsonValue
     */
    private void addChildren(TreeItem treeItem, JSONValue jsonValue) {
        JSONArray jsonArray;
        JSONObject jsonObject;
        JSONString jsonString;

        if ((jsonArray = jsonValue.isArray()) != null) {
          for (int i = 0; i < jsonArray.size(); ++i) {
            TreeItem child = treeItem.addItem(getChildText("["
                + Integer.toString(i) + "]"));
            addChildren(child, jsonArray.get(i));
          }
        } else if ((jsonObject = jsonValue.isObject()) != null) {
          Set keys = jsonObject.keySet();
          for (Iterator iter = keys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            TreeItem child = treeItem.addItem(getChildText(key));
            addChildren(child, jsonObject.get(key));
          }
        } else if ((jsonString = jsonValue.isString()) != null) {
          // Use stringValue instead of toString() because we don't want escaping
          treeItem.addItem(jsonString.stringValue());
        } else {
          // JSONBoolean, JSONNumber, and JSONNull work well with toString().
          treeItem.addItem(getChildText(jsonValue.toString()));
        }
      }

    /*
     * Causes the text of child elements to wrap.
     */
    private String getChildText(String text) {
      return "<span style='white-space:normal'>" + text + "</span>";
    }
}
