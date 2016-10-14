package com.example.tweekend;


import java.util.List;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@SuppressLint({ "UseValueOf", "SimpleDateFormat", "InflateParams" })
public class MentionActivity extends ListActivity {

	private TweetAdapter mAdapter;
	private Twitter mTwitter;
	private Status Tweet;
	private ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ListActivityを使うときは不要
		// setContentView(R.layout.activity_main);

		if (!TwitterUtils.hasAccessToken(this)) {
			Intent intent = new Intent(this, TwitterOAuthActivity.class);
			startActivity(intent);
			finish();
		} else {
			lv = getListView();
			mAdapter = new TweetAdapter(this);
			setListAdapter(mAdapter);

			mTwitter = TwitterUtils.getTwitterInstance(this);
			reloadTimeLine();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.layout.activity_mention, menu);
		return true;
	}

	// Activity動かす前に動作
	@Override
	protected void onResume() {
		super.onResume();
		mAdapter.notifyDataSetChanged();

		//ListViewの背景色設定
		lv.setBackgroundColor(Color.argb(100, 0, 150, 255));

		//タップした時に開くメニュー
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					Tweet = mAdapter.getItem(position);
					TapMenu tapMenu = new TapMenu(MentionActivity.this);
					tapMenu.tapMenu(Tweet, mTwitter);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void reloadTimeLine() {

		final ProgressDialog p = new ProgressDialog((this));

		AsyncTask<Void, Void, List<twitter4j.Status>> task = new AsyncTask<Void, Void, List<twitter4j.Status>>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				p.setTitle("TL更新中");
				p.setMessage("しばらくお待ちください");
				p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				p.setCancelable(false);
				p.setMax(100);
				p.setProgress(0);
				p.show();
			}

			@Override
			protected List<twitter4j.Status> doInBackground(Void... params) {
				try {
					Paging paging = new Paging(1,50);
					return mTwitter.getMentionsTimeline(paging);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<twitter4j.Status> result) {
				if (result != null) {
					mAdapter.clear();
					for (twitter4j.Status status : result) {
						mAdapter.add(status);
					}
					p.dismiss();
				} else {
					p.dismiss();
					showToast("タイムラインの取得に失敗しました。。。");
				}
			}
		};
		task.execute();
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			reloadTimeLine();
			return true;
		case R.id.menu_tweet:
			Intent intent = new Intent(this, TweetActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_illust_tweet:

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}