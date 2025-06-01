package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NumsInvalidos extends Respuesta {
    private String regexTelInvalid;

    public NumsInvalidos(@JsonProperty("telsInvalidos") String texto) {
        super("NUMS_INVALIDOS");
        this.regexTelInvalid = texto;
    }

    public String getRegexTelInvalid() {
        return regexTelInvalid;
    }
}
