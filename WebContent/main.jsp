<%@ page import = "com.friendship.server.Main"
	import = "com.friendship.db.database"
%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%! 
	database db;
%>
<%
	db = new database();
	String str = db.GetMain();
	out.print(str);
%>