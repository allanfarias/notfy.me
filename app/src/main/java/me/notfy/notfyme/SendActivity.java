package me.notfy.notfyme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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


public class SendActivity extends Activity {

    //static final String URL_APP = "https://notfy.me/app/home/";
    static final String URL_APP = "file:///android_asset/share.html";

    static final int REQUEST_CODE_LOGIN = 1;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 11;


    /* Objeto para persistência de dados. Funciona como um "banco de dados" simplificado. */
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private Toolbar barraTopo;
    private WebView webViewPrincipal;
    private WebView webViewLogin;
    private RelativeLayout viewSplash;
    private LinearLayout dialogoErroHttp;
    private LinearLayout telaLogin;
    private Context mContext;
    private CookieManager cookieManager;
    private RelativeLayout carregando;
    private Boolean appIniciado = false;
    private ErroHttp erroHttp;

    private String postDataIniApp;

    MonitorLocalizacao monitorLocalizacao;

    /* New */

    private String shareLink;

    /* New */


    @SuppressLint("AddJavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Define arquivo .xml do Layout - interface gráfica - a ser utilizado por esta Activity */
        setContentView(R.layout.activity_main);

        /* Instancia o objeto SharedPreference criando um "arquivo de configurações",
         * com o nome fornecido no parâmetro "name". Este arquivo funcionará commo uma
         * espécie de "banco de dados". */
        sharedPreferences = getSharedPreferences("motostart", 0);

        /* Instancia o objeto que permite modificar os dados do SharedPreference */
        sharedPreferencesEditor = sharedPreferences.edit();

        /* Insere o dado */
        //sharedPreferencesEditor.putString("url_svc", URL_SVC);

        /* Salva os dados */
        sharedPreferencesEditor.apply();

        //sharedPreferencesEditor.putInt("idcidadao",0);
        //sharedPreferencesEditor.apply();

        int idCidadao = sharedPreferences.getInt("idcidadao", 0);

        barraTopo = findViewById(R.id.barratopo);
        webViewPrincipal = findViewById(R.id.activity_main);
        webViewLogin = findViewById(R.id.login);
        telaLogin = findViewById(R.id.telalogin);
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
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        webViewPrincipal.addJavascriptInterface(
                new JsInterface(this), "InterfaceJS");
        webViewPrincipal.setWebViewClient(new webViewPrincipalClient());
        webViewPrincipal.setWebChromeClient(new WebChromeClient());

        mContext = getApplicationContext();

        erroHttp = new ErroHttp();

        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType()))
        {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if(sharedText != null)
                    shareLink = sharedText;
        }

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

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
    }

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

        //webViewPrincipal.reload();

        //startActivity(new Intent(this,MainActivity.class));
        //finish();
    }

    public void reload()
    {
        finish();
        startActivity(new Intent(this, SendActivity.class));
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

            String host = Uri.parse(url).getHost();

            carregando.setVisibility(View.VISIBLE);

            if(!host.contains("motostart.com.br"))
            {
                barraTopo.setVisibility(View.VISIBLE);
            }else{
                barraTopo.setVisibility(View.GONE);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url)
        {
            String host = Uri.parse(url).getHost();

            if(url.contains("#activitylogin"))
            {
                Intent intent = new Intent(SendActivity.this, LoginActivity.class);
                startActivityForResult(intent, REQUEST_CODE_LOGIN);
                return true;
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

            appIniciado = true;

            if( viewSplash.isShown() && !erroHttp.houve)
            {
                viewSplash.setVisibility(View.GONE);
            }

            if(carregando.isShown()) {
                carregando.setVisibility(View.GONE);
            }

            webViewPrincipal.loadUrl("javascript:app.pasteLink('"+shareLink+"');");
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