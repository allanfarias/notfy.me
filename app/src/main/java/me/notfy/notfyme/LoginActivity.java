package me.notfy.notfyme;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


public class LoginActivity extends Activity {

    static final String URL_APP = "https://notfy.me/app/login/";
    static final String UA_STR_M =
            "Mozilla/5.0 (Linux; Android 8.0.0;) "
            +"AppleWebKit/537.36 (KHTML, like Gecko) "
            +"Chrome/80.0.3987.149 Mobile Safari/537.36";
    static final String UA_STR_D =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            +"AppleWebKit/537.36 (KHTML, like Gecko) "
            +"Chrome/80.0.3987.149 Safari/537.36";

    private Toolbar barraTopo;
    private WebView webViewLogin;
    private Context mContext;
    private CookieManager cookieManager;
    private RelativeLayout carregando;

    private String postDataIniApp;

    @SuppressLint("AddJavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Define arquivo .xml do Layout - interface gráfica - a ser utilizado por esta Activity */
        setContentView(R.layout.activity_login);

        webViewLogin = findViewById(R.id.login);
        carregando = findViewById(R.id.carregando);
        WebSettings webSettings = webViewLogin.getSettings();
        webSettings.setUserAgentString(UA_STR_M);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        CookieSyncManager.createInstance(this);
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        webViewLogin.addJavascriptInterface(
                new JsInterface(this), "JsInterface");
        webViewLogin.setWebViewClient(new webViewPrincipalClient());
        webViewLogin.setWebChromeClient(new WebChromeClient());

        mContext = getApplicationContext();


        webViewLogin.loadUrl(URL_APP);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if((keyCode == KeyEvent.KEYCODE_BACK)&& webViewLogin.canGoBack())
        {
            webViewLogin.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed()
    {
        if(webViewLogin.canGoBack())
        {
            webViewLogin.loadUrl("javascript: app.voltar();");
        }
        else
        {
            super.onBackPressed();
        }
    }

    public void alert( String title, String message)
    {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }
                )
                .create()
                .show();
    }

    public void limpaCache(){
        cookieManager.removeAllCookie();
    }

    /**
     * Cliente HTTP da WebView
     */
    private class webViewPrincipalClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);

            carregando.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url)
        {
            String host = Uri.parse(url).getHost();

            if(url.contains("instagram.com"))
            {
                webViewLogin.getSettings().setUserAgentString(UA_STR_D);
            }else{
                webViewLogin.getSettings().setUserAgentString(UA_STR_M);
            }

            if(url.startsWith("http:") || url.startsWith("https:"))
            {
                //if(host.contains("motostart.com.br") && url.contains("motostart.com.br/appdev1/cms"))

                if(carregando.isShown())
                    carregando.setVisibility(View.VISIBLE);

                return false;

                // Quando a URL não é pra ser aberta dentro do Aplicativo
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //startActivity(intent);
                //return true;
            }

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

            String host = Uri.parse(url).getHost();

            if(url.contains("/login-passageiro-sucesso"))
            {
                //Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                //startActivity(intent);
                setResult(RESULT_OK);
                finish();
            }

            if(carregando.isShown()) {
                carregando.setVisibility(View.GONE);
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl)
        {
            //Toast.makeText(mContext,"onReceivedError",Toast.LENGTH_SHORT).show();
        }

        @TargetApi(android.os.Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr)
        {
            // Redirect to deprecated method, so you can use it in all SDK versions
            onReceivedError(view, rerr.getErrorCode(),
                    rerr.getDescription().toString(), req.getUrl().toString());
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
        {
            super.onReceivedSslError(view, handler, error);

            Toast.makeText(LoginActivity.this,"Falha de SSL",Toast.LENGTH_LONG).show();

            try{
                handler.proceed();
            }catch (Exception e){
                Toast.makeText(LoginActivity.this,e.toString(),Toast.LENGTH_LONG).show();
            }
        }
    }
}