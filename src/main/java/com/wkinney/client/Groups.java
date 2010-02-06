/**
 * 
 */
package com.wkinney.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wkinney
 *
 */
public class Groups extends AdminTab {

  private final HorizontalSplitPanel hSplit = new HorizontalSplitPanel();
  
  // LEFT
  private final VerticalPanel groupPanel = new VerticalPanel();
  private final StackPanel groupMembersStack = new StackPanel();
  private final Image loadingLeft = new Image("images/loading_red.gif");
  
  // RIGHT
  private final VerticalPanel memberDetailPanel = new VerticalPanel();
  private final Image loading = new Image("images/loading_red.gif");
  
  //  - Add Membership
  private final ListBox addMemberUserListBox = new ListBox();
  private final ListBox addMemberGroupListBox = new ListBox();
  private final Grid grid = new Grid(3,2);
  //  - Delete Membership
  private final Label deleteMemberName = new Label();
  private final Label deleteGroupName = new Label();
  {
    
    grid.setStyleName("grid");
  }
  
  public static AdminTabInfo init(final AdminTab.Images images) {
    return new AdminTabInfo("Groups", "<h2>Groups</h2><p>This tab allows you to manage groups and user memberships to groups. </p>") {

      public AdminTab createInstance() {
        return new Groups(images);
      }

      public String getColor() {
        return "#ce344e";
      }
    };
  }

  private void loadGroups() {
    loadGroups(null);
  }
  
  private void loadGroups(final String selectedGroup) {

    final AsyncCallback groupMemberCallback = new AsyncCallback() {
      public void onSuccess(Object result) {

        groupMembersStack.clear();
        addMemberGroupListBox.clear();
        
        Map<String, List<String>> memberMap = (Map<String, List<String>>) result;

        List<String> memberList = null;
        VerticalPanel memberPanel = null;
        
        Set<String> groupSet = memberMap.keySet();
        List<String> groupList = new ArrayList<String>(groupSet);
        Collections.sort(groupList);
        
        // get select index number
        int selectIndex = 0;
        if (selectedGroup != null) {
          for (int i = 0; i < groupList.size(); i++) {
            String groupName = groupList.get(i);
            if (groupName.equals(selectedGroup)) {
              selectIndex = i;
              break;
            }
          }
        }
        
        
        
        for (Iterator<String> i = groupList.iterator(); i.hasNext();) {
          String group = i.next();

          // populate group list box for add membership
          addMemberGroupListBox.addItem(group);
          
          memberList = memberMap.get(group);
          
          Collections.sort(memberList);
          
          memberPanel = new VerticalPanel();
          memberPanel.setTitle(group);
          memberPanel.addStyleName("member-panel");
                   
          Label memberLabel = null;

          if (memberList.size() == 0) {
            memberLabel = new Label("no members");
            memberLabel.setWidth("400px");
            memberLabel.setStyleName("member-label-empty");
            memberPanel.add(memberLabel);
          }
          
          for (Iterator<String> i2 = memberList.iterator(); i2.hasNext();) {
            String member = i2.next();
            memberLabel = new Label(member);
            
            ClickListener memberClickListener = new ClickListener() { 
              public void onClick(Widget sender) {
                Label me = (Label) sender;
                
                final String selectedUserName = me.getText();
                
                int selectedIndex = groupMembersStack.getSelectedIndex();
                VerticalPanel selectedVp = (VerticalPanel) groupMembersStack.getWidget(selectedIndex);
                final String selectedGroupName = selectedVp.getTitle();
                
                
                memberDetailPanel.setVisible(false);
                memberDetailPanel.clear();
                
                deleteMemberName.setText(selectedUserName);
                deleteGroupName.setText(selectedGroupName);
                
                final PushButton removeMembershipButton = new PushButton("Delete Membership", new ClickListener() {
                  public void onClick(Widget sender) {

                    final PushButton me = (PushButton) sender;
                    
                    loading.setVisible(true);
                    me.setEnabled(false);

                    AsyncCallback deleteMembershipCallback = new AsyncCallback() {
                      public void onSuccess(Object result) {

                        loadGroups(selectedGroupName);

                        loading.setVisible(false);
                       // Window.alert("Successfully deleted membership for user: " + selectedUserName + " to group: " + selectedGroupName);
                        me.setEnabled(true);
                        memberDetailPanel.setVisible(false);
                        memberDetailPanel.clear();
                      }

                      public void onFailure(Throwable caught) {
                        loading.setVisible(false);
                        Window.alert("Error in deleting membership: " + caught.getMessage());
                        me.setEnabled(true);

                      }
                    };
                    List<Membership> membershipList = new ArrayList<Membership>(1);
                    membershipList.add(new Membership(selectedGroupName, selectedUserName));
                    
                    
                    svnAdminService.removeMembership(membershipList, deleteMembershipCallback);

                  }
                });
                removeMembershipButton.setWidth("140px");
                removeMembershipButton.addStyleName("user-delete-icon");
                
                
                grid.setWidget(0, 0, new HTML("<strong>User</strong>"));
                grid.setWidget(0, 1, deleteMemberName);
                grid.setWidget(1, 0, new HTML("<strong>Group</strong>"));
                grid.setWidget(1, 1, deleteGroupName);
                grid.setWidget(2, 0, new HTML(""));
                grid.setWidget(2, 1, new HTML(""));

                memberDetailPanel.add(new HTML("<h3>Delete Membership</h3>"));
                memberDetailPanel.add(grid);
                
                final HorizontalPanel bottomButtonsHp = new HorizontalPanel();
                bottomButtonsHp.add(removeMembershipButton);
                bottomButtonsHp.add(loading);
                bottomButtonsHp.setSpacing(5); 
                
                memberDetailPanel.add(bottomButtonsHp);
                                
                loading.setVisible(false);
                
                memberDetailPanel.setVisible(true);
                
              }
            };
            memberLabel.addClickListener(memberClickListener);
            
            MouseListener memberMouseListener = new MouseListenerAdapter() {
              public void onMouseEnter(Widget sender) {
                Label me = (Label) sender;
                me.addStyleName("member-label-hover");
              }
              public void onMouseLeave(Widget sender) {
                Label me = (Label) sender;
                me.removeStyleName("member-label-hover");
              }
            };
            memberLabel.addMouseListener(memberMouseListener);
            memberLabel.setWidth("400px");
            
            
            memberPanel.add(memberLabel);
          }
          groupMembersStack.add(memberPanel, group, true);

          
          
          
          
        }

        groupMembersStack.showStack(selectIndex);
        
      }

      public void onFailure(Throwable caught) {
        Window.alert("Error in populating group member data: " + caught.getMessage());
      }
    };
    svnAdminService.getGroupMembersMap(groupMemberCallback);

  }

  public Groups(AdminTab.Images images) {

    addMemberUserListBox.setWidth("180px");
    addMemberGroupListBox.setWidth("180px");
    
    
    groupPanel.add(new HTML("<h3>Groups and Members</h3>"));
    
    loadGroups();

    final PushButton addGroupButton = new PushButton("Add Group", new ClickListener() {
      public void onClick(Widget sender) {

        memberDetailPanel.setVisible(false);
        memberDetailPanel.clear();
       
        
        final TextBox groupNameTextBox = new TextBox();
        groupNameTextBox.setWidth("120px");
        
        
        final PushButton addGroupSubmitButton = new PushButton("Submit", new ClickListener() {
          public void onClick(Widget sender) {

            final PushButton me = (PushButton) sender;

     
            final String groupToAdd = groupNameTextBox.getText();
            
            if (groupToAdd == null || groupToAdd.trim().equals("")) {
              return;
            }
            
            final String groupToAddFinal = groupToAdd.trim();
            
            
            loading.setVisible(true);
            me.setEnabled(false);

            AsyncCallback addGroupCallback = new AsyncCallback() {
              public void onSuccess(Object result) {

                loadGroups(groupToAddFinal);

                loading.setVisible(false);
             //   Window.alert("Successfully added group: " + groupToAddFinal);
                me.setEnabled(true);
                memberDetailPanel.setVisible(false);
                memberDetailPanel.clear();
              }

              public void onFailure(Throwable caught) {
                loading.setVisible(false);
                Window.alert("Error in adding group: " + caught.getMessage());
                me.setEnabled(true);

              }
            };

            svnAdminService.addGroup(groupToAddFinal, addGroupCallback);

          }
        });
        addGroupSubmitButton.setWidth("50px");

        grid.setWidget(0, 0, new HTML("<strong>Group</strong>"));
        grid.setWidget(0, 1, groupNameTextBox);
        grid.setWidget(1, 0, new HTML(""));
        grid.setWidget(1, 1, new HTML(""));
        grid.setWidget(2, 0, new HTML(""));
        grid.setWidget(2, 1, new HTML(""));

        memberDetailPanel.add(new HTML("<h3>Add Group</h3>"));
        memberDetailPanel.add(grid);
                
        final HorizontalPanel bottomButtonHp = new HorizontalPanel();
        bottomButtonHp.add(addGroupSubmitButton);
        bottomButtonHp.setSpacing(5);
        
        memberDetailPanel.add(bottomButtonHp);
        memberDetailPanel.add(loading);

        loading.setVisible(false);

        memberDetailPanel.setVisible(true);

      }
    });
    addGroupButton.setWidth("92px");
    addGroupButton.addStyleName("user-add-icon");
    
    
    
    
    PushButton addMembershipButton = new PushButton("Add Membership", new ClickListener() {
      public void onClick(Widget sender) {

        memberDetailPanel.setVisible(false);
        memberDetailPanel.clear();
        
        int selectedIndex = groupMembersStack.getSelectedIndex();
        VerticalPanel selectedVp = (VerticalPanel) groupMembersStack.getWidget(selectedIndex);
        final String selectedGroupName = selectedVp.getTitle();
        
        
        for (int i = 0; i < addMemberGroupListBox.getItemCount(); i++) {
          String groupText = addMemberGroupListBox.getValue(i);
          if (groupText.equals(selectedGroupName)) {
            addMemberGroupListBox.setSelectedIndex(i);
            break;
          }
        }
        
        // Populate users drop down
        AsyncCallback populateUserListCallback = new AsyncCallback() {
          public void onSuccess(Object result) {
            addMemberUserListBox.clear();
            
            List<String> userList = (List<String>) result;

            Collections.sort(userList);
            
            for (int i = 0; i < userList.size(); i++) {
              addMemberUserListBox.addItem(userList.get(i));
            }
          }
          public void onFailure(Throwable caught) {
            Window.alert("Error in populating user drop down list: " + caught.getMessage());
          }
        };
        svnAdminService.getUserList(populateUserListCallback);
        
        final PushButton addMembershipSubmitButton = new PushButton("Submit", new ClickListener() {
          public void onClick(Widget sender) {

            final PushButton me = (PushButton) sender;

            // TODO Validate form ?
            int selectedIndex = addMemberUserListBox.getSelectedIndex();
            final String userNameToAdd = addMemberUserListBox.getItemText(selectedIndex);
            selectedIndex = addMemberGroupListBox.getSelectedIndex();
            final String groupToAdd = addMemberGroupListBox.getItemText(selectedIndex);
            
            loading.setVisible(true);
            me.setEnabled(false);

            AsyncCallback addMembershipCallback = new AsyncCallback() {
              public void onSuccess(Object result) {

                loadGroups(groupToAdd);

                loading.setVisible(false);
               // Window.alert("Successfully added user: " + userNameToAdd + " to group: " + groupToAdd);
                me.setEnabled(true);
                memberDetailPanel.setVisible(false);
                memberDetailPanel.clear();
              }

              public void onFailure(Throwable caught) {
                loading.setVisible(false);
                Window.alert("Error in adding user membership: " + caught.getMessage());
                me.setEnabled(true);

              }
            };
            List<String> userListToAdd = new ArrayList<String>(1);
            userListToAdd.add(userNameToAdd);
            svnAdminService.addMembership(groupToAdd, userListToAdd, addMembershipCallback);

          }
        });
        addMembershipSubmitButton.setWidth("50px");

        grid.setWidget(0, 0, new HTML("<strong>Group</strong>"));
        grid.setWidget(0, 1, addMemberGroupListBox);
        grid.setWidget(1, 0, new HTML("<strong>User</strong>"));
        grid.setWidget(1, 1, addMemberUserListBox);
        grid.setWidget(2, 0, new HTML(""));
        grid.setWidget(2, 1, new HTML(""));

        memberDetailPanel.add(new HTML("<h3>Add Membership</h3>"));
        memberDetailPanel.add(grid);
        
        final HorizontalPanel bottomButtonHp = new HorizontalPanel();
        bottomButtonHp.add(addMembershipSubmitButton);
        bottomButtonHp.setSpacing(5);
        
        memberDetailPanel.add(bottomButtonHp);
        memberDetailPanel.add(loading);

        loading.setVisible(false);

        memberDetailPanel.setVisible(true);

      }
    });
    addMembershipButton.setWidth("125px");
    addMembershipButton.addStyleName("user-add-icon");
    
    
    loadingLeft.setVisible(false);
    final PushButton removeGroupButton = new PushButton("Delete Group", new ClickListener() {
      public void onClick(Widget sender) {

        final PushButton me = (PushButton) sender;
        
        int selectedIndex = groupMembersStack.getSelectedIndex();
        VerticalPanel selectedVp = (VerticalPanel) groupMembersStack.getWidget(selectedIndex);
        final String selectedGroupName = selectedVp.getTitle();
        
        boolean delete = Window.confirm("Are you sure you want to delete group: " + selectedGroupName + " ?");
        
        if (!delete) {
          return;
        }
        
        loadingLeft.setVisible(true);
        me.setEnabled(false);
        
        AsyncCallback deleteGroupCallback = new AsyncCallback() {
          public void onSuccess(Object result) {

            loadGroups();

            loadingLeft.setVisible(false);
            Window.alert("Successfully deleted group: " + selectedGroupName);
            me.setEnabled(true);
            memberDetailPanel.setVisible(false);
            memberDetailPanel.clear();
          }

          public void onFailure(Throwable caught) {
            loadingLeft.setVisible(false);
            Window.alert("Error in deleting group: " + caught.getMessage());
            me.setEnabled(true);

          }
        };

        svnAdminService.deleteGroup(selectedGroupName, deleteGroupCallback);

      }
    });
    removeGroupButton.setWidth("110px");
    removeGroupButton.addStyleName("user-delete-icon");
    
    
    
    final HorizontalPanel topButtonsHp = new HorizontalPanel();
    topButtonsHp.add(addGroupButton);
    topButtonsHp.add(removeGroupButton);
    topButtonsHp.add(loadingLeft);
    topButtonsHp.setSpacing(5); 
    
    groupPanel.add(topButtonsHp);
    groupPanel.add(groupMembersStack);
    
    final HorizontalPanel bottomButtonsHp = new HorizontalPanel();
    bottomButtonsHp.add(addMembershipButton);
    bottomButtonsHp.setSpacing(5); 
    
    groupPanel.add(bottomButtonsHp);
    hSplit.setLeftWidget(groupPanel);
    hSplit.setRightWidget(memberDetailPanel);

    initWidget(hSplit);
    hSplit.setSize("100%", "450px");
  }

  public void onShow() {
  }

}
