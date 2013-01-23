package com.grafian.quran;

import com.grafian.quran.parser.MetaData;
import com.grafian.quran.parser.Quran;
import com.grafian.quran.prefs.Bookmark;
import com.grafian.quran.prefs.Config;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

public class App extends Application {

	public static String PACKAGE_NAME;

	final public Config config = new Config();
	final public Bookmark bookmark = new Bookmark();
	final public MetaData metaData = new MetaData();

	final public Quran quran = new Quran();
	final public Quran translation = new Quran();

	public boolean loaded = false;
	public static App app;
	private int loadedQuran = -1;
	private int loadedTranslation = -1;

	@Override
	public void onCreate() {
		super.onCreate();

		PACKAGE_NAME = getPackageName();
		app = this;

		config.load(this);
		bookmark.load(this);
		metaData.load(this, R.raw.quran_data);
	}

	private int getQuranID() {
		return R.raw.quran_simple;
	}

	private int getTranslationID() {
		if (config.lang.equals("id")) {
			return R.raw.id_indonesian;
		} else {
			return R.raw.en_sahih;
		}
	}

	public boolean needDataReload() {
		if (loaded && loadedQuran == getQuranID() && loadedTranslation == getTranslationID()) {
			return false;
		}
		return true;
	}

	public boolean loadAllData(final Context context, final ProgressListener listener) {
		loaded = false;
		new AsyncTask<Void, Integer, Void>() {
			int tick;
			ProgressDialog dialog;

			final private ProgressListener onProgress = new ProgressListener() {
				@Override
				public void onProgress() {
					publishProgress(tick++);
				}

				@Override
				public void onFinish() {
				}
			};

			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			protected void onPreExecute() {
				int max = 0;
				if (loadedQuran != getQuranID()) {
					max += 6236;
				}
				if (loadedTranslation != getTranslationID()) {
					max += 6236;
				}

				dialog = new ProgressDialog(context);
				dialog.setCancelable(true);
				dialog.setMessage(getString(R.string.loading));
				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setProgress(0);
				dialog.setMax(max);
				if (Build.VERSION.SDK_INT >= 11) {
					dialog.setProgressNumberFormat(null);
				}
				dialog.show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				if (loadedQuran != getQuranID()) {
					quran.load(App.this, getQuranID(), metaData, true, onProgress);
					loadedQuran = getQuranID();
				}
				if (loadedTranslation != getTranslationID()) {
					translation.load(App.this, getTranslationID(), metaData, false, onProgress);
					loadedTranslation = getTranslationID();
				}
				loaded = true;
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				int val = values[0];
				if (val % 200 == 0) {
					dialog.setProgress(val);
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				dialog.dismiss();
				loaded = true;
				listener.onFinish();
				if (tick != dialog.getMax()) {
					Toast.makeText(context, "" + tick, Toast.LENGTH_LONG).show();
				}
			}
		}.execute();

		return true;
	}

	public static String getSuraName(int i) {
		String items[];
		if ("en".equals(app.config.lang)) {
			items = app.getResources().getStringArray(R.array.sura_name_en);
		} else {
			items = app.getResources().getStringArray(R.array.sura_name_id);
		}
		return items[i - 1];
	}

	public static String getSuraTranslation(int i) {
		String items[];
		if ("en".equals(app.config.lang)) {
			items = app.getResources().getStringArray(R.array.sura_translation_en);
		} else {
			items = app.getResources().getStringArray(R.array.sura_translation_id);
		}
		return items[i - 1];
	}

}
