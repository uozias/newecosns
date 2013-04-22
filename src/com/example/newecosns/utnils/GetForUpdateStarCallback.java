package com.example.newecosns.utnils;

import jp.innovationplus.ipp.core.IPPQueryCallback;
import jp.innovationplus.ipp.jsontype.IPPApplicationResource;
import android.content.Context;

class GetForUpdateStarCallback implements IPPQueryCallback<IPPApplicationResource>{

	private Context context;

	public GetForUpdateStarCallback(Context context){
		this.context = context;
	}

	@Override
	public void ippDidError(int arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}


	@Override
	public void ippDidFinishLoading(IPPApplicationResource arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}


}