package com.example.newecosns.cohesive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import jp.innovationplus.ipp.client.IPPApplicationResourceClient;
import jp.innovationplus.ipp.client.IPPApplicationResourceClient.QueryCondition;
import jp.innovationplus.ipp.core.IPPQueryCallback;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.newecosns.IPPLoginActivity;
import com.example.newecosns.InputLogActivity;
import com.example.newecosns.MainActivity;
import com.example.newecosns.R;
import com.example.newecosns.models.LogAdapter;
import com.example.newecosns.models.LogItem;
import com.example.newecosns.models.PEBItem;
import com.example.newecosns.models.PairItem;
import com.example.newecosns.models.PictureItem;
import com.example.newecosns.models.ProductItem;
import com.example.newecosns.models.SummaryAdapter;
import com.example.newecosns.models.SummaryItem;
import com.example.newecosns.utnils.ImageCache;
import com.example.newecosns.utnils.NetworkManager;


public class OthersLogFragment extends SherlockFragment {

	//デバッグ用タグ
	String TAG = "OthersLogFragment";

	//ビュー操作用
	View view =null;
	Button input_log_btn = null;
	LogAdapter adapter = null;


	//内部DB操作用
	SQLiteDatabase peb_db= null;


	//IPPユーザデータ
	SharedPreferences ipp_pref;
	SharedPreferences.Editor ipp_editor;
	private String ipp_auth_key;
	private String ipp_id_string;
	private String ipp_pass_string;
	private String ipp_screen_name;
	private String team_resource_id;
	private int role_self  = 0;
	private String pair_common_id;
	 List<PairItem> pair_item_list = null;
	 String pair_item_list_string = null;

	public String stress_now;

	//UI
	ProgressBar wait_bar = null;
	ProgressBar wait_bar_summary = null;


	//リスト末尾のタイムスタンプ
	ProgressBar waitBar = null;

	//オプションメニューのid
	private int ipp_login_menu_id = 1;
	private int reload_menu_id = 2;
	private int addget_menu_id = 5;

	//PEBリスト
	private HashMap<String, PEBItem> peb_list = null;

	//製品関連
	private HashMap<String, ProductItem> product_list = null;



	//月変更用ボタン
	Button this_month = null;
	Button next_month = null;
	Button previous_month = null;

	private AlertDialog dateDialog = null;
	DatePicker datePicker = null;

	Calendar calendar = null;

	//今表示してる内容
	int target_year = 0;
	int target_month = 0;
	long last_timestamp = 0;

	private long since;
	private long until;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//本フラグメントを表示する
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_others_log, container, false); //summaryFragment共通のレイアウトを使いまわす
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//メイン処理
	@Override
	public void onStart(){
		super.onStart();



		//wait_bar = (ProgressBar) getSherlockActivity().findViewById(R.id.waitBarInLogList);
		wait_bar_summary = (ProgressBar) getSherlockActivity().findViewById(R.id.waitBarInSummaryList);
		calendar = Calendar.getInstance();

		//PEBリスト準備
		//prepareTypeList();

		//ログインチェック
		IPPLoginCheck();

        //登録ずみ家計簿リスティング
		showLogAndSummaryList(0,0, 0);




		//表示する月の変更
		prepareChangeMonth();

		// 登録ボタンクリック処理(家計簿入力画面へ)
		prepareGoToInputLog();



	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);


	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	private void prepareTypeList() {

		//PEBリストの取得
		PEBListAccessor peb_list_accessor = new PEBListAccessor(getSherlockActivity().getApplicationContext());
		peb_db = peb_list_accessor.getdb();

		String sqlstr = "select _id, name, text, money, coe, co2 from peb_list";
		Cursor c = peb_db.rawQuery(sqlstr, null);
		c.moveToFirst();
		CharSequence[] list = new CharSequence[c.getCount()];

		//ハッシュマップにいれとく
		peb_list = new HashMap<String, PEBItem>();


		if(list.length > 0){
			for (int i = 0; i < list.length; i++) {
				//DB結果からモデルに
				PEBItem item = new PEBItem();
				item.setId(c.getInt(0));
				item.setName(c.getString(1));
				item.setText(c.getString(2));
				item.setMoney(c.getDouble(3));
				item.setCoe(c.getDouble(4));
				item.setCo2(c.getDouble(5));
				peb_list.put(String.valueOf(item.getId()), item);
				c.moveToNext();
			}
		}

		peb_db.close();
		peb_list_accessor.closeDb();

		//プロダクトリストの取得

		ProductSQLPraparation product_sql_praparation = new ProductSQLPraparation(getSherlockActivity().getApplicationContext());
		SQLiteDatabase product_sql_db = product_sql_praparation.getdb();

		sqlstr = "select _id, name, mark_id, producer from product";
		c = product_sql_db.rawQuery(sqlstr, null);
		c.moveToFirst();
		list = new CharSequence[c.getCount()];

		product_list =new HashMap<String, ProductItem>();

		if(list.length > 0){
			for (int i = 0; i < list.length; i++) {
				//DB結果からモデルに
				ProductItem item = new ProductItem();
				item.setId(c.getInt(0));
				item.setName(c.getString(1));
				item.setMark_id(c.getInt(2));
				item.setProducer(c.getString(3));

				product_list.put(String.valueOf(item.getId()), item);
				c.moveToNext();
			}
		}
	}
*/

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// 登録ボタンクリック処理(家計簿入力画面へ)
	private void prepareGoToInputLog(){


		input_log_btn = (Button)getActivity().findViewById(R.id.inputLogBtn);
		input_log_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				//家計簿入力画面へ遷移
				Intent intent = new Intent(getActivity(), InputLogActivity.class);
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});


	}

////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void IPPLoginCheck(){
		ipp_pref = getSherlockActivity().getSharedPreferences("IPP", Context.MODE_PRIVATE);
		ipp_auth_key = ipp_pref.getString("ipp_auth_key", "");
		ipp_id_string = ipp_pref.getString("ipp_id_string", "");
		ipp_pass_string = ipp_pref.getString("ipp_pass","");
		ipp_screen_name  = ipp_pref.getString("ipp_screen_name", "");
		team_resource_id = ipp_pref.getString("team_resource_id", "");
		role_self = ipp_pref.getInt("role_self", 0);
		pair_common_id = ipp_pref.getString("pair_common_id", "");
		pair_item_list_string = ipp_pref.getString("pair_item_list_string", "");

		ObjectMapper localObjectMapper = new ObjectMapper();
	    try
	    {
	      this.pair_item_list = (List<PairItem>)localObjectMapper.readValue(this.pair_item_list_string, new TypeReference<ArrayList<PairItem>>(){});
	      if (this.ipp_auth_key.equals(""))
	        startActivityForResult(new Intent(getSherlockActivity(), IPPLoginActivity.class), MainActivity.REQUEST_IPP_LOGIN);
	      return;
	    }
	    catch (JsonParseException localJsonParseException)
	    {
	      while (true)
	        Log.d(this.TAG, localJsonParseException.toString());
	    }
	    catch (JsonMappingException localJsonMappingException)
	    {
	      while (true)
	        Log.d(this.TAG, localJsonMappingException.toString());
	    }
	    catch (IOException localIOException)
	    {
	      while (true)
	        Log.d(this.TAG, localIOException.toString());
	    }
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void showLogAndSummaryList(int target_year, int target_month, long until){
		//wait_bar.setVisibility(View.VISIBLE);
		try{
			wait_bar_summary.setVisibility(View.VISIBLE);
		}catch(NullPointerException e){

		}



		//showLogList(target_year, target_month, until);
		showSummaryList(target_year, target_month);
	}


	public void showLogList(int target_year, int target_month, long until){



		//オンラインなら外部DBから他人のデータ読み出し//
		if(NetworkManager.isConnected(getActivity().getApplicationContext())){


			IPPApplicationResourceClient public_resource_client = new IPPApplicationResourceClient(getActivity().getApplicationContext());
			public_resource_client.setAuthKey(ipp_auth_key);
			QueryCondition condition = new QueryCondition();
			condition.setCount(10);


			//読みだすデータの日時の範囲を指定
			int year = 0;
			int month = 0;



			if(target_month == 0 && target_year == 0){
				//今月1日のUNIXタイムスタンプ(ミリ秒)を取得
				year = calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH);
				calendar.set(year, month, 1, 0, 0, 0);
				condition.setSince(calendar.getTimeInMillis()); //今月頭から
				if(until != 0){
					condition.setUntil(until);//指定した日時まで
				}else{

					condition.setUntil(System.currentTimeMillis()); //現在まで
				}


			}else{//月と日を指定されたら
				year = target_year;
				calendar.set(year, target_month, 1, 0, 0, 0); //指定月の1日から
				condition.setSince(calendar.getTimeInMillis());
				if(until != 0){
					condition.setUntil(until);//指定した日時まで
				}else{
					calendar.add(Calendar.MONTH, 1);
					condition.setUntil(calendar.getTimeInMillis()); //次の月の1日まで
					 calendar.add(Calendar.MONTH, -1); //戻しとく
				}

			}

			condition.build();

			if(until == 0){

				public_resource_client.query(LogItem.class,condition, new LogGetCallback()); //最初のよみこみ

			}else{

				public_resource_client.query(LogItem.class, condition,new AdditionalLogGetCallback()); //追加読み込み
			}

		}


	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//IPPプラットフォーム上からログをを読み込むコールバック
	private class LogGetCallback implements IPPQueryCallback <LogItem[]>{



		@Override
		public void ippDidFinishLoading(LogItem[] log_item_array) {


			List<LogItem> LogItemList = new ArrayList<LogItem>();


			LogItem item = null;
			for ( int i = 0; i < log_item_array.length; ++i ) {

				item  =	log_item_array[i];
				LogItemList.add(item);
			}
			if(item != null){
				last_timestamp = item.getTimestamp();
			}



			ListView listView = (ListView) OthersLogFragment.this.getActivity().findViewById(R.id.log_list);//自分で用意したListView

			adapter = new LogAdapter(OthersLogFragment.this.getActivity(), LogItemList, R.layout.row_log, new PictureGetCallback(), ipp_auth_key);
			try{
				listView.setAdapter(adapter);
			}catch( NullPointerException e){
				Log.d(TAG, "ログ読み込みエラー");
			}

			//プログレスバー隠す
			try{
				wait_bar.setVisibility(View.GONE);
			}catch (NullPointerException e){

			}

		}

		@Override
		public void ippDidError(int paramInt) {
			Toast.makeText(OthersLogFragment.this.getActivity(), "家計簿読み込みエラー"+paramInt, Toast.LENGTH_LONG).show();

		}




	}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//IPPプラットフォーム上から追加でログを読み込むコールバック

	private class AdditionalLogGetCallback implements IPPQueryCallback <LogItem[]>{

		@Override
		public void ippDidFinishLoading(LogItem[] log_item_array) {

			for ( int i = 0; i < log_item_array.length; ++i ) {
				LogItem item = new LogItem();
				item  =	log_item_array[i];
				adapter.add(item);
			}

			wait_bar.setVisibility(View.GONE);
		}

		@Override
		public void ippDidError(int paramInt) {
			Toast.makeText(OthersLogFragment.this.getActivity(), "家計簿読み込みエラー"+paramInt, Toast.LENGTH_LONG).show();

		}


	}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//画像を読み込むコールバック(キャッシュ対応)
	private class PictureGetCallback implements IPPQueryCallback<PictureItem>{

		@Override
		public void ippDidError(int arg0) {
			Log.d(TAG,"画像読み込み失敗");
		}

		@Override
		public void ippDidFinishLoading(PictureItem pictureItem) {

			ImageView imageView = adapter.getPictureViewList().get(pictureItem.getResource_id());
			ProgressBar picture_wait_bar = adapter.getProgressBarList().get(pictureItem.getResource_id());

			if(pictureItem.getData() != null && imageView != null){
				Bitmap bitmap = BitmapFactory.decodeByteArray(pictureItem.getData(), 0, pictureItem.getData().length);
				imageView.setImageBitmap(bitmap);
				ImageCache.setImage(pictureItem.getResource_id(), bitmap);//キャッシュ

				imageView.setVisibility(View.VISIBLE);
				picture_wait_bar.setVisibility(View.GONE);
			}



		}

	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//まとめの表示
	private void showSummaryList(int target_year, int target_month){
		IPPApplicationResourceClient public_resource_client = new IPPApplicationResourceClient(getActivity().getApplicationContext());
		public_resource_client.setAuthKey(ipp_auth_key);
		QueryCondition condition = new QueryCondition();
		condition.setCount(10);

		//adapter.clear()


		//読みだすデータの日時の範囲を指定
		int year = 0;
		int month = 0;

		if(target_month == 0 && target_year == 0){
			//今月1日のUNIXタイムスタンプ(ミリ秒)を取得
			year = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH);
			calendar.set(year, month, 1, 0, 0, 0);

			since = calendar.getTimeInMillis(); //今月頭から
			until = System.currentTimeMillis(); //現在まで



		}else{//月と日を指定されたら
			year = target_year;
			calendar.set(year, target_month, 1, 0, 0, 0); //指定月の1日から
			since = calendar.getTimeInMillis();
			calendar.add(Calendar.MONTH, 1);
			until = calendar.getTimeInMillis(); //次の月の1日まで
			calendar.add(Calendar.MONTH, -1); //戻しとく


		}

		condition.setSince(since);
		condition.setUntil(until);
		condition.eq("pair_common_id",pair_common_id);

		condition.build();
		public_resource_client.query(SummaryItem.class,condition, new SummaryGetCallback());

	}

	//コールバック リストに描画
	class SummaryGetCallback implements IPPQueryCallback<SummaryItem[]>{

		@Override
		public void ippDidError(int arg0) {
			Log.d(TAG,String.valueOf(arg0));

		}

		@Override
		public void ippDidFinishLoading(SummaryItem[] summary_item_array) {
			//ビューの準備
			ListView list_of_summary = (ListView) getSherlockActivity().findViewById(R.id.summary_list_others);

			//配列をリストに変換
			List<SummaryItem> summary_item_list = new ArrayList<SummaryItem>(Arrays.asList(summary_item_array));


			SummaryAdapter summary_adapter = new SummaryAdapter(getSherlockActivity(), summary_item_list, team_resource_id, role_self, ipp_auth_key);

			try{
				list_of_summary.setAdapter(summary_adapter);
			}catch(NullPointerException e){


			}
			try{
				wait_bar_summary.setVisibility(View.GONE);
			}catch (NullPointerException e){

			}
		}


	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	//オプションメニューの追加
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);


		//IPPログイン
		MenuItem ipp_login = menu.add(0, ipp_login_menu_id, Menu.NONE, getString(R.string.ipp_login));
		ipp_login.setIcon(android.R.drawable.ic_menu_preferences);
		ipp_login.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//リロード
		MenuItem reload = menu.add(0, reload_menu_id, Menu.NONE, getString(R.string.comment_reload));
		reload.setIcon(android.R.drawable.ic_menu_preferences);
		reload.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//追加読み込み
		MenuItem addget= menu.add(0, addget_menu_id, Menu.NONE, getString(R.string.additionalGet));
		addget.setIcon(android.R.drawable.ic_menu_preferences);
		addget.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);



	}

	//メニューを選んだ時の操作
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//IPPログイン
		if (item.getItemId() == ipp_login_menu_id) {
			Intent intent = new Intent(getSherlockActivity(), IPPLoginActivity.class);
			startActivityForResult(intent, MainActivity.REQUEST_IPP_LOGIN);
		}
		//リロード
		if (item.getItemId() == reload_menu_id) { //自分の家計簿のみ/他人の家計簿もをきりかえ
			showLogAndSummaryList(target_year,target_month,0);

		}


		//追加読み込み
		if(item.getItemId() == addget_menu_id){

			long until = last_timestamp;
			waitBar.setVisibility(View.VISIBLE);
			showLogAndSummaryList(target_year,target_month,until);
		}
		return false;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//表示する月の変更

	private void prepareChangeMonth(){
		this_month = (Button) getSherlockActivity().findViewById(R.id.summary_month);
		previous_month = (Button) getSherlockActivity().findViewById(R.id.previous_month);
		next_month = (Button) getSherlockActivity().findViewById(R.id.next_month);
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= 11) {
			makeDateOnlyPickerH();
		}else{
			makeDateOnlyPicker();
		}



		this_month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dateDialog.show();

			}
		});

		previous_month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				calendar.add(Calendar.MONTH, -1);
				this_month.setText(String.valueOf(calendar.get(Calendar.MONTH)+1)+"月");
				target_year = calendar.get(Calendar.YEAR);
				target_month = calendar.get(Calendar.MONTH);
				showLogAndSummaryList(target_year,target_month , 0);

			}
		});

		next_month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				calendar.add(Calendar.MONTH, 1);
				this_month.setText(String.valueOf(calendar.get(Calendar.MONTH)+1)+"月");
				target_year = calendar.get(Calendar.YEAR);
				target_month = calendar.get(Calendar.MONTH);
				showLogAndSummaryList(target_year,target_month , 0);

			}
		});

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void makeDateOnlyPickerH(){

		//月日選択ダイアログを作る
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		datePicker = new DatePicker(getSherlockActivity());

		datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
		int day_id = Resources.getSystem().getIdentifier("day", "id", "android");

		datePicker.findViewById(day_id).setVisibility(View.GONE);
		datePicker.setCalendarViewShown(false);

		dateDialog = builder.setView(datePicker)
		       .setTitle(getSherlockActivity().getApplicationContext().getString(R.string.select_month))
		       .setPositiveButton(android.R.string.ok,
		                          new DialogInterface.OnClickListener() {
		                              @Override
		                              public void onClick(DialogInterface d, int w) {
		                            	  int month_id = Resources.getSystem().getIdentifier("month", "id", "android");
		                            	  NumberPicker monthView = (NumberPicker) datePicker.findViewById(month_id);
		                            	  int year_id = Resources.getSystem().getIdentifier("year", "id", "android");
		                            	  NumberPicker yearView = (NumberPicker) datePicker.findViewById(year_id);
		                            	  this_month.setText(String.valueOf(monthView.getValue()+1)+"月");
		                            	  calendar.set(yearView.getValue(), monthView.getValue(), 1, 0, 0, 0);
			                      			target_year = calendar.get(Calendar.YEAR);
			                    			target_month = calendar.get(Calendar.MONTH);
			                    			showLogAndSummaryList(target_year,target_month , 0);

		                              }
		                          })
		       .setNegativeButton(android.R.string.cancel, null)
		       .create();
	}

	private void makeDateOnlyPicker(){

		//月日選択ダイアログを作る
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		datePicker = new DatePicker(getSherlockActivity());

		datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
		int day_id = Resources.getSystem().getIdentifier("day", "id", "android");
		datePicker.findViewById(day_id).setVisibility(View.GONE);
		dateDialog = builder.setView(datePicker)
		       .setTitle(getSherlockActivity().getApplicationContext().getString(R.string.select_month))
		       .setPositiveButton(android.R.string.ok,
		                          new DialogInterface.OnClickListener() {
		                              @Override
		                              public void onClick(DialogInterface d, int w) {
		                            	  previous_month.setText(datePicker.getMonth());
		                            	  calendar.set(datePicker.getYear(), datePicker.getMonth(), 1, 0, 0, 0);
		                            		target_year = calendar.get(Calendar.YEAR);
			                    			target_month = calendar.get(Calendar.MONTH);
			                    			showLogAndSummaryList(target_year,target_month , 0);

		                              }
		                          })
		       .setNegativeButton(android.R.string.cancel, null)
		       .create();
	}

	//ログイン用アクティビティから戻ってくるデータを保存
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);


		 if ((requestCode == MainActivity.REQUEST_IPP_LOGIN) && (resultCode == 200)){
		      startActivity(new Intent(getSherlockActivity(), MainActivity.class));
		  }
	}



}