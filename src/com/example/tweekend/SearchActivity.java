package com.example.tweekend;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@SuppressLint("SimpleDateFormat")
public class SearchActivity extends ListActivity {

	private Twitter mTwitter;
	private TweetSearchAdapter mAdapter;
	private QueryResult result;
	private SQLiteDatabase db;
	private ListView lv;
	private String searchTweet;
	private List<Status> tweetList;
	private Context context;
	private Status tweet;
	private ArrayList<Boolean> items = new ArrayList<Boolean>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mTwitter = TwitterUtils.getTwitterInstance(this);

		mAdapter = new TweetSearchAdapter(this, items);

	}

	@Override
	protected void onResume() {
		super.onResume();

		lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		context = this;
		ParsonOpenHelper helper = new ParsonOpenHelper(this);
		db = helper.getWritableDatabase();

		// タップした時に開くメニュー
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					tweet = mAdapter.getItem(position);
					TapMenu tapMenu = new TapMenu(SearchActivity.this);
					tapMenu.tapMenu(tweet, mTwitter);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_mention:

			final EditText text = new EditText(this);
			final Builder bilder = new AlertDialog.Builder(this);

			bilder.setTitle("検索ワード入力").setView(text).setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					searchTweet = text.getText().toString();
					final ProgressDialog p = new ProgressDialog(context);
					AsyncTask<Void, Void, List<twitter4j.Status>> test = new AsyncTask<Void, Void, List<twitter4j.Status>>() {

						@Override
						protected void onPreExecute() {
							super.onPreExecute();
							p.setTitle("検索中");
							p.setMessage("しばらくお待ちください");
							p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							p.setCancelable(false);
							p.setMax(100);
							p.setProgress(0);
							p.show();

							if (tweetList != null) {
								tweetList.clear();
							}
						}

						// 非同期で処理する部分
						@Override
						protected List<twitter4j.Status> doInBackground(Void... params) {

							try {
								// Twitterの検索ワードを入れる型
								Query query = new Query();

								// 検索ワードをセット
								query.setQuery(searchTweet + " -rt");
								query.setCount(100);

								// 検索実行
								result = mTwitter.search(query);
								tweetList = result.getTweets();

								return tweetList;

							} catch (TwitterException e) {
								e.printStackTrace();
							}
							return null;
						}

						// 非同期処理が終わった後に呼び出される
						@Override
						protected void onPostExecute(List<twitter4j.Status> resul) {
							if (resul != null) {
								mAdapter.clear();
								items.clear();
								for (twitter4j.Status status : resul) {
									mAdapter.add(status);
									items.add(false);
								}

							}
							setListAdapter(mAdapter);
							p.dismiss();
							showToast("検索完了");
						}
					};
					test.execute();

				}
			}).setNegativeButton("キャンセル", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).show();

			return true;
		case R.id.menu_refresh:
			Intent intent = new Intent(this, DataBaseActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_illust_tweet:
			// データベースに挿入
			if (tweetList != null) {

				new TweetInDatabase(this, db, tweetList, items);

				showToast("保存しました");
			}
			return true;
		default:
			return true;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// DBに接続してたら閉じる
		if (db != null) {
			db.close();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (db != null) {
			db.close();
		}
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

}

//検索画面からデータベースに挿入するためのクラス
//処理が煩雑になったので別クラスに分けた
class TweetInDatabase {

	private long statusId;
	private String userId;
	private String tweetText;
	private String tweetTime;
	private InputStream tweetPhoto1, tweetPhoto2, tweetPhoto3, tweetPhoto4;
	private byte[] image1, image2, image3, image4;
	private ArrayList<Boolean> items;
	private List<Status> tweetList;
	private ArrayList<Status> checkTweetList;
	private SQLiteDatabase db;
	private Context context;

	public TweetInDatabase(Context context, SQLiteDatabase db, List<Status> l, ArrayList<Boolean> items) {
		super();
		this.db = db;
		this.tweetList = l;
		this.items = items;
		this.context = context;

		tweetInDb();
	}

	private void tweetInDb() {

		final ProgressDialog p = new ProgressDialog(context);

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				// TODO 自動生成されたメソッド・スタブ
				super.onPreExecute();
				p.setTitle("保存中");
				p.setMessage("しばらくお待ちください");
				p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				p.setCancelable(false);
				p.setMax(100);
				p.setProgress(0);
				p.show();
			}

			@Override
			protected Void doInBackground(Void... params) {

				try {
					checkTweetList = new ArrayList<twitter4j.Status>();

					// チェックボックスにチェックが入っている行を取得
					int twItr = 0; // 検索して取得したリスト用のフラグ
					int itemItr = 0; // チェックボックスのチェックが入っている判定用のフラグ

					for (twitter4j.Status tw : tweetList) {
						itemItr = 0;
						for (boolean boo : items) {
							if (twItr == itemItr) {
								if (boo == true) {
									checkTweetList.add(tw);
								}
							}
							itemItr++;
						}
						twItr++;
					}

					db.beginTransaction();

					for (twitter4j.Status tweet : checkTweetList) {

						image1 = null;
						image2 = null;
						image3 = null;
						image4 = null;
						statusId = tweet.getId();
						userId = tweet.getUser().getScreenName();
						tweetText = tweet.getText();
						Date tweet_time = tweet.getCreatedAt();
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						tweetTime = format.format(tweet_time).toString();
						MediaEntity[] me = tweet.getExtendedMediaEntities();
						if (me.length != 0) {
							for (int i = 0; i < me.length; i++) {
								MediaEntity m = me[i];
								String imageUrl = m.getMediaURL();
								URL url = new URL(imageUrl);
								URLConnection conn = url.openConnection();
								InputStream in = conn.getInputStream();

								if (i == 0) {
									tweetPhoto1 = in;
									image1 = getBytes(tweetPhoto1);
								}
								if (i == 1) {
									tweetPhoto2 = in;
									image2 = getBytes(tweetPhoto2);
								}
								if (i == 2) {
									tweetPhoto3 = in;
									image3 = getBytes(tweetPhoto3);
								}
								if (i == 3) {
									tweetPhoto4 = in;
									image4 = getBytes(tweetPhoto4);
								}
							}
						}
						// ここからDB挿入処理
						ContentValues values = new ContentValues();
						values.put("status_id", statusId);
						values.put("user_id", userId);
						values.put("tweet", tweetText);
						values.put("tweet_time", tweetTime);
						values.put("photo1", image1);
						values.put("photo2", image2);
						values.put("photo3", image3);
						values.put("photo4", image4);
						db.insert("twit_log", null, values);

					}

					db.setTransactionSuccessful();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					db.endTransaction();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				p.dismiss();
			}
		};
		task.execute();
	}

	// byte型の配列を出力先とするクラス。
	// 通常、バイト出力ストリームはファイルやソケットを出力先とするが、
	// ByteArrayOutputStreamクラスはbyte[]変数、つまりメモリを出力先とする。
	private static byte[] getBytes(InputStream is) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		OutputStream os = new BufferedOutputStream(b);
		int c;
		try {
			while ((c = is.read()) != -1) {
				os.write(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.flush();
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// 書き込み先はByteArrayOutputStreamクラス内部となる。
		// この書き込まれたバイトデータをbyte型配列として取り出す場合には、
		// toByteArray()メソッドを呼び出す。
		return b.toByteArray();
	}

}