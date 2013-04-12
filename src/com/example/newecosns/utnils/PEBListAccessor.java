package com.example.newecosns.utnils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PEBListAccessor {

	/*
	 * PEBテーブルの構造
	 * CREATE TABLE peb_list(_id integer primary key autoincrement, name text, text text, money integer, coe real, co2 real, detail text, place text, author_name text, resource_id text);
	 *
	 */

	private String TAG = "PEBListAccessor";
	private SQLiteDatabase db = null;
	private Context context = null;

	private static final String DB_PATH = "/data/data/com.example.newecosns/databases/";
    private static final String DB_NAME = "peb_list.db";
    private static final String DB_NAME_ASSET = "peb_list.db";

	//コンストラクタ DBを作ってくれる
	public PEBListAccessor(Context applicationContext){

		this.context = applicationContext;
		try {
			createEmptyDataBase();

	    } catch (IOException ioe) {
	        throw new Error("Unable to create database");
	    } catch(SQLException sqle){
	        throw sqle;
		}
		try{
			PEBSQLHelper helper2 = new PEBSQLHelper(applicationContext);
			db = helper2.getWritableDatabase();
		}catch(SQLiteException e){
			Log.w("ERROR","DB open error");

		}

	}

	public SQLiteDatabase getdb(){

		return db;
	}

	public void closeDb(){
		db.close();
	}


	 /**
    * asset に格納したデータベースをコピーするための空のデータベースを作成する
    *
    **/
   public void createEmptyDataBase() throws IOException{
       boolean dbExist = checkDataBaseExists();

       if(dbExist){
           // すでにデータベースは作成されている
       }else{
           // このメソッドを呼ぶことで、空のデータベースが
           // アプリのデフォルトシステムパスに作られる
    	   PEBSQLHelper helper = new PEBSQLHelper(context);
    	   helper.getReadableDatabase();

           try {
               // asset に格納したデータベースをコピーする
               copyDataBaseFromAsset();

           } catch (IOException e) {
               throw new Error("Error copying database");
           }
           helper.close();
       }
   }

   /**
    * 再コピーを防止するために、すでにデータベースがあるかどうか判定する
    *
    * @return 存在している場合 {@code true}
    */
   private boolean checkDataBaseExists() {
       SQLiteDatabase checkDb = null;

       try{
           String dbPath = DB_PATH + DB_NAME;
           checkDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
       }catch(SQLiteException e){
           // データベースはまだ存在していない
       }

       if(checkDb != null){
           checkDb.close();
       }
       return checkDb != null ? true : false;
   }

   /**
    * asset に格納したデーだベースをデフォルトの
    * データベースパスに作成したからのデータベースにコピーする
    * */
   private void copyDataBaseFromAsset() throws IOException{

       // asset 内のデータベースファイルにアクセス
       InputStream mInput = context.getAssets().open(DB_NAME_ASSET);

       // デフォルトのデータベースパスに作成した空のDB
       String outFileName = DB_PATH + DB_NAME;

       OutputStream mOutput = new FileOutputStream(outFileName);

       // コピー
       byte[] buffer = new byte[1024];
       int size;
       while ((size = mInput.read(buffer)) > 0){
           mOutput.write(buffer, 0, size);
       }

       //Close the streams
       mOutput.flush();
       mOutput.close();
       mInput.close();
   }

	public class PEBSQLHelper extends SQLiteOpenHelper {

		String TAG = "PEBSQLiteHelper";
		private final Context mContext;



		public PEBSQLHelper(Context context) {
			super(context, DB_NAME, null, 16);
			this.mContext = context;

		}



		@Override
		public void onCreate(SQLiteDatabase db) {


		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {


		}



	}
}
