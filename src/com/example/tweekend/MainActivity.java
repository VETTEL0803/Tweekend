package com.example.tweekend;
/*
 * MainActivityと謳っているけどTL表示のクラス
 * リプライとかDMとか検索はここを改変すれば良さそう？
 */

import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
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

public class MainActivity extends ListActivity {

	private TweetAdapter mAdapter;
	private Twitter mTwitter;
	private Status Tweet;
	private ListView lv;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// ListActivityを使うときは不要
		// setContentView(R.layout.activity_main);
		if (!TwitterUtils.hasAccessToken(this)) { // アクセストークンを持っていなかったら取りに行く
			Intent intent = new Intent(this, TwitterOAuthActivity.class);
			startActivity(intent);
			finish();
		} else { // タイムラインを読み込む(クライアント起動時に最新状態になる)
			lv = getListView();
			mAdapter = new TweetAdapter(this);
			setListAdapter(mAdapter);

			mTwitter = TwitterUtils.getTwitterInstance(this);

			reloadTimeLine();

		}
	}

	// Activity動かす前に動作
	@Override
	protected void onResume() {
		super.onResume();
		mAdapter.notifyDataSetChanged();

		// タップした時に開くメニュー
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					Tweet = mAdapter.getItem(position);
					TapMenu tapMenu = new TapMenu(MainActivity.this);
					tapMenu.tapMenu(Tweet, mTwitter);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.moveTaskToBack(true);
	};

	// 画面上部のメニューを作成
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// TL読み込みメソッド
	private void reloadTimeLine() {

		progressDialog = new ProgressDialog(this);

		AsyncTask<Void, Void, List<twitter4j.Status>> task = new AsyncTask<Void, Void, List<twitter4j.Status>>() {
			ProgressDialog p = progressDialog;

			// doInBackground実行前に処理が行われる
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
					Paging paging = new Paging(1, 50);
					return mTwitter.getHomeTimeline(paging);
				} catch (TwitterException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			// 読み込んだTLデータをAdapterに格納してる
			@Override
			protected void onPostExecute(List<twitter4j.Status> result) {
				if (result != null) {
					mAdapter.clear();
					for (twitter4j.Status status : result) {
						mAdapter.add(status);
					}

					p.dismiss();
					showToast("TLを取得しました");

				} else {
					p.dismiss();
					showToast("TLの取得に失敗しました");
				}
			}
		};
		task.execute();
	}

	// トースト出力
	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	// 上部メニューの処理とか
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
		case R.id.menu_mention:
			Intent intent2 = new Intent(this, MentionActivity.class);
			startActivity(intent2);
			return true;
		case R.id.menu_illust_tweet:
			// Intent intent4 = new Intent(this, IllustActivity.class);
			// Intent intent4 = new Intent(this, SearchActivity.class);
			Intent intent4 = new Intent(this, Testa.class);
			// Intent intent4 = new Intent(this, DataBaseActivity.class);
			startActivity(intent4);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
