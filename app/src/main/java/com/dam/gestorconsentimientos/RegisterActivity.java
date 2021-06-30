package com.dam.gestorconsentimientos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.UriType;
import org.json.JSONException;
import org.json.JSONObject;

import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;

import static com.dam.gestorconsentimientos.Aplicacion.ctx;

public class RegisterActivity extends AppCompatActivity {

    private Button atras;
    EditText nombre, user, password, dni, tarjsanitaria, tlf, hospital, departamento, codigo;
    LinearLayout layoutdni, layouttarj, layouttlf, layouthosp, layoutdepart, layoutcod;

    Paciente paciente = new Paciente();
    Practicante practicante = new Practicante();

    Boolean acceso = false;
    String tipo = "ciud";
    JSONObject code;
    JSONObject objeto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nombre = (EditText) findViewById(R.id.nombre);
        user = (EditText) findViewById(R.id.usuario);
        password = (EditText) findViewById(R.id.password);
        dni = (EditText) findViewById(R.id.dni);
        tarjsanitaria = (EditText) findViewById(R.id.tarjsanitaria);
        tlf = (EditText) findViewById(R.id.telefono);
        hospital = (EditText) findViewById(R.id.hospital);
        departamento = (EditText) findViewById(R.id.departamento);
        codigo = (EditText) findViewById(R.id.codigo);

        layoutdni = (LinearLayout) findViewById(R.id.layoutdni);
        layouttarj = (LinearLayout) findViewById(R.id.layouttarjsanitaria);
        layouttlf = (LinearLayout) findViewById(R.id.layouttelefono);
        layouthosp = (LinearLayout) findViewById(R.id.layouthospital);
        layoutdepart = (LinearLayout) findViewById(R.id.layoutdepart);
        layoutcod = (LinearLayout) findViewById(R.id.layoutcod);

        atras = (Button) findViewById(R.id.atras);

        atras.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    public void register(View v) {
        final String URL = "http://192.168.1.108:8080/TFGREST/" + tipo + "/reg/" + password.getText().toString();
        final ProgressDialog dlg = ProgressDialog.show(
                this,
                "Actualizando datos",
                "Por favor, espere...", true);

        if (tipo == "ciud") {

            HumanName name = new HumanName();
            name.setText(nombre.getText().toString());
            paciente.addName(name);

            StringDt usu = new StringDt();
            usu.setValueAsString(user.getText().toString());
            paciente.setUsu(usu);

            Identifier id = new Identifier();
            id.setId(password.getText().toString());
            id.setSystemElement(new UriType("http://localhost:8080/TFGREST/ciud/" + password.getText().toString()));
            paciente.addIdentifier(id);

            IntegerDt tarj = new IntegerDt();
            tarj.setValue(Integer.parseInt(tarjsanitaria.getText().toString()));
            paciente.setTarjsanitaria(tarj);

            ContactPoint com = new ContactPoint();
            com.setSystem(ContactPoint.ContactPointSystem.PHONE);
            com.setValue(tlf.getText().toString());
            com.setUse(ContactPoint.ContactPointUse.MOBILE);
            paciente.addTelecom(com);

            try{
                objeto = new JSONObject(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(paciente));
            } catch (JSONException exc){
                Toast.makeText(getApplicationContext(),"Error en objeto JSON de Paciente", Toast.LENGTH_SHORT).show();
            }

        }
        else{

            Identifier id = new Identifier();
            id.setId(dni.getText().toString());
            id.setSystemElement(new UriType("http://localhost:8080/TFGREST/agente/" + dni.getText().toString()));
            practicante.addIdentifier(id);

            HumanName name = new HumanName();
            name.setText(nombre.getText().toString());
            practicante.addName(name);

            StringDt contrasena = new StringDt();
            contrasena.setValueAsString(password.getText().toString());
            practicante.setContra(contrasena);

            StringDt usu = new StringDt();
            usu.setValueAsString(user.getText().toString());
            practicante.setUsu(usu);

            StringDt hosp = new StringDt();
            hosp.setValueAsString(hospital.getText().toString());
            practicante.setHospital(hosp);

            StringDt depart = new StringDt();
            depart.setValueAsString(departamento.getText().toString());
            practicante.setDepart(depart);

            StringDt cod = new StringDt();
            cod.setValueAsString(codigo.getText().toString());
            practicante.setCodigo(cod);

            try{
                objeto = new JSONObject(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(practicante));
            } catch (JSONException exc){
                Toast.makeText(getApplicationContext(),"Error en objeto JSON de Practicante", Toast.LENGTH_SHORT).show();
            }
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, objeto,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dlg.dismiss();
                try {

                    code = response.getJSONObject("codigo");
                    Integer cod = code.getInt("valueInteger");

                    if (cod == 400) {
                        Toast.makeText(getApplicationContext(),
                                "Persona inexistente", Toast.LENGTH_SHORT).show();
                    } else {
                        if (cod == 200) {
                            Toast.makeText(getApplicationContext(),
                                    "Usuario ya registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            if (cod == 500){
                                if (tipo == "ciud"){
                                    Toast.makeText(getApplicationContext(),
                                            "Número de tarjeta sanitaria incorrecto", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Código de agente incorrecto", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if(cod == 600){
                                    Toast.makeText(getApplicationContext(),
                                            "Hospital o Departamento incorrectos para contraseña y código",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    if (cod == 300) {
                                        Toast.makeText(getApplicationContext(),
                                                "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                                        acceso = true;
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "Error al registrarse", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                    if (acceso) {
                        if (tipo == "ciud") {
                            acceso = false;
                            Intent intent = new Intent(RegisterActivity.this, CiudActivity.class);
                            intent.putExtra("dni", password.getText().toString());
                            startActivity(intent);

                        } else {
                            acceso = false;
                            Intent intent = new Intent(RegisterActivity.this, AgenteActivity.class);
                            intent.putExtra("login", user.getText().toString());
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
                Toast.makeText(getApplicationContext(), "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Aplicacion.getInstance().getRequestQueue().add(request);
    }

    public void onClick (View view) {

        switch (view.getId()) {

            case R.id.buttonciud:
                password.setHint("DNI");
                layouttarj.setVisibility(View.VISIBLE);
                layouttlf.setVisibility(View.VISIBLE);
                layoutdni.setVisibility(View.GONE);
                layouthosp.setVisibility(View.GONE);
                layoutdepart.setVisibility(View.GONE);
                layoutcod.setVisibility(View.GONE);
                tipo = "ciud";
                break;

            case R.id.buttonagente:
                password.setHint("Contraseña");
                layouttarj.setVisibility(View.GONE);
                layouttlf.setVisibility(View.GONE);
                layoutdni.setVisibility(View.VISIBLE);
                layouthosp.setVisibility(View.VISIBLE);
                layoutdepart.setVisibility(View.VISIBLE);
                layoutcod.setVisibility(View.VISIBLE);
                tipo = "agente";
                break;
        }
    }
}