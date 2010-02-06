/**
 * 
 */
package com.wkinney.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author wkinney
 * 
 */
public class Projects extends AdminTab {

  private final HorizontalSplitPanel hSplit = new HorizontalSplitPanel();

  // LEFT
  private final VerticalPanel projectPanel = new VerticalPanel();
  private final ListBox pathLb = new ListBox();
  private final PushButton deletePathButton = new PushButton("Delete Project Path");
  private final PushButton addPathButton = new PushButton("Add Project Path");
  private final Image loadingLeft = new Image("images/loading_red.gif");
  // RIGHT
  private final VerticalPanel accessPanel = new VerticalPanel();
  private final Image loading = new Image("images/loading_red.gif");
  // - Add Project Access
  private final ListBox addAccessGroupListBox = new ListBox();
  private final ListBox addAccessTypeListBox = new ListBox();
  private final PushButton addAccessButton = new PushButton("Add Group Access");

  {
    addAccessTypeListBox.addItem("rw");
    addAccessTypeListBox.addItem("r");
    addAccessTypeListBox.addItem("w");
    addAccessTypeListBox.setWidth("100px");
  }

  public static AdminTabInfo init(final AdminTab.Images images) {
    return new AdminTabInfo("Projects", "<h2>Projects</h2><p>This tab allows you to manage group access to projects (or \"paths\")  . </p>") {

      public AdminTab createInstance() {
        return new Projects(images);
      }

      public String getColor() {
        return "#b0e02b";
      }
    };
  }

  private void loadProjects() {

    AsyncCallback getProjectsCallback = new AsyncCallback() {
      public void onSuccess(Object result) {

        Map<String, List<String>> projectAccessMap = (Map<String, List<String>>) result;

        projectPanel.setVisible(false);
        pathLb.clear();

        Set<String> projectSet = projectAccessMap.keySet();
        List<String> projectList = new ArrayList<String>(projectSet);

        Collections.sort(projectList);

        for (String projectPath : projectList) {
          pathLb.addItem(projectPath);
        }

        projectPanel.setVisible(true);

      }

      public void onFailure(Throwable caught) {
        Window.alert("Error in populating projects: " + caught.getMessage());
      };

    };
    svnAdminService.getProjectAccessMap(getProjectsCallback);

  }

  public Projects(AdminTab.Images images) {

    loadProjects();

    final ChangeListener projectChangeListener = new ChangeListener() {

      public void onChange(Widget sender) {
        ListBox me = (ListBox) sender;

        int i = me.getSelectedIndex();
        final String selectedProjectPath = me.getItemText(i);

        if (!deletePathButton.isEnabled()) {
          deletePathButton.setEnabled(true);
        }

        final ClickListener addAccessClickListener = new ClickListener() {
          public void onClick(Widget sender) {

            accessPanel.setVisible(false);
            accessPanel.clear();

            int selectedIndex = pathLb.getSelectedIndex();
            final String selectedProjectPath = pathLb.getItemText(selectedIndex);

            // Populate users drop down
            AsyncCallback populateGroupListCallback = new AsyncCallback() {
              public void onSuccess(Object result) {
                addAccessGroupListBox.clear();

                Map<String, List<String>> groupMap = (Map<String, List<String>>) result;

                Set<String> groupSet = groupMap.keySet();

                List<String> groupList = new ArrayList<String>(groupSet.size() + 1);

                // groupList.add("*");
                groupList.addAll(groupSet);

                Collections.sort(groupList);

                addAccessGroupListBox.addItem("*");
                for (String groupName : groupList) {
                  addAccessGroupListBox.addItem(groupName);
                }
              }

              public void onFailure(Throwable caught) {
                Window.alert("Error in populating group drop down list: " + caught.getMessage());
              }
            };
            svnAdminService.getGroupMembersMap(populateGroupListCallback);

            final PushButton addAccessSubmitButton = new PushButton("Submit", new ClickListener() {
              public void onClick(Widget sender) {

                final PushButton me = (PushButton) sender;

                int selectedIndex = addAccessGroupListBox.getSelectedIndex();
                final String groupAccessToAdd = addAccessGroupListBox.getItemText(selectedIndex);
                selectedIndex = addAccessTypeListBox.getSelectedIndex();
                final String accessToAdd = addAccessTypeListBox.getItemText(selectedIndex);

                loading.setVisible(true);
                me.setEnabled(false);

                AsyncCallback addAccessCallback = new AsyncCallback() {
                  public void onSuccess(Object result) {

                    loading.setVisible(false);
//                    Window.alert("Successfully added project access to: " + selectedProjectPath + " for : " + groupAccessToAdd + " of type: "
//                        + accessToAdd);
                    me.setEnabled(true);
                    accessPanel.setVisible(false);
                    accessPanel.clear();

                    
                    //pathLb.setSelectedIndex(-1);

                    setAccessPanelForPath(selectedProjectPath);
                    
                    
                  }

                  public void onFailure(Throwable caught) {
                    loading.setVisible(false);
                    Window.alert("Error in adding project access: " + caught.getMessage());
                    me.setEnabled(true);

                  }
                };

                svnAdminService.addProjectAccess(selectedProjectPath, groupAccessToAdd, accessToAdd, addAccessCallback);

              }
            });
            addAccessSubmitButton.setWidth("50px");

            final Grid grid = new Grid(3, 2);
            grid.setWidget(0, 0, new HTML("<strong>Group</strong>"));
            grid.setWidget(0, 1, addAccessGroupListBox);
            grid.setWidget(1, 0, new HTML("<strong>Access Type</strong>"));
            grid.setWidget(1, 1, addAccessTypeListBox);
            grid.setWidget(2, 0, new HTML(""));
            grid.setWidget(2, 1, new HTML(""));

            accessPanel.add(new HTML("<h3>Add Group Access : " + selectedProjectPath + "</h3>"));
            accessPanel.add(grid);
            accessPanel.add(addAccessSubmitButton);
            accessPanel.add(loading);

            loading.setVisible(false);

            accessPanel.setVisible(true);

          } // end Add Group Access onClick();

          // End addAccessClickListener
        };
        addAccessButton.addClickListener(addAccessClickListener);
        addAccessButton.setWidth("140px");
        addAccessButton.addStyleName("user-add-icon");

        // set Right panel, showing access for path
        setAccessPanelForPath(selectedProjectPath);

        // end projectChangeListener onChange()
      }

      // end projectChangeListener
    };
    final TextBox addPathTextBox = new TextBox();
    addPathTextBox.setWidth("150px");

    final PushButton addPathSubmitButton = new PushButton("Submit", new ClickListener() {
      public void onClick(Widget sender) {

        final PushButton me = (PushButton) sender;

        String pathToAdd = addPathTextBox.getText();

        if (pathToAdd == null || pathToAdd.trim().equals("")) {
          return;
        }
        
        if (!pathToAdd.startsWith("/")) {
          Window.alert("Project path must start with \"/\"");
          return;
        }

        final String finalPathToAdd = pathToAdd.trim();

        loading.setVisible(true);
        me.setEnabled(false);

        AsyncCallback addPathCallback = new AsyncCallback() {
          public void onSuccess(Object result) {

            loadProjects();

            loading.setVisible(false);
            Window.alert("Successfully added project path: " + finalPathToAdd);
            me.setEnabled(true);
            addPathTextBox.setText("");
            accessPanel.setVisible(false);
            accessPanel.clear();
          }

          public void onFailure(Throwable caught) {
            loading.setVisible(false);
            Window.alert("Error in adding project path: " + caught.getMessage());
            me.setEnabled(true);

          }
        };

        svnAdminService.addProject(finalPathToAdd, addPathCallback);

      }
    });
    addPathSubmitButton.setWidth("50px");

    addPathButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {

        final PushButton me = (PushButton) sender;
        accessPanel.setVisible(false);
        accessPanel.clear();
        accessPanel.setWidth("80%");

        loading.setVisible(false);

        accessPanel.add(new HTML("<h3>Add Project Path</h3>"));
        final Grid grid = new Grid(1, 2);
        grid.setWidget(0, 0, new HTML("<strong>Project Path</strong>"));
        grid.setWidget(0, 1, addPathTextBox);
        accessPanel.add(grid);
        accessPanel.add(addPathSubmitButton);
        accessPanel.add(loading);

        accessPanel.setVisible(true);

      }
    });
    addPathButton.setWidth("107px");

    deletePathButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {

        final PushButton me = (PushButton) sender;
        
        int selectedIndex = pathLb.getSelectedIndex();
        final String selectedProjectPath = pathLb.getItemText(selectedIndex);
        
        boolean deletePath = Window.confirm("Are you sure you want to delete project path: " + selectedProjectPath + " ?");
        
        if (!deletePath) {
          return;
        }
        
        me.setEnabled(false);
        
        loadingLeft.setVisible(true);
        
        accessPanel.setVisible(false);
        accessPanel.clear();
        
        
        AsyncCallback deletePathCallback = new AsyncCallback() {
          public void onSuccess(Object result) {

            loadProjects();

            loadingLeft.setVisible(false);            
          }

          public void onFailure(Throwable caught) {
            loadingLeft.setVisible(false);
            Window.alert("Error in deleting project path: " + caught.getMessage());
            me.setEnabled(true);

          }
        };

        svnAdminService.removeProject(selectedProjectPath, deletePathCallback);
        

      }
    });
    deletePathButton.setWidth("127px");
    deletePathButton.setEnabled(false);
    
    pathLb.addChangeListener(projectChangeListener);

    pathLb.setWidth("150px");
    pathLb.setVisibleItemCount(20);

    projectPanel.add(new HTML("<h3>Project Paths</h3>"));
        
    loadingLeft.setVisible(false);
    final HorizontalPanel topButtonsHp = new HorizontalPanel();
    topButtonsHp.add(addPathButton);
    topButtonsHp.setSpacing(5); 
    
    projectPanel.add(topButtonsHp);
    
    projectPanel.add(pathLb);
    
    final HorizontalPanel bottomButtonsHp = new HorizontalPanel();
    bottomButtonsHp.add(deletePathButton);
    bottomButtonsHp.add(loadingLeft);
    bottomButtonsHp.setSpacing(5); 
    
    projectPanel.add(bottomButtonsHp);

    hSplit.setLeftWidget(projectPanel);
    hSplit.setRightWidget(accessPanel);

    initWidget(hSplit);
    hSplit.setSize("100%", "450px");

  }

  public void onShow() {
  }


  private void setAccessPanelForPath(final String selectedProjectPath) {
    

    final FlexTable projectAccessGrid = new FlexTable();
    final PushButton deleteAccessButton = new PushButton("Delete Group Access");
    
    final TextBox selectedAccessTextBox = new TextBox();
    selectedAccessTextBox.setVisible(false);

    final TextBox selectedAccessRightsTextBox = new TextBox();
    selectedAccessRightsTextBox.setVisible(false);

    TableListener accessRowListener = new TableListener() {
      public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
        if (row == 0) {
          return;
        }
        
        FlexTable me = (FlexTable) sender;
        
        if (me.getRowFormatter().getStyleName(1).contains("access-row-empty")) {
          return;
        }


        for (int i = 1; i < me.getRowCount(); i++) {
          if (me.getRowFormatter().getStyleName(i).contains("access-row-selected") && row != i) {
            me.getRowFormatter().removeStyleName(i, "access-row-selected");
          }
        }

        if (me.getRowFormatter().getStyleName(row).contains("access-row-selected")) {
          me.getRowFormatter().removeStyleName(row, "access-row-selected");
          deleteAccessButton.setEnabled(false);
        } else {
          me.getRowFormatter().addStyleName(row, "access-row-selected");
          deleteAccessButton.setEnabled(true);
        }

        final String selectedGroup = me.getText(row, 0);
        selectedAccessTextBox.setText(selectedGroup);
        final String selectedAccessRights = me.getText(row, 1);
        selectedAccessRightsTextBox.setText(selectedAccessRights);

      }
    };
    projectAccessGrid.addTableListener(accessRowListener);
    projectAccessGrid.setStyleName("access-grid");
    projectAccessGrid.setWidth("80%");

    accessPanel.setVisible(false);
    accessPanel.clear();
    accessPanel.setWidth("80%");

    accessPanel.add(new HTML("<h3>" + "Access for : " + selectedProjectPath + "</h3>"));
    
    final HorizontalPanel topPanel = new HorizontalPanel();
    topPanel.add(addAccessButton);
    topPanel.setSpacing(5); 
    
    accessPanel.add(topPanel);
    accessPanel.add(projectAccessGrid);
    accessPanel.add(selectedAccessTextBox);

    deleteAccessButton.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {

        final PushButton me = (PushButton) sender;

        final String selectedGroup = selectedAccessTextBox.getText();
        final String selectedAccessType = selectedAccessRightsTextBox.getText();

        boolean delete = Window.confirm("Are you sure you want to delete project access to: " + selectedProjectPath + ", for group: "
            + selectedGroup + " ?");

        if (!delete) {
          return;
        }

        loading.setVisible(true);
        me.setEnabled(false);

        AsyncCallback deleteAccessCallback = new AsyncCallback() {
          public void onSuccess(Object result) {

            // no loading of left menu b/c no change in values

            loading.setVisible(false);
            me.setEnabled(true);
            accessPanel.setVisible(false);
            accessPanel.clear();
            

            setAccessPanelForPath(selectedProjectPath);
          }

          public void onFailure(Throwable caught) {
            loading.setVisible(false);
            Window.alert("Error in deleting access to: " + selectedProjectPath + ", for group: " + selectedGroup);
            me.setEnabled(true);

          }
        };
        svnAdminService.removeProjectAccess(selectedProjectPath, selectedGroup, selectedAccessType, deleteAccessCallback);

      }
    });
    deleteAccessButton.setWidth("150px");
    deleteAccessButton.addStyleName("user-delete-icon");
    deleteAccessButton.setEnabled(false);

    final HorizontalPanel bottomPanel = new HorizontalPanel();
    bottomPanel.add(deleteAccessButton);
    bottomPanel.add(loading);
    bottomPanel.setSpacing(5); 
    
    accessPanel.add(bottomPanel);

    loading.setVisible(false);

    // Get project access for projectPath
    AsyncCallback getProjectAccessCallback = new AsyncCallback() {
      public void onSuccess(Object result) {

        Map<String, List<String>> projectAccessMap = (Map<String, List<String>>) result;

        List<String> accessList = projectAccessMap.get(selectedProjectPath);

        Map<String, String> groupRightsMap = parseProjectRightsString(accessList);
        Set<String> groupSet = groupRightsMap.keySet();
        List<String> groupList = new ArrayList<String>(groupSet);
        Collections.sort(groupList);

        // headers
        projectAccessGrid.setText(0, 0, "Group");
        projectAccessGrid.setText(0, 1, "Access Type");
        // adds rollover style
        projectAccessGrid.getRowFormatter().addStyleName(0, "access-row-header");
        // adds width style
        projectAccessGrid.getColumnFormatter().addStyleName(0, "access-col-header");

        int i = 1;
        for (String group : groupList) {
          String access = groupRightsMap.get(group);
          projectAccessGrid.setText(i, 0, group);
          projectAccessGrid.setText(i, 1, access);
          projectAccessGrid.getRowFormatter().addStyleName(i, "access-row");
          i++;
        }

        if (groupList.size() == 0) {
          projectAccessGrid.setWidget(1, 0, new HTML("<em>empty</em>"));
          projectAccessGrid.setText(1, 1, "");
          projectAccessGrid.getRowFormatter().addStyleName(1, "access-row-empty");
        }
        
        
        accessPanel.setVisible(true);

      }

      public void onFailure(Throwable caught) {
        Window.alert("Error in populating project access: " + caught.getMessage());
      };

    };
    svnAdminService.getProjectAccessMap(getProjectAccessCallback);
  }
  
  private Map<String, String> parseProjectRightsString(List<String> lineList) {

    if (lineList == null || lineList.size() == 0) {
      return new HashMap<String, String>(0);
    }

    Map<String, String> tempMap = new HashMap<String, String>();

    for (Iterator<String> i = lineList.iterator(); i.hasNext();) {
      String line = i.next();

      String owner = null;
      // @ == group reference
      if (line.startsWith("@")) {
        owner = line.substring(1, line.indexOf('=')).trim();
      } else {
        owner = line.substring(0, line.indexOf('=')).trim();
      }

      String access = null;
      access = line.substring(line.indexOf('=') + 1, line.length());
      access = access.trim();

      tempMap.put(owner, access);

    }

    return tempMap;
  }

}
