package com.rathee.hackerearth;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LMClassifier;
import com.aliasi.util.AbstractExternalizable;

public class SentimentClassifier {

	String[] categories;
	LMClassifier LMClass;

	public SentimentClassifier() {
		try {
			File file = new File(Environment.getExternalStorageDirectory(),
					Stats.fileName);
			LMClass = (LMClassifier) AbstractExternalizable
					.readObject(file);
			categories = LMClass.categories();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String classify(String text) {
		ConditionalClassification classification = LMClass.classify(text);
		return classification.bestCategory();
	}
}
