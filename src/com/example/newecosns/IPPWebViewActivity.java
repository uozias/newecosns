package com.example.newecosns;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class IPPWebViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ippweb_view);

		//webビューの設定
		WebView webView = (WebView)findViewById(R.id.webview_in_ipp_webview);
	    webView.setWebViewClient(new WebViewClient());
	    Resources res = getResources();
	    webView.loadUrl(res.getString(R.string.url_ipp_login));

	    //mainActivityに戻るボタン
	    Button button = (Button) findViewById(R.id.button_label_back_to_main_in_ippwebview);
	    button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ippweb_view, menu);
		return true;
	}

}
