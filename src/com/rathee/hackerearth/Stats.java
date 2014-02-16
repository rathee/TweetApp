package com.rathee.hackerearth;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import twitter4j.GeoLocation;
import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Stats extends Activity {

	final static String fileName = "classifier.txt";

	Vector<String> tweets = new Vector<String>();
	Vector<String> sentiments = new Vector<String>();
	Vector<Date> dates = new Vector<Date>();
	Vector<GeoLocation> locations = new Vector<GeoLocation>();
	Vector<Integer> reTweets = new Vector<Integer>();
	static HashMap<Long, Long> reTweetMap = new HashMap<Long, Long>();

	public static final Integer[] images = { R.drawable.happy,
			R.drawable.neutral, R.drawable.sad };

	public static final Integer[] traffic = { R.drawable.green,
			R.drawable.yellow, R.drawable.red };

	static String TWITTER_CONSUMER_KEY = "SDz7ur6u6Bs4J73RtlkgiA";
	static String TWITTER_CONSUMER_SECRET = "FNre7vmarUrBRlfE8vH0Y0dWqB8L1rJVOh0i8EhckE";

	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	public SharedPreferences mSharedPreferences;

	private Button btnTweets, btnSentiments, btnLocation, btnOurGuess,
			btnLastAddress;

	boolean btnFlag = false;
	boolean reTweetFlag = false;

	String USER_NAME;
	long USER_ID = (long) 0.0;

	public Stats() {

		// writeToSDCard();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stats);

		Intent intent = getIntent();

		final String userName = intent.getStringExtra("userName");
		final long userID = intent.getLongExtra("userID", 0);

		USER_NAME = userName;
		USER_ID = userID;

		btnFlag = true;
		
		btnTweets = (Button) findViewById(R.id.btnTweets);
		btnSentiments = (Button) findViewById(R.id.btnSentiments);
		btnLocation = (Button) findViewById(R.id.btnLocation);
		btnOurGuess = (Button) findViewById(R.id.btnOurGuess);
		btnLastAddress = ( Button ) findViewById(R.id.btnLastAddress);

		mSharedPreferences = getApplicationContext().getSharedPreferences(
				"MyPref", 0);

		btnTweets.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getTweetsFromTwitter();

			}
		});

		btnSentiments.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getSentiments();

			}
		});

		btnLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Stats.this, MapActivity.class);
				intent.putExtra("userID", userID);
				startActivity(intent);
				finish();

			}
		});

		btnOurGuess.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ourGuess();

			}
		});

		btnLastAddress.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				lastAddress();

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stats, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	void getSentiments() {

		if (tweets.size() == 0)
			fillTweetsAndSentiments();

		List<RowItem> rowItems = new ArrayList<RowItem>();

		for (int i = 0; i < dates.size(); i++) {
			Date date = dates.elementAt(i);
			String sentiment = sentiments.elementAt(i);

			RowItem rowItem;
			if (sentiment.equals("pos")) {
				rowItem = new RowItem(images[0], date.toString());
			} else {
				if (sentiment.equals("neu")) {
					rowItem = new RowItem(images[1], date.toString());
				} else {
					rowItem = new RowItem(images[2], date.toString());
				}
			}
			// arrayList.add(tweettext);
			rowItems.add(rowItem);
		}

		ListView lv = (ListView) findViewById(R.id.friendTweets);

		CustomListViewAdapter adapter = new CustomListViewAdapter(this,
				R.layout.list_item, rowItems);

		lv.setAdapter(adapter);

	}

	private void getTweetsFromTwitter() {

		if (tweets.size() == 0)
			fillTweetsAndSentiments();

		List<RowItem> rowItems = new ArrayList<RowItem>();

		for (int i = 0; i < tweets.size(); i++) {
			String tweettext = tweets.elementAt(i);
			String sentiment = sentiments.elementAt(i);

			RowItem rowItem;
			if (sentiment.equals("pos")) {
				rowItem = new RowItem(images[0], tweettext);
			} else {
				if (sentiment.equals("neu")) {
					rowItem = new RowItem(images[1], tweettext);
				} else {
					rowItem = new RowItem(images[2], tweettext);
				}
			}
			// arrayList.add(tweettext);
			rowItems.add(rowItem);
		}

		ListView lv = (ListView) findViewById(R.id.friendTweets);

		CustomListViewAdapter adapter = new CustomListViewAdapter(this,
				R.layout.list_item, rowItems);

		lv.setAdapter(adapter);

	}

	void fillTweetsAndSentiments() {

		try {
			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
			confbuilder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			confbuilder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

			// Access Token
			String access_token = mSharedPreferences.getString(
					PREF_KEY_OAUTH_TOKEN, "");
			// Access Token Secret
			String access_token_secret = mSharedPreferences.getString(
					PREF_KEY_OAUTH_SECRET, "");

			AccessToken accessToken = new AccessToken(access_token,
					access_token_secret);

			if (btnFlag) {
				btnTweets.setVisibility(View.GONE);
				btnSentiments.setVisibility(View.GONE);
				btnLocation.setVisibility(View.GONE);
				btnOurGuess.setVisibility(View.GONE);
				btnLastAddress.setVisibility(View.GONE);
			}

			Twitter twitter = new TwitterFactory(confbuilder.build())
					.getInstance(accessToken);
			List<Status> statuses = twitter.getUserTimeline(USER_ID,
					new Paging(1, 40));

			SentimentClassifier sentimentClassifier = new SentimentClassifier();

			// Collections.reverse(statuses); // reverse order
			ArrayList<String> arrayList = new ArrayList<String>();

			for (Status status : statuses) {

				String tweettext = status.getText();
				String sentiment = sentimentClassifier.classify(tweettext);
				Date date = status.getCreatedAt();
				GeoLocation location = status.getGeoLocation();

				if (reTweetFlag) {
				
					IDs userIDs = twitter.getRetweeterIds(status.getId(), -1);
					
					do {
			            for (long id : userIDs.getIDs()) {               


			            	if( reTweetMap.containsKey(id)) {
			            		long l = reTweetMap.get(id) + 1;
			            		reTweetMap.remove(id);
			            		reTweetMap.put( id, l);
			            	}
			               
			            	else {
			            		reTweetMap.put(id, (long) 1);
			            	}

			            }
			        } while (userIDs.hasNext());
				}

				tweets.add(tweettext);
				sentiments.add(sentiment);
				dates.add(date);
				locations.add(location);

			}

		} catch (Exception ex) {
			Toast.makeText(this, "Some Problem Occured", Toast.LENGTH_LONG)
					.show();
			// failed to fetch tweet
		}
	}

	void ourGuess() {

		btnLastAddress.setVisibility(View.VISIBLE);
		if (tweets.size() == 0)
			fillTweetsAndSentiments();

		List<RowItem> rowItems = new ArrayList<RowItem>();

		Date current_date = new Date();

		long sentimentPrediction = (long) 0.0;

		for (int i = 0; i < sentiments.size() && i < 10; i++) {
			Date tweetDate = dates.elementAt(i);
			String sentiment = sentiments.elementAt(i);

			long diff = Math.abs(current_date.getTime() - tweetDate.getTime());
			int diffDays = (int) diff / (24 * 60 * 60 * 1000);

			RowItem rowItem;
			if (sentiment.equals("pos")) {
				sentimentPrediction += 1.0 / (diffDays + 1);
			} else {
				if (sentiment.equals("neu")) {

				} else {

					sentimentPrediction -= 1.0 / (diffDays + 1);
				}
			}
			// arrayList.add(tweettext);

		}

		if (sentimentPrediction > 0.5) {
			RowItem rowItem = new RowItem(
					traffic[0],
					"Sentimental value for the user for last 10 tweets comes around "
							+ sentimentPrediction
							+ "\n"
							+ "We strongly recommend you to meet and close your deal \n Last Tweet : " + dates.elementAt(0));
			rowItems.add(rowItem);
		} else {
			if (sentimentPrediction > 0.2) {
				RowItem rowItem = new RowItem(
						traffic[0],
						"Sentimental value for the user for last 10 tweets comes around "
								+ sentimentPrediction
								+ "\n"
								+ "Sentimental value is low but still you can meet and close your deal \n");
				rowItems.add(rowItem);
			} else {
				if (sentimentPrediction >= 0.0) {
					RowItem rowItem = new RowItem(
							traffic[1],
							"Sentimental value for the user for last 10 tweets comes around "
									+ sentimentPrediction
									+ "\n"
									+ "It depends on you to meet that guy or not \n "
									+ "Just check last some tweets and predict yourself");
					rowItems.add(rowItem);
				} else {
					RowItem rowItem = new RowItem(
							traffic[2],
							"Sentimental value for the user for last 10 tweets comes around "
									+ sentimentPrediction
									+ "\n"
									+ "We strongly recommend not to meet at this particular time \n");
					rowItems.add(rowItem);
				}
			}
		}

		ListView lv = (ListView) findViewById(R.id.friendTweets);

		CustomListViewAdapter adapter = new CustomListViewAdapter(this,
				R.layout.list_item, rowItems);

		lv.setAdapter(adapter);

	}

	void lastAddress() {

		GeoLocation location = new GeoLocation(0, 0);
		Date date = null;
		boolean flag = true;
		if (locations.size() == 0)
			fillTweetsAndSentiments();
		if (locations.size() > 0) {
			for (int i = 0; i < locations.size(); i++) {

				if (locations.elementAt(i) != null) {
					location = locations.elementAt(i);
					date = dates.elementAt(i);
					flag = false;
					break;
				}
			}

			if (flag) {
				Toast.makeText(getApplicationContext(),
						"No Last Location for this particular Friend",
						Toast.LENGTH_LONG).show();
				return;
			}
			btnTweets.setVisibility(View.GONE);
			btnSentiments.setVisibility(View.GONE);
			btnLocation.setVisibility(View.GONE);
			btnOurGuess.setVisibility(View.GONE);
			btnLastAddress.setVisibility(View.GONE);
			String addressString;

			try {
				Geocoder geocoder = new Geocoder(this, Locale.getDefault());
				List<Address> addresses = geocoder.getFromLocation(
						location.getLatitude(), location.getLongitude(), 1);
				StringBuilder sb = new StringBuilder();
				if (addresses.size() > 0) {
					Address address = addresses.get(0);

					sb.append(address.getLocality()).append("\n");
					sb.append(address.getCountryName());
				}

				addressString = sb.toString();

				TextView lastAddress = (TextView) findViewById(R.id.lastAddress);
				lastAddress.setVisibility(View.VISIBLE);
				this.setContentView(lastAddress);

				lastAddress.setText("Date : " + date.toString() + "\n"
						+ addressString);

				Log.e("Address from lat,long ;", addressString);
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), "Some Problem Occured",
						Toast.LENGTH_LONG).show();
			}
		}

		else {
			Toast.makeText(getApplicationContext(), "Not Enough Information",
					Toast.LENGTH_LONG).show();
		}
	}

}
