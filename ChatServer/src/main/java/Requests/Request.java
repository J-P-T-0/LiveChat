package Requests;

import com.fasterxml.jackson.annotation.JsonCreator;
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
        @JsonSubTypes.Type(value = Login.class, name = "LOGIN"),
        @JsonSubTypes.Type(value = Registrarse.class, name = "REGISTRARSE"),
        @JsonSubTypes.Type(value = GetConversaciones.class, name = "GET_CONVERSACIONES"),
        @JsonSubTypes.Type(value = GetMensajes.class, name = "GET_MENSAJES"),
        @JsonSubTypes.Type(value = EnviarMensaje.class, name = "ENVIAR_MENSAJE"),
        @JsonSubTypes.Type(value = CrearConversacionIndividual.class, name = "CREAR_CONV_PRIV"),
        @JsonSubTypes.Type(value = CrearGrupo.class, name = "CREAR_GRUPO"),
        @JsonSubTypes.Type(value = GetEstadoMensaje.class, name = "GET_ESTADO_MENSAJE"),
        @JsonSubTypes.Type(value = MarcarLeido.class, name = "MARCAR_LEIDO"),
        @JsonSubTypes.Type(value = Close.class, name = "CLOSE_CONNECTION"),
        @JsonSubTypes.Type(value = Reconnect.class, name = "RECONECTAR")
})

public abstract class Request {
    protected String comando;

    @JsonCreator
    public Request(@JsonProperty("comando") String comando) {
        this.comando = comando;
    }

    public String getComando() {
        return comando;
    }
}

