package com.wkinney.server.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import com.wkinney.client.Membership;
import com.wkinney.server.SVNAdminServiceImpl;

public class SVNAdminServiceImplTest extends TestCase {

    public SVNAdminServiceImplTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    public void testLoadData() throws Exception {
        SVNAdminServiceImpl svnAdminServiceImpl = new SVNAdminServiceImpl();
 
        // calls loadData() 
        
    }
 
    public void testAddAndRemoveGroup() throws Exception {
        SVNAdminServiceImpl svnAdminServiceImpl = new SVNAdminServiceImpl();
        
        final String g = "group" + System.currentTimeMillis();
        
        svnAdminServiceImpl.addGroup(g);
        
        
        Map<String, List<String>> memberMap = svnAdminServiceImpl.getGroupMembersMap();
        
        assertTrue(memberMap.containsKey(g));
        
        
        // delete it
        
        svnAdminServiceImpl.deleteGroup(g);
        
        memberMap = svnAdminServiceImpl.getGroupMembersMap();
        
        assertTrue(!memberMap.containsKey(g));
        
        
    }
    
    
    public void testAddAndRemoveUser() throws Exception {
        SVNAdminServiceImpl svnAdminServiceImpl = new SVNAdminServiceImpl();
        
        final String userToAdd = "member" + System.currentTimeMillis();
        
        
        svnAdminServiceImpl.addUser(userToAdd, "junit" + System.currentTimeMillis());
        
        List<String> users = svnAdminServiceImpl.getUserList();
        
        boolean userFound = false;
        for (Iterator<String> i = users.iterator(); i.hasNext();) {
            String user = i.next();
            if (user.equals(userToAdd)) {
                userFound = true;
                break;
            }
        }
        assertTrue(userFound);
        
        // remove them now
        
        svnAdminServiceImpl.deleteUser(userToAdd);
        
        users = svnAdminServiceImpl.getUserList();
        
        userFound = false;
        for (Iterator<String> i = users.iterator(); i.hasNext();) {
            String user = i.next();
            if (user.equals(userToAdd)) {
                userFound = true;
                break;
            }
        }
        assertTrue(!userFound);
        
        
    }
    
    public void testUpdateUserPassword() throws Exception {
        SVNAdminServiceImpl svnAdminServiceImpl = new SVNAdminServiceImpl();
        
        List<String> users = svnAdminServiceImpl.getUserList();
        
        final String userToUpdate = users.get(new Random().nextInt(users.size()-1));
        final String password = "pass" + System.currentTimeMillis();
        
        
        svnAdminServiceImpl.editUser(userToUpdate, userToUpdate, password);
        
      
        
    }
    
    public void testAddAndRemoveMembership() throws Exception {
        
        SVNAdminServiceImpl svnAdminServiceImpl = new SVNAdminServiceImpl();
        
        final String memberToAdd = "member" + System.currentTimeMillis();
        
        // need to add them as a user first
        svnAdminServiceImpl.addUser(memberToAdd, "junit" + System.currentTimeMillis());
        
        Map<String, List<String>> memberMap = svnAdminServiceImpl.getGroupMembersMap();
        
        final String group = memberMap.keySet().iterator().next();
        
        List<String> memberList = memberMap.get(group);
        
        memberList.add(memberToAdd);
        
        svnAdminServiceImpl.addMembership(group, new ArrayList(memberList));
        
        // make sure member was added
        
        memberMap = svnAdminServiceImpl.getGroupMembersMap();
        
        memberList = memberMap.get(group);
        
        assertTrue(memberList.contains(memberToAdd));
        
        // now remove them 
        
        Set<Membership> removes = new HashSet<Membership>();
        removes.add(new Membership(group, memberToAdd));
        //removeMap
        svnAdminServiceImpl.removeMembership(new ArrayList(removes));
        
        memberMap = svnAdminServiceImpl.getGroupMembersMap();
        
        memberList = memberMap.get(group);
        
        // make sure the member was removed
        assertTrue(!memberList.contains(memberToAdd));
        
        // delete user
        
        
        svnAdminServiceImpl.deleteUser(memberToAdd);
        
        List<String> users = svnAdminServiceImpl.getUserList();
        
        boolean userFound = false;
        for (Iterator<String> i = users.iterator(); i.hasNext();) {
            String user = i.next();
            if (user.equals(memberToAdd)) {
                userFound = true;
                break;
            }
        }
        assertTrue(!userFound);
        
    }

    public void testAddAndRemoveProject() throws Exception {
        SVNAdminServiceImpl svnAdminServiceImpl = new SVNAdminServiceImpl();
        
        final String projectName = "/p" + System.currentTimeMillis();
        
        svnAdminServiceImpl.addProject(projectName);
        
        Map projectAccessMap = svnAdminServiceImpl.getProjectAccessMap();
     
        boolean foundProject = false;
        for(Iterator i = projectAccessMap.keySet().iterator(); i.hasNext();) {
            String projectPath = (String) i.next();
            if (projectPath.equals(projectName)) {
                foundProject = true;
                break;
            }
            
        }
        assertTrue(foundProject);
        
        // remove it
        Set projectPathList = new HashSet(1);
        projectPathList.add(projectName);
        
        svnAdminServiceImpl.removeProject(new ArrayList(projectPathList));
        
        projectAccessMap = svnAdminServiceImpl.getProjectAccessMap();
        
        foundProject = false;
        for(Iterator i = projectAccessMap.keySet().iterator(); i.hasNext();) {
            String projectPath = (String) i.next();
            if (projectPath.equals(projectName)) {
                foundProject = true;
                break;
            }
            
        }
        assertTrue(!foundProject);
        
        
    }
    
    
    public void testAddAndRemoveProjectAccess() throws Exception {
        SVNAdminServiceImpl svnAdminServiceImpl = new SVNAdminServiceImpl();
        
        Map<String, List<String>> projectAccessMap = svnAdminServiceImpl.getProjectAccessMap();
        
        final String projectPath = projectAccessMap.keySet().iterator().next();
        
       // Collection accessList = projectAccessMap.get(projectPath);
        
        //Map<String, Collection<String>> groupMemberMap = svnAdminServiceImpl.getGroupMembersMap();
        
        final String groupName = "junit" + System.currentTimeMillis();
        
        // add new group
        svnAdminServiceImpl.addGroup(groupName);
        
        final String at = "rw";
        
        // add access
        svnAdminServiceImpl.addProjectAccess(projectPath, groupName, at);
        
        
        // verify added access
        projectAccessMap = svnAdminServiceImpl.getProjectAccessMap();
        
        // access line to find
        String al = svnAdminServiceImpl.createAccessLine(groupName, at);
        
        boolean foundAL = false;
        
        for (Iterator<String> i = projectAccessMap.keySet().iterator(); i.hasNext();) {
            String pp = i.next();
            if (pp.equals(projectPath)) {
                Collection accessList = projectAccessMap.get(pp);
                
                if (accessList.contains(al)) {
                    foundAL = true;
                    
                }
                break;
            }
        }
        assertTrue(foundAL);
        
        // remove access
        svnAdminServiceImpl.removeProjectAccess(projectPath, groupName, at);
        
     // verify removed access
        boolean accessRemoved = false;
        projectAccessMap = svnAdminServiceImpl.getProjectAccessMap();
        for (Iterator<String> i = projectAccessMap.keySet().iterator(); i.hasNext();) {
            String pp = i.next();
            if (pp.equals(projectPath)) {
                Collection accessList = projectAccessMap.get(pp);
                
                if (!accessList.contains(al)) {
                    accessRemoved = true;
                    
                }
                break;
            }
        }
        assertTrue(accessRemoved);
        
        // remove group
        svnAdminServiceImpl.deleteGroup(groupName);
        
    }
    
    
    public void testUpdateProjectAccess() throws Exception {
        SVNAdminServiceImpl svnAdminServiceImpl = new SVNAdminServiceImpl();
        
        Map<String, List<String>> projectAccessMap = svnAdminServiceImpl.getProjectAccessMap();
        
        final String projectPath = (String) projectAccessMap.keySet().iterator().next();
        
        List<String> accessList = projectAccessMap.get(projectPath);

        Map<String, String> tempMap = svnAdminServiceImpl.parseProjectRightsString(accessList);
        
        final String g = "group" + System.currentTimeMillis();
        final String accessRights = "w";
        
        // add group
        
        svnAdminServiceImpl.addGroup(g);
        
        
        tempMap.put(g, accessRights);
        
        svnAdminServiceImpl.updateProjectAccess(projectPath, tempMap);
        
        // make sure newly added group and access is there after update
        
        projectAccessMap = svnAdminServiceImpl.getProjectAccessMap();
        
        accessList = projectAccessMap.get(projectPath);

        String accessLineTemp = svnAdminServiceImpl.createAccessLine(g, accessRights);
        
        assertTrue(accessList.contains(accessLineTemp));
        
        // delete it again
        
        tempMap.remove(g);
        
        
        svnAdminServiceImpl.updateProjectAccess(projectPath, tempMap);
        
        projectAccessMap = svnAdminServiceImpl.getProjectAccessMap();
        
        accessList = projectAccessMap.get(projectPath);

        accessLineTemp = svnAdminServiceImpl.createAccessLine(g, accessRights);
        
        // make sure it is now gone
        
        assertTrue(!accessList.contains(accessLineTemp));
        
        
        // remove group
        svnAdminServiceImpl.deleteGroup(g);
        
        Map<String, List<String>> memberMap = svnAdminServiceImpl.getGroupMembersMap();
        
        assertTrue(!memberMap.containsKey(g));
    }
    
    
}
