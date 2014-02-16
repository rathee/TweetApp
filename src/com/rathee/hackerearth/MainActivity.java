package com.rathee.hackerearth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	final String fileName = "classifier.txt";
	
	static String TWITTER_CONSUMER_KEY = "SDz7ur6u6Bs4J73RtlkgiA";
	static String TWITTER_CONSUMER_SECRET = "FNre7vmarUrBRlfE8vH0Y0dWqB8L1rJVOh0i8EhckE";

	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
	static long USER_ID = (long) 0.0;

	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	static Button btnLoginTwitter;
	static Button btnLogoutTwitter;
	static Button btnGetTweets;
	static Button btnShowOnMap;
	static Button btnUsers, btnFollowers;

	static TextView lblUpdate;
	static TextView lblUserName;
	

	// Progress dialog
	ProgressDialog pDialog;

	// Twitter
	private static Twitter twitter;
	private static RequestToken requestToken;

	// Shared Preferences
	public static SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		writeToSDCard();
		
		btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
		btnLogoutTwitter = (Button) findViewById(R.id.btnLogoutTwitter);
		lblUserName = (TextView) findViewById(R.id.lblUserName);
		btnGetTweets = (Button) findViewById(R.id.getTweets);
		btnShowOnMap = (Button) findViewById(R.id.showOnMap);
		btnUsers = ( Button ) findViewById(R.id.btnUser);
		btnFollowers = ( Button ) findViewById(R.id.btnFollower);
		mSharedPreferences = getApplicationContext().getSharedPreferences(
				"MyPref", 0);

		
		btnLoginTwitter.setOnClickListener(new View.OnClickListener() {

			
			@Override
			public void onClick(View arg0) {
				
				if (isTwitterLoggedInAlready()) {
					logoutFromTwitter();
				}
				// Call login twitter function
				loginToTwitter();
				finish();		

			}
		});

		btnLogoutTwitter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Call logout twitter function
				logoutFromTwitter();
			}
		});

		btnGetTweets.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Call logout twitter function
				getTweetsFromTwitter();
				//finish();
			}
		});
		
		btnShowOnMap.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Call logout twitter function
				Intent intent = new Intent(MainActivity.this, MapActivity.class);
				intent.putExtra("userID", USER_ID);
	            startActivity(intent);      
	            finish();
			}
		});
		
		btnUsers.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent( MainActivity.this, Following.class);
				startActivity(intent);
				finish();
				
			}
		});
		
		btnFollowers.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent( MainActivity.this, Followers.class);
				startActivity(intent);
				finish();
				
			}
		});

		/**
		 * This if conditions is tested once is redirected from twitter page.
		 * Parse the uri to get oAuth Verifier
		 * */
		if (!isTwitterLoggedInAlready()) {
			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
				// oAuth verifier
				String verifier = uri
						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);

					// Shared Preferences
					Editor e = mSharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,
							accessToken.getTokenSecret());
					// Store login status - true
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					e.commit(); // save changes

					if (TWITTER_CONSUMER_KEY.trim().length() == 0
							|| TWITTER_CONSUMER_SECRET.trim().length() == 0) {
						// Internet Connection is not present
						Log.e("Twitter oAuth tokens",
								"Please set your twitter oauth tokens first!");
						// stop executing code by return
						return;
					}
					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

					btnLogoutTwitter.setVisibility(View.VISIBLE);

					btnLoginTwitter.setVisibility(View.GONE);
					btnGetTweets.setVisibility(View.VISIBLE);
					btnUsers.setVisibility(View.VISIBLE);
					btnShowOnMap.setVisibility(View.VISIBLE);
					btnFollowers.setVisibility(View.VISIBLE);

					// Show Update Twitter
					// lblUpdate.setVisibility(View.VISIBLE);

					// Getting user details from twitter
					// For now i am getting his name only
					USER_ID = accessToken.getUserId();
					User user = twitter.showUser(USER_ID);
					String username = user.getName();
					

					// Displaying in xml ui
					lblUserName.setText(Html.fromHtml("<b>Welcome " + username
							+ "</b>"));
				} catch (Exception e) {
					// Check log for login errors
					e.printStackTrace();
					Log.e("Twitter Login Error", "> " + e.getMessage());
				}

			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}
	
	

	private void loginToTwitter() {
		// Check if already logged in
		if (!isTwitterLoggedInAlready()) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();

			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			try {
				requestToken = twitter
						.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(requestToken.getAuthenticationURL())));
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		} else {
			// user already logged into twitter
			Toast.makeText(getApplicationContext(),
					"Already Logged into twitter", Toast.LENGTH_LONG).show();
		}
	}

	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	private void logoutFromTwitter() {
		// Clear the shared preferences
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.commit();

		// After this take the appropriate action
		// I am showing the hiding/showing buttons again
		// You might not needed this code
		btnLogoutTwitter.setVisibility(View.GONE);
		btnGetTweets.setVisibility(View.GONE);
		lblUserName.setVisibility(View.GONE);
		btnShowOnMap.setVisibility(View.GONE);
		btnUsers.setVisibility(View.GONE);

		btnLoginTwitter.setVisibility(View.VISIBLE);
	}

	private void getTweetsFromTwitter() {
		try {
			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
			confbuilder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			confbuilder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			
			// Access Token
            String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
            // Access Token Secret
            String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
             
            AccessToken accessToken = new AccessToken(access_token, access_token_secret);
            
			btnLogoutTwitter.setVisibility(View.GONE);
			btnGetTweets.setVisibility(View.GONE);
			lblUserName.setVisibility(View.GONE);
			btnShowOnMap.setVisibility(View.GONE);
			btnUsers.setVisibility(View.GONE);

			Twitter twitter = new TwitterFactory(confbuilder.build())
					.getInstance(accessToken);
			List<Status> statuses = twitter.getUserTimeline(USER_ID,
					new Paging(1, 10));
			
			IDs following = twitter.getFriendsIDs(USER_ID);
			
			long[] followingList = following.getIDs();
			
			for (int i = 0; i < followingList.length; i++) {
				
				
				Toast.makeText(getApplicationContext(), twitter.showUser(followingList[i]).toString(), 100).show();
			}
			
			int count = 0;
			//Collections.reverse(statuses); // reverse order
			ArrayList<String> arrayList = new ArrayList<String>();
			for (Status status : statuses) {
				String tweettext = status.getUser().getName() + ":"
						+ status.getText() + "\n ("
						+ status.getCreatedAt().toLocaleString() + ")" + status.getGeoLocation();
				arrayList.add(tweettext);
				
				// now single tweet is in tweettext
			}
			
			ListView lv = (ListView) findViewById(R.id.storeTweet);
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
	                android.R.layout.simple_list_item_1, arrayList);

	        lv.setAdapter(arrayAdapter);
		} catch (Exception ex) {
			// failed to fetch tweet
		}

	}
	
	void writeToSDCard() {

		OutputStream outputStream = null;
		try {
			AssetManager am = getAssets();
			String[] files = am.list("");
		
			InputStream is = am.open(fileName);
			
			File file = new File(Environment.getExternalStorageDirectory(),
					fileName);
						outputStream = new FileOutputStream(file);
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			is.close();
			outputStream.close();
			
		} catch (FileNotFoundException e) {
			// handle exception
		} catch (IOException e) {
			// handle exception
		}
	}
	
	
}
