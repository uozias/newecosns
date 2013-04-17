package com.example.newecosns.utnils;



import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.newecosns.R;




public class TabListener<T extends SherlockFragment> implements ActionBar.TabListener {
	private SherlockFragment mFragment;
	private SherlockFragmentActivity mActivity = null;
	private String mTag = null;
	private Class<T> mClass = null;


	//IPPユーザデータとTwitterOAuthデータ保存用
	SharedPreferences ipp_pref;
	SharedPreferences.Editor ipp_editor;
	SharedPreferences tw_pref;
	SharedPreferences.Editor tw_editor;

	private String ipp_auth_key;
	private String ipp_id_string;
	private String ipp_pass_string;
	private static long user_id=0L;
	private static String screen_name=null;
	private static String token=null;
	private static String token_secret=null;

	private String ipp_screen_name;
	private String team_resource_id;
	private String stress_now = null;
	private int role_self  = 0;

	public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
		mActivity = activity;
		mTag = tag;
		mClass = clz;
		//this.stress_now = stress_now;
		//おそらく第一引数には呼び出し側のアクティビティが入っているんだろう
		//第２引数のタグに対応したフラグメントを取得
		mFragment = (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		//ftはバグで常にnullらしい
		if (mFragment == null) {
			mFragment = (SherlockFragment) Fragment.instantiate(mActivity, mClass.getName()); //よく分からん 第三引数のclzは何？
			FragmentManager fm = mActivity.getSupportFragmentManager();

			//ログイン状態を渡す
			Bundle bundle = new Bundle();


			//IPPログイン状態を渡す
			/*
			ipp_pref =mActivity.getSharedPreferences("IPP", Context.MODE_PRIVATE);
			ipp_auth_key = ipp_pref.getString("ipp_auth_key", "");
			ipp_id_string = ipp_pref.getString("ipp_id_string", "");
			ipp_pass_string = ipp_pref.getString("ipp_pass","");
			ipp_screen_name = ipp_pref.getString("ipp_screen_name","");
			stress_now = ipp_pref.getString("stress_now","");
			team_resource_id = ipp_pref.getString("team_resource_id","");
			role_self = ipp_pref.getInt("role_self",0);


			bundle.putString("ipp_auth_key", ipp_auth_key );
			bundle.putString("ipp_id_string", ipp_id_string);
			bundle.putString("ipp_pass_string", ipp_pass_string );
			bundle.putString("ipp_screen_name", ipp_screen_name );

			bundle.putString("team_resource_id", team_resource_id );
			bundle.putInt("role_self", role_self );
			bundle.putString("stress_now", stress_now );

			//twitterログイン状態を渡す
			tw_pref = mActivity.getSharedPreferences("t4jdata", Context.MODE_PRIVATE);
			token = tw_pref.getString("token", "");
			token_secret = tw_pref.getString("token_secret", "");
			screen_name = tw_pref.getString("screen_name", "");
			user_id = tw_pref.getLong("user_id", 0L);
			bundle.putString("token ", token );
			bundle.putString("token_secret", token_secret );
			bundle.putString("screen_name", screen_name );
			*/

			mFragment.setArguments(bundle);

			fm.beginTransaction().add(R.id.container, mFragment, mTag).commit(); //おそらくcontainer内に指定されたフラグメントを表示している
			//fm.beginTransaction().add(R.id.container, mFragment, mTag).commitAllowingStateLoss();

		} else {
			if (mFragment.isDetached()) {
				FragmentManager fm = mActivity.getSupportFragmentManager();
				fm.beginTransaction().attach(mFragment).commit();
				//fm.beginTransaction().attach(mFragment).commitAllowingStateLoss();

			}
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {


	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		//フラグメントをUIから取り外す
		if (mFragment != null) {
			FragmentManager fm = mActivity.getSupportFragmentManager();
			fm.beginTransaction().detach(mFragment).commit();
			//fm.beginTransaction().detach(mFragment).commitAllowingStateLoss();
		}
	}




}