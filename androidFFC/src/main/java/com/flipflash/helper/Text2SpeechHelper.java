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
        dict.put("prs-AF","Afghanistan - Dari ");
        dict.put("ps-AF","Afghanistan - Pashto ");
        dict.put("sq-AL","Albania - Albanian ");
        dict.put("ar-DZ","Algeria - Arabic ");
        dict.put("tzm-DZ","Algeria - Tamazight (Latin) ");
        dict.put("es-AR","Argentina - Spanish ");
        dict.put("hy-AM","Armenia - Armenian ");
        dict.put("en-AU","Australia - English ");
        dict.put("de-AT","Austria - German ");
        dict.put("az-AZ","Azerbaijan - Azeri (Cyrillic) ");
        dict.put("az-AZ","Azerbaijan - Azeri (Latin) ");
        dict.put("ar-BH","Bahrain - Arabic ");
        dict.put("bn-BD","Bangladesh - Bengali ");
        dict.put("be-BY","Belarus - Belarusian ");
        dict.put("nl-BE","Belgium - Dutch ");
        dict.put("fr-BE","Belgium - French ");
        dict.put("en-BZ","Belize - English ");
        dict.put("es-VE","Bolivarian Republic of Venezuela - Spanish ");
        dict.put("quz-BO","Bolivia - Quechua ");
        dict.put("es-BO","Bolivia - Spanish ");
        dict.put("bs-BA","Bosnia and Herzegovina - Bosnian (Cyrillic) ");
        dict.put("bs-BA","Bosnia and Herzegovina - Bosnian (Latin) ");
        dict.put("hr-BA","Bosnia and Herzegovina - Croatian ");
        dict.put("sr-BA","Bosnia and Herzegovina - Serbian (Cyrillic) ");
        dict.put("sr-BA","Bosnia and Herzegovina - Serbian (Latin) ");
        dict.put("pt-BR","Brazil - Portuguese ");
        dict.put("ms-BN","Brunei Darussalam - Malay ");
        dict.put("bg-BG","Bulgaria - Bulgarian ");
        dict.put("km-KH","Cambodia - Khmer ");
        dict.put("en-CA","Canada - English ");
        dict.put("fr-CA","Canada - French ");
        dict.put("iu-CA","Canada - Inuktitut (Latin) ");
        dict.put("iu-CA","Canada - Inuktitut (Syllabics) ");
        dict.put("moh-CA","Canada - Mohawk ");
        dict.put("en-029","Caribbean - English ");
        dict.put("arn-CL","Chile - Mapudungun ");
        dict.put("es-CL","Chile - Spanish ");
        dict.put("es-CO","Colombia - Spanish ");
        dict.put("es-CR","Costa Rica - Spanish ");
        dict.put("hr-HR","Croatia - Croatian ");
        dict.put("cs-CZ","Czech Republic - Czech ");
        dict.put("da-DK","Denmark - Danish ");
        dict.put("es-DO","Dominican Republic - Spanish ");
        dict.put("quz-EC","Ecuador - Quechua ");
        dict.put("es-EC","Ecuador - Spanish ");
        dict.put("ar-EG","Egypt - Arabic ");
        dict.put("es-SV","El Salvador - Spanish ");
        dict.put("et-EE","Estonia - Estonian ");
        dict.put("am-ET","Ethiopia - Amharic ");
        dict.put("fo-FO","Faroe Islands - Faroese ");
        dict.put("fi-FI","Finland - Finnish ");
        dict.put("smn-FI","Finland - Sami (Inari) ");
        dict.put("se-FI","Finland - Sami (Northern) ");
        dict.put("sms-FI","Finland - Sami (Skolt) ");
        dict.put("sv-FI","Finland - Swedish ");
        dict.put("gsw-FR","France - Alsatian ");
        dict.put("br-FR","France - Breton ");
        dict.put("co-FR","France - Corsican ");
        dict.put("fr-FR","France - French ");
        dict.put("oc-FR","France - Occitan ");
        dict.put("ka-GE","Georgia - Georgian ");
        dict.put("de-DE","Germany - German ");
        dict.put("dsb-DE","Germany - Lower Sorbian ");
        dict.put("hsb-DE","Germany - Upper Sorbian ");
        dict.put("el-GR","Greece - Greek ");
        dict.put("kl-GL","Greenland - Greenlandic ");
        dict.put("qut-GT","Guatemala - K'iche ");
        dict.put("es-GT","Guatemala - Spanish ");
        dict.put("es-HN","Honduras - Spanish ");
        dict.put("zh-HK","Hong Kong S.A.R. - Chinese (Traditional) Legacy ");
        dict.put("hu-HU","Hungary - Hungarian ");
        dict.put("is-IS","Iceland - Icelandic ");
        dict.put("as-IN","India - Assamese ");
        dict.put("bn-IN","India - Bengali ");
        dict.put("en-IN","India - English ");
        dict.put("gu-IN","India - Gujarati ");
        dict.put("hi-IN","India - Hindi ");
        dict.put("kn-IN","India - Kannada ");
        dict.put("kok-IN","India - Konkani ");
        dict.put("ml-IN","India - Malayalam ");
        dict.put("mr-IN","India - Marathi ");
        dict.put("or-IN","India - Oriya ");
        dict.put("pa-IN","India - Punjabi ");
        dict.put("sa-IN","India - Sanskrit ");
        dict.put("ta-IN","India - Tamil ");
        dict.put("te-IN","India - Telugu ");
        dict.put("id-ID","Indonesia - Indonesian ");
        dict.put("fa-IR","Iran - Persian ");
        dict.put("ar-IQ","Iraq - Arabic ");
        dict.put("en-IE","Ireland - English ");
        dict.put("ga-IE","Ireland - Irish ");
        dict.put("ur-PK","Islamic Republic of Pakistan - Urdu ");
        dict.put("he-IL","Israel - Hebrew ");
        dict.put("it-IT","Italy - Italian ");
        dict.put("en-JM","Jamaica - English ");
        dict.put("ja-JP","Japan - Japanese ");
        dict.put("ar-JO","Jordan - Arabic ");
        dict.put("kk-KZ","Kazakhstan - Kazakh ");
        dict.put("sw-KE","Kenya - Kiswahili ");
        dict.put("ko-KR","Korea - Korean ");
        dict.put("ar-KW","Kuwait - Arabic ");
        dict.put("ky-KG","Kyrgyzstan - Kyrgyz ");
        dict.put("lo-LA","Lao P.D.R. - Lao ");
        dict.put("lv-LV","Latvia - Latvian ");
        dict.put("ar-LB","Lebanon - Arabic ");
        dict.put("ar-LY","Libya - Arabic ");
        dict.put("de-LI","Liechtenstein - German ");
        dict.put("lt-LT","Lithuania - Lithuanian ");
        dict.put("fr-LU","Luxembourg - French ");
        dict.put("de-LU","Luxembourg - German ");
        dict.put("lb-LU","Luxembourg - Luxembourgish ");
        dict.put("zh-MO","Macao S.A.R. - Chinese (Traditional) Legacy ");
        dict.put("mk-MK","Macedonia (FYROM) - Macedonian (FYROM) ");
        dict.put("en-MY","Malaysia - English ");
        dict.put("ms-MY","Malaysia - Malay ");
        dict.put("dv-MV","Maldives - Divehi ");
        dict.put("mt-MT","Malta - Maltese ");
        dict.put("es-MX","Mexico - Spanish ");
        dict.put("mn-MN","Mongolia - Mongolian (Cyrillic) ");
        dict.put("sr-ME","Montenegro - Serbian (Cyrillic) ");
        dict.put("sr-ME","Montenegro - Serbian (Latin) ");
        dict.put("ar-MA","Morocco - Arabic ");
        dict.put("ne-NP","Nepal - Nepali ");
        dict.put("nl-NL","Netherlands - Dutch ");
        dict.put("fy-NL","Netherlands - Frisian ");
        dict.put("en-NZ","New Zealand - English ");
        dict.put("mi-NZ","New Zealand - Maori ");
        dict.put("es-NI","Nicaragua - Spanish ");
        dict.put("ha-NG","Nigeria - Hausa (Latin) ");
        dict.put("ig-NG","Nigeria - Igbo ");
        dict.put("yo-NG","Nigeria - Yoruba ");
        dict.put("nb-NO","Norway - Norwegian (Bokmal) ");
        dict.put("nn-NO","Norway - Norwegian (Nynorsk) ");
        dict.put("smj-NO","Norway - Sami (Lule) ");
        dict.put("se-NO","Norway - Sami (Northern) ");
        dict.put("sma-NO","Norway - Sami (Southern) ");
        dict.put("ar-OM","Oman - Arabic ");
        dict.put("es-PA","Panama - Spanish ");
        dict.put("es-PY","Paraguay - Spanish ");
        dict.put("zh-CN","People's Republic of China - Chinese (Simplified) Legacy ");
        dict.put("mn-CN","People's Republic of China - Mongolian (Traditional Mongolian) ");
        dict.put("bo-CN","People's Republic of China - Tibetan ");
        dict.put("ug-CN","People's Republic of China - Uyghur ");
        dict.put("ii-CN","People's Republic of China - Yi ");
        dict.put("quz-PE","Peru - Quechua ");
        dict.put("es-PE","Peru - Spanish ");
        dict.put("fil-PH","Philippines - Filipino ");
        dict.put("pl-PL","Poland - Polish ");
        dict.put("pt-PT","Portugal - Portuguese ");
        dict.put("fr-MC","Principality of Monaco - French ");
        dict.put("es-PR","Puerto Rico - Spanish ");
        dict.put("ar-QA","Qatar - Arabic ");
        dict.put("en-PH","Republic of the Philippines - English ");
        dict.put("ro-RO","Romania - Romanian ");
        dict.put("ba-RU","Russia - Bashkir ");
        dict.put("ru-RU","Russia - Russian ");
        dict.put("tt-RU","Russia - Tatar ");
        dict.put("sah-RU","Russia - Yakut ");
        dict.put("rw-RW","Rwanda - Kinyarwanda ");
        dict.put("ar-SA","Saudi Arabia - Arabic ");
        dict.put("wo-SN","Senegal - Wolof ");
        dict.put("sr-RS","Serbia - Serbian (Cyrillic) ");
        dict.put("sr-RS","Serbia - Serbian (Latin) ");
        dict.put("sr-CS","Serbia and Montenegro (Former) - Serbian (Cyrillic) ");
        dict.put("sr-CS","Serbia and Montenegro (Former) - Serbian (Latin) ");
        dict.put("zh-SG","Singapore - Chinese (Simplified) Legacy ");
        dict.put("en-SG","Singapore - English ");
        dict.put("sk-SK","Slovakia - Slovak ");
        dict.put("sl-SI","Slovenia - Slovenian ");
        dict.put("af-ZA","South Africa - Afrikaans ");
        dict.put("en-ZA","South Africa - English ");
        dict.put("xh-ZA","South Africa - isiXhosa ");
        dict.put("zu-ZA","South Africa - isiZulu ");
        dict.put("nso-ZA","South Africa - Sesotho sa Leboa ");
        dict.put("tn-ZA","South Africa - Setswana ");
        dict.put("eu-ES","Spain - Basque ");
        dict.put("ca-ES","Spain - Catalan ");
        dict.put("gl-ES","Spain - Galician ");
        dict.put("es-ES","Spain - Spanish ");
        dict.put("si-LK","Sri Lanka - Sinhala ");
        dict.put("smj-SE","Sweden - Sami (Lule) ");
        dict.put("se-SE","Sweden - Sami (Northern) ");
        dict.put("sma-SE","Sweden - Sami (Southern) ");
        dict.put("sv-SE","Sweden - Swedish ");
        dict.put("fr-CH","Switzerland - French ");
        dict.put("de-CH","Switzerland - German ");
        dict.put("it-CH","Switzerland - Italian ");
        dict.put("rm-CH","Switzerland - Romansh ");
        dict.put("ar-SY","Syria - Arabic ");
        dict.put("syr-SY","Syria - Syriac ");
        dict.put("zh-TW","Taiwan - Chinese (Traditional) Legacy ");
        dict.put("tg-TJ","Tajikistan - Tajik (Cyrillic) ");
        dict.put("th-TH","Thailand - Thai ");
        dict.put("en-TT","Trinidad and Tobago - English ");
        dict.put("ar-TN","Tunisia - Arabic ");
        dict.put("tr-TR","Turkey - Turkish ");
        dict.put("tk-TM","Turkmenistan - Turkmen ");
        dict.put("ar-AE","U.A.E. - Arabic ");
        dict.put("uk-UA","Ukraine - Ukrainian ");
        dict.put("en-GB","United Kingdom - English ");
        dict.put("gd-GB","United Kingdom - Scottish Gaelic ");
        dict.put("cy-GB","United Kingdom - Welsh ");
        dict.put("en-US","United States - English ");
        dict.put("es-US","United States - Spanish ");
        dict.put("es-UY","Uruguay - Spanish ");
        dict.put("uz-UZ","Uzbekistan - Uzbek (Cyrillic) ");
        dict.put("uz-UZ","Uzbekistan - Uzbek (Latin) ");
        dict.put("vi-VN","Vietnam - Vietnamese ");
        dict.put("ar-YE","Yemen - Arabic ");
        dict.put("en-ZW","Zimbabwe - English ");

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
