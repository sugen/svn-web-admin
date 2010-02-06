/**
 *
 */
package com.wkinney.client;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author wkinney
 *
 */
public interface SVNAdminService extends RemoteService {

    public List<String> getUserList();

    /**
     * @gwt.typeArgs <java.lang.String, java.util.List<java.lang.String>>
     */
    public Map<String, List<String>> getGroupMembersMap();

    /**
     * @gwt.typeArgs <java.lang.String, java.lang.String>
     */
    public Map<String, List<String>> getProjectAccessMap();

    public void addUser(String username, String password);

    public void deleteUser(String username);

    public void editUser(String oldUser, String user, String password);

    /**
     * @gwt.typeArgs updatedProjectAccessMap <java.lang.String, java.lang.String>
     */
    public void updateProjectAccess(String projectPath, Map<String, String> updatedProjectAccessMap);

    public void addProjectAccess(String projectPath, String groupName, String accessType);

    public void removeProjectAccess(String projectPath, String groupName, String accessType);

    public void addGroup(String groupName);

    public void deleteGroup(String groupName);

    /**
     * @gwt.typeArgs memberships <com.wkinney.client.Membership>
     */
    public void removeMembership(List<Membership> memberships);

    /**
     * @gwt.typeArgs userList <java.lang.String>
     */
    public void addMembership(String groupName, List<String> userList);

    public void removeProject(List<String> projectPath);

    public void removeProject(String projectPath);

    public void addProject(String projectPath);

}
