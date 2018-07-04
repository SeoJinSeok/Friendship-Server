package com.friendship.server;

import java.net.URLDecoder;
import java.util.Date;

import org.json.JSONObject;

import com.friendship.db.database;

import com.friendship.obj.AES256Util;
import com.friendship.obj.AuthMail;

public class Account{
	private static Account account;
	private static AuthMail auth;
	private JSONObject jobj;
	private static database db;
	private static AES256Util key = null;
	private static String authID;
	
	private Account(){
		super();
	}
	
	public static synchronized Account getInstance(){
		if (account == null){
			db = new database();
			account = new Account();
		}
		return account;
	}
	
	// 회원가입
	public String SignUp(String obj){
		String email = db.SignUp(obj);
		if (email != null){
			key = new AES256Util("user-signup-process");
			try{
				jobj = new JSONObject(URLDecoder.decode(key.aesDecode(obj), "UTF-8"));
			}catch(Exception e){
				return null;
			}
			AuthEmailSend(email, jobj.getInt("id"));
			return email;
		}else	return null;
		
	}
	
	// 비밀번호 변경
	public boolean UpdatePWD(String obj){
		jobj = new JSONObject(obj);
		return db.UpdatePWD(jobj);
	}
	
	// 비밀번호 찾기
	public boolean FindPWD(String obj){
		if(db.FindPWD(obj)){
			key = new AES256Util("user-password-find");
			jobj = new JSONObject(key.aesDecode(obj));
			AuthEmailSend(jobj.getString("email"), jobj.getInt("id"));
			return true;
		}
		return false;
	}
	
	// 인증 이메일 보내기
	public boolean AuthEmailSend(String email, int id){
		authID = email;
		auth = new AuthMail(authID.substring(authID.indexOf("@") + 1, authID.length()), authID.substring(0, authID.indexOf("@")), key.aesEncode(new Date().toString()), id);
		return true;
	}
	
	// 이메일 인증하기
	public boolean AuthEmailCheck(String code, int id){
		switch(id){
		// 유저 인증
		case 0:
			if (auth.Auth(code))	return db.UserAuth(authID);
			else	return false;
		// 비밀번호 찾기
		case 1:
			return auth.Auth(code);
		}
		return false;
	}
}