package com.example.newecosns.relaxed;


import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import jp.innovationplus.ipp.client.IPPApplicationResourceClient;
import jp.innovationplus.ipp.client.IPPGeoLocationClient;
import jp.innovationplus.ipp.client.IPPGeoResourceClient;
import jp.innovationplus.ipp.client.IPPGeoResourceClient.QueryCondition;
import jp.innovationplus.ipp.core.IPPQueryCallback;
import jp.innovationplus.ipp.jsontype.IPPApplicationResource;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
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
import android.widget.ImageView;
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
import com.example.newecosns.geomodel.LogGeoResource;
import com.example.newecosns.models.CommentItem;
import com.example.newecosns.models.LogItem;
import com.example.newecosns.models.PairItem;
import com.example.newecosns.models.PictureItem;
import com.example.newecosns.models.StressItem;
import com.example.newecosns.utnils.ImageCache;
import com.example.newecosns.utnils.NetworkManager;
import com.example.newecosns.utnils.PublicResourceComparator;
import com.example.newecosns.utnils.TweetTaskLoader;

public class MixedTimelineFragment extends SherlockFragment implements LoaderCallbacks<Status>, LocationListener  {
	static final String CALLBACK = "http://sns.uozias.jp";
	static final String CONSUMER_KEY = "AJOoyPGkkIRBgmjAtVNw";
	static final String CONSUMER_SECRET = "1OMzUfMcqy4QHkyT6jJoUyxN4KXEu7R87k3bVOzp8c";
	static final int REQUEST_OAUTH = 1;
	static final String hash_tag = "ecosns_test";
	private static long user_id = 0L;
	private static String screen_name = null;
	private static String token = null;
	private static String token_secret = null;

	//ストレス状態 MainActivityが操作する
	public String stress_now = StressItem.RELAXED;


	//IPPユーザデータ
	private String ipp_auth_key;
	private String ipp_id_string;
	private String ipp_pass_string;
	SharedPreferences ipp_pref;
	SharedPreferences.Editor ipp_editor;
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



	//リスト読み込み用
	List<IPPApplicationResource> IPPPublicResourceList = null;

	List<IPPApplicationResource>  IPPPublicResourceListTmp  = null;

	ListView listView = null;
	TextView twTx = null;
	MixedAdapter adapter = null;

	//ロケーション関連
		private LocationManager mLocationManager = null;
		private Location mNowLocation = null;

	//写真表示用
	private HashMap<String, ImageView> pictureViewList = null;
	private HashMap<String, ProgressBar> progressBarList = null;

	//月変更用ボタン
	Button this_month = null;
	Button next_month = null;
	Button previous_month = null;
	Calendar calendar = null;

	private AlertDialog dateDialog = null;
	DatePicker datePicker = null;

	//今表示してる内容
	int target_year = 0;
	int target_month = 0;
	long last_timestamp = 0;

	private long since;
	private long until;


	private int near = 0;
	private int radiusSquare = 5000;

	CommentItem last_timestamp_holder_comment = null;
	LogItem last_timestamp_holder_log = null;

	Resources res = null;

	//入力チェック
	private boolean user_inputted = false;

	private String TAG = "MixedTimelineFragment";

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
		this.getSherlockActivity().supportInvalidateOptionsMenu();
		if( getSherlockActivity().getSupportActionBar().getSelectedTab().getPosition() == 0){
			//使いまわす変数の初期化
			initListUI();

			res = getResources();

			//IPPログインチェック
			ippLoginCheck();



		     //位置情報が利用できるか否かをチェック
			prepareLocation();

			//ツイートする チェックボックスをタップしたら
			 //prapereTweetCheck();


			//コメント投稿ボタンを押したら
			prapareCommentSend();

			prepareChangeMonth();

			//アクションバー右上のメニュー切り替え
			switch (MainActivity.mMenuType) {
			case MainActivity.SummaryFragmentMenu:
				MainActivity.mMenuType = MainActivity.CommentFragmentMenu;
				break;
			default:
				break;
			}



			((TextView) getSherlockActivity().findViewById(R.id.label_kouhai)).setVisibility(View.GONE);
			((TextView) getSherlockActivity().findViewById(R.id.label_senpai)).setVisibility(View.GONE);

			showMixedList(target_year, target_month, 0, near);

		}




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
	//ツイートする チェックボックスをタップしたら
	/*
	private void prapereTweetCheck(){

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


	}
	*/



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
			twTx = (TextView) getSherlockActivity().findViewById(R.id.editText);
			twTx.setOnFocusChangeListener( new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if(((TextView) v).getText().toString().equals(res.getText(R.string.instruction_input_comment))){
						((TextView) v).setText("");
						user_inputted = true;
					}


				}
			});


			twBtn = (Button) getSherlockActivity().findViewById(R.id.tweetBtn);
			twBtn.setText(res.getText(R.string.label_mutter_mixed));
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
									getSherlockActivity().getSupportLoaderManager().initLoader(0, args, MixedTimelineFragment.this);
								}
							}
							*/
						}

						//IPPへコメント投稿
						CommentItem commentItem = new CommentItem();
						commentItem.setCommentText(tweetContent);

						//現在の日時
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						commentItem.setCommentCreated(timestamp.toString());
						commentItem.setTimestamp(System.currentTimeMillis());

						//ユーザ名(IPP上の)
						commentItem.setCommentUserName(ipp_id_string);

						//スクリーンネーム
						commentItem.setCommentScreenName(ipp_screen_name);

						//緯度経度精度
						commentItem.setLongitude(mNowLocation.getLongitude());
						commentItem.setLatitude(mNowLocation.getLatitude());
						commentItem.setAccuracy(mNowLocation.getAccuracy());
						commentItem.setProvider(mNowLocation.getProvider());

						//チームid
						commentItem.setTeam_resource_id(team_resource_id);
						commentItem.setPair_common_id(pair_common_id);


						//返信
						TextView comment_parent_id_new = (TextView) MixedTimelineFragment.this.getSherlockActivity().findViewById(R.id.CommentParentIdNew);
						commentItem.setCommentParentResourceId(comment_parent_id_new.getText().toString());

						//送信
						IPPApplicationResourceClient client = new IPPApplicationResourceClient(getSherlockActivity());
						client.setAuthKey(ipp_auth_key);
						client.setDebugMessage(true);
						client.create(CommentItem.class, commentItem, new SendCommentCallback());
						}




					//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());//debug

				}

			});

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




	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//ツイート用コールバック

	@Override
	public Loader<Status> onCreateLoader(int arg0, Bundle arg1) {
		String tweetContent = arg1.getString("tweetContent");
		TweetTaskLoader loader = new TweetTaskLoader(getSherlockActivity().getApplicationContext(), tweetContent,
				token, token_secret);
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Status> loader, Status result) {
		Toast toast = null;
		if (result != null) {
			toast = Toast.makeText(getSherlockActivity().getApplicationContext(), "ツイート成功", Toast.LENGTH_LONG);
		} else {

			toast = Toast.makeText(getSherlockActivity().getApplicationContext(), "ツイート失敗", Toast.LENGTH_LONG);
		}
		getLoaderManager().destroyLoader(loader.getId());//ツイートしたら破棄
		//ソフトキー外す
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(twTx.getWindowToken(), 0);
		((TextView) getSherlockActivity().findViewById(R.id.editText)).setText("");


		toast.show();

	}

	@Override
	public void onLoaderReset(Loader<Status> arg0) {


	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

			showMixedList(target_year, target_month, 0, near);

		}

	}


///////////////////////////////////////////////////////////////////////////////////////////////////
//位置情報送信後のコールバック
class geoPostCallback implements IPPQueryCallback<String> {

@Override
	public void ippDidError(int arg0) {
		Log.d(TAG, getString(arg0));

	}


	@Override
	public void ippDidFinishLoading(String arg0) {
		Log.d(TAG, arg0);
	}
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//オプションメニューの追加
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);

		//ツイッターログイン
		/*
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
		 * 	MenuItem stress_change = menu.add(0, stress_change_menu_id, Menu.NONE, getString(R.string.stress_change));
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
		if (item.getItemId() == reload_menu_id) { //自分の家計簿のみ/他人の家計簿もをきりかえ
			showMixedList(target_year, target_month, 0, near);

		}


		//追加読み込み
		if(item.getItemId() == addget_menu_id){


			showMixedList(target_year, target_month, last_timestamp, near);
		}

		//ストレス変更
		/*
		 * 		if(item.getItemId() == stress_change_menu_id){

			//メインアクティビティに教えてやる
			 Intent intent = new Intent(getSherlockActivity(), MainActivity.class);
			 intent.putExtra("stress_now", MainActivity.COHESIVE); //このフラグメントはゆるい方なので、密な方にしか変わらない
			 startActivity(intent);

		}
		 */

		//近くに限る
		if(item.getItemId() == near_menu_id){
			if(near == 0){
				near =1 ;
				showMixedList(target_year, target_month, 0, near);
				nearMenu.setTitle(R.string.near_cancel);
			}else{
				near =0;
				showMixedList(target_year, target_month, 0, near);
				nearMenu.setTitle(R.string.near);
			}

		}


	return false;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//リスト関連のビューなどの初期化
	public void initListUI(){
		pictureViewList = new HashMap<String, ImageView>();
		progressBarList = new HashMap<String, ProgressBar>();

		waitBar = (ProgressBar) getSherlockActivity().findViewById(R.id.ProgressBarInCommentList);
		listView = (ListView) MixedTimelineFragment.this.getSherlockActivity().findViewById(R.id.comment_list);//自分で用意したListView //commentFragmtnの使い回し
		calendar = Calendar.getInstance();

		IPPPublicResourceList = new ArrayList<IPPApplicationResource>();
		adapter = new MixedAdapter(MixedTimelineFragment.this.getSherlockActivity().getApplicationContext(), IPPPublicResourceList);


		try{
			listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
			listView.setAdapter(adapter);
		} catch (NullPointerException e){
			Log.d(TAG, "writing list error");
		}
	}



//IPPからコメント読み込むメソッド
	public void showMixedList(int target_year, int target_month, long until, int near) {
		//オンラインなら外部DBから他人のデータ読み出し//
		try{
			waitBar.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}catch (NullPointerException e){
			Log.d(TAG,e.toString());
		}


		if(NetworkManager.isConnected(getActivity().getApplicationContext())){
			adapter.clear(); //リスト初期化





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

			//コメントの方
			IPPGeoResourceClient public_resource_client = new IPPGeoResourceClient(getActivity().getApplicationContext());
			public_resource_client.setAuthKey(ipp_auth_key);
			QueryCondition condition = new QueryCondition();
			condition.setCount(10);

			condition.setSince(since);
			condition.setUntil(mUntil);
			condition.eq("pair_common_id",pair_common_id);

			if(near == 1){
				condition.setBoundByRadiusSquare(mNowLocation.getLatitude(), mNowLocation.getLongitude(), radiusSquare);

			}


			public_resource_client.query(CommentGeoResource.class, condition, new CommentGetCallback(mUntil)); //最初




		}

	}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//コメント取得コールバック
	public class CommentGetCallback implements IPPQueryCallback<CommentGeoResource[]> {

		private long mUntil = 0;

		public CommentGetCallback(long until){
			mUntil = until;

		}


		@Override
		public void ippDidError(int paramInt) {
			Toast.makeText(MixedTimelineFragment.this.getActivity(), "コメント読み込みエラー"+paramInt, Toast.LENGTH_LONG).show();
		}

		@Override
		public void ippDidFinishLoading(CommentGeoResource[] comment_geo_item_array) {
			//ログを読み込む

			QueryCondition condition = new QueryCondition();

			//IPPプラットフォームから取得したコメントのリスト
			//そのままだと配列なので、リストにする
			IPPPublicResourceListTmp = new ArrayList<IPPApplicationResource>();
			if(comment_geo_item_array.length != 0){

				for(CommentGeoResource item: Arrays.asList(comment_geo_item_array)){
					IPPPublicResourceListTmp.add(item.getResource());
				}


				last_timestamp_holder_comment = comment_geo_item_array[comment_geo_item_array.length -1].getResource();
				last_timestamp = last_timestamp_holder_comment.getTimestamp() -1; //最後の要素のタイムスタンプを得る(1秒過去にしておく)

				condition.setSince(last_timestamp);//一番古いコメントの入力時刻から

			}else{
				//これ以上古いコメントはないとき
				condition.setSince(since); //月の初めから


			}

			IPPGeoResourceClient public_resource_client = new IPPGeoResourceClient(getActivity().getApplicationContext());

			public_resource_client.setAuthKey(ipp_auth_key);
			condition .setCount(10);

			condition.setUntil(mUntil); //コメント読み込み限界まで
			condition.eq("pair_common_id",pair_common_id);
			condition.build();

			if(near == 1){
				condition.setBoundByRadiusSquare(mNowLocation.getLatitude(), mNowLocation.getLongitude(), radiusSquare);
			}

			public_resource_client.query(LogGeoResource.class, condition, new LogGetCallback()); //最初のよみこみ


		}

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//IPPプラットフォーム上からログをを読み込むコールバック
	private class LogGetCallback implements IPPQueryCallback <LogGeoResource[]>{



		@Override
		public void ippDidFinishLoading(LogGeoResource[] log_geo_item_array) {

			if(log_geo_item_array.length != 0){
				for(LogGeoResource item : Arrays.asList(log_geo_item_array)){
					IPPPublicResourceListTmp.add(item.getResource());
				}


				if(last_timestamp > log_geo_item_array[log_geo_item_array.length -1].getResource().getTimestamp()){
					last_timestamp = log_geo_item_array[log_geo_item_array.length -1].getResource().getTimestamp() -1;
				}

			}


			Collections.sort(IPPPublicResourceListTmp, new PublicResourceComparator());
			for(IPPApplicationResource resource: IPPPublicResourceListTmp){
				adapter.add(resource);
			}



			//インジゲータ操作
			try{
				waitBar.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			} catch (NullPointerException e){
				Log.d(TAG, "writing list error");
			}


		}

		@Override
		public void ippDidError(int paramInt) {
			Toast.makeText(MixedTimelineFragment.this.getSherlockActivity(), "家計簿読み込みエラー"+paramInt, Toast.LENGTH_LONG).show();

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


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//コメント・家計簿混在表示用表示用アダプター
	class MixedAdapter extends ArrayAdapter<IPPApplicationResource> {
		private LayoutInflater mInflater;
		private String stress_now;


		public MixedAdapter(Context context, List<IPPApplicationResource> list) {
			super(context, 0, list);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {


		//データが入ってるオブジェクトをとる
		final IPPApplicationResource item0 = this.getItem(position);


		if (stress_now == null) {
			stress_now = StressItem.RELAXED;

		}
		if (item0.getClass() == LogItem.class){
			convertView = mInflater.inflate(R.layout.row_log_relaxed, null);
			LogItem item = (LogItem)item0;
			if(item != null){
				//ビューに値をセット
				((TextView)convertView.findViewById(R.id.screen_name_in_log_list)).setText(item.getScreen_name());
				((TextView)convertView.findViewById(R.id.InputLogNameInList)).setText(item.getName());

				((TextView)convertView.findViewById(R.id.InputLogCategoryInList)).setText(item.getCategory());
				((TextView)convertView.findViewById(R.id.InputLogNameInList)).setText(item.getName());


				NumberFormat nf = NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(2);


				if(item.getCo2() != 0){
					((TextView)convertView.findViewById(R.id.InputLogCo2AmountInList)).setText(String.valueOf(nf.format(item.getCo2())));
				}else{

					((View)convertView.findViewById(R.id.LogCo2WrapperInList)).setVisibility(View.GONE);
				}

				if(item.getMoney() != 0){
					((TextView)convertView.findViewById(R.id.InputLogAmountInList)).setText(String.valueOf(nf.format(item.getMoney())));
				}else{

					((View)convertView.findViewById(R.id.LogAmountWrapperInList)).setVisibility(View.GONE);
				}

				if(item.getPrice() != 0){
					((TextView)convertView.findViewById(R.id.InputLogPriceInList)).setText(String.valueOf(nf.format(item.getPrice())));

				}else{
					((View)convertView.findViewById(R.id.LogPriceWrapperInList)).setVisibility(View.GONE);

				}



				((TextView)convertView.findViewById(R.id.InputLogCreatedInList)).setText(item.getCreated());

				//写真
				ImageView imageView = (ImageView)convertView.findViewById(R.id.pictureInList);
				ProgressBar waitBar = (ProgressBar)convertView.findViewById(R.id.ProgressBarInList);
				if (item.getPicture_resource_id() != null){
					Bitmap bitmap = ImageCache.getImage(item.getPicture_resource_id()); //写真のリソースidをキーにキャッシュしている
					if(bitmap == null){
						//写真の非同期読み込み処理

						waitBar.setVisibility(View.VISIBLE);


						imageView.setVisibility(View.GONE);
						imageView.setImageDrawable(MixedTimelineFragment.this.getResources().getDrawable(R.drawable.abs__ab_bottom_transparent_dark_holo));

						pictureViewList.put(item.getPicture_resource_id(), imageView); //イメージビューをリストに保持
						progressBarList.put(item.getPicture_resource_id(), waitBar);

						IPPApplicationResourceClient public_resource_client = new IPPApplicationResourceClient(MixedTimelineFragment.this.getSherlockActivity().getApplicationContext()); //写真のリソースid指定
						public_resource_client.setAuthKey(ipp_auth_key);

						public_resource_client.get(PictureItem.class, item.getPicture_resource_id(), new PictureGetCallback());
					}else{
						 imageView.setImageBitmap(bitmap);
						 waitBar.setVisibility(View.GONE);
					}

				}else{

					waitBar.setVisibility(View.GONE);
				}


			}

		}else if (item0.getClass() == CommentItem.class){
			convertView = mInflater.inflate(R.layout.row_comment_relaxed, null);

			CommentItem item  = (CommentItem) item0;

			//ビューに値をセット
			((TextView) convertView.findViewById(R.id.CommentScreenNameInList)).setText(item.getCommentScreenName());

			TextView comment_text_view = (TextView) convertView.findViewById(R.id.text_in_comment_list);

			String comment_text = item.getCommentText();
			comment_text_view.setText(comment_text);


			//日付は最初からストリング yyyy-MM-dd HH:mm:ss
			((TextView) convertView.findViewById(R.id.CommentCreatedInList)).setText(item.getCommentCreated());

		}

		//ビューに値をセッ

		return convertView;

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

			ImageView imageView = pictureViewList.get(pictureItem.getResource_id());
			ProgressBar waitBar = progressBarList.get(pictureItem.getResource_id());

			if(pictureItem.getData() != null){
				Bitmap bitmap = BitmapFactory.decodeByteArray(pictureItem.getData(), 0, pictureItem.getData().length);
				imageView.setImageBitmap(bitmap);
				ImageCache.setImage(pictureItem.getResource_id(), bitmap);//キャッシュ

				imageView.setVisibility(View.VISIBLE);
				waitBar.setVisibility(View.GONE);
			}

		}

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
				showMixedList(target_year,target_month , 0, near);

			}
		});

		next_month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				calendar.add(Calendar.MONTH, 1);
				this_month.setText(String.valueOf(calendar.get(Calendar.MONTH)+1)+"月");
				target_year = calendar.get(Calendar.YEAR);
				target_month = calendar.get(Calendar.MONTH);
				showMixedList(target_year,target_month , 0, near);

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
			                    			showMixedList(target_year,target_month , 0, near);

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
			                    			showMixedList(target_year,target_month , 0, near);

		                              }
		                          })
		       .setNegativeButton(android.R.string.cancel, null)
		       .create();
	}

}





