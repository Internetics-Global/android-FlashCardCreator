package com.flipflash.android_ffc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.flipflash.helper.Text2SpeechHelper;


import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

/**
 * Created by BourneWang on 5/12/2015.
 */
public class SelectText2SpeechLanguageActivity extends Activity {

    ListView               mListView;

    /*
     * key is the format of "zh-tw", value is display name: Chinese (Taiwan)
     */
    HashMap<String,String> mMapBetweenLanguageLocaleAndDescription;

    List<Locale>                 mAllAvailableLocaleList;

    private static final String TAG = PlayActivity.class.getSimpleName();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "onCreate:");

        mMapBetweenLanguageLocaleAndDescription = Text2SpeechHelper.sharedHelper().getMapsBetweenLanguageLocalAndDescription();

        mAllAvailableLocaleList = Text2SpeechHelper.sharedHelper().getAllVText2SpeechLocales();;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.select_speech_language);

        setTitle(getString(R.string.Table_Item_Speech_Language_Select));

        setupListView();

    }

    private void setupListView() {

        mListView = (ListView) findViewById(R.id.list_view);

        ArrayAdapter<String> arrayAdapter = new SpeechLanguageAdapter(SelectText2SpeechLanguageActivity.this);
        mListView.setAdapter(arrayAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                itemClicked(i);
            }
        });
    }


    private void itemClicked(int position) {

        Locale locale = mAllAvailableLocaleList.get(position);
        String str = Text2SpeechHelper.sharedHelper().getLanguageAndLocaleString(locale);

        Text2SpeechHelper.sharedHelper().setSelectedText2SpeechLanguage(str);
        ((ArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();

    }

    class SpeechLanguageAdapter extends ArrayAdapter<String> {

        private Context   mContext;

        public SpeechLanguageAdapter(Context context) {
            super(context,R.layout.select_speech_language_listview_item);
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mAllAvailableLocaleList == null?0:mAllAvailableLocaleList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.select_speech_language_listview_item, parent, false);
            TextView titleTextView = (TextView) convertView.findViewById(R.id.title_textview);

            Locale locale = mAllAvailableLocaleList.get(position);
            String key = Text2SpeechHelper.sharedHelper().getLanguageAndLocaleString(locale);

            String displayStr = mMapBetweenLanguageLocaleAndDescription.get(key);
            if (displayStr == null) {
                displayStr = key;
            }

            titleTextView.setText(displayStr);

            Button checkedButton = (Button) convertView.findViewById(R.id.checked_button);
            if (key.equals(Text2SpeechHelper.sharedHelper().getSelectedText2SpeechLanguage())) {
                checkedButton.setVisibility(View.VISIBLE);
            } else {
                checkedButton.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

    }
}