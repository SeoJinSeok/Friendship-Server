<%@ page import = "com.friendship.server.Main"
	import = "com.friendship.db.database"
%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%! 
	database db = null;
	String str = null, id = null;
	int jq = -1;
	Main main = null;
%>
<%
	db = new database();
	if (request.getMethod().equals("GET")){
		str = request.getParameter("mid");
		if (request.getParameter("jq") != null)	jq = Integer.parseInt(request.getParameter("jq"));
		
		if (request.getParameter("id") != null)	id = request.getParameter("id");
		else	id = request.getHeader("member_no");
		
		if (jq == -1)	out.print(db.MoimInfo(str, id));
		else	db.JQMoim(str, jq, id);
	}else{
		main = new Main();
		str = main.POST(request);
		System.out.println(str);
		if(!db.MakeMoim(str, request.getHeader("member_no").toString()))	response.setStatus(199);
	}
%>