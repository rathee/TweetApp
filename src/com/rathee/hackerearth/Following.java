package com.rathee.hackerearth;


import java.util.ArrayList;
import java.util.Vector;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewDebug.IntToString;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Following extends Activity implements OnItemClickListener{

	ArrayList<Model> arrayList;
	long[] FOLLOWING_LIST;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_following);
		
		getFollowingList();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	       finish();
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onBackPressed(){
	     
	     super.onBackPressed();       
	}
	
	
	public void getFollowingList() {

		try {
			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
			confbuilder.setOAuthConsumerKey(MainActivity.TWITTER_CONSUMER_KEY);
			confbuilder
					.setOAuthConsumerSecret(MainActivity.TWITTER_CONSUMER_SECRET);

			// Access Token
			String access_token = MainActivity.mSharedPreferences.getString(
					MainActivity.PREF_KEY_OAUTH_TOKEN, "");
			// Access Token Secret
			String access_token_secret = MainActivity.mSharedPreferences
					.getString(MainActivity.PREF_KEY_OAUTH_SECRET, "");

			AccessToken accessToken = new AccessToken(access_token,
					access_token_secret);

			MainActivity.btnLogoutTwitter.setVisibility(View.GONE);
			MainActivity.btnGetTweets.setVisibility(View.GONE);
			MainActivity.lblUserName.setVisibility(View.GONE);
			MainActivity.btnShowOnMap.setVisibility(View.GONE);
			MainActivity.btnUsers.setVisibility(View.GONE);
			MainActivity.btnFollowers.setVisibility(View.GONE);

			Twitter twitter = new TwitterFactory(confbuilder.build())
					.getInstance(accessToken);

			long cursor = -1;
			
			IDs following = twitter.getFriendsIDs( cursor );

			final long[] followingList = following.getIDs();
			FOLLOWING_LIST = followingList;

			//ArrayList<String> arrayList = new ArrayList<String>();
			arrayList = new ArrayList<Model>();

			ResponseList<User> users = twitter.lookupUsers(followingList);
			
			
			
			for (int i = 0; i < users.size(); i++) {
				
				User user = users.get(i);
				/*
				String url = user.getProfileImageURL();
				URL imageURL = new URL(url);
				Bitmap bmp = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
				*/
				
				String url = user.getProfileImageURL();
				String userName = user.getScreenName();
				
				/*
				UserInfo userInfo = new UserInfo();
				userInfo.name = userName;
				*/
				Model model = new Model(userName);
				//arrayList.add(userName);
				arrayList.add(model);
			}
			
			final ListView lv = (ListView) findViewById(R.id.storeUser);
			//ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arrayList);
			ArrayAdapter<Model> arrayAdapter = new MyAdapter(this, arrayList);
			
			lv.setAdapter(arrayAdapter);
			
			/*
			lv.setOnItemClickListener(new OnItemClickListener() {
				  @Override
				  public void onItemClick(AdapterView<?> parent, View view,
				    int position, long id) {
				    
				    Intent intent = new Intent(Following.this, Stats.class);
				    intent.putExtra("userName", lv.getItemIdAtPosition(position));
				    intent.putExtra("userID", followingList[position]);
		            startActivity(intent);      
		            finish();
				  }
				}); 
				
				*/
			lv.setOnItemClickListener(this);
			
			Button btnSubmit = (Button) findViewById(R.id.submitFollowings);
			
			btnSubmit.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// Call logout twitter function
					singleOrMore();
				}
			});
			

		} catch (Exception ex) {
			// failed to fetch tweet
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
		TextView label = (TextView) v.getTag(R.id.label);
		CheckBox checkbox = (CheckBox) v.getTag(R.id.check);
		Toast.makeText(v.getContext(), label.getText().toString()+" "+isCheckedOrNot(checkbox), Toast.LENGTH_LONG).show();
		
	}
	
	private String isCheckedOrNot(CheckBox checkbox) {
		if(checkbox.isChecked())
			return "is checked";
		else
			return "is not checked";
	}
	
	void singleOrMore( ) {
		
		int count = 0;
		Vector<Integer> positions = new Vector<Integer>();
		ArrayList<Long> userIDs = new ArrayList<Long>();
		ArrayList<String> userNames = new ArrayList<String>();
		
		final ListView lv = (ListView) findViewById(R.id.storeUser);
		
		for (int i = 0; i < arrayList.size(); i++) {
			Model model = arrayList.get(i);
			
			if( model.isSelected() ) {
				count++;
				positions.add(i);
				userIDs.add(FOLLOWING_LIST[i]);
				
				userNames.add( model.getName());
			}
		}
		
		
		
		if ( count == 1 ) {
			Model model = arrayList.get(0);
			Intent intent = new Intent(Following.this, Stats.class);
		    intent.putExtra("userName", model.getName());
		    intent.putExtra("userID", FOLLOWING_LIST[positions.elementAt(0)]);
            startActivity(intent);      
            finish();
		}
		else {
			Intent intent = new Intent(Following.this, StatsMultipleUsers.class);
			intent.putExtra("userIDs", userIDs);
			intent.putExtra("userNames", userNames);
			startActivity(intent);
			finish();
		}
	}

}

