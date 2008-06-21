package com.wkinney.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GroupMemberData extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {


    /**
     *
     */
    private static final long serialVersionUID = 3298334539608012952L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String r = "<groups>\r\n" +
        		"    <trip title=\"Trip1\" allowDrag=\"false\" trip=\"true\">\r\n" +
        		"    </trip>\r\n" +
        		"    <trip title=\"Trip2\" allowDrag=\"false\" trip=\"true\">\r\n" +
        		"    </trip>\r\n" +
        		"    <trip title=\"Trip3\" allowDrag=\"false\" trip=\"true\">\r\n" +
        		"    </trip>\r\n" +
        		"</groups>";

        resp.getWriter().write(r);

        super.doPost(req, resp);
    }


}
