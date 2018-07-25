<%@ page import = "com.friendship.server.Main"
	import = "com.friendship.db.database" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%! 
	database db;
	Main main;
%>
<%
	main = new Main();
	db = new database();
	if (request.getMethod().equals("POST")){
		String id = db.Login(main.POST(request));
		if (id != null){
			int no = db.getuserID(id);
			Cookie cookie = new Cookie("member_" + no, Integer.toString(no));
			cookie.setMaxAge(60*60);
			response.addCookie(cookie);
			response.setStatus(response.SC_OK);
		}else	response.setStatus(199);
	}else{
		Cookie [] cookies = request.getCookies();
		for (Cookie c : cookies){
			if (c.getName().equals("member_"+request.getHeader("member_no")))	c.setMaxAge(0);
		}response.setStatus(response.SC_OK);
	}
%>