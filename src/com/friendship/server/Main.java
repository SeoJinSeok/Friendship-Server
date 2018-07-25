package com.friendship.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

public class Main{
	public String POST(HttpServletRequest request) throws Exception{
		ServletInputStream sis = request.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(sis));
		String data = new String();
		while(in.ready())	data += in.readLine();
		return data;
	}
	
	public String GET() throws Exception{
		return "하이";
	}
}