package com.example.newecosns.utnils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ProductSQLPraparation {

	private String TAG = "ProductSQLPraparation";
	private SQLiteDatabase db = null;
	private Context context = null;
	private final String sql =  "create table if not exists product ( "
			+"_id integer primary key autoincrement, "
			+"author_name text,"
			+"name text,"
			+"producer text,"
			+"producer_id integer,"
			+"mark_id integer,"
			+"resource_id text"
			+ ")";

	public ProductSQLPraparation(Context applicationContext) {

		 /*
         * 内部DB処理準備(log)
         */
        LogSQLiteHelper helper = new LogSQLiteHelper(applicationContext);
		try{
			db = helper.getWritableDatabase();
		}catch(SQLiteException e){
			Log.w("ERROR","DB open error");

		}

		try{
			db.execSQL(sql);
		}catch(SQLiteException e){
			Log.d(TAG,e.getMessage());
		}

	}

	public SQLiteDatabase getdb(){

		return db;
	}

	public void closeDb(){
		db.close();
	}

	public class LogSQLiteHelper extends SQLiteOpenHelper {

		String TAG = "LogSQLiteHelper";
		private static final String name = "ecosns.db";
		public LogSQLiteHelper(Context context) {

			super(context, name , null, 18);

		}

		//初回に実行
		@Override
		public void onCreate(SQLiteDatabase db) {

			//String sql = "drop table if exists log;";


			//db.execSQL(sql);


			try{
				db.execSQL(sql);
			}catch(SQLiteException e){
				Log.d(TAG,e.getMessage());
			}

		}


		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			String sql = "drop table if exists log;";

			try {
			    db.execSQL(sql);
			} catch (SQLiteException e) {
			    Log.e(TAG, e.toString());
			}

			try{
				db.execSQL(sql);
			}catch(SQLiteException e){
				Log.d(TAG,e.getMessage());
			}

		}

	}



}