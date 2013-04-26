package com.example.newecosns.cohesive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.newecosns.MainActivity;
import com.example.newecosns.R;
import com.example.newecosns.models.CommentItem;
import com.example.newecosns.utnils.Constants;
import com.example.newecosns.utnils.StarCallback;

public class CommentAdapter extends ArrayAdapter<CommentItem> {
	private LayoutInflater mInflater;
	private Context context;

	private String ipp_auth_key;

	private Button replyBtn = null;
	private Button showConversationBtn = null;

	private String team_resource_id = null;
	private int role_self = 0;

	private TextView number_of_star = null;
	private Button buton_evaluate_it = null;
	private Fragment fragment;

	private View selected_elemenbt = null;
	private int replyActivityFlg = 0;
	private Location mNowLocation = null;

	private String ipp_pass_string;
	private String ipp_screen_name;
	private String pair_common_id;
	private String pair_item_list_string;
	private SharedPreferences ipp_pref;
	private String ipp_id_string;

	private ViewGroup replyView = null;

	//リソース
	Resources res = null;

	private Map<String, CommentItem> replyTargets = new HashMap<String, CommentItem>(); //選択状態のコメントのid, itemが入っている

	public CommentAdapter(Context context, List<CommentItem> comments) {
		super(context, 0, comments);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		 res = context.getResources();

	}

	public CommentAdapter(Context context, List<CommentItem> comments, String team_resource_id, int role_self,
			String ipp_auth_key, int replyActivityFlg) {
		this(context, comments, team_resource_id, role_self, ipp_auth_key, null);
		this.replyActivityFlg = replyActivityFlg;

	}

	public CommentAdapter(Context context, List<CommentItem> comments, String team_resource_id, int role_self,
			String ipp_auth_key, Location NowLocation) {
		super(context, 0, comments);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.context = context;
		this.team_resource_id = team_resource_id;
		this.role_self = role_self;
		this.ipp_auth_key = ipp_auth_key;
		this.mNowLocation = NowLocation;
		 res = context.getResources();

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		//データが入ってるオブジェクトをとる
		final CommentItem item = this.getItem(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_comment_cohesive, null);
		}

		//自分の今のroleを参照して、自分のチームは自分のロール、相手のチームは自分と対になるロールに設定する
		if (team_resource_id != null && role_self != 0) {
			//自分側チームの時
			if (item.getTeam_resource_id().equals(team_resource_id)) {
				switch (role_self) {
				case Constants.KOHAI:
					convertView.findViewById(R.id.wrapper_comment_fuki).setBackgroundDrawable(
							context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_kouhai));
					//先輩アイコン消す
					convertView.findViewById(R.id.imagesWrapperRight).setVisibility(View.GONE);
					convertView.findViewById(R.id.imagesWrapperLeft).setVisibility(View.VISIBLE);
					((TextView) convertView.findViewById(R.id.CommentScreenNameInListLeft)).setText(item
							.getCommentScreenName());
					//TODO アイコンのセット
					break;
				case Constants.SENPAI:
					convertView.findViewById(R.id.wrapper_comment_fuki).setBackgroundDrawable(
							context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_sennpai));
					//後輩アイコン消す
					convertView.findViewById(R.id.imagesWrapperLeft).setVisibility(View.GONE);
					convertView.findViewById(R.id.imagesWrapperRight).setVisibility(View.VISIBLE);
					((TextView) convertView.findViewById(R.id.CommentScreenNameInListRight)).setText(item
							.getCommentScreenName());
					//TODO アイコンのセット
					break;
				/*
				case Constants.RELAXED_ROLE:
					convertView.findViewById(R.id.wrapper_comment_fuki).setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_comment_relaxed)); //リラックスのとき
					break;

				*/

				}
				//相手側チーム(というか自分のチーム以外)のとき
			} else {
				switch (role_self) {
				case Constants.KOHAI:
					convertView.findViewById(R.id.wrapper_comment_fuki).setBackgroundDrawable(
							context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_sennpai));
					//後輩アイコン消す
					convertView.findViewById(R.id.imagesWrapperLeft).setVisibility(View.GONE);
					convertView.findViewById(R.id.imagesWrapperRight).setVisibility(View.VISIBLE);
					((TextView) convertView.findViewById(R.id.CommentScreenNameInListRight)).setText(item
							.getCommentScreenName());
					//TODO アイコンのセット
					break;
				case Constants.SENPAI:
					convertView.findViewById(R.id.wrapper_comment_fuki).setBackgroundDrawable(
							context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_kouhai));
					//先輩アイコン消す
					convertView.findViewById(R.id.imagesWrapperRight).setVisibility(View.GONE);
					convertView.findViewById(R.id.imagesWrapperLeft).setVisibility(View.VISIBLE);
					((TextView) convertView.findViewById(R.id.CommentScreenNameInListLeft)).setText(item
							.getCommentScreenName());
					//TODO アイコンのセット

					break;
				/*
				case Constants.RELAXED_ROLE:
					convertView.findViewById(R.id.wrapper_comment_fuki).setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_comment_relaxed)); //リラックスのとき
					break;
				*/
				}

			}
		} else {
			convertView.findViewById(R.id.wrapper_comment_fuki).setBackgroundDrawable(
					context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_kouhai));
			//先輩アイコン消す
			convertView.findViewById(R.id.imagesWrapperRight).setVisibility(View.GONE);

		}

		//ビューに値をセット

		((TextView) convertView.findViewById(R.id.CommentTextInList)).setText(item.getCommentText());

		//日付は最初からストリング yyyy-MM-dd HH:mm:ss
		((TextView) convertView.findViewById(R.id.CommentCreatedInList)).setText(item.getCommentCreated());

		//resourceId, parentResourceIdは隠しビュー
		TextView resourceid = (TextView) convertView.findViewById(R.id.CommentResourceIdInList);
		resourceid.setText(item.getResource_id());
		resourceid.setVisibility(View.GONE);

		//TextView parentresourceid = (TextView) convertView.findViewById(R.id.CommentParentResourceIdInList);
		//parentresourceid.setText(item.getCommentParentResourceId());
		//parentresourceid.setVisibility(View.GONE);

		//返信先表示

		showConversationBtn = (Button) convertView.findViewById(R.id.showConversation);
		if (item.getCommentParentResourceId() != null && replyActivityFlg == 0) {

			showConversationBtn.setVisibility(View.VISIBLE);

			showConversationBtn.setOnClickListener(new GetConversationListener(item, context));

		} else {//会話を表示」メニュー
			showConversationBtn.setVisibility(View.GONE);
		}

		//返信ボタンをタップしたら
		replyBtn = (Button) convertView.findViewById(R.id.replyBtn);

		if (replyActivityFlg == 1) {

			replyBtn.setVisibility(View.GONE);
		} else {
			replyBtn.setOnClickListener(new setReplyTarget(item, context, replyTargets));

		}

		//評価ボタン押す //スター
		buton_evaluate_it = (Button) convertView.findViewById(R.id.buton_evaluate_it);
		number_of_star = (TextView) convertView.findViewById(R.id.number_of_star);
		buton_evaluate_it.setOnClickListener(new StarCallback(item, ipp_auth_key, context, number_of_star));

		number_of_star.setText(String.valueOf(item.getStar()));

		//返信状態チェック これをしないとビューが使いまわされておかしなことになる
		View view_reply = (View) convertView.findViewById(R.id.view_reply);
		if(context.getClass().equals(MainActivity.class) ){
			CommentFragment commentFragment = (CommentFragment) ((SherlockFragmentActivity) context).getSupportFragmentManager().findFragmentByTag("commentFragment");

			ViewGroup wrapper_submit_comment = (ViewGroup) commentFragment.getView().findViewById(R.id.wrapper_submit_comment2);
			if (replyTargets.get(item.getResource_id()) == null ) {
				if(view_reply != null){
					((ViewGroup) convertView).removeView(view_reply);

					(replyBtn).setText(res.getString(R.string.reply_to_comment)); //文字変える
				}



				//wrapper_submit_comment.setVisibility(View.VISIBLE);
				//((ViewGroup) wrapper_submit_comment.getParent()).removeView(wrapper_submit_comment);
				//commentFragment.getView().requestLayout();


			}

			if (replyTargets.get(item.getResource_id()) != null) {
				if(view_reply == null){
					setReplyView(replyBtn, item, (ViewGroup) convertView, replyTargets);
					replyBtn.setText(res.getString(R.string.button_calcel_reply_to_comment));
				}


			}
		}




		return convertView;
	}

	public class GetConversationListener implements View.OnClickListener {

		CommentItem item = null;
		private Context context;

		public GetConversationListener(CommentItem item, Context context) {
			this.item = item;
			this.context = context;

		}

		@Override
		public void onClick(View v) {
			//返信関係にあるコメントを全部とってきて表示するアクティビティを立ち上げる
			Intent intent = new Intent(context, RepliesActivity.class);
			intent.putExtra("resource_id", item.getResource_id());
			intent.putExtra("parent_resource_id", item.getCommentParentResourceId());

			((Activity) context).startActivityForResult(intent, 1);
			((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

		}

	}

	//返信用のビューを追加する処理
	public void setReplyView(View v, CommentItem item, ViewGroup list_element, Map<String, CommentItem> replyTargets) {







		//追加するビューを取得
		LayoutInflater inflater1 = LayoutInflater.from(context);
		ViewGroup replyView = (ViewGroup) inflater1.inflate(R.layout.view_reply, null);



		//先輩後輩状態に合わせて見た目を整形
		switch (role_self) {
		case Constants.KOHAI:
			replyView.findViewById(R.id.wrapper_comment_fuki).setBackgroundDrawable(
					context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_kouhai));
			//先輩アイコン消す
			replyView.findViewById(R.id.imagesWrapperRight).setVisibility(View.INVISIBLE);
			replyView.findViewById(R.id.imagesWrapperLeft).setVisibility(View.VISIBLE);
			((TextView) replyView.findViewById(R.id.CommentScreenNameInListLeft)).setText(item
					.getCommentScreenName());
			//TODO アイコンのセット
			break;
		case Constants.SENPAI:
			replyView.findViewById(R.id.wrapper_comment_fuki).setBackgroundDrawable(
					context.getResources().getDrawable(R.drawable.bg_row_comment_cohesive_sennpai));
			//後輩アイコン消す
			replyView.findViewById(R.id.imagesWrapperLeft).setVisibility(View.INVISIBLE);
			replyView.findViewById(R.id.imagesWrapperRight).setVisibility(View.VISIBLE);
			((TextView) replyView.findViewById(R.id.CommentScreenNameInListRight)).setText(item
					.getCommentScreenName());

			break;
		}


		//その中に返信用のビューを入れる
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.BELOW, R.id.wrapper);
		list_element.addView(replyView, param);

		//this.replyView = replyView;




		//内容
		EditText content = (EditText) list_element.findViewById(R.id.textReply);

		content.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {


			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {


			}

			@Override
			public void afterTextChanged(Editable s) {


			}
		});


		ipp_pref = context.getSharedPreferences("IPP", Context.MODE_PRIVATE);

		ipp_id_string = ipp_pref.getString("ipp_id_string", "");
		ipp_pref = context.getSharedPreferences("IPP", 0);
		ipp_pass_string = ipp_pref.getString("ipp_pass", "");
		ipp_screen_name = ipp_pref.getString("ipp_screen_name", "");
		pair_common_id = ipp_pref.getString("pair_common_id", "");
		pair_item_list_string = ipp_pref.getString("pair_item_list_string", "");

		CommentFragment commentFragment = (CommentFragment) ((SherlockFragmentActivity) context).getSupportFragmentManager().findFragmentByTag("commentFragment");

		//投稿のリスナセット
		replyView.findViewById(R.id.submitButton).setOnClickListener(
				(commentFragment.new TapSendCommentListener(content, ipp_id_string, ipp_screen_name, team_resource_id,
						pair_common_id, item.getResource_id(), mNowLocation)));



	}



	/*
	public class replyFocusListener implements OnFocusChangeListener {

		ViewGroup fragmentView = null;

		public replyFocusListener(ViewGroup fragmentView) {
			this.fragmentView = fragmentView;

		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			LinearLayout wrapper_submit_comment   = (LinearLayout) fragmentView.findViewById(R.id.wrapper_submit_comment2);

			if (hasFocus == true) {
				wrapper_submit_comment.setVisibility(View.GONE);

				//ViewGroup parent = (ViewGroup) wrapper_submit_comment.getParent();
				//parent.removeView(wrapper_submit_comment);
				//parent.invalidate();
				//parent.findViewById(R.id.comment_list_wrapper).invalidate();

				//RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
				//((LinearLayout) fragmentView.findViewById(R.id.wrapper_submit_comment)).setLayoutParams(params);
			} else {

				wrapper_submit_comment.setVisibility(View.VISIBLE);
				//RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				//((LinearLayout) fragmentView.findViewById(R.id.wrapper_submit_comment)).setLayoutParams(params);


			}

		}
	}
	*/





	//返信ボタンをおしたら、
	public class setReplyTarget implements View.OnClickListener {
		CommentItem item = null;

		private Map<String, CommentItem> replyTargets;

		public setReplyTarget(CommentItem item, Context context, Map<String, CommentItem> replyTargets) {
			this.item = item;

			this.replyTargets = replyTargets;
		}

		@Override
		public void onClick(View v) {
			//リストの一番上の子を取得
			RelativeLayout list_element = (RelativeLayout) v.getParent().getParent().getParent().getParent();


			CommentFragment commentFragment = (CommentFragment) ((SherlockFragmentActivity) context).getSupportFragmentManager().findFragmentByTag("commentFragment");
			LinearLayout wrapper_submit_comment   = (LinearLayout) commentFragment.getView().findViewById(R.id.wrapper_submit_comment2);

			if(replyTargets.get(item.getResource_id()) == null){

				wrapper_submit_comment.setVisibility(View.GONE);

				setReplyView(v, item, list_element, replyTargets);
				replyTargets.put(item.getResource_id(), item); //返信中であるという印

				((Button) v).setText(res.getString(R.string.button_calcel_reply_to_comment)); //文字変える

			}else{
				((Button) v).setText(res.getString(R.string.reply_to_comment)); //文字変える
				//返信対象リストから削除
				replyTargets.remove(item.getResource_id());
				//返信欄を消す
				list_element.removeView(list_element.findViewById(R.id.view_reply));
				//下の投稿欄をだす
				wrapper_submit_comment.setVisibility(View.VISIBLE);



			}

		}

	}

}
