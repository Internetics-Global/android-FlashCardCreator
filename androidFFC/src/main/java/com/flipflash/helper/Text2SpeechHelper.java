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
        dict.put("af-ZA","Afrikaans (South Africa) ");
        dict.put("ar-AE","Arabic (U.A.E.) ");
        dict.put("ar-BH","Arabic (Bahrain) ");
        dict.put("ar-DZ","Arabic (Algeria) ");
        dict.put("ar-EG","Arabic (Egypt) ");
        dict.put("ar-IQ","Arabic (Iraq) ");
        dict.put("ar-JO","Arabic (Jordan) ");
        dict.put("ar-KW","Arabic (Kuwait) ");
        dict.put("ar-LB","Arabic (Lebanon) ");
        dict.put("ar-LY","Arabic (Libya) ");
        dict.put("ar-MA","Arabic (Morocco) ");
        dict.put("ar-OM","Arabic (Oman) ");
        dict.put("ar-QA","Arabic (Qatar) ");
        dict.put("ar-SY","Arabic (Syria) ");
        dict.put("ar-TN","Arabic (Tunisia) ");
        dict.put("ar-YE","Arabic (Yemen) ");
        dict.put("az-AZ","Azeri (Latin) (Azerbaijan) ");
        dict.put("be-BY","Belarusian (Belarus) ");
        dict.put("bg-BG","Bulgarian (Bulgaria) ");
        dict.put("bs-BA","Bosnian (Bosnia and Herzegovina) ");
        dict.put("cy-GB","Welsh (United Kingdom) ");

        dict.put("cs-CZ","Czech (Czech Republic) ");
        dict.put("da-DK","Danish (Denmark) ");
        dict.put("de-DE","German(Germany) ");
        dict.put("de-AT","German (Austria) ");
        dict.put("de-CH","German (Switzerland) ");
        dict.put("de-DE","German (Germany) ");
        dict.put("de-LI","German (Liechtenstein) ");
        dict.put("de-LU","German (Luxembourg) ");
        dict.put("en-BZ","English (Belize) ");
        dict.put("en-CA","English (Canada) ");
        dict.put("en-CB","English (Caribbean) ");
        dict.put("en-JM","English (Jamaica) ");
        dict.put("en-NZ","English (New Zealand) ");
        dict.put("en-PH","English (Republic of the Philippines) ");
        dict.put("en-TT","English (Trinidad and Tobago) ");
        dict.put("en-ZW","English (Zimbabwe) ");
        dict.put("es-AR","Spanish (Argentina) ");
        dict.put("es-BO","Spanish (Bolivia) ");
        dict.put("es-CL","Spanish (Chile) ");
        dict.put("es-CO","Spanish (Colombia) ");
        dict.put("es-CR","Spanish (Costa Rica) ");
        dict.put("es-DO","Spanish (Dominican Republic) ");
        dict.put("es-EC","Spanish (Ecuador) ");
        dict.put("es-ES","Spanish (Castilian) ");
        dict.put("es-GT","Spanish (Guatemala) ");
        dict.put("es-HN","Spanish (Honduras) ");
        dict.put("es-NI","Spanish (Nicaragua) ");
        dict.put("es-PA","Spanish (Panama) ");
        dict.put("es-PE","Spanish (Peru) ");
        dict.put("es-PR","Spanish (Puerto Rico) ");
        dict.put("es-PY","Spanish (Paraguay) ");
        dict.put("es-SV","Spanish (El Salvador) ");
        dict.put("es-UY","Spanish (Uruguay) ");
        dict.put("es-VE","Spanish (Venezuela) ");
        dict.put("eu-ES","Basque (Spain) ");

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

        dict.put("fa-IR","Farsi (Iran) ");
        dict.put("fr-CA","French (Canada) ");
        dict.put("fr-CH","French (Switzerland) ");
        dict.put("fr-LU","French (Luxembourg) ");
        dict.put("fr-MC","French (Principality of Monaco) ");
        dict.put("gl-ES","Galician (Spain) ");
        dict.put("gu-IN","Gujarati (India) ");


        dict.put("fi-FI","Finnish (Finland) ");
        dict.put("fr-CA","French (Canada) ");
        dict.put("fr-FR","French (France) ");
        dict.put("fr-BE","French (Belgium) ");
        dict.put("he-IL","Hebrew (Israel) ");
        dict.put("hi-IN","Hindi (India) ");
        dict.put("hr-BA","Croatian (Bosnia and Herzegovina) ");
        dict.put("hr-HR","Croatian (Croatia) ");
        dict.put("hy-AM","Armenian (Armenia) ");

        dict.put("hu-HU","Hungarian (Hungary) ");
        dict.put("in-ID","Indonesia (Indonesia) ");
        dict.put("id-ID","Indonesian (Indonesia) ");
        dict.put("is-IS","Icelandic (Iceland) ");

        dict.put("it-IT","Italian (Italy) ");
        dict.put("ja-JP","Japanese (Japan) ");
        dict.put("ko-KR","Korean (Republic of Korea) ");
        dict.put("ka-GE","Georgian (Georgia) ");
        dict.put("kk-KZ","Kazakh (Kazakhstan) ");
        dict.put("kn-IN","Kannada (Indi ");
        dict.put("ko-KR","Korean (Korea) ");
        dict.put("kok-IN","Konkani (India) ");
        dict.put("ky-KG","Kyrgyz (Kyrgyzstan) ");
        dict.put("lt-LT","Lithuanian (Lithuania) ");
        dict.put("lv-LV","Latvian (Latvia) ");
        dict.put("mi-NZ","Maori (New Zealand) ");
        dict.put("mk-MK","FYRO Macedonian (Former Yugoslav Republic of Macedonia) ");
        dict.put("mn-MN","Mongolian (Mongolia) ");
        dict.put("mr-IN","Marathi (India) ");
        dict.put("ms-MY","Malay (Malaysia) ");
        dict.put("ms-BN","Malay (Brunei Darussalam) ");
        dict.put("mt-MT","Maltese (Malta) ");
        dict.put("nn-NO","Norwegian (Nynorsk) (Norway) ");
        dict.put("ns-ZA","Northern Sotho (South Africa) ");

        dict.put("nl-BE","Dutch (Belgium) ");
        dict.put("nl-NL","Dutch (Netherlands) ");
        dict.put("no-NO","Norwegian (Norway) ");

        dict.put("pa-IN","Punjabi (India) ");
        dict.put("ps-AR","Pashto (Afghanistan) ");
        dict.put("pl-PL","Polish (Poland) ");
        dict.put("pt-BR","Portuguese (Brazil) ");
        dict.put("pt-PT","Portuguese (Portugal) ");

        dict.put("qu-BO","Quechua (Bolivia) ");
        dict.put("qu-EC","Quechua (Ecuador) ");
        dict.put("qu-PE","Quechua (Peru) ");
        dict.put("ro-RO","Romanian (Romania) ");
        dict.put("ru-RU","Russian (Russian Federation) ");

        dict.put("se-FI","Sami (Northern) (Finland) ");
        dict.put("se-NO","Sami (Northern) (Norway) ");
        dict.put("se-SE","Sami (Northern) (Sweden) ");
        dict.put("sl-SI","Slovenian (Slovenia) ");
        dict.put("sq-AL","Albanian (Albania) ");
        dict.put("sr-BA","Serbian (Latin) (Bosnia and Herzegovina) ");
        dict.put("sr-SP","Serbian (Latin) (Serbia and Montenegro) ");
        dict.put("sv-FI","Swedish (Finland) ");
        dict.put("sw-KE","Swahili (Kenya) ");
        dict.put("syr-SY","Syriac (Syria) ");
        dict.put("sk-SK","Slovak (Slovakia) ");
        dict.put("sv-SE","Swedish (Sweden) ");

        dict.put("ta-IN","Tamil (India) ");
        dict.put("te-IN","\tTelugu (India) ");
        dict.put("tl-PH","Tagalog (Philippines) ");
        dict.put("tn-ZA","Tswana (South Africa) ");
        dict.put("uk-UA","Ukrainian (Ukraine) ");
        dict.put("ur-PK","Urdu (Islamic Republic of Pakistan) ");
        dict.put("uz-UZ","Uzbek (Latin) (Uzbekistan) ");
        dict.put("uz-UZ","Uzbek (Cyrillic) (Uzbekistan) ");
        dict.put("vi-VN","Vietnamese (Viet Nam) ");
        dict.put("xh-ZA","Xhosa (South Africa) ");
        dict.put("aaa","bbb ");
        dict.put("th-TH","Thai (Thailand) ");
        dict.put("tr-TR","Turkish (Turkey) ");
        dict.put("zh-CN","Chinese (China) ");
        dict.put("zh-HK","Chinese (Hong Kong) ");
        dict.put("zh-TW","Chinese (Taiwan) ");
        dict.put("bn-BD","Bengali (Bangladesh) ");
        dict.put("bn-IN","Bengali (India) ");
        dict.put("zu-ZA","Zulu (South Africa) ");
        dict.put("et-EE","Estonian (Estonia) ");
        dict.put("fil-PH","Filipino (Philippines) ");

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

        if (mAllAvailableLocaleList == null) {
            mAllAvailableLocaleList = fetchAllVText2SpeechLocales();
        }

        for (int i =0; i < mAllAvailableLocaleList.size(); i++) {
            String key = getLanguageLocaleStringFrom(mAllAvailableLocaleList.get(i));
            String displayStr = map.get(key);
            if (displayStr == null) {
                displayStr = key;
            }
            resultList.add(displayStr);
        }

        return resultList;

    }

}
