<%@ page import = "com.friendship.server.Main"
	import = "com.friendship.db.database"
%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%! 
	database db = null;
	int id = 0;
	String str = null, jq = null;
	Main main = null;
%>
<%
	db = new database();
	if (request.getMethod() == "GET"){
		str = request.getParameter("mid");
		jq = request.getParameter("jqid");
		if (jq == null)	out.print(db.MoimInfo(str));
		else	out.print(db.JQMoim(str, jq, request.getHeader("member_no").toString()));
	}else{
		main = new Main();
		str = main.POST(request);
		System.out.println(str);
		if(!db.MakeMoim(str, request.getHeader("member_no").toString()))	response.setStatus(199);
	}
%>