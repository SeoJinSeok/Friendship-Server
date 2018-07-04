package com.friendship.obj;

import java.net.URLEncoder;

import org.bouncycastle.jcajce.provider.digest.Keccak.DigestKeccak;
import org.json.JSONObject;

import com.friendship.db.database;

public class UserData {
	private static database db;
	private static AES256Util acoder;
	private static DigestKeccak dcoder;
	private static String [] MoimParams = {"title", "onecomm", "cont", "back", "limit", "regi", "cate", "agel", "ageh"};
	
	public static void main(String[] args) throws Exception{
		SetMoim();
	}
	
	// 모임 설정
    public static void SetMoim() {
        try {
            JSONObject Jobj = new JSONObject();
            Jobj.put(MoimParams[0], "ㅎㅇ");
            Jobj.put(MoimParams[1], "ㅎㅇ");
            Jobj.put(MoimParams[2], "ㅎㅇ");
            Jobj.put(MoimParams[3], "");
            Jobj.put(MoimParams[4], 10);
            Jobj.put(MoimParams[5], "서울특별시-관악구");
            Jobj.put(MoimParams[6], "사교/인맥");
            Jobj.put(MoimParams[7], 20);
            Jobj.put(MoimParams[8], 29);
            db = new database();
            System.out.println(db.MakeMoim(Encode(URLEncoder.encode(Jobj.toString(), "UTF-8"), 1, "setting-moim-objects"), "3"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	// 암호화 
    private static String Encode(String key, int id, String hash){
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

		// 癰귣벏�깈占쎌넅
		private static String Decode(String key, String hash){
			try{
				acoder = new AES256Util(hash);
				return acoder.aesDecode(key);
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
}
