package com.roque.meza.navigationdrawerloginmysql;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.TextInputEditText;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.roque.meza.navigationdrawerloginmysql.Utils.UserParcelable;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maycol Meza on 14/11/2017.
 */

public class LoginActivity extends AppCompatActivity {
    private Button acceder;
    private TextView registrar;
    private TextInputEditText email;
    private TextInputEditText password;
    private ProgressDialog progreso;
    private RequestQueue requestQueue;
    StringRequest stringRequest;

    // Key o ID de las preferences, por lo general se coloca el nombre del paquete
    private static final String preferecesKey = "navigationdrawerloginmysql";
    // Nombre de la preference
    private static final String preferecesSession = "sessionUser";
    private static final String preferecesID = "sessionID";
    private static final String preferecesEmail = "sessionEmail";
    private static final String preferecesNombre = "sessionNombre";
    private static final String preferecesImg = "sessionImg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Manejo toda la logica de verificar si hay sesion iniciada
        if (getSessionPreference()){
            SharedPreferences sessionUser = getSharedPreferences(preferecesKey, MODE_PRIVATE);
            Integer sessionID = sessionUser.getInt(preferecesID, 0);
            String sessionEmail = sessionUser.getString(preferecesEmail, "");
            String sessionNombre = sessionUser.getString(preferecesNombre, "");
            String sessionImg = sessionUser.getString(preferecesImg, "");

            UserParcelable userParcelable = new UserParcelable();
            userParcelable.setId(sessionID);
            userParcelable.setEmail(sessionEmail);
            userParcelable.setNombre(sessionNombre);
            userParcelable.setImage(sessionImg);

            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.putExtra("DATA_USER",userParcelable);
            startActivity(intent);
            finish();
        }

        email = (TextInputEditText)findViewById(R.id.etusuario);
        password = (TextInputEditText)findViewById(R.id.etpass);
        acceder = (Button)findViewById(R.id.btn_acceder);
        registrar = (TextView)findViewById(R.id.signup);
        requestQueue = Volley.newRequestQueue(this);

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getApplicationContext(),SignupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
            }
        });

        acceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciar();
            }
        });
    }

    // Variables para guardar las SharedPreferences
    Integer userParceID;
    String userParceEmail = "";
    String userParceNombre = "";
    String userParceImg = "";

    private void iniciar() {

        if (!validar()) return;

        progreso = new ProgressDialog(this);
        progreso.setMessage("Iniciando...");
        progreso.show();
        String url = "https://www.simcrs.org.sv/ochoa/login/login_movil.php?";

        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UserParcelable userParcelable = new UserParcelable();
                Log.i("RESPUESTA JSON: ",""+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.names().get(0).equals("success")){
                        email.setText("");
                        password.setText("");

                        userParcelable.setId(jsonObject.getJSONArray("usuario").getJSONObject(0).getInt("iduser_"));
                        userParcelable.setEmail(jsonObject.getJSONArray("usuario").getJSONObject(0).getString("email"));
                        userParcelable.setNombre(jsonObject.getJSONArray("usuario").getJSONObject(0).getString("nombres"));
                        userParcelable.setImage(jsonObject.getJSONArray("usuario").getJSONObject(0).getString("photo"));

                        // Seteo mis variables
                        userParceID = userParcelable.getId();
                        userParceEmail = userParcelable.getEmail();
                        userParceNombre = userParcelable.getNombre();
                        userParceImg = userParcelable.getImage();

                        // Guardo las preferencias
                        saveSessionPreference();

                        Toast.makeText(getApplicationContext(),jsonObject.getString("success"),Toast.LENGTH_SHORT).show();
                        progreso.dismiss();

                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.putExtra("DATA_USER",userParcelable);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),jsonObject.getString("error"),Toast.LENGTH_SHORT).show();
                        Log.i("RESPUESTA JSON: ",""+jsonObject.getString("error"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progreso.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"No se ha podido conectar",Toast.LENGTH_SHORT).show();
                progreso.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError { //para enviar los datos mediante POST
                String sEmail = email.getText().toString();
                String sPassword =  password.getText().toString();

                Map<String,String> parametros = new HashMap<>();
                parametros.put("email",sEmail);
                parametros.put("password",sPassword);
                //estos parametros son enviados a nuestro web service

                return parametros;
            }
        };

        requestQueue.add(stringRequest);

    }

    private boolean validar() {
        boolean valid = true;

        String sEmail = email.getText().toString();
        String sPassword = password.getText().toString();

        if (sEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
            email.setError("Introduzca una dirección de correo electrónico válida");
            valid = false;
        } else {
            email.setError(null);
        }

        if (sPassword.isEmpty() || password.length() < 4 || password.length() > 10) {
            password.setError("Entre 4 y 10 caracteres alfanuméricos");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

    // Guardaremos las SharedPreferences
    public void saveSessionPreference(){
        SharedPreferences sessionUser  = getSharedPreferences(preferecesKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = sessionUser.edit();
        // Guardaremos true para definir que en edecto hay una sesion
        editor.putBoolean(preferecesSession, true);
        editor.putInt(preferecesID, userParceID);
        editor.putString(preferecesEmail, userParceEmail);
        editor.putString(preferecesNombre, userParceNombre);
        editor.putString(preferecesImg, userParceImg);
        editor.commit();
    }

    // Recuperaremos el estado, pero por defecto al inicio sera false
    public boolean getSessionPreference(){
        SharedPreferences sessionUser = getSharedPreferences(preferecesKey,MODE_PRIVATE);
        return sessionUser.getBoolean(preferecesSession, false);
    }
}
