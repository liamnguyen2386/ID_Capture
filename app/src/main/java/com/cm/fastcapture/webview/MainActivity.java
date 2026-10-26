package com.cm.fastcapture.webview;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    // THAY URL NÀY BẰNG ĐỊA CHỈ GITHUB PAGES CỦA BẠN, CÓ / CUỐI.
    private static final String WEB_APP_URL = "https://liamnguyen2386.github.io/CM-fast-capture-web/";
    private static final int CAMERA_REQUEST = 700;
    private WebView webView;
    private PermissionRequest pendingWebPermission;

    private boolean isTrustedUrl(String url) {
        try {
            Uri target = Uri.parse(WEB_APP_URL);
            Uri current = Uri.parse(url);
            return "https".equalsIgnoreCase(current.getScheme())
                && target.getHost() != null && target.getHost().equalsIgnoreCase(current.getHost())
                && current.getPath() != null && current.getPath().startsWith(target.getPath());
        } catch (Exception e) { return false; }
    }

    private boolean isTrustedOrigin(Uri origin) {
        try {
            Uri target = Uri.parse(WEB_APP_URL);
            return "https".equalsIgnoreCase(origin.getScheme())
                && target.getHost() != null && target.getHost().equalsIgnoreCase(origin.getHost());
        } catch (Exception e) { return false; }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (WEB_APP_URL.contains("YOUR_GITHUB_USERNAME")) {
            TextView info = new TextView(this);
            info.setPadding(36, 80, 36, 36);
            info.setText("Chưa cấu hình URL GitHub Pages. Mở MainActivity.java và thay WEB_APP_URL trước khi build APK.");
            setContentView(info);
            return;
        }
        webView = new WebView(this);
        webView.setBackgroundColor(0xFFFFFFFF);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // localStorage, sessionStorage, IndexedDB
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                // Không cho điều hướng sang trang ngoài trong ứng dụng
                return !isTrustedUrl(request.getUrl().toString());
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    if (!isTrustedOrigin(request.getOrigin())) { request.deny(); return; }
                    boolean cameraOnly = false;
                    for (String r : request.getResources()) {
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) cameraOnly = true;
                        else { request.deny(); return; }
                    }
                    if (!cameraOnly) { request.deny(); return; }
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                    } else {
                        pendingWebPermission = request;
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                    }
                });
            }
            @Override public void onPermissionRequestCanceled(PermissionRequest request) {
                if (pendingWebPermission == request) pendingWebPermission = null;
            }
            @Override public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                if (!isTrustedUrl(url)) { result.cancel(); return true; }
                EditText field = new EditText(MainActivity.this);
                field.setSingleLine(true);
                field.setText(defaultValue == null ? "" : defaultValue);
                if (message != null && message.toLowerCase().contains("mật khẩu")) {
                    field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                FrameLayout wrapper = new FrameLayout(MainActivity.this);
                int pad = (int)(20 * getResources().getDisplayMetrics().density);
                wrapper.setPadding(pad, 0, pad, 0);
                wrapper.addView(field);
                new AlertDialog.Builder(MainActivity.this).setTitle("CM Fast Capture")
                    .setMessage(message).setView(wrapper)
                    .setPositiveButton("OK", (d,w) -> result.confirm(field.getText().toString()))
                    .setNegativeButton("Hủy", (d,w) -> result.cancel())
                    .setOnCancelListener(d -> result.cancel()).show();
                return true;
            }
            @Override public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                if (!isTrustedUrl(url)) { result.cancel(); return true; }
                new AlertDialog.Builder(MainActivity.this).setMessage(message)
                    .setPositiveButton("OK", (d,w) -> result.confirm())
                    .setOnCancelListener(d -> result.confirm()).show();
                return true;
            }
        });
        webView.loadUrl(WEB_APP_URL);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST && pendingWebPermission != null) {
            PermissionRequest request = pendingWebPermission;
            pendingWebPermission = null;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
            } else request.deny();
        }
    }
    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (pendingWebPermission != null) { pendingWebPermission.deny(); pendingWebPermission = null; }
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
