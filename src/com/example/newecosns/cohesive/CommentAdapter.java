package com.example.newecosns.cohesive;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
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

	public CommentAdapter(Context context, List<CommentItem> comments) {
		super(context, 0, comments);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;

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
		if (item.getCommentParentResourceId().length() != 0 && replyActivityFlg == 0) {

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
			replyBtn.setOnClickListener(new setReplyTarget(item, context)) ;

		}

		//評価ボタン押す //スター
		buton_evaluate_it = (Button) convertView.findViewById(R.id.buton_evaluate_it);
		number_of_star = (TextView) convertView.findViewById(R.id.number_of_star);
		buton_evaluate_it.setOnClickListener(new StarCallback(item, ipp_auth_key, context, number_of_star));

		number_of_star.setText(String.valueOf(item.getStar()));

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

	public class setReplyTarget implements View.OnClickListener {
		CommentItem item = null;
		private String ipp_pass_string;
		private String ipp_screen_name;
		private String pair_common_id;
		private String pair_item_list_string;
		private SharedPreferences ipp_pref;
		private String ipp_id_string;
		private Context context;


		public setReplyTarget(CommentItem item, Context context) {
			this.item = item;
			this.context = context;
		}

		@Override
		public void onClick(View v) {
			v.setEnabled(false); //ボタンを押せなくする

			//追加するビューを取得
			LayoutInflater inflater1 = LayoutInflater.from(context);
			View replyView = inflater1.inflate(R.layout.view_reply, null);

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
			//リストの一番上の子を取得
			RelativeLayout list_element = (RelativeLayout) v.getParent().getParent().getParent();

			//その中に返信用のビューを入れる
			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			param.addRule(RelativeLayout.BELOW, R.id.wrapper_comment_fuki);
			list_element.addView(replyView, param);

			//内容
			EditText content = (EditText) list_element.findViewById(R.id.textReply);
			ipp_pref = context.getSharedPreferences("IPP", Context.MODE_PRIVATE);

			ipp_id_string = ipp_pref.getString("ipp_id_string", "");
			ipp_pref = context.getSharedPreferences("IPP", 0);
			ipp_pass_string = ipp_pref.getString("ipp_pass", "");
			ipp_screen_name = ipp_pref.getString("ipp_screen_name", "");
			pair_common_id = ipp_pref.getString("pair_common_id", "");
			pair_item_list_string = ipp_pref.getString("pair_item_list_string", "");

			//投稿のリスナセット
			CommentFragment commentFragment =  (CommentFragment)  ((SherlockFragmentActivity) context).getSupportFragmentManager().findFragmentByTag("commentFragment");
			replyView.findViewById(R.id.submitButton).setOnClickListener((commentFragment.new TapSendCommentListener(content, ipp_id_string, ipp_screen_name, team_resource_id,
							pair_common_id, item.getResource_id(), mNowLocation)));



		}

	}

}
