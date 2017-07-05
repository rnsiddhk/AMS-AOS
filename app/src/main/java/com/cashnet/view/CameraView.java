package com.cashnet.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CameraView extends Activity {

	private static final int CROP_FROM_CAMERA = 2;


	private Uri mImageCaptureUri;
	public byte[] a;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		doTakePhotoAction();
	}

	@Override
	protected void onResume() {

		super.onResume();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	// Camera Call!!
	private void doTakePhotoAction() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mImageCaptureUri = createSaveCropFile();

		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
		startActivityForResult(intent, CROP_FROM_CAMERA);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_CANCELED || resultCode != Activity.RESULT_OK) {
			this.finish();
			return;
		}

		switch (requestCode) {

			case CROP_FROM_CAMERA: {

				String full_path = mImageCaptureUri.getPath();

				MediaScanning media = new MediaScanning(this, new File(full_path));
				media.Start();

				break;
			}

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private Uri createSaveCropFile() {
		Uri uri;

		String url = "bgf/BGFNetWorksIMG_"
				+ String.valueOf(System.currentTimeMillis() + ".jpg");

		uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				url));

		return uri;
	}

	// Select Image -> Path
	private File getImageFile(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };

		if (uri == null) {
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		}

		Cursor mCursor = getContentResolver().query(uri, projection, null,
				null, MediaStore.Images.Media.DATE_MODIFIED + "desc");

		if (mCursor == null || mCursor.getCount() < 1) {
			return null; // no Cursor || no Record
		}

		int columnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		mCursor.moveToFirst();

		String path = mCursor.getString(columnIndex);

		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}

		return new File(path);

	}

	public static boolean copyFile(File srcFile, File destFile) {
		boolean result = false;

		try {
			InputStream in = new FileInputStream(srcFile);

			try {
				result = copyToFile(in, destFile);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			result = false;
		}

		return result;
	}

	public static boolean copyToFile(InputStream inputStream, File destFile) {
		try {
			OutputStream out = new FileOutputStream(destFile);

			try {
				byte[] buffer = new byte[1024];
				//4096
				int byteRead;

				while ((byteRead = inputStream.read(buffer)) >= 0) {
					out.write(buffer, 0, byteRead);

				}
			} finally {
				out.flush();
				out.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}

	}


	public String encodeTobase64(Bitmap image) {
		String str = null;
		Bitmap bitmap = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();

		str = Base64.encodeToString(b, Base64.DEFAULT);

		return str;
	}

	public void Success() {
		Intent intent = new Intent();
		intent.putExtra("filepath", mImageCaptureUri.getPath());
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	class MediaScanning implements MediaScannerConnectionClient{

		private MediaScannerConnection mConnection;
		private File mTargetFile;

		public MediaScanning(Context mContext, File targetFile) {
			this.mTargetFile = targetFile;
			mConnection = new MediaScannerConnection(mContext, this);
		}

		public void Start() {
			mConnection.connect();
		}

		@Override
		public void onMediaScannerConnected() {
			mConnection.scanFile(mTargetFile.getAbsolutePath(), null);
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			mConnection.disconnect();
			Success();
		}
	}
}
