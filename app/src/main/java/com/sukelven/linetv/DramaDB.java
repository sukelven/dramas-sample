package com.sukelven.linetv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;


public class DramaDB extends SQLiteOpenHelper {
	private final static String TABLE_NAME = "dramas";
	private final static int DB_VERSION = 1;

    String M_DRAMA_ID = "mDramaId", M_NAME = "mName", M_TOTAL_VIEWS = "mTotalViews",
		   M_CREATED_AT = "mCreatedAt", M_THUMB = "mThumb", M_RATING = "mRating",
		   M_BLOB = "mBlob";

	private static DramaDB instance;

	private DramaDB(final Context context) {
		super(context, TABLE_NAME, null, DB_VERSION);
	}

	public static synchronized DramaDB getInstance(final Context c) {
		if (instance == null) {
			instance = new DramaDB(c.getApplicationContext());
		}
		return instance;
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		String sql = "CREATE TABLE " + TABLE_NAME+" ("+
				M_DRAMA_ID+" VARCHAR,"+
				M_NAME+" VARCHAR,"+
				M_TOTAL_VIEWS+" VARCHAR,"+
				M_CREATED_AT+" VARCHAR,"+
				M_THUMB+" VARCHAR,"+
				M_RATING+" VARCHAR,"+
				M_BLOB+" BLOB)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public void onOpen(SQLiteDatabase db){
		super.onOpen(db);
	}

	public void AddMain(DramaObj mDramaObj){
		ContentValues values = new ContentValues();
		values.put(M_DRAMA_ID, mDramaObj.drama_id);
		values.put(M_NAME, mDramaObj.name);
		values.put(M_TOTAL_VIEWS, mDramaObj.total_views);
		values.put(M_CREATED_AT, mDramaObj.created_at);
		values.put(M_THUMB, mDramaObj.thumb);
		values.put(M_RATING, mDramaObj.rating);
		getWritableDatabase().insert(TABLE_NAME, null, values);
	}

	public void SavePreview(String drama_id, Bitmap bm){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		byte[] vByteArray = bos.toByteArray();
		ContentValues cv = new ContentValues();
		cv.put(M_BLOB, vByteArray);
		getWritableDatabase().update(TABLE_NAME, cv, M_DRAMA_ID+"='"+drama_id+"'", null);
	}

	/**
	 * Query the 'drama' table. Remember to close the cursor!
	 *
	 * @param columns       the columns
	 * @param selection     the selection
	 * @param selectionArgs the selection arguments
	 * @param groupBy       the group by statement
	 * @param having        the having statement
	 * @param orderBy       the order by statement
	 * @return the cursor
	 */
	public Cursor query(final String[] columns, final String selection,
						final String[] selectionArgs, final String groupBy, final String having,
						final String orderBy, final String limit) {
		return getReadableDatabase()
				.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
	}

	public synchronized void clearDB(){
		try{
		getWritableDatabase().execSQL("DELETE FROM "+ TABLE_NAME);
		}catch(Exception e){e.printStackTrace();}
	}

}

