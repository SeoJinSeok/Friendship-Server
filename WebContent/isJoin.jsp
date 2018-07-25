<%@ page import = "com.friendship.db.database" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%
	database db = new database();
	if (db.isJoin(request.getParameter("mid"), request.getHeader("member_no").toString()))
		response.setStatus(200);
	else	response.setStatus(199);
%>