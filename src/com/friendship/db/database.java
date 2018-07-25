package com.friendship.db;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jcajce.provider.digest.Keccak.DigestKeccak;
import org.json.JSONArray;
import org.json.JSONObject;

import com.friendship.obj.AES256Util;

public class database {
	private Connection connect = null;
	private DigestKeccak dcoder = null;
	private AES256Util acoder = null;

	String [] ProfTables = {"nick", "icon", "iconuri", "comm", "birth", "sex", "favos", "region", "join"},
	MoimParams = {"title", "onecomm", "cont", "back", "limit", "regi", "cate", "agel", "ageh"};

	// DB 연결
	private Connection conn(){
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/friendship?serverTimezone=UTC", "root", "emfaks23");
			return conn;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// 로그인
	public String Login(String req){
		connect = conn();
		try{
			JSONObject jobj = new JSONObject(URLDecoder.decode(Decode(req, "user-login-process"), "UTF-8"));
			String ID = jobj.getString("email"), PWD = jobj.getString("password");
			ResultSet rs = connect.prepareStatement("select id from user where email='" + ID +"' and password='" + PWD + "'").executeQuery();

			if (!rs.next()){
				connect.close();
				return null;
			}else{
				connect.close();
				return rs.getString(1);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// 회원 가입
	public String SignUp(String obj){
		String [] tables = {"email", "password"};
		try{
			JSONObject jobj = new JSONObject(URLDecoder.decode(Decode(obj, "user-signup-process"), "UTF-8"));
			connect = conn();
			PreparedStatement st = connect.prepareStatement("select * from user where email=?");
			st.setString(1, jobj.getString(tables[0]));
			ResultSet rs = st.executeQuery();
			if (rs.first()){
				connect.close();
				return null;
			}

			String email = jobj.getString("email"), pwd = jobj.getString("password");
			st = connect.prepareStatement("insert into user values(0, '" + email + "', '" + pwd + "', NULL, NULL, 'default.png', NULL, 0, NULL, 0, NULL, NULL, NULL, ?, 0)");
			
			SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			st.setString(1, transFormat.format(new Date()));
			
			if (!st.execute())	return jobj.getString("email");
			else	return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// 비밀번호 변경
	public boolean UpdatePWD(JSONObject obj){
		try{
			connect = conn();
			PreparedStatement st = connect.prepareStatement("update user set password=? where email=?");
			st.setString(1, Decode(obj.getString("email"), obj.getString("token")));
			st.setString(2, Decode(obj.getString("password"), obj.getString("token")));

			if (st.executeUpdate() > 0){
				connect.close();
				return true;
			}else{
				connect.close();
				return false;
			}
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}
	}

	// 비밀번호 찾기
	public boolean FindPWD(String obj){
		try{
			JSONObject jobj = new JSONObject(Decode(obj, "user-password-find")); 
			connect = conn();
			PreparedStatement st = connect.prepareStatement("select password from user where email=?");
			st.setString(1, jobj.getString("email"));
			if (st.executeUpdate() > 0){
				connect.close();
				return true;
			}
			else{
				connect.close();
				return false;
			}
		}catch(SQLException e){
		e.printStackTrace();
		return false;
		}
	}

	// 이메일 인증
	public boolean UserAuth(String id){
		try{
			connect = conn();
			PreparedStatement st = connect.prepareStatement("update user set authenticated=1 where email=?");
			st.setString(1, id);
			st.executeUpdate();
			connect.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	// 프로필 불러오기
	public String GetProfile(String id){
		JSONObject jobj = new JSONObject();
		int index = 0;

		try{
			connect = conn();
			PreparedStatement st = connect.prepareStatement("select Nickname,Prof_url,Intro,Birth,Sex,Favorites,Region,JoinMoims from user where id=" + id);
			ResultSet rs = st.executeQuery();
			rs.next();
			int j = 0;
			while(index < ProfTables.length-1){
				if (index==1){
					jobj.put(ProfTables[index], readFile(rs.getString(index+1)));
					j = 1;
				}
				else	jobj.put(ProfTables[index+j], rs.getObject(index+1));
				index++;
			}
			index = 0;
			connect.close();
			return Encode(jobj.toString(), 1, "getting-user-profile");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// 프로필 변경
	public boolean SetProfile(String obj, String id){
		int index = 1;
		try{
			connect = conn();
			JSONObject jobj = new JSONObject(URLDecoder.decode(Decode(obj, "setting-user-profile"), "UTF-8"));
			PreparedStatement st = connect.prepareStatement("update user set Nickname=?, Prof_url=?, Intro=?, Birth=?, sex=?, Region=?, Favorites=? where Id = ?");
			int j = 0;
			while(index < ProfTables.length-1){
				if (index==2){
					saveFile(jobj.getString(ProfTables[index-1]), jobj.getString(ProfTables[index]));
					st.setString(index, jobj.getString(ProfTables[index]));
					j = 1;
				}
				else	st.setObject(index, jobj.get(ProfTables[index-1+j]));
				index++;
			}
			st.setObject(index, id);
			st.executeUpdate();
			connect.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	// 모임 생성
	public boolean MakeMoim(String obj, String id){
		int index = 0, j = 0;
		try{
			JSONObject jobj = new JSONObject(URLDecoder.decode(Decode(obj, "setting-moim-objects"), "UTF-8"));
			connect = conn();
			
			// DB에 추가
			PreparedStatement st = connect.prepareStatement("insert into moim values(0, ?, ?, ?, NULL, ?, ?, ?, ?, ?, NULL)");
			while(index < MoimParams.length){
				if (index == 3){
					index++;
					j = -1;
				}
				else st.setObject(index+1+j, jobj.get(MoimParams[index++]));
			}
			
			// 추가 후 게시판 DB 생성, 이미지 URL 저장, 당사자 가입시킨다. 
			if (st.executeUpdate() < 1)	return false;
			else{
				int mid = 0; 
				ResultSet rs = connect.prepareStatement("select id from moim where name=\'" + jobj.get(MoimParams[0])+ "\'").executeQuery();
				rs.next();
				mid = rs.getInt(1);
				JSONArray a = new JSONArray();
				JSONObject u = new JSONObject();
				u.put("id", id);
				u.put("lev", 1);
				a.put(0, u);
				connect.prepareStatement("update moim set Img_url='moim/"+ mid + "/main.png', " + "Members=" + "\'" + a.toString() + "\'" + "where id=" + mid).executeUpdate();
				saveFile(jobj.getString(MoimParams[3]), "moim/" + mid + "/main.png");
				connect.close();
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	// 모임 변경
	public boolean updateMoim(int mid, String req){
		int index = 1;
		try{
			JSONObject jobj = new JSONObject(URLDecoder.decode(Decode(req, "setting-moim-objects"), "UTF-8"));
			connect = conn();
			PreparedStatement st = connect.prepareStatement("update moim set title=?, oneintro=?, desc=?, img_url=?, max_member=?, region=?, category=?, agel=?, ageh=?, members=? where id=" + mid);
			while(index < MoimParams.length + 1){
				if (MoimParams[index-1].equals("back")){
					saveFile(jobj.getString(MoimParams[index-1]), ("moim/" + mid+ "/icon.png"));
					index += 2;
				}
				st.setObject(index, jobj.get(MoimParams[index-1]));
			}
			st.executeUpdate();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	// 모임 조회
	public String MoimInfo(String mid, String id){
		String [] JMoimParams = { "title", "onecomm", "content", "back", "limit", "regi", "cate", "mem", "board"}; // 가입 시 보여지는 정보에 대한 파라미터
		int i = 0, j = 0;
		try{
			connect = conn();
			JSONObject jobj = new JSONObject();
			jobj.put("isJoin", false);
			ResultSet rs = connect.prepareStatement("select * from moim where id=" + mid).executeQuery();
			rs.next();
			while(i < JMoimParams.length-1){
				// 모임 이미지 읽기
				if (i == 3)	jobj.put(JMoimParams[i], readFile(rs.getString(i+2)));
				// 모임 멤버 읽기
				else if (i == 7){
					JSONArray objs = new JSONArray(rs.getString(i+4));
					for (int a=0; a<objs.length(); a++){
						if (objs.getJSONObject(a).get("id").equals(id)){
							jobj.put("isJoin", true);
							break;
						}
					}
					jobj.put(JMoimParams[i], MemList(objs));
				}
				else	jobj.put(JMoimParams[i], rs.getString(i+2));
				i++;
			}
			connect.close();
			
			// 모임 게시판 불러오기
			connect = conn();
			rs = connect.prepareStatement("select Title,Content,Writer,Write_Time from moim_board where Moim_id=" + mid).executeQuery();
			JSONArray jarr = new JSONArray();
			while(rs.next()){
				JSONObject b = new JSONObject();
				b.put("title", rs.getString(1));
				b.put("content", rs.getString(2));
				b.put("writer", rs.getString(3));
				b.put("wtime", rs.getString(4));
				jarr.put(j++, b);
			}
			jobj.put(JMoimParams[i], jarr.toString());
			connect.close();
			return Encode(jobj.toString(), 1, "getting-moim-info");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// 모임 멤버 리스트 불러오기
	private String MemList(JSONArray mem){
		try{
			connect = conn();
			JSONObject jobj = null;
			JSONArray jarr = new JSONArray();

			int i = 0;
			int [] lev = new int[mem.length()];
			String id = new String();

			while(mem.isNull(i)){
				JSONObject j = mem.getJSONObject(i);
				if (i == mem.length())	id.concat(j.getString("id"));
				else	id.concat(j.getString("id") + ",");
				lev[i] = j.getInt("lev");
				i++;
			}
			ResultSet rs = connect.prepareStatement("select id, nickname, Prof_url, Intro from user where id = \'" + id.replace(",", "or") + "\'").executeQuery();
			rs.next();
			i = 0;
			while(rs.next()){
				jobj = new JSONObject();
				jobj.put("id", rs.getString(1));
				jobj.put("nick", rs.getString(2));
				jobj.put("icon", readFile(rs.getString(3)));
				jobj.put("comm", rs.getString(4));
				jobj.put("lev", lev[i]);
				jarr.put(i++, jobj);
			}

			connect.close();
			return jarr.toString();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// 모임 리스트 불러오기
	public String GetMoimList(String id){
		int index = 0;
		try{
			connect = conn();
			JSONObject jobj = null;
			JSONArray jarr = null;
			ResultSet rs = connect.prepareStatement("select joinmoims from user where id=" + id).executeQuery();
			String arr = null;
			if (!rs.next())	arr = rs.getString(1).replace(",", "or"); 
			else 	return null;
			
			jarr = new JSONArray();
			PreparedStatement st = connect.prepareStatement("select id, name, oneintro, img_url from moim where id=" + arr);
			rs = st.executeQuery();
			while(rs.next()){
				jobj = new JSONObject();
				jobj.put("id", rs.getInt(1));
				jobj.put("title", rs.getString(2));
				jobj.put("onecomm", rs.getString(3));
				jobj.put("back", readFile(rs.getString(4)));
				jobj.put("num", rs.getInt(5));
				jarr.put(index++, jobj);
			}
			return Encode(jarr.toString(), 1, "getting-usermoim-list");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// 모임 가입 / 탈.강퇴
	public boolean JQMoim(String mid, int jq, String id){
		try{
			boolean isJoin;
			connect = conn();
			JSONObject jobj = null;

			// 모임 가입, 탈/강퇴인지 확인
			if (jq == 0)	isJoin = false;
			else{
				isJoin = true;
				jobj = new JSONObject();
				jobj.put("id", id);
				jobj.put("lev", 3);
			}

			// 가입 시 DB에 추가, 강퇴시 DB에 삭제
			ResultSet rs = connect.prepareStatement("select Members from moim where id=" + mid).executeQuery();
			rs.next();
			JSONArray mlist = new JSONArray(rs.getString(1));
			if (isJoin)	mlist.put(mlist.length(), jobj);
			else{
				for (int i=0; i<mlist.length(); i++){
					if (mlist.getJSONObject(i).getString("id").equals(id)){
						mlist.remove(i);
						break;
					}
				}
			}

			// 해당 회원의 가입된 모임 DB 수정
			rs = connect.prepareStatement("select joinmoims from user where id=" + id).executeQuery();
			rs.next();
			String jlist = new String();
			if (isJoin){
				if (rs.getString(1) == null)	jlist = String.valueOf(mid);
				else	jlist = rs.getString(1) + "," + mid;
			}else{
				jlist = rs.getString(1);
				jlist = jlist.substring(jlist.indexOf(mid)-1, jlist.indexOf(mid));
			}

			// 모임 정보 업데이트
			if (connect.prepareStatement("update moim set Members='" + mlist.toString() + "' where id=" + mid).executeUpdate() < 1){
				connect.close();
				return false;
			}
			
			int success = connect.prepareStatement("update user set joinmoims='" + jlist + "' where id=" + id).executeUpdate();
			connect.close();
			if (success > 0)	return true;
			else	return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}	

	// 메인 페이지 불러오기
	public String GetMain(){
		String [] MainParams = {"id", "icon", "title", "onecomm"};
		JSONArray jarr = new JSONArray();

		try{
			int i = 0;
			connect = conn();

			// TOP 3 모임 불러오기
			ResultSet rs = connect.prepareStatement("select Id, Img_url, Name, Oneintro from moim order by rand() limit 15").executeQuery();
			while(rs.next()){
				JSONObject jObj = new JSONObject();
				jObj.put(MainParams[0], rs.getInt(1));
				jObj.put(MainParams[1], readFile(rs.getString(2)));
				jObj.put(MainParams[2], rs.getString(3));
				jObj.put(MainParams[3], rs.getString(4));
				jarr.put(i++, jObj);
			}
			/*
			// 이 코드는 지역별, 관심사별 모임 DB 불러오는 코드입니다.
			rs = connect.prepareStatement("select id, background, title, onecomment from moim order by rand() limit 6").executeQuery();
			while(rs.next()){
			JSONObject jObj = new JSONObject();
			jObj.put(MainParams[0], rs.getInt(1));
			jObj.put(MainParams[1], readFile(rs.getString(2)));
			jObj.put(MainParams[2], rs.getString(3));
			jObj.put(MainParams[3], rs.getString(4));
			jarr.put(i++, jObj);
			}
			i=0;
			
			rs = connect.prepareStatement("select id, background, title, onecomment from moim order by rand() limit 6").executeQuery();
			while(rs.next()){
			JSONObject jObj = new JSONObject();
			jObj.put(MainParams[0], rs.getInt(1));
			jObj.put(MainParams[1], readFile(rs.getString(2)));
			jObj.put(MainParams[2], rs.getString(3));
			jObj.put(MainParams[3], rs.getString(4));
			jarr.put(i++, jObj);
			}
			*/
			return Encode(jarr.toString(), 1, "getting-main-objects");
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// 유저 넘버 구하기
	public int getuserID(String email){
		try{
			connect = conn();
			ResultSet rs = connect.prepareStatement("select Id from user where email='" + email + "'").executeQuery();
			rs.next();
			return rs.getInt(1);
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
	}

	// 암호화
	private String Encode(String key, int id, String hash){
		try{
			if (id==0){
				dcoder = new DigestKeccak(Integer.parseInt(hash));
				dcoder.update(key.getBytes());
				byte[] digest = dcoder.digest();
				return org.bouncycastle.util.encoders.Hex.toHexString(digest);
			}else if (id==1){
				acoder = new AES256Util(hash);
				return acoder.aesEncode(key);
			}else	return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// 복호화
	private String Decode(String key, String hash){
		try{
			acoder = new AES256Util(hash);
			return acoder.aesDecode(key);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private String readFile(String uri){
		try{
			FileInputStream file = new FileInputStream("c:/FriendShip-Server/WebContent/WEB-INF/images/" + uri);
			ByteArrayOutputStream buf = new ByteArrayOutputStream();

			int read;
			byte [] data = new byte[1024*256];

			while((read = file.read(data, 0, data.length)) != -1)	buf.write(data, 0, read);
			file.close();

			return Base64.encodeBase64String(buf.toByteArray());
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private boolean saveFile(String str, String uri){
		try{
			String fp = "c:/Friendship-Server/WebContent/WEB-INF/images/" + uri;
			String p = fp.substring(0, fp.lastIndexOf("/"));
			File path = new File(p);
			if (!path.exists())	path.mkdirs();
			
			File file = new File(fp);	
			FileOutputStream icon = new FileOutputStream(file);
			byte [] bytearr = Base64.decodeBase64(str);
			icon.write(bytearr);
			icon.close();
			return true;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
	}
}
