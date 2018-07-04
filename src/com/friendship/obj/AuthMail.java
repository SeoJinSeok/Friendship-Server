package com.friendship.obj;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 
public class AuthMail {
	private static String token[] = new String[100];
	private static int index = 0;
	
	public AuthMail(String host, String user, String key, int code){
		AuthMail.token[index] = key;
		index++;
        int port=465;
         
        // 메일 내용
        String subject = "[FriendShip] 인증 메일입니다..";
        String body = "해당 주소를 클릭하시면 가입이 완료됩니다. http://192.168.219.109:8081/FriendShip/main/auth?key=" + key + "&id=" + Integer.toString(code);
        
        Properties props = System.getProperties();
        
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwail", "false");
        
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            String un="friendship.plot@gmail.com";
            String pw="emfaks23";
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(un, pw);
            }
        });
          
        MimeMessage msg = new MimeMessage(session);
        try{
        	msg.setFrom(new InternetAddress("friendship.plot@gmail.com"));
	        msg.setHeader("Content-type", "text/HTML; charset=UTF-8");	        	        
	        msg.setSubject(subject, "UTF-8");
	        msg.setText(body, "UTF-8");
	        msg.setSentDate(new Date());
	        
	        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(user + "@" + host));
	        Transport.send(msg);
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
	
	public boolean Auth(String token){
		boolean isEqual = false;
		int i = 0;
		while(i < index){
			if (AuthMail.token[i].equals(token)){
				if (i > 0){
					AuthMail.token[i] = null;
					i--;
				}else	AuthMail.token[i] = null;
				isEqual = true;
				break;
			}
			i++;
		}
    	return isEqual;
    }
	
}