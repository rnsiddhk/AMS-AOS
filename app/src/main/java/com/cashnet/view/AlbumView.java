package com.cashnet.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

public class AlbumView extends Activity {

	private final String TAG = AlbumView.class.getSimpleName();
	private final String NICK = "kyPark";

	private static final int PICK_FROM_CAMERA = 0;
	private static final int PICK_FROM_ALBUM = 1;
	private static final int CROP_FROM_CAMERA = 2;

	public static final int REQUEST_CODE = 0x1001;

	private Uri mImageCaptureUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		doTakeAlbumAction();

	}

	// Album Call!!
	private void doTakeAlbumAction() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
		startActivityForResult(intent, PICK_FROM_ALBUM);
	}

	// Camera Call!!
	public void doTakeCamera() {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		String url = "tmp_"
				+ String.valueOf(System.currentTimeMillis() + ".jpg");
		mImageCaptureUri = Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), url));

		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
				mImageCaptureUri);
		startActivityForResult(intent, PICK_FROM_CAMERA);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_CANCELED
				|| resultCode != Activity.RESULT_OK) {
			this.finish();
			return;
		}

		switch (requestCode) {
		case PICK_FROM_ALBUM: {
			
			mImageCaptureUri = data.getData();
			
			File originalFile = getImageFile(mImageCaptureUri);
			String full_path = originalFile.getPath();
			
			Intent intent = new Intent();
			intent.putExtra("filepath", full_path);
			setResult(Activity.RESULT_OK, intent);
			finish();
			break;
			
		}

		case CROP_FROM_CAMERA: {

			String full_path = mImageCaptureUri.getPath();

			Intent intent = new Intent();
			intent.putExtra("filepath", full_path);
			setResult(Activity.RESULT_OK, intent);
			
			finish();

			break;
		}


		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public File getImageFile(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };

		if (uri == null) {
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		}

		Cursor mCursor = getContentResolver().query(uri, projection, null,
				null, MediaStore.Images.Media.DATE_MODIFIED + " desc");

		if (mCursor == null || mCursor.getCount() < 1) {
			return null;
		}

		int column_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		mCursor.moveToFirst();

		String path = mCursor.getString(column_index);

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
				byte[] buffer = new byte[4096];
				int byteRead;

				while ((byteRead = inputStream.read(buffer)) >= 0) {
					out.write(buffer, 0, byteRead);

				}
			} finally {
				out.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}

	}

	public String encodeToBase64(Bitmap image) {
		String str = null;

		Bitmap bitmap = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();

		str = Base64.encodeToString(b, Base64.DEFAULT);

		return str;
	}

}
