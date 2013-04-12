package com.example.newecosns.utnils;


import twitter4j.Status;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.Toast;

public class TweetAsyncLoaderClass implements LoaderCallbacks<Status>{
	private TwLoaderCallbacks mInterface;
	private String token_secret=null;
	private String token=null;
	private Context context=null;
	private String tweetContent = null;


	public TweetAsyncLoaderClass(TwLoaderCallbacks mInterface,  String token, String token_secret,Context context){
		this.mInterface = mInterface;
		this.token = token;
		this.token_secret = token_secret;
		this.context = context;
		this.tweetContent = tweetContent;


	}


	@Override
	public Loader<Status> onCreateLoader(int arg0, Bundle arg1) {
		String tweetContent = arg1.getString("tweetContent");
		TweetTaskLoader loader = new TweetTaskLoader(context, tweetContent,
				token, token_secret);
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Status> loader, Status result) {
		Toast toast = null;
		if (result != null) {
			toast = Toast.makeText(context, "ツイート成功", Toast.LENGTH_LONG);
		} else {

			toast = Toast.makeText(context, "ツイート失敗", Toast.LENGTH_LONG);
		}

		toast.show();

	}

	@Override
	public void onLoaderReset(Loader<Status> arg0) {


	}
}
