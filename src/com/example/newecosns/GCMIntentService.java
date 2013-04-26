package com.example.newecosns;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService{

	@Override
	protected void onError(Context context, String errorId) {
		// TODO エラーの評価

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		// TODO データ取り出して表示

	}

	@Override
	protected void onRegistered(Context context, String regId) {
		// TODO appサーバにregIdを送る

	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		// TODO appサーバにregIdを送る

	}

}
