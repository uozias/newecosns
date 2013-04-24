package com.example.newecosns;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.newecosns.R;
import com.example.newecosns.cohesive.SummaryAdapter;
import com.example.newecosns.models.LogItem;
import com.example.newecosns.models.PEBItem;
import com.example.newecosns.models.SummaryItem;
import com.example.newecosns.utnils.LogSQLPraparation;
import com.example.newecosns.utnils.PEBListAccessor;


public class SummaryFragment extends SherlockFragment  {
	//リソース
	Resources res = null;

	//デバッグ用タグ
	String TAG = "summaryFragment";

	//ビュー操作用
	View view =null;
	Button input_log_btn = null;

	//家計簿入力画面(inputLogActivity)に遷移するとき渡す登録モード
	static final int REQUEST_CODE_NEW = 1;
	static final int REQUEST_CODE_EDIT= 2;

	//内部DB操作用
	SQLiteDatabase sdb = null;
	SQLiteDatabase sdb_s = null;
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

	//ストレス状態 MainActivityが操作する
	public String stress_now;

	//UI
	ProgressBar wait_bar = null;
	ProgressBar wait_bar_summary = null;

	//オプションメニューのid
	private int ipp_login_menu_id = 1;

	//PEBリスト
	private HashMap<String, PEBItem> peb_list = null;


	//月変更用ボタン
	Button this_month = null;
	Button next_month = null;
	Button previous_month = null;

	Calendar calendar = null;

	private AlertDialog dateDialog = null;
	DatePicker datePicker = null;

	//まとめ
	double sum_of_money = 0;
	double sum_of_co2 = 0;
	int sum_of_number = 0;
	int sum_of_price = 0;

	//今表示してる内容 TODO
	int target_year = 0;
	int target_month = 0;
	long last_timestamp = 0;

	//家計簿入力への遷移
	public final static  int  REQUEST_INPUT_LOG = 4;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//本フラグメントを表示する
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View  view = inflater.inflate(R.layout.fragment_summary, container, false);
        return  view;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onStart(){
		super.onStart();
		this.getSherlockActivity().supportInvalidateOptionsMenu();





		wait_bar = (ProgressBar) getSherlockActivity().findViewById(R.id.waitBarInLogList);
		wait_bar_summary = (ProgressBar) getSherlockActivity().findViewById(R.id.waitBarInSummaryList);

		calendar = Calendar.getInstance();

		//PEBリスト準備
		prepareTypeList();

		//IPPログインチェック
		ippLoginCheck();

		//登録ずみ家計簿リスティング
		showLogAndSummaryList(0,0);

        // 親Activityのメニュー管理用変数を切り替える
        switch (MainActivity.mMenuType) {
        case MainActivity.CommentFragmentMenu:
        	MainActivity.mMenuType = MainActivity.SummaryFragmentMenu;
            break;
        default:
            break;
        }

		// 登録ボタンクリック処理(家計簿入力画面へ)
		prepareGoToInputLog();


		//表示する月の変更
		prepareChangeMonth();





	}

	@Override
	public void onStop(){
		super.onStop();

	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		int i = 1; //debug

	}







/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 登録ボタンクリック処理(家計簿入力画面へ)
	private void prepareGoToInputLog(){


			input_log_btn = (Button)getActivity().findViewById(R.id.inputLogBtn);
			input_log_btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					//家計簿入力画面へ遷移
					Intent intent = new Intent(getActivity(), InputLogActivity.class);
					//startActivityForResult(intent, REQUEST_INPUT_LOG);
					startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			});
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void ippLoginCheck(){


		/*
		 * MainActivityからIPPログイン情報を受け取る
		 */
		ipp_pref = getSherlockActivity().getSharedPreferences("IPP", Context.MODE_PRIVATE);
		ipp_auth_key = ipp_pref.getString("ipp_auth_key", "");
		ipp_id_string = ipp_pref.getString("ipp_id_string", "");
		ipp_pass_string = ipp_pref.getString("ipp_pass","");
		ipp_screen_name  = ipp_pref.getString("ipp_screen_name", "");
		team_resource_id = ipp_pref.getString("team_resource_id", "");
		role_self = ipp_pref.getInt("role_self", 0);

		//auth_keyがなければ
		if(ipp_auth_key.equals("")){
			//IPPログインに飛ばす
			startActivityForResult(new Intent(getSherlockActivity(), IPPLoginActivity.class), MainActivity.REQUEST_IPP_LOGIN);
		}

	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//登録ずみ家計簿リスティング関数


	private void showLogAndSummaryList(int target_year, int target_month){
		showLogList(target_year, target_month);
		showSummaryList();

	}

	private void showLogList(int target_year, int target_month){


		/*
         * 内部DB処理準備
         */
		LogSQLPraparation sdb_prapare = new LogSQLPraparation(getActivity().getApplicationContext());
		sdb =sdb_prapare.getdb();


		if(sdb != null){

			/*
	         * 個々の家計簿ログ表示
	         */

			sum_of_number = 0;
			sum_of_co2 = 0;
			sum_of_money = 0;
			sum_of_price = 0;

			//ビューの準備
			List<LogItem> LogItemList = new ArrayList<LogItem>();
			ListView listView = (ListView) getActivity().findViewById(R.id.log_list);//自分で用意したListView



			//読みだすデータの日時の範囲を指定
			String sqlstr = null;
			String until = null;
			String since = null;

			int year = 0;
			int month = 0;

			if(target_month == 0 && target_year == 0){
				//今月1日のUNIXタイムスタンプ(ミリ秒)を取得
				year = calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH);
				calendar.set(year, month, 1, 0, 0, 0);

				 since = String.valueOf(calendar.getTimeInMillis());
				 until = String.valueOf(System.currentTimeMillis());

			}else{//月と日を指定されたら
				year = target_year;
				calendar.set(year, target_month, 1, 0, 0, 0); //指定月の1日
				since = String.valueOf(calendar.getTimeInMillis());
				calendar.add(Calendar.MONTH, 1);
				 until = String.valueOf(calendar.getTimeInMillis());
				 calendar.add(Calendar.MONTH, -1); //戻しとく

			}
			sqlstr = "select _id, created, category, name, place, money, peb_id, co2, picture, timestamp, price from log where timestamp < "+ until + " and timestamp > "+ since +" order by timestamp DESC";
			//sqlstr = "select _id, created, category, name, place, money, peb_id, co2, picture, timestamp from log ";

			//自分のデータは常に内部DB空読み出し
			Cursor c = sdb.rawQuery(sqlstr, null);
			c.moveToFirst();



			CharSequence[] list = new CharSequence[c.getCount()];
			if(list.length > 0){
				for (int i = 0; i < list.length; i++) {
					//DB結果からモデルに
					LogItem item = new LogItem();

					item.setId_in_user(Integer.parseInt(c.getString(0)));
					item.setCreated(c.getString(1));
					item.setCategory(c.getString(2));
					item.setName(c.getString(3));
					item.setPlace(c.getString(4));
					item.setMoney(Double.parseDouble(c.getString(5)));

					sum_of_money = sum_of_money + item.getMoney();

					item.setPeb_id(c.getInt(6));
					item.setCo2(Double.parseDouble(c.getString(7)));


					sum_of_co2 = sum_of_co2 + item.getCo2();


					item.setPicture(c.getBlob(8));
					item.setTimestamp(c.getLong(9));

					item.setPrice(c.getInt(10));

					sum_of_price = sum_of_price  + item.getPrice();

					LogItemList.add(item);
					c.moveToNext();
				}

			}
			sum_of_number = list.length;


			c.close();
			sdb_prapare.closeDb();

			//データをアダプタにセットして表示
			ListAdapter adapter = new LogAdapter(getActivity().getApplicationContext(), LogItemList,  R.layout.row_log);
			try{
				listView.setAdapter(adapter);

			} catch (NullPointerException e){
				Log.d(TAG, "writing log list error");
			}finally{
				//プログレスバー隠す
				wait_bar.setVisibility(View.GONE);

			}

			sdb.close();

			//リストクリック処理(家計簿編集)
			//listView.setOnItemClickListener(new  LogClickListener());//とりえあずなし

		}

	  }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//リストクリック処理(家計簿編集)

	class LogClickListener implements AdapterView.OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//リストにある情報を取得
			ListView listView = (ListView) parent;
			LogItem item = (LogItem) listView.getItemAtPosition(position);

			//情報付きでinputLogActivityにおくる
			//家計簿入力画面へ遷移
			Intent intent = new Intent(getActivity(), InputLogActivity.class);
			//リスト中の行からデータを取得
			//todo オリジナルのリストにしてパース
			intent.putExtra("created",item.getCreated()); //注意 ここにはいっているのはlong型じゃない

			intent.putExtra("category",item.getCategory());
			intent.putExtra("name",item.getName());
			intent.putExtra("place",item.getPlace());

			intent.putExtra("money",item.getMoney());
			intent.putExtra("peb_id", item.getPeb_id());
			intent.putExtra("co2",item.getCo2());
			intent.putExtra("price",item.getPrice());

			intent.putExtra("id_in_user",item.getId_in_user());

			intent.putExtra("picture",item.getPicture());


			startActivityForResult(intent, REQUEST_CODE_EDIT);
			//アニメーション
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
	}



	private void showSummaryList(){
		//まとめようリストビューにデータ流しこむ
		ListView list_of_summary = (ListView) getSherlockActivity().findViewById(R.id.summary_list);

		SummaryItem summary_item = new SummaryItem();
		summary_item.setTimestamp(calendar.getTimeInMillis());
		summary_item.setCo2(sum_of_co2);
		summary_item.setMoney(sum_of_money);
		summary_item.setNumber(sum_of_number);
		summary_item.setPrice(sum_of_price);
		summary_item.setScreen_name(ipp_screen_name);

		List<SummaryItem> summary_items = new ArrayList<SummaryItem>();
		summary_items.add(summary_item);

		SummaryAdapter summary_adapter = new SummaryAdapter(getSherlockActivity().getApplicationContext(), summary_items);

		try{
			list_of_summary.setAdapter(summary_adapter);
		}catch(NullPointerException e){
			Log.d(TAG,e.toString());

		}
		wait_bar_summary.setVisibility(View.GONE);
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//オプションメニューの追加
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){


		super.onCreateOptionsMenu(menu, inflater);
		//IPPログイン
		MenuItem ipp_login = menu.add(0, ipp_login_menu_id, Menu.NONE, getString(R.string.ipp_login));
		ipp_login.setIcon(android.R.drawable.ic_menu_preferences);
		ipp_login.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

	}

		//メニューを選んだ時の操作
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		//IPPログイン
		if(item.getItemId() == ipp_login_menu_id){
			Intent intent = new Intent(getSherlockActivity(), IPPLoginActivity.class);
			startActivityForResult(intent, MainActivity.REQUEST_IPP_LOGIN);
			//スクリーンネーム消す
			//TextView result = (TextView) getSherlockActivity().findViewById(R.id.screen_name); //debug
			//result.setText("");
		}



		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		//家計簿入力画面から帰ってきたら


		//登録済み家計簿リスティング

		if ((requestCode == MainActivity.REQUEST_IPP_LOGIN) && (resultCode == 200)){
		      startActivity(new Intent(getSherlockActivity(), MainActivity.class));
		  }





	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
    			showLogAndSummaryList(target_year,target_month );


			}
		});

		next_month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				calendar.add(Calendar.MONTH, 1);
				this_month.setText(String.valueOf(calendar.get(Calendar.MONTH)+1)+"月");
	    		target_year = calendar.get(Calendar.YEAR);
    			target_month = calendar.get(Calendar.MONTH);
    			showLogAndSummaryList(target_year,target_month );


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
		                        			showLogAndSummaryList(target_year,target_month );


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
	                        			showLogAndSummaryList(target_year,target_month );


		                              }
		                          })
		       .setNegativeButton(android.R.string.cancel, null)
		       .create();
	}



}
