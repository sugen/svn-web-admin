**svn-web-admin** is a web application that allow you to manager users, groups and access to a subversion server configured via Apache Httpd (mod\_dav, mod\_dav\_svn, mod\_authz\_svn). For subversion server configuration details, see [Chapter 6. Server Configuration](http://svnbook.red-bean.com/en/1.0/ch06s04.html) of the free [Version Control with Subversion](http://svnbook.red-bean.com/) online book.

This subversion configuration is most commonly used with the [CollabNet's Subversion](http://www.collab.net/products/subversion/).

Software Requirements:
  * Java SE 5
  * Ant 1.7.0
  * Servlet Container of choice (e.g. Tomcat)
Also, ensure you have `htpasswd` script installed (see [HTTP Server documentation](http://httpd.apache.org/docs/2.0/programs/htpasswd.html)), and read/write access to the subversion access (`AuthzSVNAccessFile`) and user authorization (`AuthUserFile`) files.

Build instructions:
```
 - Execute: ant war
```

Optionally you can deploy the binary release [svn-web-admin-1.0.war](http://svn-web-admin.googlecode.com/files/svn-web-admin-1.0.war) (compiled in Java SE 6)

Installation instructions:
```
 - Deploy war to Servlet Container
 - Edit 'svnwebadmin.properties' inside WEB-INF/classes to match environment
 - Start Container
```

Screenshot:

![http://svn-web-admin.googlecode.com/files/svn-web-admin_ss_projects.jpg](http://svn-web-admin.googlecode.com/files/svn-web-admin_ss_projects.jpg)

**svn-web-admin** uses [GWT](http://code.google.com/webtoolkit/)

Please contact the project owner with any questions or issues: ![http://svn-web-admin.googlecode.com/files/proj_owner_email.png](http://svn-web-admin.googlecode.com/files/proj_owner_email.png)