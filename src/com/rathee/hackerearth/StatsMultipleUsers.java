package com.rathee.hackerearth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class StatsMultipleUsers extends Activity {

	ArrayList<Long> USER_IDs;
	ArrayList<String> USER_NAMES;

	private TextView recyclableTextView;
	int[] fixedColumnWidths = new int[] { 31, 35, 35 };
	int[] scrollableColumnWidths = new int[] { 52, 24, 24 };
	int fixedRowHeight = 50;
	int fixedHeaderHeight = 60;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiple_stats);

		Intent intent = getIntent();
		ArrayList<Long> userIDs = (ArrayList<Long>) intent
				.getSerializableExtra("userIDs");
		ArrayList<String> userNames = (ArrayList<String>) intent
				.getSerializableExtra("userNames");
		USER_IDs = userIDs;
		USER_NAMES = userNames;

		showStats();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stats_multiple_users, menu);
		return true;
	}

	void fillReTweetMap() {
		Stats stats = new Stats();
		stats.USER_ID = MainActivity.USER_ID;

		stats.reTweetFlag = true;
		stats.mSharedPreferences = getApplicationContext()
				.getSharedPreferences("MyPref", 0);

		stats.fillTweetsAndSentiments();
	}

	void showStats() {

		 fillReTweetMap();
		TableRow.LayoutParams wrapWrapTableRowParams = new TableRow.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		TableRow row = new TableRow(this);
		// header (fixed vertically)
		TableLayout header = (TableLayout) findViewById(R.id.table_header);
		row.setLayoutParams(wrapWrapTableRowParams);
		row.setGravity(Gravity.CENTER);
		row.setBackgroundColor(Color.GRAY);
		row.addView(makeTableRowWithText("Name", fixedColumnWidths[0],
				fixedHeaderHeight));
		row.addView(makeTableRowWithText("Sentiment\nValue",
				fixedColumnWidths[1], fixedHeaderHeight));
		row.addView(makeTableRowWithText("ReTweet\nValue",
				fixedColumnWidths[2], fixedHeaderHeight));
		header.addView(row);

		TableLayout fixedColumn = (TableLayout) findViewById(R.id.fixed_column);
		// rest of the table (within a scroll view)
		TableLayout scrollablePart = (TableLayout) findViewById(R.id.scrollable_part);

		for (int i = 0; i < USER_IDs.size(); i++) {

			Stats stats = new Stats();
			stats.USER_ID = USER_IDs.get(i);
			stats.mSharedPreferences = getApplicationContext()
					.getSharedPreferences("MyPref", 0);
			stats.fillTweetsAndSentiments();

			long sentimentPrediction = ourGuess(stats.sentiments, stats.dates);
			String userName = USER_NAMES.get(i);
			long reTweet;

			if (Stats.reTweetMap.containsKey(stats.USER_ID))
				reTweet = Stats.reTweetMap.get(stats.USER_ID);
			else
				reTweet = 0;

			TextView fixedView = makeTableRowWithText(userName,
					scrollableColumnWidths[0], fixedRowHeight);
			fixedView.setBackgroundColor(Color.GRAY);
			fixedColumn.addView(fixedView);
			row = new TableRow(this);
			row.setLayoutParams(wrapWrapTableRowParams);
			row.setGravity(Gravity.CENTER);
			row.setBackgroundColor(Color.GRAY);
			row.addView(makeTableRowWithText(sentimentPrediction + "",
					scrollableColumnWidths[1], fixedRowHeight));
			row.addView(makeTableRowWithText(reTweet + "",
					scrollableColumnWidths[1], fixedRowHeight));
			scrollablePart.addView(row);
		}

	}

	public TextView makeTableRowWithText(String text,
			int widthInPercentOfScreenWidth, int fixedHeightInPixels) {
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		recyclableTextView = new TextView(this);
		recyclableTextView.setText(text);
		recyclableTextView.setTextColor(Color.BLACK);
		recyclableTextView.setTextSize(20);
		recyclableTextView.setWidth(widthInPercentOfScreenWidth * screenWidth
				/ 100);
		recyclableTextView.setHeight(fixedHeightInPixels);
		return recyclableTextView;
	}

	long ourGuess(Vector<String> sentiments, Vector<Date> dates) {

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
		return sentimentPrediction;
	}
}
