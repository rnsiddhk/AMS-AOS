package com.cashnet.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class JsonClient{
	
	
	
	// GET 방식
	public static JSONObject connect(String URL){

        URL url = null;
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        String response = "";
        JSONObject json = null;

        try {

            url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            // 200 성공코드
            // 400 문법에러
            // 401 권한 에러 : 서버키 확인
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                System.out.println("URL Connection 결과 >>>" + "성공");

                is = conn.getInputStream();
                bos = new ByteArrayOutputStream();

                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    bos.write(byteBuffer, 0, nLength);
                }
                byteData = bos.toByteArray();

                response = new String(byteData);

                System.err.println("성공 response >>" + response);

                json  = new JSONObject(response);

            }else{
                System.out.println("URL Connection 실패>>>" + conn.getResponseCode()+"");
            }
            conn.disconnect();
        } catch (Exception ex) {
            Log.e("GET 전송 Exception >> ", "호출 URL >> " + URL + "  에러 >>" + ex.toString());
        }
        return json;
	}
	
	
	// POST 방식
	public static JSONObject sendHttpPost(String URL, JSONObject jsonObjSend ){
		
        URL url = null;
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        String response = "";
        JSONObject json = null;
        
        try {
            
            url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            
 		   // 새로운 OutputStream에 요청할 OutputStream을 넣는다.
		    os = conn.getOutputStream();
		    os.write(jsonObjSend.toString().getBytes());
		    os.flush();

		    // 200 성공코드
		    // 400 문법에러
		    // 401 권한 에러 : 서버키 확인
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	
            	System.out.println("URL Connection 결과 >>>" + "성공");	
            	
            	is = conn.getInputStream();
            	bos = new ByteArrayOutputStream();
            	
                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    bos.write(byteBuffer, 0, nLength);
                }
                byteData = bos.toByteArray();
                 
                response = new String(byteData);
            	
                System.err.println("성공 response >>" + response);

                json  = new JSONObject(response);
            	
			}else{
				System.out.println("URL Connection 실패>>>" + conn.getResponseCode()+"");
			}
            conn.disconnect();
       } catch (Exception ex) {
    	   Log.e("POST 전송 Exception >> ", "호출 URL >> " + URL + "  에러 >>" + ex.toString());
       }
        return json;
	}

	// POST 방식
	public static JSONObject sendHttpPost(String URL, JSONArray jsonObjSend ){

        URL url = null;
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        String response = "";
        JSONObject json = null;

        try {

            url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);

 		   // 새로운 OutputStream에 요청할 OutputStream을 넣는다.
		    os = conn.getOutputStream();
		    os.write(jsonObjSend.toString().getBytes());
		    os.flush();

		    // 200 성공코드
		    // 400 문법에러
		    // 401 권한 에러 : 서버키 확인
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

            	Log.e("URL Connection 결과 >>>" , "성공 " + conn.getResponseCode() );

            	is = conn.getInputStream();
            	bos = new ByteArrayOutputStream();

                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    bos.write(byteBuffer, 0, nLength);
                }
                byteData = bos.toByteArray();

                response = new String(byteData);

                System.err.println("성공 response >>" + response);

                json  = new JSONObject(response);

			}else{
                Log.e("URL Connection 결과 >>>" , "실패 " + conn.getResponseCode() );
			}
            conn.disconnect();
       } catch (Exception ex) {
    	   Log.e("POST 전송 Exception >> ", "호출 URL >> " + URL + "  에러 >>" + ex.toString());
       }
        return json;
	}
}