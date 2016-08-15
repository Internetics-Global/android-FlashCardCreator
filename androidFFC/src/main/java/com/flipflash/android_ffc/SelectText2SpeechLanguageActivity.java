package com.flipflash.android_ffc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flipflash.util.AppConfig;
import com.flipflash.util.AppContext;
import com.orhanobut.hawk.Hawk;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

/**
 * Created by BourneWang on 5/12/2015.
 */
public class SelectText2SpeechLanguageActivity extends Activity {

    ListView               mListView;
    private TextToSpeech   mTTS;

    /*
     * key is the format of "zh-tw", value is display name: Chinese (Taiwan)
     */
    HashMap<String,String>       mLanguageLocalePairings = getLanguageLocalePairings();

    List<Locale>                 mAllAvailableLocaleList;

    String                       mSelectedSpeechLanguage;

    private static final String TAG = PlayActivity.class.getSimpleName();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOGD(TAG, "onCreate:");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.select_speech_language);

        setTitle(getString(R.string.Table_Item_Speech_Language_Select));

        setupTextToSpeech();

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

    private void setupTextToSpeech() {

        LOGD(TAG, "setupTextToSpeech");

        if (mTTS == null) {

            mTTS = new TextToSpeech(AppContext.getAppContext(),new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        LOGD(TAG, "onInit: TTS Initialization Success");

                        mAllAvailableLocaleList = getAllVText2SpeechLocales();
                        mSelectedSpeechLanguage = getSelectedText2SpeechLanguage();
                        ((ArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();



                    } else {
                        LOGE(TAG, "onInit: TTS Initialization Failed!");

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                SelectText2SpeechLanguageActivity.this);
                        alertDialogBuilder.setTitle("Alert");
                        alertDialogBuilder
                                .setMessage("Failed to fetch language list, please try again").show();
                    }
                }
            });
            mTTS.setSpeechRate((float) 0.4);

        }

    }

    private void itemClicked(int position) {

        Locale locale = mAllAvailableLocaleList.get(position);
        String str = getLanguageAndLocaleString(locale);

        setSelectedText2SpeechLanguage(str);
        ((ArrayAdapter) mListView.getAdapter()).notifyDataSetChanged();

    }


    private List<Locale> getAllVText2SpeechLocales() {

        LOGD(TAG, "getText2SpeechLocale");

        List<Locale> localeList = new ArrayList<Locale>();

        try {
            Locale[] locales = Locale.getAvailableLocales();

            for (Locale locale : locales) {
                int res = mTTS.isLanguageAvailable(locale);
                if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE && ("POSIX".equals(locale.getVariant()) == false)) {
                    localeList.add(locale);
                }
            }

        } catch (MissingResourceException ex) {
            System.out.println("Error " + ex.getMessage());

        } catch (Exception ex) {
            System.out.println("Error " + ex.getMessage());
        }

        return localeList;

    }

    /*
     * There's another same method in SelectText2SpeechLanguageActivity, refactoring later
    */
    private String getLanguageAndLocaleString(Locale locale) {

        return locale.getLanguage() + "-" + locale.getCountry();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTTS != null) {
            mTTS.shutdown();
        }

        mTTS = null;
    }

    /*
     * There's another same method in PlayActivity, refactoring later
     */
    private Locale getDefaultText2SpeechLocale() {

        LOGD(TAG, "getText2SpeechLocale");

        Locale defaultLocale = new Locale("en","US");  //since no australia is available, we have to use US

        try {
            Locale[] locales = Locale.getAvailableLocales();



            if (locales == null) {
                return defaultLocale;
            }

            List<Locale> localeList = new ArrayList<Locale>();
            for (Locale locale : locales) {
                int res = mTTS.isLanguageAvailable(locale);
                //used to diff en_US_POSIX, since en_US_POSIX is the same as en_US
                if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE && ("POSIX".equals(locale.getVariant()) == false)) {
                    localeList.add(locale);
                }
            }

            String languageStr = Locale.getDefault().getLanguage();
            String countryStr = Locale.getDefault().getCountry();

            if (languageStr == null || countryStr == null) {
                return defaultLocale;
            }



            for (Locale item : localeList) {
                if (item.getCountry().equals(countryStr) && item.getLanguage().equals(languageStr)) {
                    return item;
                }
            }

            //In case that you can still find nothing
            return  defaultLocale;


        } catch (MissingResourceException ex) {
            System.out.println("Error " + ex.getMessage());

        } catch (Exception ex) {
            System.out.println("Error " + ex.getMessage());
        }

        return defaultLocale;

    }


    private HashMap getLanguageLocalePairings() {

        HashMap<String,String> dict = new HashMap();
        dict.put("ar-SA","Arabic (Saudi Arabia) ");
        dict.put("cs-CZ","Czech (Czech Republic) ");
        dict.put("da-DK","Danish (Denmark) ");
        dict.put("de-DE","German(Germany) ");
        dict.put("el-GR","Modern Greek (Greece) ");
        dict.put("en-AU","English (Australia) ");
        dict.put("en-GB","English (United Kingdom) ");
        dict.put("en-IE","English (Ireland) ");
        dict.put("en-US","English (United States) ");
        dict.put("en-ZA","English (South Africa) ");
        dict.put("es-ES","Spanish (Spain) ");
        dict.put("es-MX","Spanish (Mexico) ");
        dict.put("fi-FI","Finnish (Finland) ");
        dict.put("fr-CA","French (Canada) ");
        dict.put("fr-FR","French (France) ");
        dict.put("he-IL","Hebrew (Israel) ");
        dict.put("hi-IN","Hindi (India) ");
        dict.put("hu-HU","Hungarian (Hungary) ");
        dict.put("id-ID","Indonesian (Indonesia) ");
        dict.put("it-IT","Italian (Italy) ");
        dict.put("ja-JP","Japanese (Japan) ");
        dict.put("ko-KR","Korean (Republic of Korea) ");
        dict.put("nl-BE","Dutch (Belgium) ");
        dict.put("nl-NL","Dutch (Netherlands) ");
        dict.put("no-NO","Norwegian (Norway) ");
        dict.put("pl-PL","Polish (Poland) ");
        dict.put("pt-BR","Portuguese (Brazil) ");
        dict.put("pt-PT","Portuguese (Portugal) ");
        dict.put("ro-RO","Romanian (Romania) ");
        dict.put("ru-RU","Russian (Russian Federation) ");
        dict.put("sk-SK","Slovak (Slovakia) ");
        dict.put("sv-SE","Swedish (Sweden) ");
        dict.put("th-TH","Thai (Thailand) ");
        dict.put("tr-TR","Turkish (Turkey) ");
        dict.put("zh-CN","Chinese (China) ");
        dict.put("zh-HK","Chinese (Hong Kong) ");
        dict.put("zh-TW","Chinese (Taiwan) ");

        return dict;

    }

    private String getSelectedText2SpeechLanguage() {

        if (Hawk.contains("Selected_Text2Speech_Language")) {
            return Hawk.get("Selected_Text2Speech_Language");
        } else {

            Locale locale = getDefaultText2SpeechLocale();
            String str = getLanguageAndLocaleString(locale);
            return str;

        }

    }

    private void setSelectedText2SpeechLanguage(String languageName) {

        Hawk.put("Selected_Text2Speech_Language",languageName);
        mSelectedSpeechLanguage = getSelectedText2SpeechLanguage();

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
            String key = getLanguageAndLocaleString(locale);

            String displayStr = mLanguageLocalePairings.get(key);

            titleTextView.setText(displayStr);

            Button checkedButton = (Button) convertView.findViewById(R.id.checked_button);
            if (key.equals(mSelectedSpeechLanguage)) {
                checkedButton.setVisibility(View.VISIBLE);
            } else {
                checkedButton.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

    }
}