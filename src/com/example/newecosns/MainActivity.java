package com.example.newecosns;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager.LayoutParams;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.newecosns.cohesive.CommentFragment;
import com.example.newecosns.cohesive.OthersLogFragment;
import com.example.newecosns.models.StressItem;
import com.example.newecosns.relaxed.MixedTimelineFragment;
import com.example.newecosns.relaxed.SummaryFragment;
import com.example.newecosns.utnils.NetworkManager;
import com.example.newecosns.utnils.TabListener;


public class MainActivity extends SherlockFragmentActivity {

	public static int mMenuType = 1;
	public static final int CommentFragmentMenu= 1;
	public static final int SummaryFragmentMenu = 2;

	SharedPreferences tw_pref;
	SharedPreferences.Editor tw_editor;

	//Twitter関連データ
	static final String CALLBACK = "http://sns.uozias.jp";
	static final String CONSUMER_KEY = "AJOoyPGkkIRBgmjAtVNw";
	static final String CONSUMER_SECRET = "1OMzUfMcqy4QHkyT6jJoUyxN4KXEu7R87k3bVOzp8c";
	static final int REQUEST_OAUTH = 1;
	static final String hash_tag = "ecosns_test";
	protected static final String TAG = "MainActivity";

	private static long user_id=0L;
	private static String screen_name=null;
	private static String token=null;
	private static String token_secret=null;

	//IPPユーザデータ保存用 TwitterOAuthデータ保存用
	SharedPreferences ipp_pref;
	SharedPreferences.Editor ipp_editor;
	private String ipp_auth_key;

	private String pair_resource_id = null;
	private String stress_now = null;
	private int role_self  =0;
	private long pair_start = 0L;
	private long pair_end = 0L;

	public static final int REQUEST_IPP_LOGIN = 3;


	private String team_resource_id;

	//アクションバー上のメニュ－

	private com.actionbarsherlock.app.SherlockFragmentActivity fm = null;



	Intent i = null;


	ActionBar actionBar;



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	 public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);



		/*
		i = getIntent();
		if(i != null ){
			if(i.getStringExtra("stress_now") != null){ //もしストレス状態が指定されてたら
				if(!stress_now.equals(i.getStringExtra("stress_now"))){ //もしストレス状態を変えてたら
					stress_now = i.getStringExtra("stress_now");
				}
			}
		}
		*/



	}


	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);




	}

	@Override
	public void onResume(){
		super.onResume();


		//ログインチェク
		loginCheck();


	}



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//twitterログイン用アクティビティから戻ってくるデータを保存
	//(上のメニューからログイン画面に飛ぶとこっちで受け取る必要あり)
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
			if(requestCode == REQUEST_OAUTH && resultCode == 200){
				user_id = data.getLongExtra(OAuthActivity.USER_ID, 0);
				screen_name =data.getStringExtra(OAuthActivity.SCREEN_NAME);
				token = data.getStringExtra(OAuthActivity.TOKEN);
				token_secret = data.getStringExtra(OAuthActivity.TOKEN_SECRET);

				//認証データ保存
				tw_editor = tw_pref.edit();
				tw_editor.putString("token",token);
				tw_editor.putString("token_secret",token_secret);
				tw_editor.putString("screen_name",screen_name);
				tw_editor.putLong("user_id",user_id);

				tw_editor.commit();


			}

			//TODO IPPログインから
			if(requestCode == REQUEST_IPP_LOGIN && resultCode == 200){
				 this.ipp_pref = getSharedPreferences("IPP", 0);
			      this.ipp_auth_key = this.ipp_pref.getString("ipp_auth_key", "");
			      this.team_resource_id = this.ipp_pref.getString("team_resource_id", "");
			      this.pair_resource_id = this.ipp_pref.getString("pair_resource_id", "");
			      this.stress_now = this.ipp_pref.getString("stress_now", "");
			      this.role_self = this.ipp_pref.getInt("role_self", 0);
			      this.pair_start = this.ipp_pref.getLong("pair_start", 0L);
			      this.pair_end = this.ipp_pref.getLong("pair_end", 0L);
			      setBar();

			}

		}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//IPPログインチェク
	public void loginCheck(){

		//IPPログインチェック
		ipp_pref = this.getSharedPreferences("IPP", Context.MODE_PRIVATE);
		ipp_auth_key = ipp_pref.getString("ipp_auth_key", "");

		team_resource_id = ipp_pref.getString("team_resource_id","");
		pair_resource_id = ipp_pref.getString("pair_resource_id","");
		stress_now = ipp_pref.getString("stress_now","");
		role_self = ipp_pref.getInt("role_self",0);
		pair_start = ipp_pref.getLong("pair_start",0L);
		pair_end = ipp_pref.getLong("pair_end",0L);




		//auth_keyがなければ
		if(ipp_auth_key == null ){

			//IPPログインに飛ばす
			//圏外じゃなければ
			if(NetworkManager.isConnected(this.getApplicationContext()) != false){
			    Intent intent = new Intent(this, IPPLoginActivity.class);
			    startActivityForResult(intent, REQUEST_IPP_LOGIN);

			}
		}

		//auth_keyがなければ
		if(ipp_auth_key.equals("") ){

			//IPPログインに飛ばす
			//圏外じゃなければ
			if(NetworkManager.isConnected(this.getApplicationContext()) != false){
			    Intent intent = new Intent(this, IPPLoginActivity.class);
			    startActivityForResult(intent, REQUEST_IPP_LOGIN);

			}
		}else{
			if(pair_end == 0 || pair_end < System.currentTimeMillis()){
				Intent intent = new Intent(this, IPPLoginActivity.class);
				startActivityForResult(intent, REQUEST_IPP_LOGIN);

			}else{
				setBar();

			}

		}




	}




///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void setBar(){
		//アクションバー用意

		new Handler().post(new Runnable (){
			@Override
			public void run() {
				actionBar = getSupportActionBar();
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				actionBar.removeAllTabs();
				if(stress_now.equals(StressItem.COHESIVE)){
					actionBar.addTab(actionBar.newTab()
							.setText(R.string.tab_comment_cohesive)
							.setIcon(android.R.drawable.ic_menu_crop)
							.setTabListener(new TabListener<CommentFragment>(
									MainActivity.this, "commentFragment", CommentFragment.class))
							);
					actionBar.addTab(actionBar.newTab()
							.setText(R.string.tab_others_cohesive)
							.setIcon(android.R.drawable.ic_menu_edit)
							.setTabListener(new TabListener<OthersLogFragment>(
									MainActivity.this, "othersLogFragment", OthersLogFragment.class)));
					actionBar.addTab(actionBar.newTab()
							.setText(R.string.tab_summary_relaxed)
							.setIcon(android.R.drawable.ic_menu_edit)
							.setTabListener(new TabListener<SummaryFragment>(
									MainActivity.this, "summaryFragment", SummaryFragment.class))
							);

				}else if(stress_now.equals(StressItem.RELAXED)){
					actionBar.addTab(actionBar.newTab()
							.setText(R.string.tab_mixed_relaxed)
							.setIcon(android.R.drawable.ic_menu_crop)
							.setTabListener(new TabListener<MixedTimelineFragment>(
									MainActivity.this, "mixedTimelineFragment", MixedTimelineFragment.class))
							);
					actionBar.addTab(actionBar.newTab()
							.setText(R.string.tab_summary_relaxed)
							.setIcon(android.R.drawable.ic_menu_edit)
							.setTabListener(new TabListener<SummaryFragment>(
									MainActivity.this, "summaryFragment", SummaryFragment.class))
							);


				}

				//右はじのタブ(個人家計簿)表示
				//actionBar.getTabAt(1).select();

			}
		});


	}
}
