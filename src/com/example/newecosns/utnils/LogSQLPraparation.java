package com.example.newecosns.utnils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LogSQLPraparation {

	private String TAG = "LogSQLPraparation";
	private SQLiteDatabase db = null;
	private Context context = null;

	public LogSQLPraparation(Context applicationContext) {

		 /*
         * 内部DB処理準備(log)
         */
        LogSQLiteHelper helper = new LogSQLiteHelper(applicationContext);
		try{
			db = helper.getWritableDatabase();
		}catch(SQLiteException e){
			Log.w("ERROR","DB open error");

		}
		String init_sql2 = "create table if not exists log ( "
				+"_id integer primary key autoincrement, "
				+"user_name text,"
				+"screen_name text,"
				+"created text,"
				+"updated text,"
				+"category text, "
				+"name text, "
				+"place text,"
				+"money real,"
				+"peb_id integer,"
				+"co2 real,"
				+"coe real,"
				+"id_in_usr integer,"
				+"longitude REAL,"
				+"latitude REAL,"
				+"provider text,"
				+"accuracy REAL,"
				+"picture BLOB,"
				+"picture_resource_id text,"
				+"width real,"
				+"height real,"
				+"picture_resource_id,"
				+"timestamp integer,"
				+"resource_id text,"
				+"product_id int,"
				+"price int"
				+ ")";
		try{
			db.execSQL(init_sql2);
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


			String sql = "create table if not exists log ( "
					+"_id integer primary key autoincrement, "
					+"user_name text,"
					+"screen_name text,"
					+"created text,"
					+"updated text,"
					+"category text, "
					+"name text, "
					+"place text,"
					+"money real,"
					+"peb_id integer,"
					+"co2 real,"
					+"coe real,"
					+"id_in_usr integer,"
					+"longitude REAL,"
					+"latitude REAL,"
					+"provider text,"
					+"accuracy REAL,"
					+"picture BLOB,"
					+"picture_resource_id,"
					+"timestamp integer,"
					+"resource_id text,"
					+"product_id int,"
					+"price int"
					+ ")";
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

			sql = "create table if not exists log ( "
					+"_id integer primary key autoincrement, "
					+"user_name text,"
					+"screen_name text,"
					+"created text,"
					+"updated text,"
					+"category text, "
					+"name text, "
					+"place text,"
					+"money real,"
					+"peb_id integer,"
					+"co2 real,"
					+"coe real,"
					+"id_in_usr integer,"
					+"longitude REAL,"
					+"latitude REAL,"
					+"provider text,"
					+"accuracy REAL,"
					+"picture BLOB,"
					+"picture_resource_id,"
					+"timestamp integer,"
					+"resource_id text,"
					+"product_id int,"
					+"price int"
					+ ")";
			try{
				db.execSQL(sql);
			}catch(SQLiteException e){
				Log.d(TAG,e.getMessage());
			}

		}

	}



}