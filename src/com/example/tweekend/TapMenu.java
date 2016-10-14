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
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.URLEntity;

@SuppressLint("SimpleDateFormat")
public class TapMenu {

	Context context;
	private SQLiteDatabase db;

	public TapMenu(Context c) {
		context = c;
	}

	// ついーとをタップした時のメニュー
	public void tapMenu(final Status Tweet, final Twitter mTwitter) {
		String[] dialogItem;
		dialogItem = new String[] { "リプライ", "公式リツイート", "ふぁぼ", "ふぁぼリツイート", "リンクを開く", "ローカル保存" };

		AlertDialog.Builder dialogMenu = new AlertDialog.Builder(context);
		dialogMenu.setItems(dialogItem, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: // リプライ
					Intent intent = new Intent(context, ReplayActivity.class);
					intent.putExtra("mentionID", Tweet.getId());
					intent.putExtra("StatusID", Tweet.getUser().getScreenName());
					intent.putExtra("Name", Tweet.getUser().getName());
					intent.putExtra("Tweet", Tweet.getText());
					intent.putExtra("Image", Tweet.getUser().getProfileImageURL());
					context.startActivity(intent);
					break;

				case 1: // RT
					new AlertDialog.Builder(context).setTitle("RTしますか？").setPositiveButton("はい", new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
								// 処理をここに書く
								@Override
								protected Boolean doInBackground(Void... params) {
									try {
										mTwitter.retweetStatus(Tweet.getId());
										return true;

									} catch (TwitterException e) {
										e.printStackTrace();
									} catch (Exception e) {
										e.printStackTrace();
									}
									return false;
								}

								// 処理が終わった後の処理
								@Override
								protected void onPostExecute(Boolean result) {
									if (result != false) {
										showToast("リツイートしました");
									} else {
										showToast("リツイートに失敗しました");
									}
								}
							};
							task.execute();
						}
					}).setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// NO時の処理
						}
					}).show();

					break;

				case 2: // fav
					new AlertDialog.Builder(context).setTitle("ふぁぼしますか？")
							.setPositiveButton("はい", new OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
										// 処理をここに書く
										@Override
										protected Boolean doInBackground(Void... params) {
											try {
												mTwitter.createFavorite(Tweet.getId());
												return true;
											} catch (TwitterException e) {
												e.printStackTrace();
											} catch (Exception e) {
												e.printStackTrace();
											}
											return false;
										}

										// 処理が終わった後の処理
										@Override
										protected void onPostExecute(Boolean result) {
											if (result != false) {
												showToast("ふぁぼしました");
											} else {
												showToast("ふぁぼれなかった");
											}
										}
									};
									task.execute();
								}
							})

							.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// NO時の処理
								}
							}).show();
					break;
				case 3: // ふぁぼRT
					new AlertDialog.Builder(context).setTitle("ふぁぼRTしますか？")
							.setPositiveButton("はい", new OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
										// 処理をここに書く
										@Override
										protected Boolean doInBackground(Void... params) {
											try {
												mTwitter.retweetStatus(Tweet.getId());
												mTwitter.createFavorite(Tweet.getId());
												return true;
											} catch (TwitterException e) {
												e.printStackTrace();
											} catch (Exception e) {
												e.printStackTrace();
											}
											return false;
										}

										// 処理が終わった後の処理
										@Override
										protected void onPostExecute(Boolean result) {
											if (result != false) {
												showToast("ふぁぼリツイートしました");
											} else {
												showToast("ふぁぼリツイートに失敗しました");
											}
										}
									};
									task.execute();
								}
							}).setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// NO時の処理
								}
							}).show();

					break;
				case 4: // URL開くやつ
					Boolean URLFlg = false;
					try {
						if (Tweet.getRetweetedStatus() == null) {
							for (URLEntity UrlLink : Tweet.getURLEntities()) { // 普通のＵＲＬ
								Uri uri = Uri.parse(UrlLink.getURL().toString());
								Intent i = new Intent(Intent.ACTION_VIEW, uri);
								context.startActivity(i);
								URLFlg = true;
							}
						} else {
							for (URLEntity UrlLink : Tweet.getRetweetedStatus().getURLEntities()) { // 普通のＵＲＬ
								Uri uri = Uri.parse(UrlLink.getURL().toString());
								Intent i = new Intent(Intent.ACTION_VIEW, uri);
								context.startActivity(i);
								URLFlg = true;
							}
						}
						if (!URLFlg) {
							showToast("URLが見つかりません");
						}
					} catch (Exception e) {
						e.printStackTrace();
						showToast("なにかがおかしいです");
					}
					break;
				case 5: // DBに保存
					ParsonOpenHelper helper = new ParsonOpenHelper(context);
					db = helper.getWritableDatabase();

					Status mTweet = Tweet;
					new TweetInDatabaseMenu(context, db, mTweet);

					showToast("保存しました");
				}
			}
		});

		AlertDialog log = dialogMenu.create();
		log.show();

	}

	@SuppressLint("ShowToast")
	private void showToast(String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
}

class TweetInDatabaseMenu {

	private long statusId;
	private String userId;
	private String tweetText;
	private String tweetTime;
	private InputStream tweetPhoto1, tweetPhoto2, tweetPhoto3, tweetPhoto4;
	private byte[] image1, image2, image3, image4;
	private SQLiteDatabase db;
	private Status tweet;

	public TweetInDatabaseMenu(Context context, SQLiteDatabase db, Status tweet) {
		super();
		this.db = db;
		this.tweet = tweet;

		tweetInDb();
	}

	private void tweetInDb() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				try {
					db.beginTransaction();
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
