package com.example.newecosns.cohesive;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import jp.innovationplus.ipp.client.IPPGeoResourceClient;
import jp.innovationplus.ipp.core.IPPQueryCallback;
import jp.innovationplus.ipp.jsontype.IPPGeoLocation;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import twitter4j.Status;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.newecosns.IPPLoginActivity;
import com.example.newecosns.MainActivity;
import com.example.newecosns.OAuthActivity;
import com.example.newecosns.R;
import com.example.newecosns.geomodel.CommentGeoResource;
import com.example.newecosns.models.CommentItem;
import com.example.newecosns.models.PairItem;
import com.example.newecosns.models.StressItem;
import com.example.newecosns.utnils.Constants;
import com.example.newecosns.utnils.NetworkManager;
import com.example.newecosns.utnils.PublicResourceComparatorInverse;
import com.example.newecosns.utnils.StarCallback;
import com.example.newecosns.utnils.TwLoaderCallbacks;

public class CommentFragment extends SherlockFragment implements TwLoaderCallbacks,  LocationListener  {
	static final String CALLBACK = "http://sns.uozias.jp";
	static final String CONSUMER_KEY = "AJOoyPGkkIRBgmjAtVNw";
	static final String CONSUMER_SECRET = "1OMzUfMcqy4QHkyT6jJoUyxN4KXEu7R87k3bVOzp8c";
	static final int REQUEST_OAUTH = 1;
	static final String hash_tag = "ecosns_test";
	private static long user_id = 0L;
	private static String screen_name = null;
	private static String token = null;
	private static String token_secret = null;


	public String stress_now = StressItem.DEFAULT;


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

	//メニューid
	private int tw_login_menu_id = 1;
	private int ipp_login_menu_id = 2;
	private int reload_menu_id = 3;
	private int stress_change_menu_id = 4;
	private int addget_menu_id = 5;
	private int near_menu_id = 6;

	MenuItem  nearMenu  = null;
	//twitter OAuthデータ保存用
	SharedPreferences pref;
	SharedPreferences.Editor editor;

	Button twBtn = null;
	Button updateBtn = null;
	CheckBox tweetCheckBox = null;
	ProgressBar waitBar =null;



	//今表示してる内容
	int target_year = 0;
	int target_month = 0;
	long last_timestamp = 0;

	private long since;
	private long until;

	private int near = 0;

    ListView listView = null;
    Calendar calendar = null;
	//月変更用ボタン
	Button this_month = null;
	Button next_month = null;
	Button previous_month = null;

	EditText twTx = null;
	CohesiveCommentAdapter adapter = null;


	private AlertDialog dateDialog = null;
	DatePicker datePicker = null;

	//ロケーション関連
		private LocationManager mLocationManager = null;
		private Location mNowLocation = null;


	private String TAG = "CommentFragment";

	//入力チェック
	private boolean user_inputted = false;


	//検索範囲
	private int radiusSquare = 5000;//単位メートル?


	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View  view = inflater.inflate(R.layout.fragment_comment, container, false);
        return view;

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onStart() {
		super.onStart();

		listView = (ListView) getSherlockActivity().findViewById(R.id.comment_list);//自分で用意したListView
		waitBar = (ProgressBar) getSherlockActivity().findViewById(R.id.ProgressBarInCommentList);

		calendar = Calendar.getInstance();

		adapter = new CohesiveCommentAdapter(getSherlockActivity(), new ArrayList<CommentItem>(), team_resource_id, role_self);

		//IPPログインチェック
		ippLoginCheck();




	     //位置情報が利用できるか否かをチェック
		prepareLocation();


		//コメント投稿ボタンを押したら
		prapareCommentSend();


		//ツイートする チェックボックスをタップしたら
		/*
		tweetCheckBox = (CheckBox) getSherlockActivity().findViewById(R.id.check_tweet);
		tweetCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (tweetCheckBox.isChecked()) { //「ツイートする」にチェックが入っていたら
					//ログインチェックとログイン画面への自動遷移
					checkLoginTwitter();
				}
			}
		});
		*/

		prepareChangeMonth();

		//アクションバー右上のメニュー切り替え
		switch (MainActivity.mMenuType) {
		case MainActivity.SummaryFragmentMenu:
			MainActivity.mMenuType = MainActivity.CommentFragmentMenu;
			break;
		default:
			break;
		}
		this.getSherlockActivity().supportInvalidateOptionsMenu();

		//IPPからコメント読み込む
		//更に読みこむ機能の実装
		showCommentList(target_year, target_month, 0, near);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);



	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//ippログイン☑
	private void ippLoginCheck(){
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


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//位置情報関連
	public void prepareLocation(){

		mLocationManager = (LocationManager) getSherlockActivity().getSystemService(Context.LOCATION_SERVICE);
		int counter = 0;
		List<String> providers = mLocationManager.getProviders(true);
		for (String provider : providers) {
			if (provider.equals(LocationManager.GPS_PROVIDER) || provider.equals(LocationManager.NETWORK_PROVIDER)) {
				counter++;
			}
		}
		if (counter == 0) {
			startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

		} else {
			//位置情報登録
			 //ローケーション取得条件の設定
	        Criteria criteria = new Criteria();
	        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
	        criteria.setPowerRequirement(Criteria.POWER_LOW);
	        criteria.setSpeedRequired(true);
	        criteria.setAltitudeRequired(false);
	        criteria.setBearingRequired(false);
	        criteria.setCostAllowed(false);


			//一番いい（？）位置情報を更新し続ける
	        final String bestProvider = mLocationManager.getBestProvider(criteria, true);
	        mNowLocation = mLocationManager.getLastKnownLocation(bestProvider);
	        mLocationManager.requestLocationUpdates(bestProvider, 1000, 1, this);

		}
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//コメント投稿ボタンのイベント
	private void prapareCommentSend(){


		//入力欄にフォーカスすると内容はきえる
		twTx = (EditText) getSherlockActivity().findViewById(R.id.editText);
		twTx.setOnFocusChangeListener( new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Resources res = getResources();
				if(((TextView) v).getText().toString().equals(res.getText(R.string.instruction_input_comment))){
					((TextView) v).setText("");
					user_inputted = true;
				}


			}
		});
		/*)
		twTx.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((TextView) v).setText("");
			}
		});
		*/

		twBtn = (Button) getSherlockActivity().findViewById(R.id.tweetBtn);
		twBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.tweetBtn) {

					String tweetContent = twTx.getText().toString();
					if (tweetContent.length() == 0 || user_inputted == false) {
						Toast toast2 = Toast.makeText(getSherlockActivity().getApplicationContext(), "文を入力して下さい。",
								Toast.LENGTH_LONG);
						toast2.show();
						return;
					}else{
						/*
						if (tweetCheckBox.isChecked()) { //「ツイートする」にチェックが入っていたら
							//ログインチェックとログイン画面への自動遷移
							checkLoginTwitter();

							//ツイート機能
							if (tweetContent.length() > 140) {
								Toast toast = Toast.makeText(getSherlockActivity().getApplicationContext(), "長過ぎます。",
										Toast.LENGTH_LONG);
								toast.show();
							} else {

								Bundle args = new Bundle(1); //onCreateLoaderに渡したい値はここへ
								args.putString("tweetContent", tweetContent);
								//getSherlockActivity().getSupportLoaderManager().initLoader(0, args, CommentFragment.this);　 //こっちは自分にローダーを実装する場合

								TweetAsyncLoaderClass tweet_async_loader_class = new TweetAsyncLoaderClass(CommentFragment.this, token, token_secret, getSherlockActivity().getApplicationContext());
								getLoaderManager().restartLoader(0, args, tweet_async_loader_class);
							}
						}
						*/
					}

					//IPPへコメント投稿
					CommentItem commentItem = new CommentItem();

					//スター
					commentItem.setStar(0);

					//コメント内容
					commentItem.setCommentText(tweetContent);

					//現在の日時
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					commentItem.setCommentCreated(timestamp.toString());
					commentItem.setTimestamp(timestamp.getTime());

					//ユーザ名(IPP上の)
					commentItem.setCommentUserName(ipp_id_string);

					//スクリーンネーム
					commentItem.setCommentScreenName(ipp_screen_name);

					//緯度経度精度
					//commentItem.setLongitude(mNowLocation.getLongitude());
					//commentItem.setLatitude(mNowLocation.getLatitude());
					//commentItem.setAccuracy(mNowLocation.getAccuracy());
					//commentItem.setProvider(mNowLocation.getProvider());

					//チームid
					commentItem.setTeam_resource_id(team_resource_id);
					commentItem.setPair_common_id(CommentFragment.this.pair_common_id);

					//返信
					TextView comment_parent_id_new = (TextView) CommentFragment.this.getSherlockActivity().findViewById(R.id.CommentParentIdNew);
					commentItem.setCommentParentResourceId(comment_parent_id_new.getText().toString());

					//送信
					//IPPApplicationResourceClient client = new IPPApplicationResourceClient(getSherlockActivity());
					//client.setAuthKey(ipp_auth_key);
					//client.setDebugMessage(true);

					//client.create(CommentItem.class, commentItem, new SendCommentCallback());


					//位置情報用リソース
					IPPGeoLocation geo_location = new IPPGeoLocation();
					geo_location.setLongitude(mNowLocation.getLongitude());
					geo_location.setLatitude(mNowLocation.getLatitude());

					//testdata
					//geo_location.setLongitude(141.157322);
					//geo_location.setLatitude(43.143333);

					geo_location.setAccuracy(mNowLocation.getAccuracy());
					geo_location.setTimestamp(mNowLocation.getTime());

					geo_location.setProvider(mNowLocation.getProvider());


					List<IPPGeoLocation> geoLocations = new ArrayList();
					geoLocations.add(geo_location) ;

					CommentGeoResource resource= new CommentGeoResource();
					resource.setResource(commentItem);
					resource.setGeolocations(geoLocations);

					IPPGeoResourceClient client = new IPPGeoResourceClient(getSherlockActivity().getApplicationContext());
					client.setAuthKey(ipp_auth_key);
					client.setDebugMessage(true);
					client.create(CommentGeoResource.class, resource, new SendCommentCallback());




					}


			}

		});
	}

	//コメント送信後のコールバック
		public class SendCommentCallback implements IPPQueryCallback<String> {

			@Override
			public void ippDidError(int arg0) {
				Toast.makeText(getSherlockActivity().getApplicationContext(), "コメント投稿失敗", Toast.LENGTH_LONG).show();

			}

			@Override
			public void ippDidFinishLoading(String resource_id) {
				Toast.makeText(getSherlockActivity().getApplicationContext(), "コメント投稿成功", Toast.LENGTH_LONG).show();
				//ソフトキー外す
				InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(twTx.getWindowToken(), 0);
				((TextView) getSherlockActivity().findViewById(R.id.editText)).setText("");

				//GEO Resourceを使うのでいらない
				/*
				IPPGeoLocation geo_location = new IPPGeoLocation();
				geo_location.setLongitude(mNowLocation.getLongitude());
				geo_location.setLatitude(mNowLocation.getLatitude());
				geo_location.setAccuracy(mNowLocation.getAccuracy());
				geo_location.setTimestamp(mNowLocation.getTime());

				geo_location.setProvider(mNowLocation.getProvider());
				geo_location.setResource_id(resource_id);


				IPPGeoLocationClient geo_location_client = new IPPGeoLocationClient(getSherlockActivity());
				geo_location_client.setAuthKey(ipp_auth_key);
				geo_location_client.create(geo_location, new geoPostCallback());
				*/

				showCommentList(target_year, target_month, 0, near);

			}

		}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//ログインチェック
	public void checkLoginTwitter() {
		pref = getSherlockActivity().getSharedPreferences("t4jdata", Context.MODE_PRIVATE);
		token = pref.getString("token", "");
		token_secret = pref.getString("token_secret", "");
		screen_name = pref.getString("screen_name", "");
		user_id = pref.getLong("user_id", 0L);
		if (token.length() == 0) { //もし未認証だったら
			Intent intent = new Intent(getSherlockActivity(), OAuthActivity.class);
			intent.putExtra(OAuthActivity.CALLBACK, CALLBACK);
			intent.putExtra(OAuthActivity.CONSUMER_KEY, CONSUMER_KEY);
			intent.putExtra(OAuthActivity.CONSUMER_SECRET, CONSUMER_SECRET);
			startActivityForResult(intent, REQUEST_OAUTH);
		}

	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//ログイン用アクティビティから戻ってくるデータを保存
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_OAUTH && resultCode == 200) {
			user_id = data.getLongExtra(OAuthActivity.USER_ID, 0);
			screen_name = data.getStringExtra(OAuthActivity.SCREEN_NAME);

			token = data.getStringExtra(OAuthActivity.TOKEN);
			token_secret = data.getStringExtra(OAuthActivity.TOKEN_SECRET);
			//認証データ保存
			editor = pref.edit();
			editor.putString("token", token);
			editor.putString("token_secret", token_secret);
			editor.putString("screen_name", screen_name);
			editor.putLong("user_id", user_id);

			editor.commit();

		}

		 if ((requestCode == MainActivity.REQUEST_IPP_LOGIN) && (resultCode == 200)){
		      startActivity(new Intent(getSherlockActivity(), MainActivity.class));
		  }
	}





///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



///////////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//オプションメニューの追加
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);

		/*
		//ツイッターログイン
		MenuItem tw_login = menu.add(0, tw_login_menu_id, Menu.NONE, getString(R.string.tw_setting));
		tw_login.setIcon(android.R.drawable.ic_menu_preferences);
		tw_login.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	*/
		//IPPログイン
		MenuItem ipp_login = menu.add(0, ipp_login_menu_id, Menu.NONE, getString(R.string.ipp_login));
		ipp_login.setIcon(android.R.drawable.ic_menu_preferences);
		ipp_login.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//リロード
		MenuItem reload = menu.add(0, reload_menu_id, Menu.NONE, getString(R.string.comment_reload));
		reload.setIcon(android.R.drawable.ic_menu_preferences);
		reload.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//追加読み込み
		MenuItem addget = menu.add(0, addget_menu_id, Menu.NONE, getString(R.string.additionalGet));
		addget.setIcon(android.R.drawable.ic_menu_preferences);
		addget.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//ストレス変更
		/*
		MenuItem stress_change = menu.add(0, stress_change_menu_id, Menu.NONE, getString(R.string.stress_change));
		stress_change.setIcon(android.R.drawable.ic_menu_preferences);
		stress_change.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		*/

		//近くに限る
		nearMenu  = menu.add(0, near_menu_id, Menu.NONE, getString(R.string.near));
		nearMenu.setIcon(android.R.drawable.ic_menu_compass);
		//near.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); アクションバー上にアイコンで表示

		nearMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT); //ドロップダウンのメニューに表示

	}


	//メニューを選んだ時の操作
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		//ツイッターログイン
		if (item.getItemId() == tw_login_menu_id) {
			//ログアウト
			//認証データをカラに
			pref = getSherlockActivity().getSharedPreferences("t4jdata", Context.MODE_PRIVATE);
			editor = pref.edit();
			editor.putString("token","");
			editor.putString("token_secret","");
			editor.putString("screen_name","");
			checkLoginTwitter();
		}

		//IPPログイン
		if(item.getItemId() == ipp_login_menu_id){
			Intent intent = new Intent(getSherlockActivity(), IPPLoginActivity.class);
		    startActivityForResult(intent, MainActivity.REQUEST_IPP_LOGIN);
		    //スクリーンネーム消す
        	//TextView result = (TextView) getSherlockActivity().findViewById(R.id.screen_name); //debug
    		//result.setText("");
		}

		//リロード
		if (item.getItemId() == reload_menu_id) {
			showCommentList(target_year, target_month, 0, near);

		}


		//追加読み込み
		if(item.getItemId() == addget_menu_id){

			long until = last_timestamp;
			try{
				waitBar.setVisibility(View.VISIBLE);
			}catch (NullPointerException e){

			}
			showCommentList(target_year, target_month, until, near);
		}

		//ストレス変更
		/*

		if(item.getItemId() == stress_change_menu_id){

			//メインアクティビティに教えてやる
			 Intent intent = new Intent(getSherlockActivity(), MainActivity.class);
			 intent.putExtra("stress_now", MainActivity.RELAXED); //このフラグメントは密な方なので、ゆるい方にしか変わらない
			 startActivity(intent);

		}
		*/

		//近くに限る
		if(item.getItemId() == near_menu_id){
			if(near == 0){
				near =1 ;
				showCommentList(target_year, target_month, 0, near);
				nearMenu.setTitle(R.string.near_cancel);
			}else{
				near =0;
				showCommentList(target_year, target_month, 0, near);
				nearMenu.setTitle(R.string.near);
			}

		}

		return false;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//IPPからコメント読み込むメソッド
	public void showCommentList(int target_year, int target_month, long until, int near) {
		//オンラインなら外部DBから他人のデータ読み出し//
		try{
			waitBar.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}catch (NullPointerException e){
			Log.d(TAG,e.toString());
		}

		if(NetworkManager.isConnected(getActivity().getApplicationContext())){
			adapter.clear();



			//読みだすデータの日時の範囲を指定
			int year = 0;
			int month = 0;
			long mUntil = 0;
			this.until = until;


			if(target_month == 0 && target_year == 0){
				//今月1日のUNIXタイムスタンプ(ミリ秒)を取得
				year = calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH);
				calendar.set(year, month, 1, 0, 0, 0);

				since = calendar.getTimeInMillis(); //今月頭から

				if(until == 0){
					mUntil = System.currentTimeMillis(); //現在まで

				}else{
					mUntil = until;
				}



			}else{//月と日を指定されたら

				year = target_year;
				calendar.set(year, target_month, 1, 0, 0, 0); //指定月の1日から
				since = calendar.getTimeInMillis();

				if(until == 0){
					calendar.add(Calendar.MONTH, 1);
					mUntil = calendar.getTimeInMillis(); //次の月の1日まで
					calendar.add(Calendar.MONTH, -1); //戻しとく

				}else{
					mUntil = until;
				}


			}

			IPPGeoResourceClient client = new IPPGeoResourceClient(getSherlockActivity().getApplicationContext()) ;
			client.setAuthKey(ipp_auth_key);


			jp.innovationplus.ipp.client.IPPGeoResourceClient.QueryCondition condition = new jp.innovationplus.ipp.client.IPPGeoResourceClient.QueryCondition();

			condition.setSince(since);
			condition.setUntil(mUntil);
			condition.setCount(10);
			condition.eq("pair_common_id",pair_common_id);

			if(near == 1){
				condition.setBoundByRadiusSquare(mNowLocation.getLatitude(), mNowLocation.getLongitude(), radiusSquare);

			}

			condition.build();
			client.query(CommentGeoResource.class ,condition, new CommentGeoGetCallback());


		}


	}
	public class CommentGeoGetCallback implements IPPQueryCallback<CommentGeoResource[]> {



		@Override
		public void ippDidError(int paramInt) {
			Toast.makeText(CommentFragment.this.getActivity(), "コメント読み込みエラー"+paramInt, Toast.LENGTH_LONG).show();
		}

		@Override
		public void ippDidFinishLoading(CommentGeoResource[] comment_geo_item_array) {
			//IPPプラットフォームから取得したコメントのリスト
			//そのままだと配列なので、リストにする


			if(comment_geo_item_array.length != 0 ){
				last_timestamp = comment_geo_item_array[comment_geo_item_array.length -1].getResource().getTimestamp() -1; //最後の要素のタイムスタンプを得る


				List<CommentItem> CommentItemList = new ArrayList<CommentItem>();
				for (int i = 0; i < comment_geo_item_array.length ; i++){

					CommentItemList.add(comment_geo_item_array[i].getResource());
				}

				Collections.sort(CommentItemList, new PublicResourceComparatorInverse());

				if(until == 0){ //初期よみこみ
					for(CommentItem commentItem : CommentItemList){

						adapter.add(commentItem);
					}




					try{
						listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
						listView.setAdapter(adapter);
						//終わったら成功のトースト出す
						Toast toast = Toast.makeText(getSherlockActivity(), "コメント読み込み成功", Toast.LENGTH_LONG);
						toast.show();

					} catch (NullPointerException e){
						Log.d("BACK GROUND", "writing timeline error");
					}

				}else{ //追加読み込み


					for(CommentItem commentItem : CommentItemList){

						adapter.add(commentItem);
					}


				}

			}



			//プログレスバー隠す
			try{
				waitBar.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}catch (NullPointerException e){

			}
		}



	}






/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//コメント取得コールバック
//TODO 使ってない？
	public class CommentGetCallback implements IPPQueryCallback<CommentItem[]> {



		@Override
		public void ippDidError(int paramInt) {
			Toast.makeText(CommentFragment.this.getActivity(), "コメント読み込みエラー"+paramInt, Toast.LENGTH_LONG).show();
		}

		@Override
		public void ippDidFinishLoading(CommentItem[] comment_item_array) {
			//IPPプラットフォームから取得したコメントのリスト
			//そのままだと配列なので、リストにする


			if(comment_item_array.length != 0 ){
				last_timestamp = comment_item_array[comment_item_array.length -1].getTimestamp() -1; //最後の要素のタイムスタンプを得る
				List<CommentItem> CommentItemList = new ArrayList<CommentItem>(Arrays.asList(comment_item_array));
				Collections.sort(CommentItemList, new PublicResourceComparatorInverse());

				if(until == 0){ //初期よみこみ
					for(CommentItem commentItem : CommentItemList){

						adapter.add(commentItem);
					}




					try{
						listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
						listView.setAdapter(adapter);
						//終わったら成功のトースト出す
						Toast toast = Toast.makeText(getSherlockActivity(), "コメント読み込み成功", Toast.LENGTH_LONG);
						toast.show();

					} catch (NullPointerException e){
						Log.d("BACK GROUND", "writing timeline error");
					}

				}else{ //追加読み込み


					for(CommentItem commentItem : CommentItemList){

						adapter.add(commentItem);
					}


				}

			}



			//プログレスバー隠す
			try{
				waitBar.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}catch (NullPointerException e){

			}
		}



	}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public class CohesiveCommentAdapter extends ArrayAdapter<CommentItem> {
		private LayoutInflater mInflater;
		private Context context;

		private Button replyBtn = null;
		private Button showConversationBtn  = null;


		private String team_resource_id = null;
		private int role_self = 0;

		private TextView number_of_star = null;
		private Button buton_evaluate_it = null;

		public CohesiveCommentAdapter(Context context, List<CommentItem> comments) {
			super(context, 0, comments);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = context;

		}

		public CohesiveCommentAdapter(Context context, List<CommentItem> comments,  String team_resource_id, int role_self) {
			super(context, 0, comments);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = context;
			this.team_resource_id  = team_resource_id;
			this.role_self = role_self;


		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {


			//データが入ってるオブジェクトをとる
			final CommentItem item = this.getItem(position);

			if(convertView == null){
				convertView = mInflater.inflate(R.layout.row_comment_cohesive, null);
			}




			//自分の今のroleを参照して、自分のチームは自分のロール、相手のチームは自分と対になるロールに設定する
			if(team_resource_id != null && role_self != 0){
				//自分側チームの時
				if(item.getTeam_resource_id().equals(team_resource_id)){
					switch(role_self){
						case Constants.KOHAI:
							convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_kouhai));
							break;
						case Constants.SENPAI:
							convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_sennpai));
							break;
						case Constants.RELAXED_ROLE:
							convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_comment_relaxed)); //リラックスのとき
							break;

					}
				//相手側チーム(というか自分のチーム以外)のとき
				}else{
					switch(role_self){
					case Constants.KOHAI:
						convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_sennpai));
						break;
					case Constants.SENPAI:
						convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_kouhai));
						break;
					case Constants.RELAXED_ROLE:
						convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_comment_relaxed)); //リラックスのとき
						break;

					}

				}
			}else{
				convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_sennpai));

			}



			//ビューに値をセット
			((TextView) convertView.findViewById(R.id.CommentScreenNameInList)).setText(item.getCommentScreenName());
			((TextView) convertView.findViewById(R.id.CommentTextInList)).setText(item.getCommentText());

			//日付は最初からストリング yyyy-MM-dd HH:mm:ss
			((TextView) convertView.findViewById(R.id.CommentCreatedInList)).setText(item.getCommentCreated());


			//resourceId, parentResourceIdは隠しビュー
			TextView resourceid = (TextView)convertView.findViewById(R.id.CommentResourceIdInList);
			resourceid.setText(item.getResource_id());
			resourceid.setVisibility(View.GONE);

			TextView parentresourceid = (TextView)convertView.findViewById(R.id.CommentParentResourceIdInList);
			parentresourceid.setText(item.getCommentParentResourceId());
			parentresourceid.setVisibility(View.GONE);



			//返信先表示
			showConversationBtn = (Button) convertView.findViewById(R.id.showConversation);
			if (parentresourceid.getText().length() != 0){

				showConversationBtn.setVisibility(View.VISIBLE);


				showConversationBtn.setOnClickListener(new GetConversationListener(item));






			}else{//会話を表示」メニュー
				showConversationBtn.setVisibility(View.GONE);
			}






			//返信ボタンをタップしたら
			replyBtn = (Button) convertView.findViewById(R.id.replyBtn);

			replyBtn.setOnClickListener(new setReplyTarget(item));




			//評価ボタン押す //スター
			buton_evaluate_it = (Button) convertView.findViewById(R.id.buton_evaluate_it);
			number_of_star = (TextView) convertView.findViewById(R.id.number_of_star);
			buton_evaluate_it.setOnClickListener(new StarCallback(item, ipp_auth_key, context, number_of_star));


			number_of_star.setText(String.valueOf(item.getStar()));




			return convertView;
		}

	}


	public class GetConversationListener implements View.OnClickListener{


		CommentItem item = null;


		public GetConversationListener (CommentItem item){
			this.item = item;

		}

		@Override
		public void onClick(View v) {
			//返信関係にあるコメントを全部とってきて表示するアクティビティを立ち上げる
			Intent intent = new Intent(getActivity(), RepliesActivity.class);
			intent.putExtra("resource_id", item.getResource_id());
			intent.putExtra("parent_resource_id", item.getCommentParentResourceId());
			intent.putExtra("stress_now", stress_now);
			startActivityForResult(intent,1);
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

		}



	}



	public class setReplyTarget implements View.OnClickListener {
		CommentItem item = null;


		public setReplyTarget(CommentItem item){
			this.item = item;
		}

		@Override
		public void onClick(View v) {

			//返信先の表示
			TextView reply_for = (TextView) CommentFragment.this.getSherlockActivity().findViewById(R.id.reply_for);
			reply_for.setText(item.getCommentScreenName()+"「" + item.getCommentText() + "」への返信");

			reply_for.setVisibility(View.VISIBLE);
			View list_element = (View) v.getParent().getParent();
			TextView comment_parent_id_new = (TextView) CommentFragment.this.getSherlockActivity().findViewById(R.id.CommentParentIdNew);
			CharSequence resource_id = ((TextView) list_element.findViewById(R.id.CommentResourceIdInList)).getText();

			//一度全要素を未選択に
			ViewGroup listView = (ViewGroup) list_element.getParent();
			for (int i = 0; i < listView.getChildCount() ; i++){
				listView.getChildAt(i).setSelected(false);
			}

			//今入力しているコメントの返信先を選択中のコメントに設定
			comment_parent_id_new.setText(resource_id);
			list_element.setSelected(true);
		}


	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//位置情報関連

	//位置更新時の処理
	@Override
	public void onLocationChanged(Location location) {
		mNowLocation = location;
	}

	@Override
	public void onProviderDisabled(String arg0) {
		//TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void onProviderEnabled(String arg0) {
		//TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void onStatusChanged(String arg0, int status, Bundle arg2) {
		switch (status) {
		case LocationProvider.AVAILABLE:
			break;
		case LocationProvider.OUT_OF_SERVICE:
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			break;
		}
	}

	//アプリを止めたら
	@Override
	public void onPause() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
		}
		super.onPause();
	}



	@Override
	public void TweetLoaderCallbacks(Status result, Loader<Status> paramLoader) {
		getLoaderManager().destroyLoader(paramLoader.getId());//ツイートしたら破棄
		//ソフトキー外す
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(twTx.getWindowToken(), 0);
		((TextView) getSherlockActivity().findViewById(R.id.editText)).setText("");



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
				showCommentList(target_year,target_month , 0, near);

			}
		});

		next_month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				calendar.add(Calendar.MONTH, 1);
				this_month.setText(String.valueOf(calendar.get(Calendar.MONTH)+1)+"月");
				target_year = calendar.get(Calendar.YEAR);
				target_month = calendar.get(Calendar.MONTH);
				showCommentList(target_year,target_month , 0, near);

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
			                    			showCommentList(target_year,target_month , 0, near);

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
			                    			showCommentList(target_year,target_month , 0, near);

		                              }
		                          })
		       .setNegativeButton(android.R.string.cancel, null)
		       .create();
	}



}




