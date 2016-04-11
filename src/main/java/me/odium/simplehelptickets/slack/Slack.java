package me.odium.simplehelptickets.slack;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author KasperFranz
 */
public class Slack {

    private static final String POST = "POST";
    private static final String PAYLOAD = "payload=";
    private static final String UTF_8 = "UTF-8";
    
    private static final String CHANNEL = "channel";
    private static final String USERNAME = "username";
    private static final String HTTP = "http";
    private static final String ICON_URL = "icon_url";
    private static final String ICON_EMOJI = "icon_emoji";
    private static final String TEXT = "text";
    
    private String slackChannel = null;
    private String icon = null;
    private String username = null;
    
    private final String service;
    private final int timeout;
    private Object channel;

    public Slack(String service) {
        this(service, 5000);
    }

    public Slack(String service,
                    int timeout) {
        this.timeout = timeout;
        if (service == null) {
            throw new IllegalArgumentException(
                    "Missing WebHook URL Configuration @ SlackApi");
        } else if (!service.startsWith("https://hooks.slack.com/services/")) {
            throw new IllegalArgumentException(
                    "Invalid Service URL. WebHook URL Format: https://hooks.slack.com/services/{id_1}/{id_2}/{token}");
        }

        this.service = service;
    }

    /**
     * Prepare Message and send to Slack
     */
    public void call(String message) {
        if (message != null) {
            this.send(this.prepare(message));
        }
    }

    private String send(JsonObject message) {
        HttpURLConnection connection = null;
        try {
            // Create connection
            final URL url = new URL(this.service);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(POST);
            connection.setConnectTimeout(timeout);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            final String payload = PAYLOAD
                    + URLEncoder.encode(message.toString(), UTF_8);

            // Send request
            final DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(payload);
            wr.flush();
            wr.close();

            // Get Response
            final InputStream is = connection.getInputStream();
            final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }

            rd.close();
            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Convert SlackMessage to JSON
     * 
     * @return JsonObject
     */
    public JsonObject prepare(String text) {
        JsonObject slackMessage =  new JsonObject();
        if (slackChannel != null) {
            slackMessage.addProperty(CHANNEL, slackChannel);
        }

        if (username != null) {
            slackMessage.addProperty(USERNAME, username);
        }

        if (icon != null) {
            if (icon.contains(HTTP)) {
                slackMessage.addProperty(ICON_URL, icon);
            } else {
                slackMessage.addProperty(ICON_EMOJI, icon);
            }
        }


        if (text == null) {
            throw new IllegalArgumentException(
                    "Missing Text field @ SlackMessage");
        } else {
            slackMessage.addProperty(TEXT, text);
        }

       
        return slackMessage;
    }
}