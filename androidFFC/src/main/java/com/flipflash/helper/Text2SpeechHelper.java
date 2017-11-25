package com.flipflash.helper;

import android.speech.tts.TextToSpeech;

import com.flipflash.util.AppContext;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;


public class Text2SpeechHelper {

    private static final String TAG = FileOperationHelper.class.getSimpleName();
    private static Text2SpeechHelper mText2SpeechHelper;

    private TextToSpeech        mTTS;

    private List<Locale>             mAllAvailableLocaleList;

    private Locale              mDefaultLocale;

    public static Text2SpeechHelper sharedHelper() {
        if (mText2SpeechHelper == null) {
            mText2SpeechHelper = new Text2SpeechHelper();
        }
        return mText2SpeechHelper;
    }

    public void setup() {

        LOGD(TAG, "setupTextToSpeech");

        mDefaultLocale = getDefaultText2SpeechLocale();

        if (mTTS == null) {

            mTTS = new TextToSpeech(AppContext.getAppContext(),new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        LOGD(TAG, "onInit: TTS Initialization Success");

                        mAllAvailableLocaleList = fetchAllVText2SpeechLocales();

                    } else {
                        LOGE(TAG, "onInit: TTS Initialization Failed!");
                    }
                }
            });

        }

    }

    private void destoryTTS() {
        if (mTTS != null) {
            mTTS.shutdown();
        }

        mTTS = null;
    }

    private  List<Locale> fetchAllVText2SpeechLocales() {

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
     * There's another same method in PlayActivity, refactoring later
     * This method's performance is bad, avoid to be called multiple
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
                try {
                    int res = mTTS.isLanguageAvailable(locale);
                    //used to diff en_US_POSIX, since en_US_POSIX is the same as en_US
                    if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE && ("POSIX".equals(locale.getVariant()) == false)) {
                        localeList.add(locale);
                    }
                } catch (MissingResourceException e) {
                    //ignore the error
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

    private List<Locale> getAllVText2SpeechLocales() {
        return mAllAvailableLocaleList;
    }

    public static  String getLanguageLocaleStringFrom(Locale locale) {

        return locale.getLanguage() + "-" + locale.getCountry();

    }




    private HashMap getMapsBetweenLanguageLocalAndDescription() {

        HashMap<String,String> dict = new HashMap();
        dict.put("ar-SA","Arabic (Saudi Arabia) ");
        dict.put("cs-CZ","Czech (Czech Republic) ");
        dict.put("da-DK","Danish (Denmark) ");
        dict.put("de-DE","German(Germany) ");
        dict.put("el-GR","Modern Greek (Greece) ");
        dict.put("en-AU","English (Australia) ");
        dict.put("en-GB","English (United Kingdom) ");
        dict.put("en-IE","English (Ireland) ");
        dict.put("en-IN","English (India) ");
        dict.put("en-US","English (United States) ");
        dict.put("en-ZA","English (South Africa) ");
        dict.put("es-ES","Spanish (Spain) ");
        dict.put("es-MX","Spanish (Mexico) ");
        dict.put("es-US","Spanish (United States) ");
        dict.put("fi-FI","Finnish (Finland) ");
        dict.put("fr-CA","French (Canada) ");
        dict.put("fr-FR","French (France) ");
        dict.put("fr-BE","French (Belgium) ");
        dict.put("he-IL","Hebrew (Israel) ");
        dict.put("hi-IN","Hindi (India) ");
        dict.put("hu-HU","Hungarian (Hungary) ");
        dict.put("in-ID","Indonesia (Indonesia) ");
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

    /*
     * return languageAndLocaleString
     */
    public String getSelectedLanguageLocalString() {

        if (Hawk.contains("Selected_Text2Speech_Language")) {
            return Hawk.get("Selected_Text2Speech_Language");
        } else {

            Locale locale = mDefaultLocale;
            String str = getLanguageLocaleStringFrom(locale);
            return str;

        }

    }

    public  void setSelectedLanguageLocalString(String languageAndLocaleString) {

        Hawk.put("Selected_Text2Speech_Language",languageAndLocaleString);

    }


    public ArrayList<String> availableLanguageLocalStringList() {
        ArrayList<String> resultList = new ArrayList<>();
        for (int i =0; i < mAllAvailableLocaleList.size(); i++) {
            resultList.add(getLanguageLocaleStringFrom(mAllAvailableLocaleList.get(i)));
        }

        return resultList;
    }

    public ArrayList<String> availableDescriptionList() {

        ArrayList<String> resultList = new ArrayList<>();

        HashMap<String,String> map = getMapsBetweenLanguageLocalAndDescription();

        if (mAllAvailableLocaleList != null) {
            for (int i =0; i < mAllAvailableLocaleList.size(); i++) {
                String key = getLanguageLocaleStringFrom(mAllAvailableLocaleList.get(i));
                String displayStr = map.get(key);
                if (displayStr == null) {
                    displayStr = key;
                }
                resultList.add(displayStr);
            }
        }

        return resultList;

    }

}
