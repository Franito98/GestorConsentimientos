package com.dam.gestorconsentimientos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private Button login;
    private Button register;
    EditText usuario, password;
    RadioButton botonciud, botonagente;
    Boolean acceso = false;
    String tipo = "ciud";
    String pw = "dni";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);

        usuario = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        botonciud = (RadioButton) findViewById(R.id.buttonciud);
        botonagente = (RadioButton) findViewById(R.id.buttonagente);

        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.buttonciud:
                password.setHint("DNI");
                tipo = "ciud";
                pw = "dni";
                break;

            case R.id.buttonagente:
                password.setHint("Contraseña");
                tipo = "agente";
                pw = "contra";
                break;
        }
    }

    public void peticion_REST(View view) {
        final String URL = "http://192.168.1.54:8080/TFGREST/" + tipo + "/acceder?" + pw + "=" + password.getText().toString();
        final ProgressDialog dlg = ProgressDialog.show(
                this,
                "Consultando datos",
                "Por favor, espere...", true);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dlg.dismiss();
                        try {

                            Integer cod = (Integer) response.get("codigo");

                            if (cod == 400) {
                                Toast.makeText(getApplicationContext(),
                                        "Usuario inexistente", Toast.LENGTH_SHORT).show();
                                acceso = false;
                            }
                            else {
                                if (cod == 300) {
                                    Toast.makeText(getApplicationContext(),
                                            "Usuario no registrado",
                                            Toast.LENGTH_SHORT).show();
                                    acceso = false;
                                } else {
                                    if (cod == 200) {

                                        String usu = (String) response.get("usu");

                                        if (usuario.getText().toString().equals(usu)) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Usuario accede correctamente",
                                                    Toast.LENGTH_SHORT).show();
                                            acceso = true;
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "Usuario y " + password.getHint() + " no coinciden",
                                                    Toast.LENGTH_SHORT).show();
                                            acceso = false;
                                        }
                                    }
                                }
                            }
                            if (acceso) {
                                if (tipo == "ciud") {
                                    acceso = false;
                                    Intent intent = new Intent(LoginActivity.this, AlertasActivity.class);
                                    intent.putExtra("dni", password.getText().toString());
                                    startActivity(intent);

                                } else {
                                    acceso = false;
                                    Intent intent = new Intent(LoginActivity.this, AgenteActivity.class);
                                    intent.putExtra("login", usuario.getText().toString());
                                    startActivity(intent);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dlg.dismiss();
                VolleyLog.e("Error: ", error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // add the request object to the queue to be executed
        Aplicacion.getInstance().getRequestQueue().add(request);
    }
}