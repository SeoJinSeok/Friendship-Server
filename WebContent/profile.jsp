<%@page import="org.json.JSONObject"%>
<%@ page import = "com.friendship.server.Main"
	import = "com.friendship.db.database"
%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%! 
	database db = null;
	Main main = null;
%>
<%
	main = new Main();
	db = new database();
	String id = request.getHeader("member_no").toString();
	if (request.getMethod().equals("GET")){
		if (id != null){
			JSONObject objs = new JSONObject();
			objs.put("prof", db.GetProfile(id));
			objs.put("moim", db.GetMoimList(id));
			out.print(objs.toString());
		}
	}else{
		if(db.SetProfile(main.POST(request), id))	response.setStatus(200);
	}
%>