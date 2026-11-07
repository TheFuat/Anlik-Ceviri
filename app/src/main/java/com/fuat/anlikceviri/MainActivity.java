package com.fuat.anlikceviri;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/** Uygulama, assets/index.html dosyasındaki çeviri arayüzünü tam ekran bir WebView içinde açar. */
public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // çeviri geçmişi ve dil seçimi için
        s.setAllowFileAccess(true);
        s.setTextZoom(100);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new Bridge(), "Android");

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    /** Sayfanın "Kopyala" butonu bu köprü üzerinden Android panosuna yazar. */
    private class Bridge {
        @JavascriptInterface
        public void copy(String text) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Çeviri", text));
        }

        /** Google Çeviri isteğini WebView yerine doğrudan gönderir; sonucu window.__gtDone ile sayfaya verir. */
        @JavascriptInterface
        public void post(final int id, final String url, final String body) {
            if (!url.startsWith("https://translate.googleapis.com/")) {
                sendResult(id, false, "izin verilmeyen adres");
                return;
            }
            new Thread(() -> {
                HttpURLConnection c = null;
                try {
                    c = (HttpURLConnection) new URL(url).openConnection();
                    c.setRequestMethod("POST");
                    c.setConnectTimeout(10000);
                    c.setReadTimeout(10000);
                    c.setDoOutput(true);
                    c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    try (OutputStream os = c.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                    int code = c.getResponseCode();
                    InputStream in = code < 400 ? c.getInputStream() : c.getErrorStream();
                    String text = read(in);
                    if (code == 200) sendResult(id, true, text);
                    else sendResult(id, false, "HTTP " + code);
                } catch (Exception e) {
                    sendResult(id, false, e.getClass().getSimpleName() + ": " + e.getMessage());
                } finally {
                    if (c != null) c.disconnect();
                }
            }).start();
        }
    }

    private static String read(InputStream in) throws Exception {
        if (in == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        in.close();
        return out.toString("UTF-8");
    }

    private void sendResult(int id, boolean ok, String body) {
        final String js = "window.__gtDone(" + id + "," + ok + "," + JSONObject.quote(body) + ")";
        runOnUiThread(() -> webView.evaluateJavascript(js, null));
    }
}
