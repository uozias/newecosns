package com.example.newecosns;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

import jp.innovationplus.ipp.client.IPPApplicationResourceClient;
import jp.innovationplus.ipp.core.IPPQueryCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.newecosns.R;
import com.example.newecosns.models.LogItem;
import com.example.newecosns.models.PictureItem;
import com.example.newecosns.utnils.ImageCache;

public class LogAdapter extends ArrayAdapter<LogItem> {
	private LayoutInflater mInflater;
	private Context context = null;
	private int layout_id = 0;
	private HashMap<String, ImageView> pictureViewList = null;
	private HashMap<String, ProgressBar> progressBarList = null;
	private IPPQueryCallback callback = null;
	private String ipp_auth_key = null;


	//外部DBからの読み出し用コンストラクタ
	public LogAdapter(Context context,   List<LogItem> objects, int layout_id,  IPPQueryCallback callback, String ipp_auth_key) {
		super(context,  0, objects);
		this.context = context;
		this.layout_id = layout_id;
		this.callback = callback;
		this.ipp_auth_key = ipp_auth_key;

		pictureViewList = new HashMap<String, ImageView>();
		progressBarList = new HashMap<String, ProgressBar>();

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	//内部DBからの読み出し用コンストラクタ
	public LogAdapter(Context context, List<LogItem> logItemList, int layout_id) {
		super(context,  0,logItemList);
		this.context = context;
		this.layout_id = layout_id;

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}



	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(layout_id, null); //todo ストレス切替
		}

		//データが入ってるオブジェクトをとる
		LogItem item = this.getItem(position);
		if(item != null){
			((TextView)convertView.findViewById(R.id.InputLogPebIdInList)).setText(String.valueOf(item.getPeb_id()));

			((TextView)convertView.findViewById(R.id.InputLogCategoryInList)).setText(item.getCategory());
			///

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


			((TextView)convertView.findViewById(R.id.InputLogCreatedInList)).setText(String.valueOf(item.getCreated()));


			//写真
			ImageView imageView = (ImageView)convertView.findViewById(R.id.pictureInList);
			ProgressBar waitBar = (ProgressBar)convertView.findViewById(R.id.ProgressBarInList);


			//内部DBのとき
			if(callback == null){

				waitBar.setVisibility(View.GONE);

				if(item.getPicture() != null){
					Bitmap bitmap = BitmapFactory.decodeByteArray(item.getPicture(), 0, item.getPicture().length);

					((ImageView)convertView.findViewById(R.id.pictureInList)).setImageBitmap(bitmap);
				}else{
					((ImageView)convertView.findViewById(R.id.pictureInList)).setImageBitmap(null);
				}


			}else{

				//外部DBのとき
				if (item.getPicture_resource_id() != null){
					Bitmap bitmap = ImageCache.getImage(item.getPicture_resource_id()); //写真のリソースidをキーにキャッシュしている
					if(bitmap == null){
						//写真の非同期読み込み処理

						waitBar.setVisibility(View.VISIBLE);

						imageView.setVisibility(View.GONE);
						imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.abs__ab_bottom_transparent_dark_holo));

						pictureViewList.put(item.getPicture_resource_id(), imageView); //イメージビューをリストに保持
						progressBarList.put(item.getPicture_resource_id(), waitBar);

						IPPApplicationResourceClient public_resource_client = new IPPApplicationResourceClient(context.getApplicationContext()); //写真のリソースid指定
						public_resource_client.setAuthKey(ipp_auth_key);
						String target_resource_id = item.getPicture_resource_id();
						public_resource_client.get( PictureItem.class, target_resource_id,callback);
					}else{
						 imageView.setImageBitmap(bitmap);
						 waitBar.setVisibility(View.GONE);
					}

				}else{

					waitBar.setVisibility(View.GONE);
				}

			}

			//ユーザ内idは隠しビュー
			TextView logcidinuser = (TextView)convertView.findViewById(R.id.InputLogIdInUserInList);
			logcidinuser.setText(item.getCreated());
			logcidinuser.setVisibility(View.GONE);

		}
		return convertView;

	}



	public HashMap<String, ImageView> getPictureViewList() {
		return pictureViewList;
	}






	public HashMap<String, ProgressBar> getProgressBarList() {
		return progressBarList;
	}



}
