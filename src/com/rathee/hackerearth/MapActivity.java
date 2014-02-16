package com.rathee.hackerearth;

import java.util.List;
import java.util.Vector;

import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends FragmentActivity {

	private GoogleMap googleMap;
	
	long USER_ID = (long) 0.0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		Intent intent = getIntent();

		final long userID = intent.getLongExtra("userID", 0);
		USER_ID = userID;

		showTweetOnMap();
		
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
	
	

	private void showTweetOnMap() {

		initilizeMap();

		try {
			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
			confbuilder.setOAuthConsumerKey(MainActivity.TWITTER_CONSUMER_KEY);
			confbuilder
					.setOAuthConsumerSecret(MainActivity.TWITTER_CONSUMER_SECRET);

			// Access Token
			String access_token = MainActivity.mSharedPreferences.getString(
					MainActivity.PREF_KEY_OAUTH_TOKEN, "");
			// Access Token Secret
			String access_token_secret = MainActivity.mSharedPreferences.getString(
					MainActivity.PREF_KEY_OAUTH_SECRET, "");

			AccessToken accessToken = new AccessToken(access_token,
					access_token_secret);

			Twitter twitter = new TwitterFactory(confbuilder.build())
					.getInstance(accessToken);
			List<Status> statuses = twitter.getUserTimeline(
					USER_ID, new Paging(1, 10));
			Vector<MarkerOptions> markers = new Vector<MarkerOptions>();
			
			for (Status status : statuses) {
				
				if( status.getGeoLocation() != null){
				GeoLocation location = status.getGeoLocation();

				MarkerOptions marker = new MarkerOptions().position(
						new LatLng(location.getLatitude(), location
								.getLongitude())).title("tweets");
				markers.add(marker);
				
				}
			}
			
			for (int i = 0; i < markers.size(); i++) {
				googleMap.addMarker(markers.elementAt(i));
			}
			//googleMap.addMarker(markers.elementAt(0));

		} catch (Exception ex) {
			// failed to fetch tweet
		}

	}

	private void initilizeMap() {

		if (googleMap == null) {
			googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

}
