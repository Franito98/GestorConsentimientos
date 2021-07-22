package com.dam.gestorconsentimientos;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;

@ResourceDef(name = "Patient")
public class Paciente extends Patient {

    public Paciente() {
        super();
    }

    /** * This is a basic extension, with a DataType value (in this case, StringDt) */
    //Atributo extendido Usuario
    @Child(name = "usuario")
    @Description(shortDefinition = "Contiene el nombre de usuario con el que se registró el ciudadano")
    @Extension(url = "localhost:8080/extensión/patient/usuario", isModifier = false, definedLocally = true)
    private StringDt usu;

    /** * This is a basic extension, with a DataType value (in this case, IntegerDt) */
    //Atributo extendido Tarjeta Sanitaria
    @Child(name = "tarjsanitaria")
    @Description(shortDefinition = "Contiene el número de la tarjeta sanitaria del ciudadano")
    @Extension(url = "localhost:8080/extensión/patient/tarjsanitaria", isModifier = false, definedLocally = true)
    private IntegerDt tarjsanitaria;

    public StringDt getUsu() {
        return usu;
    }

    public void setUsu(StringDt usu) {
        this.usu = usu;
    }

    public IntegerDt getTarjsanitaria() {
        return tarjsanitaria;
    }

    public void setTarjsanitaria(IntegerDt tarjsanitaria) {
        this.tarjsanitaria = tarjsanitaria;
    }

}
