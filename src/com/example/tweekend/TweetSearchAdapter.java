package com.example.tweekend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.loopj.android.image.SmartImageView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import twitter4j.MediaEntity;
import twitter4j.Status;

//SearchActivityで使ってるアダプタ
@SuppressLint({ "InflateParams", "SimpleDateFormat", "UseValueOf" })
public class TweetSearchAdapter extends ArrayAdapter<twitter4j.Status> {

	private LayoutInflater mInflater;
	private ArrayList<Boolean> items;
	private ViewHolder holder;

	private class ViewHolder {

		TextView name;
		TextView screenName;
		TextView text;
		SmartImageView icon;
		TextView time;
		TextView via;
		SmartImageView photo1;
		SmartImageView photo2;
		SmartImageView photo3;
		SmartImageView photo4;
		CheckBox check;
	}

	public TweetSearchAdapter(Context context, ArrayList<Boolean> b) {
		super(context, android.R.layout.simple_list_item_multiple_choice);
		mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		this.items = b;
	}

	// TLに表示する内容のフォーマット
	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Status item = getItem(position);
		View view = convertView;

		if (view == null) {
			view = mInflater.inflate(R.layout.list_item_tweet_search, null);

			holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.name);
			holder.screenName = (TextView) view.findViewById(R.id.screen_name);
			holder.text = (TextView) view.findViewById(R.id.text);
			holder.icon = (SmartImageView) view.findViewById(R.id.icon);
			holder.time = (TextView) view.findViewById(R.id.time);
			holder.via = (TextView) view.findViewById(R.id.via);
			holder.photo1 = (SmartImageView) view.findViewById(R.id.photo1);
			holder.photo2 = (SmartImageView) view.findViewById(R.id.photo2);
			holder.photo3 = (SmartImageView) view.findViewById(R.id.photo3);
			holder.photo4 = (SmartImageView) view.findViewById(R.id.photo4);

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
		holder.photo1.setTag(item.getId());

		holder.name.setText(item.getUser().getName());
		holder.screenName.setText("@" + item.getUser().getScreenName());
		holder.text.setText(item.getText());
		holder.icon.setImageUrl(item.getUser().getProfileImageURL());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		holder.time.setText(formatter.format(item.getCreatedAt()).toString());
		String str = item.getSource();
		str = str.replaceAll("<.+?>", "");
		holder.via.setText("via " + str);
		// 投稿画像を取得
		if (holder.photo1.getTag().equals(item.getId())) {
			if (item.getExtendedMediaEntities() != null) {
				MediaEntity[] me = item.getExtendedMediaEntities();
				for (int i = 0; i < me.length; i++) {
					MediaEntity m = me[i];
					String imageUrl = m.getMediaURL();
					if (i == 0) {
						holder.photo1.setVisibility(View.VISIBLE);
						holder.photo1.setImageUrl(imageUrl);
					}
					if (i == 1) {
						holder.photo2.setVisibility(View.VISIBLE);
						holder.photo2.setImageUrl(imageUrl);
					}
					if (i == 2) {
						holder.photo3.setVisibility(View.VISIBLE);
						holder.photo3.setImageUrl(imageUrl);
					}
					if (i == 3) {
						holder.photo4.setVisibility(View.VISIBLE);
						holder.photo4.setImageUrl(imageUrl);
					}
				}
			}
		}
		// チェックボックス
		holder.check = (CheckBox) view.findViewById(R.id.checkBox);
		final int p = position;
		holder.check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				items.set(p, isChecked);
			}
		});
		holder.check.setChecked(items.get(position));

		return view;
	}

}