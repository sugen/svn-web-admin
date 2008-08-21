/**
 * 
 */
package com.wkinney.client;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wkinney
 *
 */
public class Users extends AdminTab {
    
    private final HorizontalSplitPanel hSplit = new HorizontalSplitPanel();
  
    // LEFT
    private final VerticalPanel userPanel = new VerticalPanel();
    private final ListBox usersLb = new ListBox();
    
    // RIGHT
    private final VerticalPanel userEditPanel = new VerticalPanel();
    private final Image loading = new Image("images/loading_red.gif");
    private final Grid grid = new Grid(4,2);
    
    public static AdminTabInfo init(final AdminTab.Images images) {
        return new AdminTabInfo("Users", "<h2>Users</h2><p>This tab allows you to manage users in subversion. </p>") {

            public AdminTab createInstance() {
                               
                return new Users(images);
            }

            public String getColor() {
                return "#c4c4c4";
            }
        };
    }

    
    private void loadUsers() {
        
        AsyncCallback userListCallback = new AsyncCallback() {
            public void onSuccess(Object result) {

                usersLb.clear();
                
                List<String> userList = (List<String>) result;

                Collections.sort(userList);
                
                for (int i = 0; i < userList.size(); i++) {
                    usersLb.addItem(userList.get(i));
                }

            }

            public void onFailure(Throwable caught) {
              //  System.out.println("Fail! " + System.currentTimeMillis());
                caught.printStackTrace();
                Window.alert("Error in loading users: " + caught.getMessage());
            }
        };

        
        svnAdminService.getUserList(userListCallback);
        
    }
    
    public Users(AdminTab.Images images) {
        

        
        userPanel.add(new HTML("<h3>Users</h3>"));
        
     
        loadUsers();

        ChangeListener userChangeListener = new ChangeListener() {

            public void onChange(Widget sender) {
                ListBox lb = (ListBox) sender;
                
                int i = lb.getSelectedIndex();
                String userName = lb.getItemText(i);
                
                userEditPanel.setVisible(false);
                userEditPanel.clear();
                                
                
                final TextBox tbUsername = new TextBox();
                tbUsername.setText(userName);
                tbUsername.setName("editUserName");
                tbUsername.setTitle("username");


                final TextBox tbUsernameHidden = new TextBox();
                tbUsernameHidden.setText(userName);
                tbUsernameHidden.setName("editUserNameHidden");
                tbUsernameHidden.setVisible(false);
                tbUsernameHidden.setReadOnly(true);

                          
                final PasswordTextBox tbNewPassword = new PasswordTextBox();
                tbNewPassword.setName("editPassword");
                tbNewPassword.setTitle("New Password");
                

                final PasswordTextBox tbNewPasswordAgain = new PasswordTextBox();
                tbNewPasswordAgain.setName("editPasswordAgain");
                tbNewPasswordAgain.setTitle("Confirm Password");

                final PushButton editUserSubmitButton = new PushButton("Submit", new ClickListener() {
                    public void onClick(Widget sender) {
                      
                      final PushButton me = (PushButton) sender;
                        
                      if (tbUsername.getText() == null || tbUsername.getText().trim().equals("")) {
                          Window.alert(tbUsername.getTitle() + " is invalid");
                          return;
                      }
                      if (tbNewPassword.getText() == null || tbNewPassword.getText().trim().equals("")) {
                          Window.alert(tbNewPassword.getTitle() + " is invalid");
                          return;
                      }
                      if (tbNewPasswordAgain.getText() == null || tbNewPasswordAgain.getText().trim().equals("")) {
                          Window.alert(tbNewPasswordAgain.getTitle() + " is invalid");
                          return;
                      }
                      if (!tbNewPassword.getText().equals(tbNewPasswordAgain.getText())) {
                          Window.alert(tbNewPassword.getTitle() + " and " + tbNewPasswordAgain.getTitle() + " do not equal");
                          return;
                      }
                      
                      loading.setVisible(true);
                      me.setEnabled(false);
                      
                      if (!tbUsername.getText().equals(tbUsernameHidden.getText())) {
                          
                          AsyncCallback userEditCallback = new AsyncCallback() {
                              public void onSuccess(Object result) {
                                  
                                  loadUsers();
                                  
                                  loading.setVisible(false);
                                  Window.alert("Successfully updated username and password for username: " + tbUsernameHidden.getText());
                                  me.setEnabled(true);
                                  userEditPanel.setVisible(false);
                                  userEditPanel.clear();
                              }

                              public void onFailure(Throwable caught) {
                                  loading.setVisible(false);
                                  Window.alert("Error in updating username and password for username: " + tbUsernameHidden.getText());
                                  me.setEnabled(true);
                                  
                              }
                          };
                          svnAdminService.editUser(tbUsernameHidden.getText(), tbUsername.getText(), tbNewPassword.getText(), userEditCallback);
                          
                      } else {

                          AsyncCallback userEditCallback = new AsyncCallback() {
                              public void onSuccess(Object result) {

                                  loadUsers();
                                  
                                  loading.setVisible(false);
                                  Window.alert("Successfully updated password for username: " + tbUsername.getText());
                                  me.setEnabled(true);
                                  userEditPanel.setVisible(false);
                                  userEditPanel.clear();
                              }

                              public void onFailure(Throwable caught) {
                                  loading.setVisible(false);
                                  Window.alert("Error in updating password for username: " + tbUsername.getText());
                                  me.setEnabled(true);
                                  
                              }
                          };
                          svnAdminService.editUser(tbUsername.getText(), tbUsername.getText(), tbNewPassword.getText(), userEditCallback);
                          
                          
                      }
                     
                      
                     
                    }
                  });
                editUserSubmitButton.setWidth("50px");
                
                final PushButton deleteUserButton = new PushButton("Delete User", new ClickListener() {
                    public void onClick(Widget sender) {
                      
                        final PushButton me = (PushButton) sender;
                      
                        boolean delete = Window.confirm("Are you sure you want to delete user: " + tbUsernameHidden.getText() + "?");
                      
                        if (!delete) {
                          return;
                        }
                                                
                        loading.setVisible(true);
                        me.setEnabled(false);
                        
                        AsyncCallback userDeleteCallback = new AsyncCallback() {
                            public void onSuccess(Object result) {

                                loadUsers();
                                
                                loading.setVisible(false);
                                Window.alert("Successfully deleted user: " + tbUsernameHidden.getText());
                                me.setEnabled(true);
                                userEditPanel.setVisible(false);
                                userEditPanel.clear();
                            }

                            public void onFailure(Throwable caught) {
                                loading.setVisible(false);
                                Window.alert("Error in deleting user: " + tbUsernameHidden.getText());
                                me.setEnabled(true);
                                
                            }
                        };
                        svnAdminService.deleteUser(tbUsernameHidden.getText(), userDeleteCallback);
                        
                    }
                });
                deleteUserButton.setWidth("100px");
                deleteUserButton.addStyleName("user-delete-icon");
                
                
               // 
                grid.setWidget(0,0,new HTML("<strong>Username</strong>"));
                grid.setWidget(0,1,tbUsername);
                grid.setWidget(1,0,new HTML("<strong>New Password</strong>"));
                grid.setWidget(1,1,tbNewPassword);
                grid.setWidget(2,0,new HTML("<strong>Confirm Password</strong>"));
                grid.setWidget(2,1,tbNewPasswordAgain);
                grid.setWidget(3,0,new HTML(""));
                grid.setWidget(3,1,tbUsernameHidden);
   
                userEditPanel.add(new HTML("<h3>Edit User : " + userName + "</h3>"));
                userEditPanel.add(grid);
                final HorizontalPanel hpButtons = new HorizontalPanel();
                hpButtons.setWidth("480px");
                hpButtons.add(editUserSubmitButton);
                hpButtons.add(deleteUserButton);
                userEditPanel.add(hpButtons);
                userEditPanel.add(loading);
                
                loading.setVisible(false);
                
                userEditPanel.setVisible(true);
                
            }
        };
        
        usersLb.addChangeListener(userChangeListener);
        


        usersLb.setWidth("150px");

        usersLb.setVisibleItemCount(20);
 
        
        
        userPanel.add(usersLb);
        
        PushButton addUserButton = new PushButton("Add User", new ClickListener() {
            public void onClick(Widget sender) {
              
                
                
                userEditPanel.setVisible(false);
                userEditPanel.clear();
                                
                
                final TextBox tbUsername = new TextBox();
                tbUsername.setName("addUserName");
                tbUsername.setTitle("username");

                          
                final PasswordTextBox tbNewPassword = new PasswordTextBox();
                tbNewPassword.setName("password");
                tbNewPassword.setTitle("Password");

                final PasswordTextBox tbNewPasswordAgain = new PasswordTextBox();
                tbNewPasswordAgain.setName("passwordAgain");
                tbNewPasswordAgain.setTitle("Confirm Password");

                final PushButton addUserSubmitButton = new PushButton("Submit", new ClickListener() {
                    public void onClick(Widget sender) {
                      
                      final PushButton me = (PushButton) sender;
                        
                      if (tbUsername.getText() == null || tbUsername.getText().trim().equals("")) {
                          Window.alert(tbUsername.getTitle() + " is invalid");
                          return;
                      }
                      if (tbNewPassword.getText() == null || tbNewPassword.getText().trim().equals("")) {
                          Window.alert(tbNewPassword.getTitle() + " is invalid");
                          return;
                      }
                      if (tbNewPasswordAgain.getText() == null || tbNewPasswordAgain.getText().trim().equals("")) {
                          Window.alert(tbNewPasswordAgain.getTitle() + " is invalid");
                          return;
                      }
                      if (!tbNewPassword.getText().equals(tbNewPasswordAgain.getText())) {
                          Window.alert(tbNewPassword.getTitle() + " and " + tbNewPasswordAgain.getTitle() + " do not equal");
                          return;
                      }
                      
                      loading.setVisible(true);
                      me.setEnabled(false);
                      
                    

                      AsyncCallback addEditCallback = new AsyncCallback() {
                          public void onSuccess(Object result) {

                              loadUsers();
                              
                              loading.setVisible(false);
                              Window.alert("Successfully added user: " + tbUsername.getText());
                              me.setEnabled(true);
                              userEditPanel.setVisible(false);
                              userEditPanel.clear();
                          }

                          public void onFailure(Throwable caught) {
                              loading.setVisible(false);
                              Window.alert("Error in adding user: " + caught.getMessage());
                              me.setEnabled(true);
                              
                          }
                      };
                      svnAdminService.addUser(tbUsername.getText(), tbNewPassword.getText(), addEditCallback);
                          
                          
                      
                     
                      
                     
                    }
                  });
                addUserSubmitButton.setWidth("50px");
                

                grid.setWidget(0,0,new HTML("<strong>Username</strong>"));
                grid.setWidget(0,1,tbUsername);
                grid.setWidget(1,0,new HTML("<strong>New Password</strong>"));
                grid.setWidget(1,1,tbNewPassword);
                grid.setWidget(2,0,new HTML("<strong>Confirm Password</strong>"));
                grid.setWidget(2,1,tbNewPasswordAgain);
                grid.setWidget(3,0,new HTML(""));
                grid.setWidget(3,1,new HTML(""));
   
                userEditPanel.add(new HTML("<h3>Add User</h3>"));
                userEditPanel.add(grid);
                userEditPanel.add(addUserSubmitButton);
                userEditPanel.add(loading);
                
                loading.setVisible(false);
                
                userEditPanel.setVisible(true);
                
                
                
                
                
            }
          });
        addUserButton.setWidth("85px");
        addUserButton.addStyleName("user-add-icon");
        
        final HorizontalPanel bottomButtonsHp = new HorizontalPanel();
        bottomButtonsHp.add(addUserButton);
        bottomButtonsHp.setSpacing(5); 
        
        userPanel.add(bottomButtonsHp);

        hSplit.setLeftWidget(userPanel);
        hSplit.setRightWidget(userEditPanel);

        initWidget(hSplit);
        hSplit.setSize("100%", "450px");
    }

    public void onShow() {
    }

}
