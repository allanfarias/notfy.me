package me.notfy.notfyme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;


public class MainActivity extends Activity {

    static final String URL_APP = "file:///android_asset/index.html";
    static final String UA_STR_M =
            "Mozilla/5.0 (Linux; Android 8.0.0;) "
                    +"AppleWebKit/537.36 (KHTML, like Gecko) "
                    +"Chrome/80.0.3987.149 Mobile Safari/537.36";

    static final String UA_STR_D =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    +"AppleWebKit/537.36 (KHTML, like Gecko) "
                    +"Chrome/80.0.3987.149 Safari/537.36";

    static final int REQUEST_CODE_LOGIN = 1;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 11;

    private WebView webViewPrincipal;
    private RelativeLayout viewSplash;
    private LinearLayout dialogoErroHttp;
    private Context mContext;
    private CookieManager cookieManager;
    private Boolean appIniciado = false;
    private ErroHttp erroHttp;

    private String postDataIniApp;

    MonitorLocalizacao monitorLocalizacao;

    /* New */

    private String urlToSend;
    private String fcmToken;

    /* New */


    @SuppressLint("AddJavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Define arquivo .xml do Layout - interface gráfica - a ser utilizado por esta Activity */
        setContentView(R.layout.activity_main);

        webViewPrincipal = findViewById(R.id.activity_main);
        viewSplash = findViewById(R.id.splash);
        dialogoErroHttp = findViewById(R.id.dialogo_erro_http);
        WebSettings webSettings = webViewPrincipal.getSettings();
        webSettings.setUserAgentString(UA_STR_M);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        CookieSyncManager.createInstance(this);
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        webViewPrincipal.addJavascriptInterface(
                new JsInterface(this), "InterfaceJS");
        webViewPrincipal.setWebViewClient(new webViewPrincipalClient());
        webViewPrincipal.setWebChromeClient(new WebChromeClient());

        monitorLocalizacao = new MonitorLocalizacao( this );

        mContext = getApplicationContext();

        erroHttp = new ErroHttp();

        webViewPrincipal.loadUrl(URL_APP);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

      FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("MainActivity", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        //String msg = token.toString();


                        String msg = getString(R.string.msg_token_fmt,token);
                        fcmToken = msg;
                        Log.d("MainActivity", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Toast.makeText(this,"onResume",Toast.LENGTH_SHORT).show();

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String url = "";
        List<String> links;
        String uerrieli;
        Matcher m;

        if (Intent.ACTION_SEND.equals(action) && type != null)
        {
            uerrieli = intent.getStringExtra(Intent.EXTRA_TEXT);

            if(uerrieli != null)
            {
                links = new ArrayList<String>();
                m = Patterns.WEB_URL.matcher(uerrieli);

                while (m.find()) {
                    url = m.group();
                }

                Toast.makeText(this,action,Toast.LENGTH_SHORT).show();
                //Toast.makeText(this,type,Toast.LENGTH_LONG).show();
                Toast.makeText(this, Uri.parse(url).toString(),Toast.LENGTH_LONG).show();
                urlToSend = url;
                webViewPrincipal.loadUrl(
                        "javascript:"
                            +"app.sendNotificationUrl('"
                                +url+"','channel','title','description');");
            }

            if ("text/plain".equals(type)) {
                //handleSendText(intent); // Handle text being sentacti
            }
        }

        if(appIniciado)
        {
            if(!monitorLocalizacao.monitoramentoIniciado())
                monitorLocalizacao.iniciaMonitoramento();
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                // If request is cancelled, the result arrays are empty.
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)
                    {
                        /* Permissão foi concedida */
                        monitorLocalizacao.iniciaMonitoramento(true);
                    }
                } else {
                    /* Permissão foi negada */
                    monitorLocalizacao.iniciaMonitoramento(false);
                }
            } // end case
        } // end switch
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Toast.makeText(
                getApplicationContext(),
                "onActivityResult",
                Toast.LENGTH_SHORT).show();
        Log.d("DBG","onActivityResult");


        if(requestCode == REQUEST_CODE_LOGIN && resultCode == RESULT_OK)
        {
            Toast.makeText(
                    getApplicationContext(),
                    "Result OK",
                    Toast.LENGTH_LONG).show();
            Log.d("DBG","Result OK");

            webViewPrincipal.postUrl(URL_APP,postDataIniApp.getBytes());
        }else{
            Toast.makeText(
                    getApplicationContext(),
                    "Result Not OK",
                    Toast.LENGTH_LONG).show();
            Log.d("DBG","Result Not OK");
        }
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

    public void limpaCache(){
        cookieManager.removeAllCookie();
    }

    public void recebeLocalizacao( String jsonLocalizacao ){
        webViewPrincipal.loadUrl("javascript: app.androidRecebeLocalizacao("+jsonLocalizacao+");");
    }

    public void erroHttpTentar(View view)
    {
        dialogoErroHttp.setVisibility(View.GONE);
        erroHttp.houve = false;

        webViewPrincipal.postUrl(URL_APP,postDataIniApp.getBytes());
    }

    public void reload()
    {
        finish();
        startActivity(new Intent(this,MainActivity.class));
    }

    public void erroHttpSair(View view)
    {
        dialogoErroHttp.setVisibility(View.GONE);
        finish();
    }

    private boolean isUserLoggedIn()
    {
        return true;
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

            Toast.makeText(mContext,"onPageFinished "+url,Toast.LENGTH_SHORT).show();

            //webViewPrincipal.loadUrl("javascript:app.printaToken('"+fcmToken+"');");

            if(url.contains("#activitylogin"))
            {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, REQUEST_CODE_LOGIN);
            }

            appIniciado = true;
            String host = Uri.parse(url).getHost();

            if(url.contains("#profile:"))
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));

            if(url.contains("#login"))
                startActivity(new Intent(MainActivity.this, LoginActivity.class));

            if( viewSplash.isShown() && !erroHttp.houve)
            {
                viewSplash.setVisibility(View.GONE);
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