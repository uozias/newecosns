package com.example.newecosns.utnils;

import twitter4j.Status;

public interface TwLoaderCallbacks {
	public void TweetLoaderCallbacks(Status result, android.support.v4.content.Loader<Status> paramLoader);
}
