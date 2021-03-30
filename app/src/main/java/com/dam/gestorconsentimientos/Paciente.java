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

    /** * This is a basic extension, with a DataType value (in this case, IntegerDt) */
    /*
     * Usaremos un código para indicar si ha habido algún problema en la obtención
     * del ciudadano:
     *  -200 --> indica que se ha obtenido correctamente
     *  -300 --> indica que el usuario con el DNI aportado no está registrado
     *  -400 --> indica que el DNI con el que se quiere acceder no existe
     */
    //Atributo extendido Código del mensaje
    @Child(name = "codmensaje")
    @Description(shortDefinition = "Contiene un código sobre la petición del cliente")
    @Extension(url = "localhost:8080/extensión/patient/codmensaje", isModifier = true, definedLocally = true)
    private IntegerDt codmensaje;

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

    public IntegerDt getCodmensaje() {
        return codmensaje;
    }

    public void setCodmensaje(IntegerDt codmensaje) {
        this.codmensaje = codmensaje;
    }
}
