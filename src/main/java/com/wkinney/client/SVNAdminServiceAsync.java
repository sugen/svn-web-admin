/**
 *
 */
package com.wkinney.client;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author wkinney
 *
 */
public interface SVNAdminServiceAsync {

    public void getUserList(AsyncCallback<List<String>> callback);

    public void getGroupMembersMap(AsyncCallback callback);

    public void getProjectAccessMap(AsyncCallback callback);

    public void addUser(String username, String password, AsyncCallback callback);

    public void deleteUser(String username, AsyncCallback callback);

    public void editUser(String oldUser, String user, String password, AsyncCallback callback);

    public void updateProjectAccess(String projectPath, Map updatedProjectAccessMap, AsyncCallback callback);

    public void addProjectAccess(String projectPath, String groupName, String accessType, AsyncCallback callback);

    public void removeProjectAccess(String projectPath, String groupName, String accessType, AsyncCallback callback);

    public void addGroup(String groupName, AsyncCallback callback);

    public void deleteGroup(String groupName, AsyncCallback callback);

    public void removeMembership(List memberships, AsyncCallback callback);

    public void addMembership(String groupName, List userList, AsyncCallback callback);

    public void removeProject(List projectPath, AsyncCallback callback);

    public void removeProject(String projectPath, AsyncCallback callback);

    public void addProject(String projectPath, AsyncCallback callback);
}
