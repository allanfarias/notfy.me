package me.notfy.notfyme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;


public class RedirectActivity extends Activity {

    static final String URL_APP = "file:///android_asset/redirect.html";

    private WebView webViewPrincipal;
    private RelativeLayout viewSplash;
    private LinearLayout dialogoErroHttp;
    private Context mContext;
    private RelativeLayout carregando;
    private Boolean appIniciado = false;
    private ErroHttp erroHttp;

    private String postDataIniApp;

    MonitorLocalizacao monitorLocalizacao;

    /* New */

    private String redirectLink;

    /* New */


    @SuppressLint("AddJavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Define arquivo .xml do Layout - interface gráfica - a ser utilizado por esta Activity */
        setContentView(R.layout.activity_redirect);

        webViewPrincipal = findViewById(R.id.activity_main);
        viewSplash = findViewById(R.id.splash);
        dialogoErroHttp = findViewById(R.id.dialogo_erro_http);
        carregando = findViewById(R.id.carregando);
        WebSettings webSettings = webViewPrincipal.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        CookieSyncManager.createInstance(this);
        webViewPrincipal.addJavascriptInterface(
                new JsInterface(this), "InterfaceJS");
        webViewPrincipal.setWebViewClient(new webViewPrincipalClient());
        webViewPrincipal.setWebChromeClient(new WebChromeClient());

        mContext = getApplicationContext();

        erroHttp = new ErroHttp();

        Intent intent = getIntent();
        redirectLink = intent.getStringExtra("link");

        webViewPrincipal.loadUrl(URL_APP);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    /*@Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
    }*/

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if((keyCode == KeyEvent.KEYCODE_BACK)&& webViewPrincipal.canGoBack())
        {
            webViewPrincipal.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed()
    {
        webViewPrincipal.loadUrl("javascript: app.voltar();");
    }

    public void alert( String title, String message){
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

    public void erroHttpTentar(View view)
    {
        dialogoErroHttp.setVisibility(View.GONE);
        erroHttp.houve = false;

        webViewPrincipal.postUrl(URL_APP,postDataIniApp.getBytes());

        //webViewPrincipal.reload();

        //startActivity(new Intent(this,MainActivity.class));
        //finish();
    }

    public void erroHttpSair(View view)
    {
        dialogoErroHttp.setVisibility(View.GONE);
        finish();
    }

    private class ErroHttp
    {
        boolean houve = false;
        int codigo;
        CharSequence mensagem;
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

            String host = Uri.parse(url).getHost();

            carregando.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url)
        {
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

            appIniciado = true;

            if( viewSplash.isShown() && !erroHttp.houve)
            {
                viewSplash.setVisibility(View.GONE);
            }

            if(carregando.isShown()) {
                carregando.setVisibility(View.GONE);
            }

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(redirectLink)));
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

            erroHttp.houve = true;
            erroHttp.codigo = rerr.getErrorCode();
            erroHttp.mensagem = rerr.getDescription().toString();

            dialogoErroHttp.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
        {
            super.onReceivedSslError(view, handler, error);

            Toast.makeText(mContext,"Falha de SSL",Toast.LENGTH_LONG).show();

            try{
                handler.proceed();
            }catch (Exception e){
                Toast.makeText(mContext,e.toString(),Toast.LENGTH_LONG).show();
            }
        }
    }
}