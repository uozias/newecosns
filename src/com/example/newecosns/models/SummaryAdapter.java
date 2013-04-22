package com.example.newecosns.models;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.newecosns.R;
import com.example.newecosns.utnils.Constants;
import com.example.newecosns.utnils.StarCallback;

public class SummaryAdapter extends ArrayAdapter<SummaryItem> {
	private LayoutInflater mInflater;

	private Context context = null;
	private String team_resource_id = null;
	private int role_self = 0;

	private String ipp_auth_key;

	private TextView number_of_star = null;
	private Button buton_evaluate_it = null;

	private int flg_self_only = 0;

	public SummaryAdapter(Context context, List<SummaryItem> objects) {
		super(context, 0,  objects);

		this.context = context;

		this.flg_self_only = 1; //こっちのコンストラクタはsummaryFragmentからなので、スターは表示しない
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}



	public SummaryAdapter(Context context, List<SummaryItem> objects, String team_resource_id, int role_self, String ipp_auth_key) {
		super(context, 0,  objects);
		this.team_resource_id  = team_resource_id;
		this.role_self = role_self;
		this.context = context;
		this.ipp_auth_key = ipp_auth_key;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_summary, null); //todo ストレス切替
		}
		SummaryItem item = this.getItem(position);

		//自分の今のroleを参照して、自分のチームは自分のロール、相手のチームは自分と対になるロールに設定する
		if(team_resource_id != null && role_self != 0){
			//自分側チームの時
			if(item.getTeam_resource_id().equals(team_resource_id)){
				switch(role_self){
					case Constants.KOHAI:
						convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_summary_cohesive_kouhai));
						break;
					case Constants.SENPAI:
						convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_summary_cohesive_sempai));
						break;
					case Constants.RELAXED_ROLE:
						convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_summary)); //リラックスのとき
						break;

				}


			//相手側チーム(というか自分のチーム以外)のとき
			}else{
				switch(role_self){
				case Constants.KOHAI:
					convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_summary_cohesive_sempai));
					break;
				case Constants.SENPAI:
					convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_summary_cohesive_kouhai));
					break;
				case Constants.RELAXED_ROLE:
					convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_summary)); //リラックスのとき
					break;

				}

			}
		}else{
			convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.bg_row_summary));

		}






		((TextView)convertView.findViewById(R.id.screen_name_in_summary_fragment)).setText(String.valueOf(item.getScreen_name()));

		Calendar calendar_now = Calendar.getInstance();
		calendar_now.setTimeInMillis(item.getTimestamp()); //今の日時

		((TextView)convertView.findViewById(R.id.month_in_summary_fragment)).setText(String.valueOf(calendar_now.get(Calendar.MONTH)+1));


		((TextView)convertView.findViewById(R.id.number_in_summary)).setText(String.valueOf(item.getNumber()));

		//((TextView)convertView.findViewById(R.id.rank_in_summary)).setText(String.valueOf(item.getRank_of_number()));
		((TextView)convertView.findViewById(R.id.rank_in_summary)).setVisibility(View.GONE);

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		((TextView)convertView.findViewById(R.id.monery_in_summary)).setText(String.valueOf(nf.format(item.getMoney())));

		((TextView)convertView.findViewById(R.id.co2_in_summary)).setText(String.valueOf(nf.format(item.getCo2())));

		((TextView)convertView.findViewById(R.id.price_in_summary)).setText(String.valueOf(nf.format(item.getPrice())));



		//評価ボタン押す
		number_of_star = (TextView) convertView.findViewById(R.id.number_of_star);
		buton_evaluate_it = (Button)convertView.findViewById(R.id.buton_evaluate_it);

		buton_evaluate_it.setOnClickListener(new StarCallback(item, ipp_auth_key, context, number_of_star));

		//スター
		if(flg_self_only != 1){
			number_of_star.setText(String.valueOf(item.getStar()));
		}else{
			((LinearLayout) convertView.findViewById(R.id.wrapper_star)).setVisibility(View.GONE);
			buton_evaluate_it.setVisibility(View.GONE);
		}




		return convertView;

	}

}
