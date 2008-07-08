/**
 *
 */
package com.wkinney.client;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author wkinney
 *
 */
public interface SVNAdminService extends RemoteService {

    public String[] getUserList();

    /**
     * @gwt.typeArgs <java.lang.String, java.util.Collection<java.lang.String>>
     */
    public Map getGroupMembersMap();

    /**
     * @gwt.typeArgs <java.lang.String, java.lang.String>
     */
    public Map getProjectAccessMap();

    public void addUser(String username, String password);

    public void deleteUser(String username);

    public void editUser(String oldUser, String user, String password);

    /**
     * @gwt.typeArgs updatedProjectAccessMap <java.lang.String, java.lang.String>
     */
    public void updateProjectAccess(String projectPath, Map updatedProjectAccessMap);

    public void addProjectAccess(String projectPath, String groupName, String accessType);

    public void removeProjectAccess(String projectPath, String groupName, String accessType);

    public void addGroup(String groupName);

    public void deleteGroup(String groupName);

    /**
     * @gwt.typeArgs memberships <com.wkinney.client.Membership>
     */
    public void removeMembership(Collection memberships);

    /**
     * @gwt.typeArgs userList <java.lang.String>
     */
    public void addMembership(String groupName, Collection userList);

    public void removeProject(Collection projectPath);

    public void removeProject(String projectPath);

    public void addProject(String projectPath);

}
