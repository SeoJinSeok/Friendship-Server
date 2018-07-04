<%@ page import = "com.friendship.server.Main"
	import = "com.friendship.server.Account"
	import = "com.friendship.db.database"
%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%! 
	database db;
	Main main;
	Account account;
	int id;
%>
<%
	main = new Main();
	account = Account.getInstance();
	db = new database();
	id = db.getuserID(account.SignUp(main.POST(request)));
	if (id > 0){
		Cookie cookie = new Cookie("member_" + id, Integer.toString(id));
		cookie.setMaxAge(60*60);
		response.addCookie(cookie);
		response.setStatus(response.SC_OK);
	}
	else	response.sendError(response.SC_NOT_ACCEPTABLE);
%>