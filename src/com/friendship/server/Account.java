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
	
	// ȸ������
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
	
	// ��й�ȣ ����
	public boolean UpdatePWD(String obj){
		jobj = new JSONObject(obj);
		return db.UpdatePWD(jobj);
	}
	
	// ��й�ȣ ã��
	public boolean FindPWD(String obj){
		if(db.FindPWD(obj)){
			key = new AES256Util("user-password-find");
			jobj = new JSONObject(key.aesDecode(obj));
			AuthEmailSend(jobj.getString("email"), jobj.getInt("id"));
			return true;
		}
		return false;
	}
	
	// ���� �̸��� ������
	public boolean AuthEmailSend(String email, int id){
		authID = email;
		auth = new AuthMail(authID.substring(authID.indexOf("@") + 1, authID.length()), authID.substring(0, authID.indexOf("@")), key.aesEncode(new Date().toString()), id);
		return true;
	}
	
	// �̸��� �����ϱ�
	public boolean AuthEmailCheck(String code, int id){
		switch(id){
		// ���� ����
		case 0:
			if (auth.Auth(code))	return db.UserAuth(authID);
			else	return false;
		// ��й�ȣ ã��
		case 1:
			return auth.Auth(code);
		}
		return false;
	}
}