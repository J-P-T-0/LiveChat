package Respuestas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "comando",
        visible = true
)

@JsonSubTypes({
        @JsonSubTypes.Type(value = Aviso.class, name = "AVISO"),
        @JsonSubTypes.Type(value = LoginAuth.class, name = "LOGIN_AUTH"),
        @JsonSubTypes.Type(value = ReturnConversaciones.class, name = "RETURN_CONVERSACIONES"),
        @JsonSubTypes.Type(value = ReturnMensajes.class, name = "RETURN_MENSAJES"),
        @JsonSubTypes.Type(value = ReturnConvID.class, name = "RETURN_CONV_ID"),
        @JsonSubTypes.Type(value = NumsInvalidos.class, name = "NUMS_INVALIDOS"),
        @JsonSubTypes.Type(value = GroupParticipants.class, name = "GROUP_PARTICIPANTS"),
        @JsonSubTypes.Type(value = ReturnEstadoMensaje.class, name = "RETURN_ESTADO_MENSAJE"),
        @JsonSubTypes.Type(value = CloseClient.class, name = "CLOSE_CLIENT")
})

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Respuesta {
    private String comando;

    public Respuesta(@JsonProperty("comando") String comando) {
        this.comando = comando;
    }

    public String getComando() {
        return comando;
    }
}
