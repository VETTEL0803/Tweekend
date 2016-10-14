package com.example.tweekend;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class ReplayActivity extends Activity {

	private EditText screenName;
    private Twitter mTwitter;
    public long mentionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        Intent intent = getIntent();
        String name = intent.getStringExtra("StatusID");
        this.mTwitter = TwitterUtils.getTwitterInstance(this);
        this.mentionId = intent.getLongExtra("mentionID", BIND_ABOVE_CLIENT);

        screenName = (EditText)findViewById(R.id.r_input_text);
        screenName.setText("@" + name + " ", BufferType.NORMAL);
        screenName.setSelection(screenName.getText().length());

        findViewById(R.id.r_action_tweet).setOnClickListener(new View.OnClickListener() {
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
                try {
                    mTwitter.updateStatus(new StatusUpdate(params[0]).inReplyToStatusId(mentionId));
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                } catch (Exception e){
                	e.printStackTrace();
                	return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                	p.dismiss();
                    showToast("リプライしました！");

                    finish();
                } else {
                	p.dismiss();
                    showToast("リプライに失敗しました");
                }
            }
        };
        task.execute(screenName.getText().toString());
    }


    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}