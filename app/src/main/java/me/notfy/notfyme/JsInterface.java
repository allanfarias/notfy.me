package me.notfy.notfyme;

import android.os.Build;
import android.webkit.JavascriptInterface;

import androidx.annotation.RequiresApi;

public class JsInterface {

    private MainActivity mainActivity;
    private ProfileActivity profileActivity;
    private LoginActivity loginActivity;
    private SendActivity sendActivity;
    private RedirectActivity redirectActivity;



    JsInterface(MainActivity actv){
        mainActivity = actv;
    }

    JsInterface(ProfileActivity actv){
        profileActivity = actv;
    }

    JsInterface(LoginActivity actv){
        loginActivity = actv;
    }

    JsInterface(SendActivity actv){
        sendActivity = actv;
    }

    JsInterface(RedirectActivity actv){
        redirectActivity = actv;
    }

    @JavascriptInterface
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void logout(){
        mainActivity.limpaCache();
    }

    @JavascriptInterface
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String obterImei()
    {
        //return mainActivity.obterImei();
        return null;
    }

    @JavascriptInterface
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void minimizaApp(){
        //mainActivity.onBackPressed();
    }

    @JavascriptInterface
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void fechar(){
        mainActivity.finish();
    }

    @JavascriptInterface
    @RequiresApi (api = Build.VERSION_CODES.LOLLIPOP)
    public void alert( String title, String message){
        mainActivity.alert( title, message);
    }

    @JavascriptInterface
    @RequiresApi (api = Build.VERSION_CODES.LOLLIPOP)
    public void reload()
    {
        mainActivity.reload();
    }
}