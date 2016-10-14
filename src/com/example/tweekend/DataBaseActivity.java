package com.example.tweekend;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class DataBaseActivity extends ListActivity {

	private ListView lv;
	private ParsonOpenHelper helper;
	private SQLiteDatabase db;
	private Cursor c;
	private ArrayList<Boolean> checkItems;
	private List<ListItems> items;
	private List<ListItems> checkTweetList;
	private View layout;
	private boolean photoCheck;
	private String sort;
	private Integer startYear, startMonth, startDay, endYear, endMonth, endDay;
	private String startCalender = "";
	private String endCalender = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		helper = new ParsonOpenHelper(this);
		db = helper.getWritableDatabase();

		checkItems = new ArrayList<Boolean>();
		showDatabase();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:

			showDatabase();
			return true;
		case R.id.menu_tweet:
			LayoutInflater inflater = getLayoutInflater();
			layout = inflater.inflate(R.layout.dbsearch_dialog, (ViewGroup) findViewById(R.id.dbsearch_dialog));

			final Context context = this;

			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("検索").setView(layout);

			final EditText dbTextForm = (EditText) layout.findViewById(R.id.tweet_text_db);

			final CheckBox dbCheckForm = (CheckBox) layout.findViewById(R.id.boolean_photo);
			final RadioGroup dbSortButton = (RadioGroup) layout.findViewById(R.id.sort_button);
			final Button calenderButtonStart = (Button) layout.findViewById(R.id.select_calender_start_button);
			final Button calenderButtonEnd = (Button) layout.findViewById(R.id.select_calender_end_button);
			final TextView selectCalenderStart = (TextView) layout.findViewById(R.id.select_calender_start);
			final TextView selectCalenderEnd = (TextView) layout.findViewById(R.id.select_calender_end);

			// チェックボックスの設定
			dbCheckForm.setChecked(false);
			dbCheckForm.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					photoCheck = isChecked;
				}
			});

			sort = "DESC";
			// ラジオボタンの設定
			dbSortButton.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if (checkedId == -1) {
						sort = "DESC";
					} else if (checkedId == R.id.sort_button_down) {
						sort = "DESC";
					} else {
						sort = "ASC";
					}

				}
			});

			// DatePickerの設定
			final DatePicker startPicker = new DatePicker(this);
			final DatePicker endPicker = new DatePicker(this);
			final Builder startBuilder = new Builder(this);
			final Builder endBuilder = new Builder(this);
			// Viewの設定
			// 開始のほう
			startPicker.setSpinnersShown(false);
			startBuilder.setView(startPicker);
			startBuilder.setTitle("開始年月日");
			startBuilder.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startYear = startPicker.getYear();
					startMonth = startPicker.getMonth() + 1;
					startDay = startPicker.getDayOfMonth();
					selectCalenderStart.setText(startYear + "年" + startMonth + "月" + startDay + "日");
					// 月日がどちらも1ケタだった時
					if (startMonth <= 9 && startDay <= 9) {
						startCalender = startYear.toString() + "-0" + startMonth.toString() + "-0"
								+ startDay.toString();
					}
					// 月が1ケタで日が2ケタだった時
					if (startMonth <= 9 && 9 < startDay) {
						startCalender = startYear.toString() + "-0" + startMonth.toString() + "-" + startDay.toString();
					}
					// 月が2ケタで日が1ケタだった時
					if (startDay <= 9 && 9 < startMonth) {
						startCalender = startYear.toString() + "-" + startMonth.toString() + "-0" + startDay.toString();
					}
					// 月日がどちらも2ケタだった時
					if (9 < startMonth && 9 < startDay) {
						startCalender = startYear.toString() + "-" + startMonth.toString() + "-" + startDay.toString();
					}
				}
			}).setNegativeButton("キャンセル", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			final AlertDialog startDialog = startBuilder.create();

			calenderButtonStart.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					startDialog.show();
				}
			});

			// 終わりの方
			endPicker.setSpinnersShown(false);
			endBuilder.setView(endPicker);
			endBuilder.setTitle("終了年月日");
			endBuilder.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					endYear = endPicker.getYear();
					endMonth = endPicker.getMonth() + 1;
					endDay = endPicker.getDayOfMonth();
					selectCalenderEnd.setText(endYear + "年" + endMonth + "月" + endDay + "日");
					// 日数が1日ずれるので日を+1
					endDay += 1;
					// 月日がどちらも1ケタだった時
					if (endMonth <= 9 && endDay <= 9) {
						endCalender = endYear + "-0" + endMonth + "-0" + endDay;
					}
					// 月が1ケタで日が2ケタだった時
					if (endMonth <= 9 && 9 < endDay) {
						endCalender = endYear + "-0" + endMonth + "-" + endDay;
					}
					// 月が2ケタで日が1ケタだった時
					if (endDay <= 9 && 9 < endMonth) {
						endCalender = endYear + "-" + endMonth + "-0" + endDay;
					}
					// 月日がどちらも2ケタだった時
					if (9 < endMonth && 9 < endDay) {
						endCalender = endYear + "-" + endMonth + "-" + endDay;
					}
				}
			}).setNegativeButton("キャンセル", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			final AlertDialog endDialog = endBuilder.create();

			calenderButtonEnd.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					endDialog.show();
				}
			});

			// 検索ダイアログの検索ボタンを押した時の処理
			builder.setPositiveButton("検索", new OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					String dbText = dbTextForm.getText().toString();

					items = new ArrayList<ListItems>();

					// sortだけしたい時
					if (dbText.length() == 0 && photoCheck == false && startCalender.length() == 0
							&& endCalender.length() == 0) {

						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								null, null, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// 指定日以後のみで検索
					if (dbText.length() == 0 && photoCheck == false && startCalender.length() > 0
							&& endCalender.length() == 0) {

						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"tweet_time >= '" + startCalender + "'", null, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// 指定日以前のみで検索
					if (dbText.length() == 0 && photoCheck == false && startCalender.length() == 0
							&& endCalender.length() > 0) {

						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"tweet_time <= '" + endCalender + "'", null, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// 指定日の範囲内でのみ検索
					if (dbText.length() == 0 && photoCheck == false && startCalender.length() > 0
							&& endCalender.length() > 0) {

						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"tweet_time >= '" + startCalender + "' AND tweet_time <= '" + endCalender + "'", null,
								null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// ツイート文のみで検索
					if (dbText.length() > 0 && photoCheck == false && startCalender.length() == 0
							&& endCalender.length() == 0) {

						String searchWord = "%" + dbText + "%";
						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"tweet like ?", new String[] { searchWord }, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// 画像が含まれているカラムのみ検索
					if (dbText.length() == 0 && photoCheck == true && startCalender.length() == 0
							&& endCalender.length() == 0) {

						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"photo1 IS NOT NULL", null, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// ツイート文且つ画像有で検索
					if (dbText.length() > 0 && photoCheck == true && startCalender.length() == 0
							&& endCalender.length() == 0) {

						String searchWord = "%" + dbText + "%";
						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"photo1 IS NOT NULL AND tweet like ?", new String[] { searchWord }, null, null,
								"status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// ツイート文のみ且つ指定日以後で検索
					if (dbText.length() > 0 && photoCheck == false && startCalender.length() > 0
							&& endCalender.length() == 0) {

						String searchWord = "%" + dbText + "%";
						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"tweet like ? AND tweet_time >= '" + startCalender + "'", new String[] { searchWord },
								null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// ツイート文のみ且つ指定日以前で検索
					if (dbText.length() > 0 && photoCheck == false && startCalender.length() == 0
							&& endCalender.length() > 0) {

						String searchWord = "%" + dbText + "%";
						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"tweet like ? AND tweet_time <= '" + endCalender + "'", new String[] { searchWord },
								null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// ツイート文のみ且つ指定日の範囲内で検索
					if (dbText.length() > 0 && photoCheck == false && startCalender.length() > 0
							&& endCalender.length() > 0) {

						String searchWord = "%" + dbText + "%";
						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"tweet like ? AND tweet_time >= '" + startCalender + "' AND tweet_time <= '"
										+ endCalender + "'",
								new String[] { searchWord }, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// 画像のみ且つ指定以後で検索
					if (dbText.length() == 0 && photoCheck == true && startCalender.length() > 0
							&& endCalender.length() == 0) {

						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"photo1 IS NOT NULL AND tweet_time >= '" + startCalender + "'", null, null, null,
								"status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// 画像のみ且つ指定日以前で検索
					if (dbText.length() == 0 && photoCheck == true && startCalender.length() == 0
							&& endCalender.length() > 0) {

						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"photo1 IS NOT NULL AND tweet_time <= '" + endCalender + "'", null, null, null,
								"status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// 画像のみ且つ指定範囲で検索
					if (dbText.length() == 0 && photoCheck == true && startCalender.length() > 0
							&& endCalender.length() > 0) {

						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"photo1 IS NOT NULL AND tweet_time >= '" + startCalender + "' AND tweet_time <= '"
										+ endCalender + "'",
								null, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// ツイート文且つ画像有且つ指定日以後で検索
					if (dbText.length() > 0 && photoCheck == true && startCalender.length() > 0
							&& endCalender.length() == 0) {

						String searchWord = "%" + dbText + "%";
						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"photo1 IS NOT NULL AND tweet like ? AND tweet_time >= '" + startCalender + "'",
								new String[] { searchWord }, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// ツイート文且つ画像有且つ指定日以前で検索
					if (dbText.length() > 0 && photoCheck == true && startCalender.length() == 0
							&& endCalender.length() > 0) {

						String searchWord = "%" + dbText + "%";
						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"photo1 IS NOT NULL AND tweet like ? AND tweet_time <= '" + endCalender + "'",
								new String[] { searchWord }, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					// ツイート文且つ画像有且つ指定範囲で検索
					if (dbText.length() > 0 && photoCheck == true && startCalender.length() == 0
							&& endCalender.length() > 0) {

						String searchWord = "%" + dbText + "%";
						c = db.query("twit_log",
								new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
										"photo1", "photo2", "photo3", "photo4" },
								"photo1 IS NOT NULL AND tweet like ? AND tweet_time >= '" + startCalender
										+ "' AND tweet_time <= '" + endCalender + "'",
								new String[] { searchWord }, null, null, "status_id " + sort);

						while (c.moveToNext()) {
							items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4),
									c.getBlob(5), c.getBlob(6), c.getBlob(7), c.getBlob(8)));
							checkItems.add(false);
						}

					}

					ListAdapter dbAdapter = new DatabaseAdapter(context, R.layout.list_item_tweet_search_db, items,
							checkItems);
					setListAdapter(dbAdapter);
					c.close();
				}
			}).show();

			return true;
		case R.id.menu_illust_tweet:
			// データベースから削除する処理
			checkTweetList = new ArrayList<ListItems>();

			if (items != null) {
				// チェックボックスにチェックが入っている行を取得
				int twItr = 0; // 検索して取得したリスト用のフラグ
				int itemItr = 0; // チェックボックスのチェックが入っている判定用のフラグ

				for (ListItems tw : items) {
					itemItr = 0;
					for (boolean boo : checkItems) {
						if (twItr == itemItr) {
							if (boo == true) {
								checkTweetList.add(tw);
							}
						}
						itemItr++;
					}
					twItr++;
				}
			}

			if (checkTweetList != null) {
				for (ListItems li : checkTweetList) {
					String matchId = li.getName();
					db.delete("twit_log", "status_id = " + matchId, null);
				}

				showToast("削除しました");
			}
			return true;
		}

		return super.onOptionsItemSelected(item);

	}

	private void showDatabase() {

		items = new ArrayList<ListItems>();

		// カーソル(自前のカーソルを使う且_idを使わない場合_idの代わりとして別のカラムを指定する。
		c = db.query("twit_log", new String[] { "status_id as _id", "status_id", "user_id", "tweet", "tweet_time",
				"photo1", "photo2", "photo3", "photo4" }, null, null, null, null, "status_id DESC");

		while (c.moveToNext()) {
			items.add(new ListItems(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getBlob(5),
					c.getBlob(6), c.getBlob(7), c.getBlob(8)));
			checkItems.add(false);
		}

		ListAdapter dbAdapter = new DatabaseAdapter(this, R.layout.list_item_tweet_search_db, items, checkItems);
		c.close();
		setListAdapter(dbAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (db == null) {
			helper = new ParsonOpenHelper(this);
			db = helper.getWritableDatabase();
		}
		lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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

class DatabaseAdapter extends ArrayAdapter<ListItems> {

	private LayoutInflater mInflater;
	private byte[] p1, p2, p3, p4;
	private List<ListItems> items;
	private ArrayList<Boolean> checkItems;
	private ViewHolder holder;

	private class ViewHolder {

		TextView name;
		TextView screenName;
		TextView text;
		TextView time;
		ImageView photo1;
		ImageView photo2;
		ImageView photo3;
		ImageView photo4;
		CheckBox check;
	}

	public DatabaseAdapter(Context context, int tvResId, List<ListItems> items, ArrayList<Boolean> checkItems) {
		super(context, tvResId, items);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.items = items;
		this.checkItems = checkItems;

	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ListItems item = items.get(position);
		View view = convertView;

		if (view == null) {
			view = mInflater.inflate(R.layout.list_item_tweet_search_db, null);

			holder = new ViewHolder();

			holder.name = (TextView) view.findViewById(R.id.name_db);
			holder.screenName = (TextView) view.findViewById(R.id.screen_name_db);
			holder.text = (TextView) view.findViewById(R.id.text_db);
			holder.time = (TextView) view.findViewById(R.id.time_db);
			holder.photo1 = (ImageView) view.findViewById(R.id.photo1_db);
			holder.photo2 = (ImageView) view.findViewById(R.id.photo2_db);
			holder.photo3 = (ImageView) view.findViewById(R.id.photo3_db);
			holder.photo4 = (ImageView) view.findViewById(R.id.photo4_db);

			view.setTag(holder);

		} else {
			holder = (ViewHolder) view.getTag();
		}

		// 画像を非表示
		holder.photo1.setVisibility(View.GONE);
		holder.photo2.setVisibility(View.GONE);
		holder.photo3.setVisibility(View.GONE);
		holder.photo4.setVisibility(View.GONE);

		// ImageViewにタグを設定
		holder.photo1.setTag(item.getName());

		// テキストは普通にセット
		holder.name.setText(item.getName());
		holder.screenName.setText("@" + item.getScreenName());
		holder.text.setText(item.getText());
		holder.time.setText(item.getTime());

		//タグとStatusIDが一致したら画像表示
		if (holder.photo1.getTag().equals(item.getName())) {
			if (item.getP1() != null) {
				p1 = item.getP1();
				p2 = item.getP2();
				p3 = item.getP3();
				p4 = item.getP4();

				if (p1 != null) {
					holder.photo1.setVisibility(View.VISIBLE);
					Bitmap pic1 = BitmapFactory.decodeByteArray(p1, 0, p1.length);
					holder.photo1.setImageBitmap(pic1);
				}
				if (p2 != null) {
					holder.photo2.setVisibility(View.VISIBLE);
					Bitmap pic2 = BitmapFactory.decodeByteArray(p2, 0, p2.length);
					holder.photo2.setImageBitmap(pic2);
				}
				if (p3 != null) {
					holder.photo3.setVisibility(View.VISIBLE);
					Bitmap pic3 = BitmapFactory.decodeByteArray(p3, 0, p3.length);
					holder.photo3.setImageBitmap(pic3);
				}
				if (p4 != null) {
					holder.photo4.setVisibility(View.VISIBLE);
					Bitmap pic4 = BitmapFactory.decodeByteArray(p4, 0, p4.length);
					holder.photo4.setImageBitmap(pic4);
				}
			}
		}
		final int p = position;
		holder.check = (CheckBox) view.findViewById(R.id.checkBox_db);
		holder.check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checkItems.set(p, isChecked);
			}
		});
		holder.check.setChecked(checkItems.get(position));

		return view;
	}

}
