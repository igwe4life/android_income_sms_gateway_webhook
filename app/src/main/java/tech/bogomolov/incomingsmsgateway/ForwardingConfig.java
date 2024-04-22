package tech.bogomolov.incomingsmsgateway;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class ForwardingConfig {
    final private Context context;

    private static final String KEY_KEY = "key";
    private static final String KEY_SENDER = "sender";
    private static final String KEY_URL = "url";
    private static final String KEY_TEMPLATE = "template";
    private static final String KEY_HEADERS = "headers";
    private static final String KEY_IGNORE_SSL = "ignore_ssl";
    private static final String KEY_IS_SMS_ENABLED = "is_sms_enabled";

    private String key;
    private String sender;
    private String url;
    private String template;
    private String headers;
    private boolean ignoreSsl = false;
    private boolean isSmsEnabled = true;

    public ForwardingConfig(Context context) {
        this.context = context;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getHeaders() {
        return this.headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public boolean getIgnoreSsl() {
        return this.ignoreSsl;
    }

    public void setIgnoreSsl(boolean ignoreSsl) {
        this.ignoreSsl = ignoreSsl;
    }

    public boolean getIsSmsEnabled() {
        return this.isSmsEnabled;
    }

    public void setIsSmsEnabled(boolean isSmsEnabled) {
        this.isSmsEnabled = isSmsEnabled;
    }

    public void save() {
        try {
            if (this.getKey() == null) {
                this.setKey(this.generateKey());
            }

            JSONObject json = new JSONObject();
            json.put(KEY_KEY, this.getKey());
            json.put(KEY_SENDER, this.sender);
            json.put(KEY_URL, this.url);
            json.put(KEY_TEMPLATE, this.template);
            json.put(KEY_HEADERS, this.headers);
            json.put(KEY_IGNORE_SSL, this.ignoreSsl);
            json.put(KEY_IS_SMS_ENABLED, this.isSmsEnabled);

            SharedPreferences.Editor editor = getEditor(context);
            editor.putString(this.getKey(), json.toString());

            editor.commit();
        } catch (Exception e) {
            Log.e("ForwardingConfig", e.getMessage());
        }
    }

    public static String getDefaultJsonTemplate() {
        return "{\n  \"from\":\"%from%\",\n  \"text\":\"%text%\",\n  \"sentStamp\":%sentStamp%,\n  \"receivedStamp\":%receivedStamp%,\n  \"sim\":\"%sim%\"\n}";
    }

    public static String getDefaultJsonHeaders() {
        return "{\"User-agent\":\"SMS Forwarder App\"}";
    }

    public static ArrayList<ForwardingConfig> getAll(Context context) {
        SharedPreferences sharedPref = getPreference(context);
        Map<String, ?> sharedPrefs = sharedPref.getAll();

        ArrayList<ForwardingConfig> configs = new ArrayList<>();

        for (Map.Entry<String, ?> entry : sharedPrefs.entrySet()) {
            ForwardingConfig config = new ForwardingConfig(context);
            config.setSender(entry.getKey());

            String value = (String) entry.getValue();

            if (value.charAt(0) == '{') {
                try {
                    JSONObject json = new JSONObject(value);

                    if (!json.has(KEY_KEY)) {
                        config.setKey(entry.getKey());
                    } else {
                        config.setKey(json.getString(KEY_KEY));
                    }

                    if (!json.has(KEY_SENDER)) {
                        config.setSender(entry.getKey());
                    } else {
                        config.setSender(json.getString(KEY_SENDER));
                    }

                    if (!json.has(KEY_IS_SMS_ENABLED)) {
                        config.setIsSmsEnabled(true);
                    } else {
                        config.setIsSmsEnabled(json.getBoolean(KEY_IS_SMS_ENABLED));
                    }

                    config.setUrl(json.getString(KEY_URL));
                    config.setTemplate(json.getString(KEY_TEMPLATE));
                    config.setHeaders(json.getString(KEY_HEADERS));

                    try {
                        config.setIgnoreSsl(json.getBoolean(KEY_IGNORE_SSL));
                    } catch (JSONException ignored) {
                    }
                } catch (JSONException e) {
                    Log.e("ForwardingConfig", e.getMessage());
                }
            } else {
                config.setUrl(value);
                config.setTemplate(ForwardingConfig.getDefaultJsonTemplate());
                config.setHeaders(ForwardingConfig.getDefaultJsonHeaders());
            }

            configs.add(config);
        }

        return configs;
    }

    public void remove() {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(this.getKey());
        editor.commit();
    }

    private static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.key_phones_preference),
                Context.MODE_PRIVATE
        );
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences sharedPref = getPreference(context);
        return sharedPref.edit();
    }

    private String generateKey() {
        String stamp = Long.toString(System.currentTimeMillis());
        int randomNum = new Random().nextInt((999990 - 100000) + 1) + 100000;
        return stamp + '_' + randomNum;
    }
}
