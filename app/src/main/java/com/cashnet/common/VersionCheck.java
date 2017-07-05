package com.cashnet.common;

import android.util.Log;

import com.cashnet.util.XmlParser;

import org.w3c.dom.Node;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 버전체크 쓰레드.
 * 
 */
public class VersionCheck {

	/**
	 * 최신버전 값 읽어오기.
	 * 
	 * @return 버전
	 */
	public int getVersion() {

		int version = 0;

//		String url = "http://210.105.193.144:10004/BGFMams/version.xml";	// 테스트서버(tweb)
		String url = "http://210.105.193.181:10004/BGFMams/version.xml";	// 실서버(mamsgw01)

		try {
			version = Integer.parseInt(DownloadHtml(url));

		} catch (Exception ex) {

			Log.e("getVersion()", ex.toString());
			return -1;
		}

		return version;
	}

	/**
	 * HTML 다운로드 메소드
	 */
	private String DownloadHtml(String addr) {
		InputStream is = null;
		XmlParser parser = new XmlParser();
		String response = "";


		try {

			URL url = new URL(addr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			if (conn != null) {
				conn.setConnectTimeout(5000);
				conn.setUseCaches(false);

				Log.e("연결 상태", conn.getResponseCode() + "");
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

					is = conn.getInputStream();
					Node root = parser.loadXmlStream(is);
					response = parser.findNodeText(root, "code");
				}
				conn.disconnect();
			}
		} catch (Exception ex) {
			Log.e("DownloadHtml", ex.toString());
			return ex.toString();
		}
		return response;
	}
}
