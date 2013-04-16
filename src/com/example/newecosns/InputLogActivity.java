package com.example.newecosns;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jp.innovationplus.ipp.client.IPPApplicationResourceClient;
import jp.innovationplus.ipp.client.IPPApplicationResourceClient.QueryCondition;
import jp.innovationplus.ipp.client.IPPGeoLocationClient;
import jp.innovationplus.ipp.core.IPPQueryCallback;
import jp.innovationplus.ipp.jsontype.IPPGeoLocation;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import twitter4j.Status;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.newecosns.models.LogItem;
import com.example.newecosns.models.MarkItem;
import com.example.newecosns.models.PEBItem;
import com.example.newecosns.models.PairItem;
import com.example.newecosns.models.PictureItem;
import com.example.newecosns.models.ProductItem;
import com.example.newecosns.models.SummaryItem;
import com.example.newecosns.utnils.Constants;
import com.example.newecosns.utnils.LogSQLPraparation;
import com.example.newecosns.utnils.NetworkManager;
import com.example.newecosns.utnils.PEBListAccessor;
import com.example.newecosns.utnils.ProductSQLPraparation;
import com.example.newecosns.utnils.TwLoaderCallbacks;

public class InputLogActivity extends SherlockFragmentActivity implements TwLoaderCallbacks, LocationListener {

	String TAG = "inputLogActivity";

	//twitter関連
	static final String CALLBACK = "http://sns.uozias.jp";
	static final String CONSUMER_KEY = "AJOoyPGkkIRBgmjAtVNw";
	static final String CONSUMER_SECRET = "1OMzUfMcqy4QHkyT6jJoUyxN4KXEu7R87k3bVOzp8c";
	static final int REQUEST_OAUTH = 1;
	static final String hash_tag = "ecosns_test";
	private static long user_id = 0L;
	private static String screen_name = null;
	private static String token = null;
	private static String token_secret = null;
	SharedPreferences pref;
	CheckBox tweetCheckBox = null;



	View view = null;
	SQLiteDatabase sdb = null;
	SQLiteDatabase sdb_s = null;
	SQLiteDatabase peb_db= null;

	Timestamp timestamp = null; //現在の時間


	Button inputFixBtn = null;
	private int delete_id = 1;

	Intent i = null;

	//OAuthデータ保存用
	SharedPreferences ipp_pref;
	SharedPreferences.Editor ipp_editor;
	private String ipp_auth_key;
	private String ipp_id_string;
	private String ipp_pass_string;
	private String ipp_screen_name;
	private String team_resource_id;
	private String pair_common_id;
	private int role_self;
	 List<PairItem> pair_item_list = null;
	 String pair_item_list_string = null;
	//サマリー更新用
	int sum = 0;
	int summary_id = 0;

	private static  int NEW_ITEM_FLG = -1; //PEBやProductの新規登録の時につかう

	//ロケーション関連
	private LocationManager mLocationManager = null;
	private Location mNowLocation = null;


	//写真用
	static final int REQUEST_CAPTURE_IMAGE = 100;
	private ImageView pictireImageView = null;
	private Button takePicBtn = null;
	Bitmap capturedImage = null;
	String sended_picture_resource_id = null;
	double widthCapturedImage = 0;
	double heightCapturedImage = 0;
	File capturedFile = null;
	Uri imageUri   = null;

	//PEBのリスト表示用
	private HashMap<String, PEBItem> peb_list = null;
	private List<PEBItem> PEBItemList = null;
	static final int PURCHASING_PEB = 1; //購入系エコ行動のid

	//選択中のPEBのidをここに入れる
	String peb_id_selected = null;
	PEBItem peb_item_selected = null; //これはサマリーからの編集操作にも使う

	PEBItem peb_item_inputted = null;
	static int MAX_LENGTH_NAME = 12; //PEB名の最長

	//操作中のlogItem
	LogItem log_item_edited  = null;

	//操作対象のまとめ
	SummaryItem summary_item = null;

	//製品関連
	private HashMap<String, ProductItem> product_list = null;
	private List<ProductItem> ProductItemList = null;

	ProductItem product_item_selected  = null;
	String product_id_selected = null;

	ProductItem  product_item_inputted = null;

	//表示するリストの管理
	static String DEFEULT_TAG = "indoor";
	String selected_place_tag = DEFEULT_TAG;

	//3つのリスト
	PEBAdapter adapter_peb_indoor = null;
	PEBAdapter adapter_peb_outdoor = null;
	ProductAdapter adapter_product_purchase= null;


	//環境ラベルのリスト
	List<MarkItem> mark_list = null;
	MarkItem selected_mark_item = null;

	//製品情報入力フォーム
	TableRow table_row_product_name = null;
	TableRow table_row_product_selected_price  = null;
	TableRow table_row_product_selected_mark  = null;
	TableRow table_row_product_selected_producer = null;

	//EPB情報入力フォーム
	TableRow table_row_peb_name = null;



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//メイン処理
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_CANCELED);
        setContentView(R.layout.activity_input_log);

        //日時取得
        timestamp = new Timestamp(System.currentTimeMillis());

        //IPP Loginちぇっく
        ippLoginCheck();



  }

	@Override
	protected void onStart(){
		super.onStart();

		//行動リスト準備
		prepareTypeList();

		//写真用ビュー取得とクリックイベント登録
        picturePrepare();

		//位置情報関連準備
		 locationManagerPrepare();


		//既存データの編集か、新規データの登録か
		editCheck();

		//リストに出す
		prepareListChange();

		 //家計簿入力ボタンを押した時の処理
		 prapareSaveAction();

		 prapareMarkList();


		//ツイートする チェックボックスをタップしたら
		 /*
		 tweetCheckBox = (CheckBox) findViewById(R.id.check_tweet);
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

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//行動タイプリスト準備


	private void prepareTypeList() {

		//PEBリストの取得
		PEBListAccessor peb_list_accessor = new PEBListAccessor(getApplicationContext());
		peb_db = peb_list_accessor.getdb();

		String sqlstr = "select _id, name, text, money, coe, co2, place from peb_list";
		Cursor c = peb_db.rawQuery(sqlstr, null);
		c.moveToFirst();
		CharSequence[] list = new CharSequence[c.getCount()];

		//ハッシュマップとリストにいれとく
		peb_list = new HashMap<String, PEBItem>();
		PEBItemList = new LinkedList<PEBItem>();

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
				item.setPlace(c.getString(6));
				PEBItemList.add(item);
				peb_list.put(String.valueOf(item.getId()), item);
				c.moveToNext();
			}
		}

		peb_db.close();
		peb_list_accessor.closeDb();


		//プロダクトリストの取得

		ProductSQLPraparation product_sql_praparation = new ProductSQLPraparation(this.getApplicationContext());
		SQLiteDatabase product_sql_db = product_sql_praparation.getdb();

		sqlstr = "select _id, name, mark_id, producer from product";
		c = product_sql_db.rawQuery(sqlstr, null);
		c.moveToFirst();
		list = new CharSequence[c.getCount()];

		product_list =new HashMap<String, ProductItem>();
		ProductItemList =new LinkedList<ProductItem>();

		if(list.length > 0){
			for (int i = 0; i < list.length; i++) {
				//DB結果からモデルに
				ProductItem item = new ProductItem();
				item.setId(c.getInt(0));
				item.setName(c.getString(1));
				item.setMark_id(c.getInt(2));
				item.setProducer(c.getString(3));
				ProductItemList.add(item);
				product_list.put(String.valueOf(item.getId()), item);
				c.moveToNext();
			}
		}

	}


	void prapareMarkList(){
		Constants constants = new Constants();
		mark_list = constants.getMarkList();

		MarkAdapter adapter = new MarkAdapter(this, mark_list, android.R.layout.simple_spinner_item );
	    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


		Spinner spinner = (Spinner) findViewById(R.id.product_selected_mark);

		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                Spinner spinner = (Spinner) parent;

                // 選択されたアイテムを取得します
                InputLogActivity.this.selected_mark_item = (MarkItem) spinner.getSelectedItem();

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
	}

	class MarkAdapter extends ArrayAdapter<MarkItem>{
		private Context context = null;
		private int layout_id = 0;
		private LayoutInflater mInflater;


		public MarkAdapter(Context context, List<MarkItem> objects, int layout_id) {
			super(context, 0, objects);
			this.layout_id = layout_id;
			this.context = context;

			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			convertView = mInflater.inflate(layout_id, null);
			MarkItem mark_item = this.getItem(position);
			((TextView) convertView.findViewById(android.R.id.text1)).setText(mark_item.getName());

			return convertView;


		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			convertView = mInflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);

		    MarkItem mark_item = this.getItem(position);
		    ((TextView) convertView.findViewById(android.R.id.text1)).setText(mark_item.getName());
		    return convertView;
		}


	}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



	 //既存データの編集か、新規データの登録か
	 private void editCheck(){


	        /*
	         * 既存データの編集か、新規データの登録か
	         */
			i = getIntent();
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	        if(i.getLongExtra("id_in_user",0) != 0){

	        	peb_item_selected = new PEBItem();
	        	log_item_edited = new LogItem();

	        	//インテントからデータ受け取り
	        	log_item_edited.setUser_name(i.getStringExtra("user_name"));
	        	log_item_edited.setScreen_name(i.getStringExtra("screen_name"));
	        	log_item_edited.setUser_id(i.getLongExtra("user_id",0));


	        	log_item_edited.setCreated(i.getStringExtra("created"));
	        	log_item_edited.setCategory(i.getStringExtra("category"));
	        	log_item_edited.setName(i.getStringExtra("name"));
	        	log_item_edited.setPlace(i.getStringExtra("place"));
	        	log_item_edited.setMoney(i.getDoubleExtra("money",0));
	        	log_item_edited.setPeb_id(i.getIntExtra("peb_id",0));
	        	log_item_edited.setCoe(i.getDoubleExtra("coe",0));
	        	log_item_edited.setCo2(i.getDoubleExtra("co2",0));
	        	log_item_edited.setId_in_user( i.getLongExtra("id_in_user",0)); //端末内部DBでのid
	        	//log_item_edited.setLongitude(i.getDoubleExtra("longitude",0));
	        	//log_item_edited.setLatitude(i.getDoubleExtra("latitude",0));


	        	if(i.getByteArrayExtra("picture") != null){
	        		capturedImage = BitmapFactory.decodeByteArray(i.getByteArrayExtra("picture"),
		        			 0, i.getByteArrayExtra("picture").length); //写真
	        		pictireImageView.setImageBitmap(capturedImage); //写真
	        	}



	        	ListView list_view = null;
	        	//節約系エコ行動
	        	if(log_item_edited.getPeb_id() != 0 && log_item_edited.getProduct_id() == 0){
	        		peb_item_selected = peb_list.get(String.valueOf(log_item_edited.getPeb_id()));

		         	peb_id_selected = String.valueOf(peb_item_selected.getId());




		         	//表示用のPEBリストを容易
		         	try{


		         		list_view  = praparePEBList(peb_item_selected.getPlace());

		         	}finally{

		         		//ビューに値をいれる

		         		View target = null;
		          		if(peb_item_selected.getPlace().equals("indoor")){

		          			target = adapter_peb_indoor.getView(adapter_peb_indoor.getPosition(peb_item_selected), null, null);
		          			list_view.setItemChecked(adapter_peb_indoor.getPosition(peb_item_selected), true);


			         	}else if(peb_item_selected.getPlace().equals("outdoor")){
			         		target = adapter_peb_outdoor.getView(adapter_peb_outdoor.getPosition(peb_item_selected), null, null);
			         		list_view.setItemChecked(adapter_peb_outdoor.getPosition(peb_item_selected), true);

				        }

		          		//((CheckableLinearLayout) target).setChecked(true); //これでいいか分からん ここでいれてるのはハッシュマップの中身だが..


			         	target.findViewById(R.id.hiddenWrapperPEB).setVisibility(View.VISIBLE);


		        	}
	        	}


	         	//商品購入系

	         	if(log_item_edited.getProduct_id() != 0){
	         		product_item_selected =product_list.get(String.valueOf(log_item_edited.getProduct_id()));

		         	product_id_selected =String.valueOf(product_item_selected.getId());

		         	//表示用の商品リストを容易
		         	try{
		         		list_view  = praparePEBList("purchase");

		         	}finally{
		         		list_view.setItemChecked(adapter_product_purchase.getPosition(product_item_selected), true);
		         		View target = adapter_product_purchase.getView(adapter_product_purchase.getPosition(product_item_selected), null, null);
		         		//((CheckableLinearLayout) target).setChecked(true); //これでいいか分からん ここでいれてるのはハッシュマップの中身だが..

		         		target.findViewById(R.id.hiddenWrapperProduct).setVisibility(View.VISIBLE);
		         	}

	         	}


	         	//日付
	         	((TextView) findViewById(R.id.InputLogCreated)).setText(log_item_edited.getCreated());


	        }else{

	        	praparePEBList(DEFEULT_TAG);

	        	((TextView) findViewById(R.id.InputLogCreated)).setText(timestamp.toString());

	        }

	 }

		private ListView praparePEBList(String tag){
			//リストビュー取得
			ListView listView = (ListView) findViewById(R.id.PEBList);//自分で用意したListView

			if(tag.equals("purchase")){

				//新規作成用の特殊なリスト要素を追加
				List<ProductItem> PartOfProductItemList = new LinkedList<ProductItem>(ProductItemList); //コピー
				ProductItem new_item = new ProductItem();
				new_item.setId(NEW_ITEM_FLG);
				Resources res = getResources();
				new_item.setName(res.getString(R.string.new_product));
				new_item.setMark_id(0);
				PartOfProductItemList.add(0,new_item);


				adapter_product_purchase = new  ProductAdapter(this.getApplicationContext(), PartOfProductItemList, R.layout.row_product);
				adapter_product_purchase.hasStableIds();

				listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
				try{

					listView.setAdapter(adapter_product_purchase);
					listView.setOnItemClickListener(new ProductClickListener());


				} catch (NullPointerException e){
					Log.d(TAG, e.toString());
				}




			}else{
				List<PEBItem> PartOfPEBItemList = new LinkedList<PEBItem>();
				for(PEBItem peb_item :PEBItemList){

					if(peb_item.getPlace().equals(tag)){
						if(peb_item.getId() != PURCHASING_PEB){
							//購入系エコはリストから削除
							PartOfPEBItemList.add(peb_item);
						}

					}
				}
				//新規作成用の特殊なリスト要素を追加
				PEBItem new_item = new PEBItem();
				new_item.setId(NEW_ITEM_FLG );
				Resources res = getResources();
				new_item.setName(res.getString(R.string.new_peb));
				PartOfPEBItemList.add(0, new_item);





				if(tag.equals("indoor")){

					adapter_peb_indoor = new PEBAdapter(this.getApplicationContext(),PartOfPEBItemList, R.layout.row_peb);
					adapter_peb_indoor.hasStableIds();
					listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

					try{

						listView.setAdapter(adapter_peb_indoor);
						listView.setOnItemClickListener(new PEBClickListener());


					} catch (NullPointerException e){
						Log.d(TAG, e.toString());
					}



				}
				if(tag.equals("outdoor")){
					adapter_peb_outdoor = new PEBAdapter(this.getApplicationContext(),PartOfPEBItemList, R.layout.row_peb);
					adapter_peb_outdoor.hasStableIds();
					try{

						listView.setAdapter(adapter_peb_outdoor);
						listView.setOnItemClickListener(new PEBClickListener());


					} catch (NullPointerException e){
						Log.d(TAG, e.toString());
					}


				}


			}
			listView.setEmptyView(findViewById(android.R.id.empty));
			return listView;



		}


		//リスト内要素をタップした時の処理
		class PEBClickListener implements OnItemClickListener {
			private PEBItem item = null;

			@Override
			public void onItemClick(AdapterView<?> parent,  View view, int position, long id) {
				item = (PEBItem) parent.getItemAtPosition(position);

				int len = parent.getChildCount();
				for(int i = 0; i < len; i++){
					parent.getChildAt(i).findViewById(R.id.namePEB).setSelected(false);
					parent.getChildAt(i).findViewById(R.id.hiddenWrapperPEB).setVisibility(View.GONE);
				}
				if (item.getId() != NEW_ITEM_FLG){
					view.findViewById(R.id.hiddenWrapperPEB).setVisibility(View.VISIBLE);
					//新規PEB用のフォームを隠す
					table_row_peb_name.setVisibility(View.GONE);

					//このPEBのItemを記録しとく
					InputLogActivity.this.peb_id_selected = ((TextView) view.findViewById(R.id.idPEB)).getText().toString();
					InputLogActivity.this.peb_item_selected = item;

				}else{

					//新規PEB用のフォームを表示
					table_row_peb_name.setVisibility(View.VISIBLE);

					InputLogActivity.this.peb_item_selected = item;
					InputLogActivity.this.peb_id_selected = String.valueOf(item.getId());
				}


			}
		}

		class ProductClickListener implements OnItemClickListener{

			private ProductItem item = null;

			@Override
			public void onItemClick(AdapterView<?> parent,  View view, int position, long id) {
				item = (ProductItem) parent.getItemAtPosition(position);

				int len = parent.getChildCount();
				for(int i = 0; i < len; i++){
					parent.getChildAt(i).findViewById(R.id.nameProduct).setSelected(false);
					parent.getChildAt(i).findViewById(R.id.hiddenWrapperProduct).setVisibility(View.GONE);
				}
				if (item.getId() != NEW_ITEM_FLG){
					view.findViewById(R.id.hiddenWrapperProduct).setVisibility(View.VISIBLE);
					table_row_product_name.setVisibility(View.GONE);
					table_row_product_selected_price.setVisibility(View.VISIBLE);
					table_row_product_selected_mark.setVisibility(View.GONE);
					table_row_product_selected_producer.setVisibility(View.GONE);


					//このPEBのItemを記録しとく
					InputLogActivity.this.product_id_selected = ((TextView) view.findViewById(R.id.idProduct)).getText().toString();
					InputLogActivity.this.product_item_selected = item;

				}else{
					table_row_product_name.setVisibility(View.VISIBLE);
					table_row_product_selected_price.setVisibility(View.VISIBLE);
					table_row_product_selected_mark.setVisibility(View.VISIBLE);
					table_row_product_selected_producer.setVisibility(View.VISIBLE);

					//このPEBのItemを記録しとく

					InputLogActivity.this.product_item_selected = item;
					InputLogActivity.this.product_id_selected = String.valueOf(item.getId());

				}

				//peb_id_selectedはエコ購入
				peb_item_selected = peb_list.get(String.valueOf(PURCHASING_PEB));
				peb_id_selected = String.valueOf( peb_item_selected.getId());




			}


		}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 //表示するリストの管理
		private void prepareListChange(){

		 //各ボタンにリスナを登録
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
		radioGroup.setOnCheckedChangeListener(new PEBCategoryClickListener(selected_place_tag));

		table_row_product_name = (TableRow) findViewById(R.id.table_row_product_name);
		table_row_product_selected_price = (TableRow) findViewById(R.id.table_row_product_selected_price);
		table_row_product_selected_mark = (TableRow) findViewById(R.id.table_row_product_selected_mark);
		table_row_product_selected_producer = (TableRow) findViewById(R.id.table_row_product_selected_producer);

		table_row_peb_name = (TableRow) findViewById(R.id.table_row_peb_name);



	}

	private class PEBCategoryClickListener implements OnCheckedChangeListener{
		String selected_place_tag = null;


		public PEBCategoryClickListener(String selected_place_tag) {
			super();
			this.selected_place_tag = selected_place_tag;
		}

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {


			RadioButton radioButton = (RadioButton) findViewById(checkedId);
			selected_place_tag = radioButton.getTag().toString();
			//下部のフォーム処理
			if(selected_place_tag.equals("purchase")){
				//table_row_product_name.setVisibility(View.VISIBLE);
				table_row_product_selected_price.setVisibility(View.VISIBLE);
				//table_row_product_selected_mark.setVisibility(View.VISIBLE);

				table_row_peb_name.setVisibility(View.GONE);

			}else{
				table_row_product_name.setVisibility(View.GONE);
				table_row_product_selected_price.setVisibility(View.GONE);
				table_row_product_selected_mark.setVisibility(View.GONE);
				table_row_product_selected_producer.setVisibility(View.GONE);
			}

			InputLogActivity.this.selected_place_tag = selected_place_tag ;

			praparePEBList(selected_place_tag);

		}




	}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//PEBリスト用アダプタ

	public class PEBAdapter extends ArrayAdapter<PEBItem> {
		private Context context = null;
		private int layout_id = 0;
		private LayoutInflater mInflater;
		private View convertView = null;
		private ViewGroup parent = null;


		public PEBAdapter(Context context, List<PEBItem> objects, int layout_id) {
			super(context, 0, objects);
			this.layout_id = layout_id;
			this.context = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			this.convertView = convertView;
			this.parent = parent;
			if (convertView == null) {
				convertView = mInflater.inflate(layout_id, null); //todo ストレス切替
			}


			PEBItem item = this.getItem(position);


			((TextView) convertView.findViewById(R.id.namePEB)).setText(item.getName());

			//最初は隠し要素とするPEBの詳細情報
			if(item.getId() != NEW_ITEM_FLG ){
				((TextView) convertView.findViewById(R.id.textPEB)).setText(item.getText());

				NumberFormat nf = NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(2);
				if(item.getMoney() != 0){

					String monery = String.valueOf(nf.format(item.getMoney())); //丸めてstringに
					((TextView) convertView.findViewById(R.id.moneyPEB)).setText(monery);

				}
				if(item.getCo2() != 0){
					String co2 = String.valueOf(nf.format(item.getCo2())); //丸めてstringに
					((TextView) convertView.findViewById(R.id.co2PEB)).setText(co2);

				}

				if(item.getCo2() == 0 && item.getMoney() == 0){
					((LinearLayout) convertView.findViewById(R.id.detail_in_peb)).setVisibility(View.GONE);

				}


			}

			//選択中の要素だったら詳細表示
			View detail = convertView.findViewById(R.id.hiddenWrapperPEB);
			if(peb_item_selected != null){
				if(item.getId() == Integer.parseInt(peb_id_selected)){ //新規作成でなかったら
					if(item.getId() != NEW_ITEM_FLG )detail.setVisibility(View.VISIBLE);
					((ListView) parent).setItemChecked(position, true);
					//((CheckableLinearLayout) convertView).setChecked(true);
				}else{
					detail.setVisibility(View.GONE);
				}
			}



			//隠し要素
			((TextView) convertView.findViewById(R.id.idPEB)).setText(String.valueOf(item.getId()));







			return convertView;

		}



	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//製品リスト用アダプタ
	public class ProductAdapter extends ArrayAdapter<ProductItem> {
		private Context context = null;
		private int layout_id = 0;
		private LayoutInflater mInflater;
		private View convertView = null;
		private ViewGroup parent = null;


		public ProductAdapter(Context context, List<ProductItem> objects, int layout_id) {
			super(context, 0, objects);
			this.layout_id = layout_id;
			this.context = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			this.convertView = convertView;
			this.parent = parent;
			if (convertView == null) {
				convertView = mInflater.inflate(layout_id, null); //todo ストレス切替
			}

			ProductItem item = this.getItem(position);

			((TextView) convertView.findViewById(R.id.nameProduct)).setText(item.getName());

			//最初は隠し要素とする製品の詳細情報
			if(item.getId() !=NEW_ITEM_FLG ){

				String mark_name = mark_list.get(item.getMark_id()).getName();
				((TextView) convertView.findViewById(R.id.markProduct)).setText(mark_name);
				((TextView) convertView.findViewById(R.id.producerProduct)).setText(item.getProducer());





			}

			//選択中の要素だったら詳細表示
			View detail = convertView.findViewById(R.id.hiddenWrapperProduct);
			if(product_item_selected != null){
				if(item.getId() == Integer.parseInt(product_id_selected) ){
					if(item.getId() != NEW_ITEM_FLG ) detail.setVisibility(View.VISIBLE);
					((ListView) parent).setItemChecked(position, true);
					//((CheckableLinearLayout) convertView).setChecked(true);
				}else{
					detail.setVisibility(View.GONE);
				}
			}


			//隠し要素
			((TextView) convertView.findViewById(R.id.idProduct)).setText(String.valueOf(item.getId()));






			return convertView;

		}



	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void ippLoginCheck(){


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
			team_resource_id  = ipp_pref.getString("team_resource_id", "");
			role_self = ipp_pref.getInt("role_self", 0);
			pair_common_id = ipp_pref.getString("pair_common_id", "");
			pair_item_list_string = ipp_pref.getString("pair_item_list_string", "");

			ObjectMapper localObjectMapper = new ObjectMapper();
		    try
		    {
		      this.pair_item_list = (List<PairItem>)localObjectMapper.readValue(this.pair_item_list_string, new TypeReference<ArrayList<PairItem>>(){});
		      if (this.ipp_auth_key.equals(""))
		        startActivityForResult(new Intent(this, IPPLoginActivity.class), MainActivity.REQUEST_IPP_LOGIN);
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


	}




///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//位置情報関連準備

		private void locationManagerPrepare(){

		      //位置情報が利用できるか否かをチェック
				mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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


					//一番いい（？）位置情報を更新し続ける //どうやら機能していないらしいが
			        final String bestProvider = mLocationManager.getBestProvider(criteria, true);
			        mNowLocation = mLocationManager.getLastKnownLocation(bestProvider);
			        mLocationManager.requestLocationUpdates(bestProvider, 1000, 1, InputLogActivity.this);

				}


		}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 //家計簿入力ボタンを押した時の処理
		public void prapareSaveAction(){
			 /*
	         * クリック処理(家計簿入力)
	         */
	        inputFixBtn = (Button)this.findViewById(R.id.inputFixBtn);
	        inputFixBtn.setOnClickListener(new View.OnClickListener() {


				@Override
				public void onClick(View v) {

					startSavingFlow();
				}
			});
		 }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		private void startSavingFlow(){
			if(peb_item_selected != null){
				if(product_item_selected != null && selected_place_tag.equals("purchase")){
					if(product_item_selected.getId() == NEW_ITEM_FLG){
						//製品新規登録フロー
						 saveNewProduct();
					}else{
						//製品既存行動フロー
						try{
							prapareSendToPlatform();
						}finally{

							//内部DBへデータ入れる
							saveToInnerDB();

							//プラットフォームにデータおくる
							sendToPlatform();

							//オフライン登録にチェックを入れてなければ
							/*
							CheckBox offline_log_checkbox = (CheckBox) InputLogActivity.this.findViewById(R.id.offline_log);
							if(!offline_log_checkbox.isChecked()){
								sendToPlatform(); //プラットフォームにデータおくる
							}
							*/
						}
					}

				}else if(selected_place_tag.equals("indoor") || selected_place_tag.equals("outdoor")){
					if( peb_item_selected.getId() == NEW_ITEM_FLG ){
						//PEB新規登録フロー
						 saveNewPEB();
					}else{
						//PEB既存行動登録フロー
						try{
							prapareSendToPlatform();
						}finally{

							//内部DBへデータ入れる
							saveToInnerDB();

							//プラットフォームにデータおくる
							sendToPlatform();

							//オフライン登録にチェックを入れてなければ
							/*
							CheckBox offline_log_checkbox = (CheckBox) InputLogActivity.this.findViewById(R.id.offline_log);
							if(!offline_log_checkbox.isChecked()){
								sendToPlatform(); //プラットフォームにデータおくる
							}
							*/
						}


					}
				}


			}else{
				Toast.makeText(InputLogActivity.this, "エコ行動を選んで下さい。", Toast.LENGTH_SHORT).show();
			}

		}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		//IPPプラットフォームへの送信
		private void prapareSendToPlatform (){


			////////フォームから値を取り出してLogItemのインスタンスへ////////
			//LogItemのインスタンス作成
			if(log_item_edited == null){

				log_item_edited = new LogItem();
			}



			//どの画面からの行動かによって代る

			if(selected_place_tag.equals("indoor") || selected_place_tag.equals("outdoor") ){ //節約系PEBを選んでいたら
				log_item_edited.setPeb_id(peb_item_selected.getId());

				//カテゴリ
				log_item_edited.setCategory(""); //今はダミー

				//名前
				log_item_edited.setName(peb_item_selected.getName());

				//金額
				log_item_edited.setMoney(peb_item_selected.getMoney());

				//CO2
				log_item_edited.setCo2(peb_item_selected.getCo2());


				log_item_edited.setPlace(peb_item_selected.getPlace());

				log_item_edited.setPeb_resource_id(peb_item_selected.getResource_id());

			}else if(selected_place_tag.equals("purchase")){ //購入系PEBを選んでいたら
				log_item_edited.setProduct_id(product_item_selected.getId());

				log_item_edited.setPeb_id(PURCHASING_PEB);

				//カテゴリ
				log_item_edited.setCategory(""); //今はダミー

				//名前
				log_item_edited.setName(product_item_selected.getName());


				//価格 価格は常に入力してもらう
				String price_string = ((TextView)findViewById(R.id.product_selected_price)).getText().toString();
				try{
					log_item_edited.setPrice(Integer.parseInt(price_string)); // priceの取得フォーム
				}catch (NumberFormatException e){
					log_item_edited.setPrice(0);
				}

				log_item_edited.setPlace("purchase");

				log_item_edited.setProduct_resource_id(product_item_selected.getResource_id());


			}

			//チーム
			log_item_edited.setTeam_resource_id(team_resource_id);
			log_item_edited.setPair_common_id(team_resource_id);

			//ログインチェックも兼ねる
			if(ipp_id_string != null){
				//ユーザ名
				log_item_edited.setUser_name(ipp_id_string);

				//表示名(IPP)
				log_item_edited.setScreen_name(ipp_screen_name );

			}else{
				ippLoginCheck();
			}


			//更新と新規で違う部分の処理
			if(log_item_edited.getId_in_user() != 0){
				//もし更新なら

				//内部DBでのID
				//log_item_edited.setId_in_user(log_item_edited.getId_in_user());


				//更新日時
				log_item_edited.setUpdated(timestamp.toString());
			}else{
				//もし新規登録なら

				//作成日時
				log_item_edited.setCreated(timestamp.toString());
			}

			//緯度経度精度
			if(mNowLocation != null){
				log_item_edited.setLongitude(mNowLocation.getLongitude());
				log_item_edited.setLatitude(mNowLocation.getLatitude());
				log_item_edited.setAccuracy(mNowLocation.getAccuracy());
				log_item_edited.setProvider(mNowLocation.getProvider());

			}

			//プラットフォーム用タイムスタンプ
			log_item_edited.setTimestamp(timestamp.getTime());

			//写真(内部)
			if(capturedImage != null){
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				capturedImage.compress(CompressFormat.PNG, 100, baos);
				byte[] bytes = baos.toByteArray();
				log_item_edited.setPicture(bytes);
			}






			}



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////内部DBにデータを入れる////////


		public void saveToInnerDB(){


		    //内部DB処理準備(log)
			LogSQLPraparation db_prapare = new LogSQLPraparation(InputLogActivity.this.getApplicationContext());
			sdb =db_prapare.getdb();

			long log_id = 0; //これは使ってる

			ContentValues values = new ContentValues();

			values.put("category", log_item_edited.getCategory()); //dummy
			values.put("name", log_item_edited.getName());
			values.put("place", log_item_edited.getPlace());
			values.put("money", log_item_edited.getMoney());
			values.put("peb_id", log_item_edited.getPeb_id());
			values.put("co2", log_item_edited.getCo2());
			values.put("coe", log_item_edited.getCoe());

			values.put("latitude", log_item_edited.getLatitude());
			values.put("longitude", log_item_edited.getLongitude());
			values.put("provider", log_item_edited.getProvider());
			values.put("accuracy", log_item_edited.getAccuracy());

			values.put("product_id", log_item_edited.getProduct_id());
			values.put("price", log_item_edited.getPrice());
			values.put("place", log_item_edited.getPlace());

			values.put("timestamp",log_item_edited.getTimestamp()); //更新だろうと新規だろうと常に入る


			values.put("picture",log_item_edited.getPicture() ); //写真

			if(log_item_edited.getId_in_user() != 0){
				//もし更新なら
				values.put("updated",timestamp.toString());
				sdb.update("log", values, "_id="+log_item_edited.getId_in_user(), null);

			}else{
				//もし新規登録なら

				values.put("user_name",ipp_id_string );
				values.put("screen_name ",log_item_edited.getScreen_name());
				values.put("created",timestamp.toString());

				log_id = sdb.insert("log", null, values);
			}

			sdb.close();



			/*
			//もしツイートするにチェックをいれてたら
			CheckBox tweetCheckBox = (CheckBox) this.findViewById(R.id.check_tweet);
			if(tweetCheckBox.isChecked()){
				String tweetContent = peb_list.get(log_item_edited.getPeb_id()).getText();

				//ログインチェックとログイン画面への自動遷移
				checkLoginTwitter();

				//ツイート機能
				if (tweetContent.length() > 140) {
					//140字以内に収める処理
					tweetContent = tweetContent.substring(0,139);

				} else {

					Bundle args = new Bundle(1); //onCreateLoaderに渡したい値はここへ
					args.putString("tweetContent", tweetContent);
					//getSherlockActivity().getSupportLoaderManager().initLoader(0, args, CommentFragment.this);　 //こっちは自分にローダーを実装する場合

					TweetAsyncLoaderClass tweet_async_loader_class = new TweetAsyncLoaderClass(this, token, token_secret, getApplicationContext());
					getSupportLoaderManager().restartLoader(0, args, tweet_async_loader_class);
				}



			}
			*/

		}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////プラットフォームにデータおくる////////
	//個々の家計簿
	public void sendToPlatform(){


		if(capturedImage != null && sended_picture_resource_id == null){
			sendPicture(); //写真を送る
		}else{

			//写真(IPP)
			log_item_edited.setPicture_resource_id(sended_picture_resource_id);
			log_item_edited.setPicture(null); //プラットフォームではlogと写真のモデルは分離


			//圏外でなければ
			if(NetworkManager.isConnected(InputLogActivity.this.getApplicationContext())){

				IPPApplicationResourceClient client = new IPPApplicationResourceClient(InputLogActivity.this);
				client.setAuthKey(ipp_auth_key);
				client.setDebugMessage(true);
				client.create(LogItem.class, log_item_edited, new InputLogCallback());

			}else{
				//圏外だったら
				Toast.makeText(InputLogActivity.this, "圏外のため送信できませんでした", Toast.LENGTH_LONG).show();

			}

			//メインアクティビティ(家計簿まとめ画面)に戻る
			sdb.close();


			Intent intent = new Intent(InputLogActivity.this, MainActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

			updateSummary(); //サマリーの更新

		}


	}

	  //ログをIPPプラットフォームに送信したコールバック
	  private class InputLogCallback implements IPPQueryCallback<String> {

		@Override
		public void ippDidError(int paramInt) {
			Toast.makeText(InputLogActivity.this, "家計簿送信失敗", Toast.LENGTH_SHORT).show();

		}

		@Override
		public void ippDidFinishLoading(String resource_id) {
		//登録したlogのリソースidを返してくれるらしい

			if(resource_id != null){
				IPPGeoLocation geo_location = new IPPGeoLocation();
				geo_location.setLongitude(mNowLocation.getLongitude());
				geo_location.setLatitude(mNowLocation.getLatitude());
				geo_location.setAccuracy(mNowLocation.getAccuracy());
				geo_location.setTimestamp(mNowLocation.getTime());
				geo_location.setProvider(mNowLocation.getProvider());
				geo_location.setResource_id(resource_id);

				IPPGeoLocationClient geo_location_client = new IPPGeoLocationClient(InputLogActivity.this);
				geo_location_client.setAuthKey(ipp_auth_key);
				geo_location_client.create(geo_location, new geoPostCallback());
			}
		}
	  }

		//位置情報送信後のコールバック
		class geoPostCallback implements IPPQueryCallback<String> {

			@Override
			public void ippDidError(int arg0) {
				Log.d(TAG, String.valueOf(arg0));

			}


			@Override
			public void ippDidFinishLoading(String arg0) {
				Log.d(TAG, arg0);
			}
		}



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////





//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



	//サマリーの更新
	private void updateSummary(){

		//自分のサマリーを呼ぶ
		IPPApplicationResourceClient summary_client = new IPPApplicationResourceClient(this);
		summary_client.setAuthKey(ipp_auth_key);
		QueryCondition condition = new QueryCondition();
		condition.setSelf();
		condition.build();
		summary_client.query(SummaryItem.class, condition, new SummaryGetCallback());


	}

	//自分のサマリーを呼び出すコールバック
	class SummaryGetCallback implements IPPQueryCallback<SummaryItem[]>{

		@Override
		public void ippDidError(int arg0) {
			Log.d(TAG,String.valueOf(arg0));

		}

		@Override
		public void ippDidFinishLoading(SummaryItem[] arg0) {

			IPPApplicationResourceClient summary_client = new IPPApplicationResourceClient(InputLogActivity.this);
			summary_client.setAuthKey(ipp_auth_key);

			if(arg0.length != 0){
				//もしサマリーがあれば
				summary_item = arg0[0]; //一番新しいのをみてみる


				Calendar calendar_now = Calendar.getInstance();
				calendar_now.setTimeInMillis(timestamp.getTime()); //今の日時

				Calendar calendar_got_summary =  Calendar.getInstance();
				calendar_got_summary.setTimeInMillis(summary_item.getTimestamp());//取得したまとめの日時


				//もし今の月のやつじゃなかったら
				if(calendar_got_summary.get(Calendar.YEAR) == calendar_now.get(Calendar.YEAR) &&
					calendar_got_summary.get(Calendar.MONTH) == calendar_now.get(Calendar.MONTH)){
					//古いの消す
					summary_client.delete(SummaryItem.class,summary_item.getResource_id(), new SummaryDeleteCallback());
				}else{
					summary_item = new SummaryItem();
					summary_item.setScreen_name(ipp_screen_name);
				}


			}else{
				//もしサマリーがなければ
				summary_item = new SummaryItem();
				summary_item.setScreen_name(ipp_screen_name);
			}
			//値の更新
			summary_item.setNumber(summary_item.getNumber()+ 1);
			summary_item.setMoney(summary_item.getMoney()+ log_item_edited.getMoney());
			summary_item.setCo2(summary_item.getCo2()+ log_item_edited.getCo2());

			summary_item.setTimestamp(timestamp.getTime());
			summary_item.setUpdated(timestamp.toString());

			//チーム
			summary_item.setTeam_resource_id(team_resource_id);

			//更新,というかあげなおす
			summary_client.create(SummaryItem.class, summary_item, new SummaryUpdateCallback());
		}


	}

	//自分のサマリーを一旦消す処理のコールバック
	class SummaryDeleteCallback implements IPPQueryCallback<String>{

		@Override
		public void ippDidError(int arg0) {
			Log.d(TAG,String.valueOf(arg0));

		}

		@Override
		public void ippDidFinishLoading(String arg0) {


		}


	}

	//自分のサマリーをもう一回上げるコールバック
	class SummaryUpdateCallback implements IPPQueryCallback<String>{

		@Override
		public void ippDidError(int arg0) {
			Log.d(TAG,String.valueOf(arg0));


		}

		@Override
		public void ippDidFinishLoading(String arg0) {
			Toast.makeText(InputLogActivity.this, "まとめ更新成功", Toast.LENGTH_SHORT).show();

		}


	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//PEBの内部登録
	private void saveNewPEB(){


		//データの用意
		peb_item_inputted = new PEBItem();

		peb_item_inputted.setAuthor_name(ipp_id_string);

		String peb_name_string = ((TextView) findViewById(R.id.product_peb_name)).getText().toString();
		if(peb_name_string.length() == 0){//入力チェク


			//Toast.makeText(this, getResources().getString(R.string.), Toast.LENGTH_SHORT).show();
			return;
		}else{
			if(peb_name_string.length() > MAX_LENGTH_NAME){
				peb_item_inputted.setText(peb_name_string);
				peb_item_inputted.setName(peb_name_string.substring(0, MAX_LENGTH_NAME -1)); //最長より長いのは縮めていれる
			}else{

				peb_item_inputted.setText(peb_name_string);

				peb_item_inputted.setName(peb_name_string);

			}
		}

		peb_item_inputted.setPlace(selected_place_tag); //タグを信用して場所にする

		peb_item_inputted.setTimestamp(timestamp.getTime());


		//IPPプラットフォーム登録
		IPPApplicationResourceClient resource_client = new IPPApplicationResourceClient(this.getApplicationContext());
		resource_client.setAuthKey(ipp_auth_key);
		resource_client.create(PEBItem.class, peb_item_inputted, new PEBRegistCallback());


		return;

	}

	//PEB登録コールバック
	private class PEBRegistCallback implements IPPQueryCallback<String>{

		@Override
		public void ippDidError(int arg0) {
			Log.d(TAG, String.valueOf(arg0));

		}

		@Override
		public void ippDidFinishLoading(String resource_id) {
			//内部登録

			PEBListAccessor peb_db = new PEBListAccessor(InputLogActivity.this.getApplicationContext());
			sdb =peb_db.getdb();

			ContentValues values = new ContentValues();
			values.put("name", peb_item_inputted.getName());
			values.put("text", peb_item_inputted.getText());
			values.put("place", peb_item_inputted.getPlace());
			values.put("author_name",peb_item_inputted.getAuthor_name());
			values.put("resource_id",resource_id);
			peb_item_inputted.setId(sdb.insert("peb_list", null, values));

			peb_item_inputted.setResource_id(resource_id);


			sdb.close();
			peb_db.closeDb();

			peb_item_selected = peb_item_inputted;
			startSavingFlow();

		}


	}


	//Procuctの登録
	private void saveNewProduct(){

		//データの用意
		product_item_inputted = new ProductItem();

		product_item_inputted.setAuthor_name(ipp_id_string);

		String peb_name_string = ((TextView) findViewById(R.id.product_selected_name)).getText().toString();
		if(peb_name_string.length() == 0){ //入力チェク

			//Toast.makeText(this, getResources().getString(R.string.), Toast.LENGTH_SHORT).show();
			return;
		}else{
			product_item_inputted.setName(peb_name_string);
		}
		int mark_id = selected_mark_item.getId();
		product_item_inputted.setMark_id(mark_id);


		product_item_inputted.setProducer(((TextView) findViewById(R.id.product_selected_producer)).getText().toString());

		product_item_inputted.setTimestamp(timestamp.getTime());


		//IPPプラットフォーム登録
		IPPApplicationResourceClient resource_client = new IPPApplicationResourceClient(this.getApplicationContext());
		resource_client.setAuthKey(ipp_auth_key);
		resource_client.create(ProductItem.class, product_item_inputted, new ProductRegistCallback());



		return;
	}


	//製品登録コールバック
		private class ProductRegistCallback implements IPPQueryCallback<String>{

			@Override
			public void ippDidError(int arg0) {
				Log.d(TAG, String.valueOf(arg0));

			}

			@Override
			public void ippDidFinishLoading(String resource_id) {
				//内部登録

				ProductSQLPraparation product_db = new ProductSQLPraparation(InputLogActivity.this.getApplicationContext());
				sdb =product_db.getdb();

				ContentValues values = new ContentValues();
				values.put("name", product_item_inputted.getName());
				values.put("mark_id", product_item_inputted.getMark_id());
				values.put("author_name",product_item_inputted.getAuthor_name());
				values.put("producer",product_item_inputted.getProducer());
				values.put("resource_id",resource_id);
				product_item_inputted.setId(sdb.insert("product", null, values));
				sdb.close();
				product_db.closeDb();

				product_item_inputted.setResource_id(resource_id);

				product_item_selected = product_item_inputted;
				startSavingFlow();


			}


		}





	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void picturePrepare(){

		 //写真用ビュー取得とクリックイベント登録
		takePicBtn = (Button)findViewById(R.id.takePicBtn);

		pictireImageView = (ImageView)findViewById(R.id.pictureImageView);
		takePicBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		   		capturedFile = new File( Environment.getExternalStorageDirectory(), "captured.jpg" );
		   		imageUri = Uri.fromFile( capturedFile );
				intent.putExtra( MediaStore.EXTRA_OUTPUT, imageUri );
				//組み込みの写真画面へ
				startActivityForResult(
					intent,
					REQUEST_CAPTURE_IMAGE);
			}
		});

	}

	//写真撮影後の処理
	@Override
	protected void onActivityResult(int requestCode,int resultCode,	Intent data) {
		if(REQUEST_CAPTURE_IMAGE == requestCode
			&& resultCode == Activity.RESULT_OK ){

			//TODO 関数化
			int orientation  = 0;
			if(data == null){
				FileInputStream fis = null;
				try {
					fis = new FileInputStream( capturedFile );

				} catch (FileNotFoundException e) {
					Log.d(TAG,e.toString());
				}
				if(fis != null){

					try {
						ExifInterface ei = new ExifInterface( capturedFile.getAbsolutePath() );
						orientation = ei.getAttributeInt( ExifInterface.TAG_ORIENTATION,0 );
					} catch (IOException e) {
						Log.d(TAG,e.toString());
					}

					capturedImage = BitmapFactory.decodeStream( fis );

				}

			}else{
				if (data.getData() == null){
					FileInputStream fis = null;
					try {
						fis = new FileInputStream( capturedFile );

					} catch (FileNotFoundException e) {
						Log.d(TAG,e.toString());
					}
					if(fis != null){

						try {
							ExifInterface ei = new ExifInterface( capturedFile.getAbsolutePath() );
							orientation = ei.getAttributeInt( ExifInterface.TAG_ORIENTATION,0 );
						} catch (IOException e) {
							Log.d(TAG,e.toString());
						}

						capturedImage = BitmapFactory.decodeStream( fis );
					}
				}else{
					try {
						capturedImage = getBitmapFromUri(data.getData());
					} catch (IOException e) {
						Log.d(TAG, e.toString());
					}

					try {
						ExifInterface ei = new ExifInterface( data.getData().getPath());
						orientation = ei.getAttributeInt( ExifInterface.TAG_ORIENTATION,0 );
					} catch (IOException e) {
						Log.d(TAG,e.toString());
					}

				}


			}
			if(capturedImage==null){
				try{
				capturedImage = (Bitmap) data.getExtras().get("data");
				}catch (NullPointerException e){
					Log.d(TAG,e.toString());

				}
			}

			if(capturedImage!=null){
				//縮小処理tと回転処理
				capturedImage = pictureSizeDown(capturedImage, orientation);
				pictireImageView.setImageBitmap(capturedImage);

				widthCapturedImage = capturedImage.getWidth();
				heightCapturedImage = capturedImage.getHeight();

				pictireImageView.setLayoutParams(new LinearLayout.LayoutParams((int) widthCapturedImage,  (int) heightCapturedImage));

			}else{
				Resources res = getResources();
				Toast.makeText(this, res.getText(R.string.message_camera_cannot_used_in_inputlog), Toast.LENGTH_SHORT).show();

			}

		}

		 if ((requestCode == MainActivity.REQUEST_IPP_LOGIN) && (resultCode == 200)){
		      startActivity(new Intent(this, MainActivity.class));
		  }

	}

	//写真縮小
	private Bitmap pictureSizeDown(Bitmap bmp, int orientation){
	    int R = 0;
		switch(orientation){
	        case 6:
	            R=90;
	            break;
	        case 1:
	            R=0;
	            break;
	        case 8:
	            R=270;
	            break;
	        case 3:
	            R=180;
	            break;
        }

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap2;

		Matrix matrix = new Matrix();
		//widthが100以下になるようにしよう
		float tmp = 320f / height;

		matrix.postRotate(R);
		matrix.postScale(tmp, tmp);

		bitmap2 = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

		return  bitmap2;
	}


	/*
	 private String getExifString(ExifInterface ei,String tag) {
		    // getAttributeNULL
		    // EXIFNULL
		    return tag + ": " + ei.getAttribute(tag);
		    }
	*/
	private Bitmap getBitmapFromUri(Uri imageUri) throws IOException {



		//画像自体は読み込まず、画像サイズなどのみを読み込む
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		ContentResolver conReslv = getContentResolver();
	    InputStream iStream = conReslv.openInputStream(imageUri);
	    BitmapFactory.decodeStream(iStream, null, options);

		//読み込むスケールを計算する
		int scaleW = options.outWidth / 320+1;
		int scaleH = options.outHeight / 240+1;
		options.inSampleSize = Math.max(scaleW, scaleH);

		//計算したスケールで画像を読み込む
		options.inJustDecodeBounds = false;
		iStream = conReslv.openInputStream(imageUri);
		Bitmap bmp = BitmapFactory.decodeStream(iStream, null, options);


        return bmp;
	}

	//写真送信
		private void sendPicture() {
		//写真の送信
		//写真 取得
		PictureItem pictureItem = new PictureItem();

		pictureItem.setTimestamp(timestamp.getTime());
		if(capturedImage != null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			capturedImage.compress(CompressFormat.PNG, 100, baos);
			byte[] bytes = baos.toByteArray();
			pictureItem.setWidth(widthCapturedImage);
			pictureItem.setHeight(heightCapturedImage);
			pictureItem.setData(bytes);
		}

		IPPApplicationResourceClient client = new IPPApplicationResourceClient(InputLogActivity.this);
		client.setAuthKey(ipp_auth_key);
		client.setDebugMessage(true);
		client.create(PictureItem.class, pictureItem, new picPostCallback());
	}

	//写真送信後のコールバック
	class picPostCallback implements IPPQueryCallback<String>{

		@Override
		public void ippDidError(int arg0) {
			Log.d(TAG, String.valueOf(arg0));
			Toast.makeText(InputLogActivity.this.getApplicationContext(), "写真送信に失敗", Toast.LENGTH_SHORT).show();

		}

		@Override
		public void ippDidFinishLoading(String resource_id) {
			sended_picture_resource_id = resource_id;
			sendToPlatform();
		}


	}






////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//オプションメニューの表示
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (log_item_edited != null){
			if (log_item_edited.getId_in_user() != 0) { //削除ボタン 編集の時だけ表示
				MenuItem go_setting_page = menu.add(0, delete_id, Menu.NONE, "削除");
				go_setting_page.setIcon(android.R.drawable.ic_menu_preferences);
				go_setting_page.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			}

		}


		return true;

	}



	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		LogSQLPraparation db_prapare = new LogSQLPraparation(this.getApplicationContext());
		sdb = db_prapare.getdb();

		//DBから当該データを削除
		if (item.getItemId() == delete_id) {


			try {
				sdb.delete("log", "_id=" + log_item_edited.getId_in_user(), null);
			} catch (SQLiteException e) {
				Log.d(TAG, e.getMessage());
			}

			sdb.close();
			db_prapare.closeDb();

			finish();
		}
		return true;

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//戻るボタンのアニメ
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			return true;
		}
		return false;
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


	}

	@Override
	public void onProviderEnabled(String arg0) {


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
	protected void onPause() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(this);
		}
		super.onPause();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//ログインチェック
	public void checkLoginTwitter() {
		pref = getSharedPreferences("t4jdata", Context.MODE_PRIVATE);
		token = pref.getString("token", "");
		token_secret = pref.getString("token_secret", "");
		screen_name = pref.getString("screen_name", "");
		user_id = pref.getLong("user_id", 0L);
		if (token.length() == 0) { //もし未認証だったら
			Intent intent = new Intent(this, OAuthActivity.class);
			intent.putExtra(OAuthActivity.CALLBACK, CALLBACK);
			intent.putExtra(OAuthActivity.CONSUMER_KEY, CONSUMER_KEY);
			intent.putExtra(OAuthActivity.CONSUMER_SECRET, CONSUMER_SECRET);
			startActivityForResult(intent, REQUEST_OAUTH);
		}

	}

	@Override
	public void TweetLoaderCallbacks(Status result, Loader<Status> paramLoader) {
		// TODO 自動生成されたメソッド・スタブ

	}




}
