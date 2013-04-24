package com.example.newecosns.cohesive;


import java.util.ArrayList;

import jp.innovationplus.ipp.client.IPPApplicationResourceClient;
import jp.innovationplus.ipp.core.IPPQueryCallback;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.newecosns.IPPLoginActivity;
import com.example.newecosns.R;
import com.example.newecosns.models.CommentItem;
import com.example.newecosns.utnils.NetworkManager;

public class RepliesActivity extends Activity {


	//リソース
	Resources res = null;

	private String TAG = "RepliesActivity";

	//ストレス状態
	public String stress_now ;

	private String team_resource_id;
	private int role_self  = 0;

	//閉じるボタン
	Button closeBtn = null;

	Intent i = null;

	//OAuthデータ保存用
	SharedPreferences ipp_pref;
	SharedPreferences.Editor ipp_editor;
	private String ipp_auth_key;
	private String ipp_id_string;
	private String ipp_pass_string;
	private String ipp_screen_name;


	//起点となるコメントデータ
	String CommentParentResourceId = null;
	String CommentResourceId = null;

	//アダプタ
	CommentAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_replies);

		res = getResources();


		 /*
         * IPPログインチェック
         */
    	//圏外じゃなければ
		if(NetworkManager.isConnected(this.getApplicationContext()) != false){
			//IPPログインチェック
			ipp_pref = this.getSharedPreferences("IPP", Context.MODE_PRIVATE);
			ipp_auth_key = ipp_pref.getString("ipp_auth_key", "");
			ipp_id_string = ipp_pref.getString("ipp_id_string", "");
			ipp_pass_string = ipp_pref.getString("ipp_pass","");
			ipp_screen_name  = ipp_pref.getString("ipp_screen_name", "");

			team_resource_id = ipp_pref.getString("team_resource_id", "");
			role_self = ipp_pref.getInt("role_self", 0);

			//auth_keyがなければ
			if(ipp_auth_key.equals("")){
				//IPPログインに飛ばす
			    Intent intent = new Intent(this, IPPLoginActivity.class);
			    startActivity(intent);
			}

		}

		//intentからデータもらう
		i = getIntent();
		CommentResourceId = i.getStringExtra("resource_id"); //
    	CommentParentResourceId= i.getStringExtra("parent_resource_id");
    	stress_now = "cohesive";


		//リストビューの準備
		ListView listView = (ListView) RepliesActivity.this.findViewById(R.id.replies_list);//自分で用意したListView
		listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		adapter = new CommentAdapter(RepliesActivity.this, new ArrayList<CommentItem>(), team_resource_id, role_self, ipp_auth_key, 1);
		listView.setAdapter(adapter);

		//ippサーバから全やり取りのデータをとってくるメソッド

		if(NetworkManager.isConnected(getApplicationContext())){
			getReplyTarget(CommentResourceId);

		}else{
			Toast.makeText(this, res.getString(R.string.message_network_disabled), Toast.LENGTH_SHORT).show();

		}

    	//getReplyTarget(CommentParentResourceId);

		//コメントフラグメントに戻る処理
    	closeBtn = (Button) this.findViewById(R.id.backToCommentFragment);
    	closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(Activity.RESULT_OK, intent);
				finish();
				overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

			}
    	});

	}


	private void getReplyTarget(String CommentResourceId) {

		IPPApplicationResourceClient public_resource_client = new IPPApplicationResourceClient(this.getApplicationContext());
		public_resource_client.setAuthKey(ipp_auth_key);

		RelyGetCallback replyGetCallback = new RelyGetCallback();
		public_resource_client.get(CommentItem.class, CommentResourceId, replyGetCallback);
	}

	//位置コメントずつ取得して、
	public class RelyGetCallback implements IPPQueryCallback<CommentItem> {


		@Override
		public void ippDidError(int arg0) {
			Log.d(TAG,String.valueOf(arg0));

		}

		@Override
		public void ippDidFinishLoading(CommentItem comment_item) {

			if(comment_item != null){

				//リストビューに上から結果を追加していく
				CommentItem commentItem = comment_item;
				adapter.add(commentItem);
				//もし親がいたらもう一周
				if(commentItem.getCommentParentResourceId().length() > 0){

					getReplyTarget(commentItem.getCommentParentResourceId());
				}

			}

		}



	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.replies, menu);
		return true;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//コメント表示用アダプター(密-ゆるい切替機能付き)
/*

	class CommentAdapter extends ArrayAdapter<CommentItem> {
	private LayoutInflater mInflater;
	private String stress_now;
	private String ipp_id_string = null;
	private Button replyBtn = null;
	private Button showConversationBtn  = null;
	private Context context;

		public CommentAdapter(Context context, String stress_now, String ipp_id_string) {
		super(context, 0);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.stress_now = stress_now;
		this.context = context;
		this.ipp_id_string = ipp_id_string;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {


			//データが入ってるオブジェクトをとる
			final CommentItem item = this.getItem(position);


			if(convertView == null){
				convertView = mInflater.inflate(R.layout.row_comment_cohesive, null);
			}
			//設定ストレスによってlayoutを変える
			if (stress_now == null) {
			stress_now = StressItem.COHESIVE;

			}


		//密な時は自分と他人を分ける
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

			//返信ボタンは消す
			replyBtn = (Button) convertView.findViewById(R.id.replyBtn);
			replyBtn.setVisibility(View.GONE);

			( (Button) convertView.findViewById(R.id.showConversation)).setVisibility(View.GONE);

			return convertView;

		}

	}

	*/

}
