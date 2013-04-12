package com.example.newecosns.utnils;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class TweetTaskLoader extends AsyncTaskLoader<Status> {

	private String token=null;
	private String token_secret=null;
	private String tweetContent=null;

	static final String CONSUMER_KEY = "AJOoyPGkkIRBgmjAtVNw";
	static final String CONSUMER_SECRET = "1OMzUfMcqy4QHkyT6jJoUyxN4KXEu7R87k3bVOzp8c";


	private Context context;

	public TweetTaskLoader(Context context, String tweetContent, String token, String token_secret) {
		super(context);
		this.context = context;
		this.token = token;
		this.token_secret = token_secret;
		this.tweetContent = tweetContent;
	}

	@Override
	public Status loadInBackground() {
		// バックグラウンド送信
		AccessToken accessToken=new AccessToken(token,token_secret);
		Configuration conf = getConfiguration();
		TwitterFactory twitterfactory = new TwitterFactory(conf);
		Twitter twitter = twitterfactory.getInstance(accessToken);

		Status status = null;

		try {
			status = twitter.updateStatus(tweetContent);
		} catch (TwitterException e) {
			//e.printStackTrace();
			//Log.d("BACK GROUND", e.toString());

		}

		return status;
	}


	private static Configuration getConfiguration() {
		ConfigurationBuilder confbuilder = new ConfigurationBuilder();
		confbuilder.setOAuthConsumerKey(CONSUMER_KEY);
		confbuilder.setOAuthConsumerSecret(CONSUMER_SECRET);
		return confbuilder.build();
	}
}



