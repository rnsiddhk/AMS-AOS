package com.cashnet.common;

import android.os.Environment;
import android.util.Log;

import com.cashnet.main.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by JJH on 2016-09-30.
 */
public class FileDownloadClass {


    // 총 파일크기.(바이트 기준)
    public static int totalSize;
    // 다운로드 받은 파일 크기.(바이트 기준)
    public static int downloadedSize = 0;

    /**
     * 폴더에 저장된 파일 지우기(단일)
     *
     *            경로/파일명 (/mnt/sdcard/MobileAMS/ams.apk)
     * @return true: 삭제성공, false: 삭제실패
     */
    public static boolean deleteFile(String path) {
        try {
            File f = new File(path);

            // 현재 디렉토리안에 파일이 존재하면 파일삭제
            if (f.isFile()) {
                f.delete();
                return true;
            }
            return false;
        } catch (Exception e) {
            // TODO: handle exception

            Log.e("Exception", "Delete File_Err: " + e.toString());
            return false;
        }
    }

    /**
     * 파일사이즈 조회
     *
     * @param DownUrl
     * @return int 파일사이즈
     */
    public static int getDownFileSize(String DownUrl) {

        try {
            URL url = new URL(DownUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            totalSize = urlConnection.getContentLength();
            urlConnection.disconnect();
            return totalSize;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("getDownFileSize error", e.getMessage());
            return 2;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getDownFileSize error", e.getMessage());
            return 3;
        }

    }

    /**
     * @http로 APK파일을 다운로드 받는다. (get방식)
     * param string downUrl: DownloadUrl 경로.
     * @return 0: 다운로드 완료. 1: URL 에러 2: File IO 에러
     * @warning
     */
    public static String APKfileDownload(String DownUrl) {

        FileOutputStream fileOutput = null;
        InputStream inputStream = null;

        try {

            URL url = new URL(DownUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File path = new File(sdCardPath, "MobileAMS");
            if (!path.isDirectory()) {
                path.mkdirs();
            }

            // SD카드에 파일을 저장하기 위한 파일 객체 생성
            File file = new File(path, "ams.apk");

            fileOutput = new FileOutputStream(file);
            inputStream = urlConnection.getInputStream();

            totalSize = urlConnection.getContentLength();
            downloadedSize = 0;

            byte[] buffer = new byte[2048];// 1024
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {

                fileOutput.write(buffer, 0, bufferLength);

                downloadedSize += bufferLength;

                Main.mProgress.setProgress(downloadedSize);

                if (Main.finish_chk) {
                    file.delete();
                    return "Stop";// 0;
                }
                Log.i("HttpDownloader", "HttpDownloader=" + downloadedSize + "total= " + totalSize);

            }

            Log.i("HttpDownloader", " Finish!!");
            return "Finish";
        } catch (IOException e) {
            e.printStackTrace();
            return "파일 다운로드를 실패했습니다.\n(File IO 에러: " + e.getMessage() + ")";// return 2;
        } catch (Exception e) {
            e.printStackTrace();
            return "파일 다운로드를 실패했습니다.\n(기타 에러: " + e.getMessage() + ")";// return 4;
        } finally {
            try {
                if (fileOutput != null)
                    fileOutput.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
