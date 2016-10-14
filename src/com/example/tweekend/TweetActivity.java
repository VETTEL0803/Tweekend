package com.example.tweekend;
/*
 * Tweet投稿用クラス
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetActivity extends Activity {

	private EditText mInputText;
	private Twitter mTwitter;
	ImageButton button1;
	ImageButton button2;
	ImageButton button3;
	ImageButton button4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet);

		mTwitter = TwitterUtils.getTwitterInstance(this);
		mInputText = (EditText) findViewById(R.id.input_text);

		findViewById(R.id.action_tweet).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tweet();
			}
		});
	}

	private void tweet() {
		final ProgressDialog p = new ProgressDialog(this);

		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				p.setTitle("投稿中");
				p.setMessage("しばらくお待ちください");
				p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				p.setCancelable(false);
				p.setMax(100);
				p.setProgress(0);
				p.show();
			}

			@Override
			protected Boolean doInBackground(String... params) {
				if (0 < params.length) {
				try {

						mTwitter.updateStatus(params[0]);
						return true;

				} catch (TwitterException e) {
					e.printStackTrace();
					return false;
				}
			}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					p.dismiss();
					showToast("ツイートしました");

					finish();
				} else {
					p.dismiss();
					showToast("ツイートに失敗しました");
				}
			}
		};
		task.execute(mInputText.getText().toString());
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

}