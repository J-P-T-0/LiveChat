package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class NumsInvalidos extends Respuesta {
    private ArrayList<String> regexTelInvalid;

    public NumsInvalidos(@JsonProperty("telsInvalidos") ArrayList<String> texto) {
        super("NUMS_INVALIDOS");
        this.regexTelInvalid = texto;
    }

    public ArrayList<String> getRegexTelInvalid() {
        return regexTelInvalid;
    }
}
