package com.example.newecosns;


/**
 * ログインフロー
 *
 * ログインチェック
 * ↓
 * ユーザ名とパスを入れてもらう
 * ↓
 * ログインボタンでログイン
 * 　入力チェック
 * 　ログイン
 * ↓
 * ログイン情報を受け取る(LoginCallback)
 * 　ユーザidとauthキーをプレファレンスにれる
 * 　ユーザ名を取りに行く　→　ユーザ名をプレファレンスに入れる(ProfileCallback)
 * 　所属チームを取りに行く
 * ↓
 * 所属チームを受け取る(UserTeamCallback)
 * 　所属チームをプレファレンスにいれる
 * 　(所属チームがなければ）今ある一番若いチームを取りに行く
 * ↓
 * 一番若いチームを受け取る(TeamCallback)
 *  このチームの人数を調べる
 * ↓
 * チームの所属者リストを受け取る(UserTeamCallback2)
 *　 (人数が10名以内なら)　→　そのチームに所属させる(匿名インナークラス)　→　ペア相手を探しに行く(getPairTarget())
 * 　(人数が10名なら)チームを作って所属させる(createTeam())								↓
 * ↓
 * チーム作成完了(TeamItemCallback2)
 *  ペア相手を探しに行く(getPairTarget())
 * ↓
 * ペア相手の
 */



import java.sql.Timestamp;

import jp.innovationplus.ipp.client.IPPApplicationResourceClient;
import jp.innovationplus.ipp.client.IPPApplicationResourceClient.QueryCondition;
import jp.innovationplus.ipp.client.IPPLoginClient;
import jp.innovationplus.ipp.client.IPPProfileClient;
import jp.innovationplus.ipp.core.IPPQueryCallback;
import jp.innovationplus.ipp.jsontype.IPPLoginResult;
import jp.innovationplus.ipp.jsontype.IPPProfile;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newecosns.models.PairItem;
import com.example.newecosns.models.TeamItem;
import com.example.newecosns.models.UserTeamItem;

public class IPPLoginActivity extends Activity {


	public static final String TAG = "IPPLoginActivity";
	private Button btnLogin;
	private EditText user_name;
	private EditText password;
	private TextView screen_name;

	private String ipp_auth_key;
	private String ipp_id_string;
	private String ipp_pass_string;
	private String ipp_screen_name;
	private String team_resource_id;
	private int role_self ;

	private Timestamp timestamp = null;


	//IPPユーザデータ保存用
	SharedPreferences ipp_pref;
	SharedPreferences.Editor ipp_editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ipplogin);
		// 初期起動でキーボードを立ち上がらないように設定
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		user_name = (EditText) findViewById(R.id.editTextId);
		password = (EditText) findViewById(R.id.editTextPass);
		btnLogin = (Button) findViewById(R.id.buttonLogin);
		btnLogin.setOnClickListener(new LoginOnClickListener());
		screen_name = (TextView) findViewById(R.id.IPPScreenName);

		//ログインチェック
		ipp_pref = this.getSharedPreferences("IPP", Context.MODE_PRIVATE);
		ipp_auth_key = ipp_pref.getString("ipp_auth_key", "");
		ipp_id_string = ipp_pref.getString("ipp_id_string", "");
		ipp_pass_string = ipp_pref.getString("ipp_pass","");
		ipp_screen_name = ipp_pref.getString("ipp_screen_name","");
		team_resource_id = ipp_pref.getString("team_resource_id","");
		role_self = ipp_pref.getInt("role_self",0);


		user_name.setText(ipp_id_string);
		password.setText(ipp_pass_string);
		screen_name.setText(ipp_screen_name);

		//IPPプラットフォームのウェブビューでのアプリ設定へ

	    Button button = (Button) findViewById(R.id.button_go_to_ippwebview_in_ipplogin);
	    button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(IPPLoginActivity.this, IPPWebViewActivity.class);
			    startActivityForResult(intent, 0);

			}
		});

	    //タイムスタンプ設定
	    timestamp = new Timestamp(System.currentTimeMillis());

	}



	//ログインボタンを押したら
	private class LoginOnClickListener implements OnClickListener{

		@Override

		public void onClick(View v) {
			if(checkLoginText()){

				//jp.innovationplus.ippkit.IPPLoginClient loginclient = new jp.innovationplus.ippkit.IPPLoginClient(IPPLoginActivity.this.getApplicationContext());
				IPPLoginClient loginclient = new IPPLoginClient(IPPLoginActivity.this.getApplicationContext());
			    loginclient.setDebugMessage(true); //TODO debug
			    //jp.innovationplus.ippkit.jsontype.IPPLoginRequest params = new jp.innovationplus.ippkit.jsontype.IPPLoginRequest();

			    String user_name_str = user_name.getText().toString();
			    String password_str = password.getText().toString();

			    //params.setPassword(password_str);
			    //params.setUsername(user_name_str);
			    //loginclient.login(params, new LoginCallback());

			    loginclient.login(user_name_str, password_str, new LoginCallback());

			}
		}
	}

	//入力チェック
	private boolean checkLoginText() {


		String user_name_check = user_name.getText().toString();
		if (user_name_check.equals("")) {
			Toast.makeText(this, "input ID", Toast.LENGTH_SHORT).show();
			return false;
		}
		String password_check = password.getText().toString();
		if (password_check.toString().equals("")) {
			Toast.makeText(this, "input pass", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	//ログインコールバック
	private class LoginCallback implements IPPQueryCallback <IPPLoginResult> {

		@Override
		public void ippDidError(int arg0) {
			//ログイン失敗時
			Toast.makeText(IPPLoginActivity.this, "ログイン失敗", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void ippDidFinishLoading(IPPLoginResult arg0) {

			//ログイン成功したら、ユーザ名やauthキーをpreferenceに保存

			String authKey = arg0.getAuth_key();

			ipp_editor = ipp_pref.edit();
			ipp_editor.putString("ipp_id_string",user_name.getText().toString());
			ipp_editor.putString("ipp_pass",password.getText().toString());
			ipp_editor.putString("ipp_auth_key",authKey);


			ipp_auth_key = authKey;
			ipp_editor.commit();

			//スクリーンネームを取得
			IPPProfileClient profileclient = new IPPProfileClient(IPPLoginActivity.this.getApplicationContext());
			profileclient.isDebugMessage();
			profileclient.setAuthKey(authKey);
			profileclient.get(new ProfileCallback());


			//ここからはじまる流れはやや複雑
			//team_idを取得
			IPPApplicationResourceClient user_team_client = new IPPApplicationResourceClient(IPPLoginActivity.this.getApplicationContext());
			user_team_client.setAuthKey(authKey);
			//ユーザ名を指定
			QueryCondition condition = new QueryCondition();
			//condition.eq("user_name",user_name.getText().toString());
			condition.setSelf();
			condition.build();
			user_team_client.query(UserTeamItem.class,condition, new UserTeamCallback());


		}

	}

	//スクリーンネーム取得のコールバック
	private class ProfileCallback implements IPPQueryCallback <IPPProfile>{

		@Override
		public void ippDidError(int arg0) {

			Toast.makeText(IPPLoginActivity.this, "表示名取得失敗", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void ippDidFinishLoading(IPPProfile profile) {

			String screen_name = profile.getScreenName();
			ipp_editor = ipp_pref.edit();
			ipp_editor.putString("ipp_screen_name",screen_name);
			ipp_editor.commit();

			if(!screen_name.equals("")){

				Toast.makeText(IPPLoginActivity.this, "ログイン成功", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(IPPLoginActivity.this, "表示名取得失敗", Toast.LENGTH_SHORT).show();
			}



		}
	}

	//チーム取得のコールバック
	private class UserTeamCallback implements IPPQueryCallback <UserTeamItem[]>{

		@Override
		public void ippDidError(int arg0) {
			Toast.makeText(IPPLoginActivity.this, "チーム取得失敗", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void ippDidFinishLoading(UserTeamItem[] user_team_item_array) {
			Log.d(TAG, user_team_item_array.toString()); //debug

			//チーム未所属ならメンバーが10名いないチームに所属させる
			if(user_team_item_array.length == 0){
				//タイムスタンプDESCでチームを呼び出し、一番若いチームの人数を調べる
				//本来こんなのサーバサイドでやるべき
				IPPApplicationResourceClient team_client = new IPPApplicationResourceClient(IPPLoginActivity.this.getApplicationContext());
				team_client.setAuthKey(ipp_auth_key);
				QueryCondition condition = new QueryCondition();
				condition.build();
				team_client.query(TeamItem.class,condition , new TeamCallback());

			}else{
				//チームに所属してたらteamのリソースidを保存
				ipp_editor = ipp_pref.edit();
				String team_resource_id = user_team_item_array[0].getTeam_resource_id();
				ipp_editor.putString("team_resource_id",team_resource_id);
				ipp_editor.commit();

				//ペアの取得
				getPair(team_resource_id);

				 //MainActivityに戻る
			    //Intent intent = new Intent(IPPLoginActivity.this, MainActivity.class);
			    //startActivity(intent);
				//finish();
			}
		}
	}

	//チーム取得のコールバック2 //一番若いチームを調べる
	private class TeamCallback implements IPPQueryCallback<TeamItem[]>{

		@Override
		public void ippDidError(int paramInt) {
			Log.d(TAG, "チーム取得失敗");

		}

		@Override
		public void ippDidFinishLoading(TeamItem[] teamItemArray) {


			if(teamItemArray.length == 0){
				//そもそもひとつもチームがなければ
				createTeam();//新チームを作って所属させる

			}else{
				TeamItem teamItem = teamItemArray[0];

				//チームの人数を調べるため、さっき取得した一番若いチームに所属するユーザのリストを呼び出す
				IPPApplicationResourceClient user_team_client = new IPPApplicationResourceClient(IPPLoginActivity.this.getApplicationContext());
				user_team_client.setAuthKey(ipp_auth_key);
				QueryCondition condition = new QueryCondition();
				condition.setCount(20);//10より多ければ問題ないはずだが一応20にしとく
				condition.eq("team_resource_id",teamItem.getResource_id().toString());

				//condition.eq("team_resource_id","515b97470cf2352f719f2625"); //debug

				condition.build();
				//チームのteam_resource_idをparamsで指定
				user_team_client.query(UserTeamItem.class,condition, new UserTeamCallback2());



			}

		}

	}

	//10名以内チームを探すための処理のコールバック
	private class UserTeamCallback2 implements IPPQueryCallback<UserTeamItem[]>{

		@Override
		public void ippDidFinishLoading(UserTeamItem[] user_team_array) {


			if(user_team_array.length >= 10){
				//10名を超えていたら
				createTeam();
			}else{
				//10名以内だったら
				//そのチームの人数を増やし、そのチームに所属させる

				IPPApplicationResourceClient user_team_client = new IPPApplicationResourceClient(IPPLoginActivity.this.getApplicationContext());
				UserTeamItem user_team_item = new UserTeamItem();
				user_team_client.setAuthKey(ipp_auth_key);
				user_team_item.setTimestamp(timestamp.getTime());

				//チームのリソースidを指定
				String team_resource_id = user_team_array[0].getTeam_resource_id();
				user_team_item.setTeam_resource_id(team_resource_id );

				//所属させる人のuser_nameを指定
				ipp_id_string = ipp_pref.getString("ipp_id_string", "");
				user_team_item.setUser_name(ipp_id_string);

				user_team_client.create(UserTeamItem.class, user_team_item, new IPPQueryCallback<String>() {

					@Override
					public void ippDidFinishLoading(String paramT) {
						Log.d(TAG,"チーム所属成功");
						//MainActivityに戻る
					    //Intent intent = new Intent(IPPLoginActivity.this, MainActivity.class);
					    //startActivity(intent);
						//finish();

					}

					@Override
					public void ippDidError(int paramInt) {
						Log.d(TAG,"チーム所属失敗");
					}
				});

				//ペアの取得
				getPair(team_resource_id);


			}

		}

		@Override
		public void ippDidError(int paramInt) {
			Log.d(TAG,String.valueOf(paramInt));


		}


	}

	public void createTeam(){

		//新チームを作って所属させる
		IPPApplicationResourceClient team_client = new IPPApplicationResourceClient(IPPLoginActivity.this.getApplicationContext());
		TeamItem teamItem2 = new TeamItem();
		teamItem2.setTimestamp(System.currentTimeMillis());

		team_client.setAuthKey(ipp_auth_key);
		team_client.create(TeamItem.class, teamItem2, new TeamItemCallback2());
	}

	//チーム作成後のコールバック user_teamにインスタンスをつくる
	private class TeamItemCallback2 implements IPPQueryCallback<String>{

		@Override
		public void ippDidFinishLoading(String resource_id) {
			IPPApplicationResourceClient user_team_client = new IPPApplicationResourceClient(IPPLoginActivity.this.getApplicationContext());
			UserTeamItem user_team_item = new UserTeamItem();
			user_team_item.setTeam_resource_id(resource_id);

			ipp_editor = ipp_pref.edit();
			ipp_editor.putString("team_resource_id",user_team_item.getTeam_resource_id());
			ipp_editor.commit();

			//ペアの取得
			getPair(resource_id);

			user_team_item.setUser_name(ipp_id_string);
			user_team_client.setAuthKey(ipp_auth_key);
			user_team_item.setTimestamp(System.currentTimeMillis());
			user_team_client.create(UserTeamItem.class, user_team_item, new IPPQueryCallback<String>() {

				@Override
				public void ippDidFinishLoading(String paramT) {
					Log.d(TAG,"チーム所属成功");


					//MainActivityに戻る
				    //Intent intent = new Intent(IPPLoginActivity.this, MainActivity.class);
				    //startActivity(intent);
					//finish();

				}

				@Override
				public void ippDidError(int paramInt) {
					Log.d(TAG,"チーム所属失敗");
				}
			});

		}

		@Override
		public void ippDidError(int paramInt) {


			Log.d(TAG, "チーム作成失敗");

		}



	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//ペア相手のチームを取ってくる処理

	private void getPair(String team_resource_id){


		IPPApplicationResourceClient client = new IPPApplicationResourceClient(this.getApplicationContext());
		QueryCondition condition = new QueryCondition();
		condition.eq("team_resource_id", team_resource_id);
		condition.build();
		client.setAuthKey(ipp_auth_key);
		client.query(PairItem.class, condition, new PairItemCallback(this.getApplicationContext()));



	}

	//ペア相手のチーム取ってくるコールバック
	class PairItemCallback implements IPPQueryCallback<PairItem[]>{

		PairItem pair_item = null;
		Context context = null;

		//IPPユーザデータ保存用
			SharedPreferences ipp_pref;
			SharedPreferences.Editor ipp_editor;

		public PairItemCallback(Context context){

			this.context = context;

		}

		@Override
		public void ippDidError(int arg0) {
			Log.d(TAG,String.valueOf(arg0));
			Log.d(TAG, "チーム読み込み失敗");

			//MainActivityに戻る
		    Intent intent = new Intent(IPPLoginActivity.this, MainActivity.class);
		    startActivity(intent);

		}

		@Override
		public void ippDidFinishLoading(PairItem[] pair_items) {
			//有効期限と今の日時を比べ、該当するタームのペア設定を読み出す
			if(pair_items.length != 0){
				for (PairItem mPairItem : pair_items){
					if(mPairItem.getStart() < timestamp.getTime() && mPairItem.getEnd() > timestamp.getTime()){
						pair_item = mPairItem;
					}
				}
			}
			//相手のresource_idやら関係の有効期限をプレファレンスに保存
			ipp_pref = context.getSharedPreferences("IPP", Context.MODE_PRIVATE);
			ipp_editor = ipp_pref.edit();
			ipp_editor.putString("pair_resource_id",pair_item.getPair_target_team_resoure_id());
			ipp_editor.putString("stress_now",pair_item.getStress_now());
			ipp_editor.putInt("role_self",pair_item.getRole_self());
			ipp_editor.putLong("pair_start",pair_item.getStart());
			ipp_editor.putLong("pair_end",pair_item.getEnd());
			ipp_editor.commit();

			//MainActivityに戻る
		    Intent intent = new Intent(IPPLoginActivity.this, MainActivity.class);
		    startActivity(intent);

		}
	}




	//プラットフォームのwebビューから戻ってくる
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}


