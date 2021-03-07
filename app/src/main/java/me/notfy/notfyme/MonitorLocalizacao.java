package me.notfy.notfyme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

//import android.support.v7.app.AlertDialog;

class MonitorLocalizacao {

    private MainActivity activitySolicitante;
    private LocationListener locationListenerGps;
    private LocationListener locationListenerNet;
    private LocationManager locationManagerGps;
    private LocationManager locationManagerNet;
    private Boolean monitoramentoFoiIniciado = false;
    private Boolean providerGpsDisabled = false;
    private Boolean providerNetDisabled = false;

    MonitorLocalizacao(final MainActivity actvSolicitante)
    {
        activitySolicitante = actvSolicitante;
        locationManagerNet =
                (LocationManager) activitySolicitante.getSystemService(Context.LOCATION_SERVICE);
        locationManagerGps =
                (LocationManager) activitySolicitante.getSystemService(Context.LOCATION_SERVICE);

        locationListenerNet = new LocationListener() {
            @Override
            public void onLocationChanged(Location location)
            {
                String latitude, longitude, precisao;
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                precisao = String.valueOf(location.getAccuracy());
                activitySolicitante.recebeLocalizacao(
                        "{'fonte':'rede',"
                                +"'precisao':" + precisao
                                +",'latitude':" + latitude
                                +",'longitude':" + longitude + "}");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {

            }

            @Override
            public void onProviderEnabled(String s)
            {
                providerNetDisabled = false;

                if(!monitoramentoFoiIniciado) iniciaMonitoramento();
            }

            @Override
            public void onProviderDisabled(String s)
            {
                providerNetDisabled = true;

                if(providerGpsDisabled)
                {
                    monitoramentoFoiIniciado = false;

                    new AlertDialog.Builder(activitySolicitante)
                            .setTitle("ATENÇÂO")
                            .setMessage("Para que o aplicativo funcione, a LOCALIZAÇÂO (REDE) do aparelho precisa estar ligado. Ligue o GPS e volte para o aplicativo.")
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int id) {
                                            //activitySolicitante.finish();
                                            activitySolicitante.startActivity(
                                                    new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    }
                            )
                            .setCancelable(false)
                            .create()
                            .show();
                }
            }
        };

        locationListenerGps = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location) {
                String latitude, longitude, precisao;
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                precisao = String.valueOf(location.getAccuracy());
                activitySolicitante.recebeLocalizacao(
                        "{'fonte':'gps',"
                                +"'precisao':" + precisao
                                +",'latitude':" + latitude
                                +",'longitude':" + longitude + "}");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {
            }

            @Override
            public void onProviderEnabled(String s)
            {
                providerGpsDisabled = false;

                if(!monitoramentoFoiIniciado) iniciaMonitoramento();
            }

            @Override
            public void onProviderDisabled(String s)
            {
                monitoramentoFoiIniciado = false;
                providerGpsDisabled = true;

                new AlertDialog.Builder(activitySolicitante)
                        .setTitle("ATENÇÂO")
                        .setMessage("Para que o aplicativo funcione, a LOCALIZAÇÂO (GPS) do aparelho precisa estar ligado. Ligue o GPS e volte para o aplicativo.")
                        .setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        //activitySolicitante.finish();
                                        activitySolicitante.startActivity(
                                                new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    }
                                }
                        )
                        .setCancelable(false)
                        .create()
                        .show();
            }
        };
    }

    Boolean monitoramentoIniciado(){
        return monitoramentoFoiIniciado;
    }

    void iniciaMonitoramento()
    {
        //activitySolicitante.checkLocationPermission();
    }

    void iniciaMonitoramento(Boolean permissaoConcedida)
    {
        if(permissaoConcedida)
        {
            if(locationManagerGps.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || locationManagerNet.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                try {
                    locationManagerGps.requestLocationUpdates(LocationManager.GPS_PROVIDER ,
                            0, 10, locationListenerGps);
                    monitoramentoFoiIniciado = true;
                } catch (SecurityException e){
                    Toast.makeText( activitySolicitante,
                            "Exception: "+e.toString(), Toast.LENGTH_SHORT).show();
                }

                try {
                    locationManagerNet.requestLocationUpdates(LocationManager.NETWORK_PROVIDER ,
                            0, 10, locationListenerNet);
                    monitoramentoFoiIniciado = true;
                } catch (SecurityException e){
                    Toast.makeText( activitySolicitante,
                            "Exception: "+e.toString(), Toast.LENGTH_SHORT).show();
                }
            }else{
                /* Localização não está habilitado */

                /* Tentativa de habilitar */
                try {
                    new AlertDialog.Builder(activitySolicitante)
                            .setTitle("ATENÇÂO")
                            .setMessage("Para que o aplicativo funcione, o GPS do aparelho precisa estar ligado. Ligue o GPS e volte para o aplicativo.")
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int id) {
                                            //activitySolicitante.finish();
                                            //dialog.dismiss();
                                            activitySolicitante.startActivity(
                                                    new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    }
                            )
                            .setCancelable(false)
                            .create()
                            .show();

                }catch (Exception e){
                    /* Caso não consiga habilitar encerra app */
                    new AlertDialog.Builder(activitySolicitante)
                            .setTitle("ATENÇÂO")
                            .setMessage("Para que o aplicativo funcione, o GPS do aparelho precisa estar ligado. Ligue o GPS e volte para o aplicativo.")
                            .setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int id) {
                                            //activitySolicitante.finish();
                                            //dialog.dismiss();
                                            activitySolicitante.startActivity(
                                                    new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    }
                            )
                            .setCancelable(false)
                            .create()
                            .show();
                }
            }
        }else{
            // Não tem permissão
            new AlertDialog.Builder(activitySolicitante)
                    .setTitle("ATENÇÂO")
                    .setMessage("Para que o aplicativo funcione, você precisa permitir acesso ao GPS.")
                    .setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id) {
                                    iniciaMonitoramento();
                                }
                            }
                    )
                    .setCancelable(false)
                    .create()
                    .show();
        }
    }
}
