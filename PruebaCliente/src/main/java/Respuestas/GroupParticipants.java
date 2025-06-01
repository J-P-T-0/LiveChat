package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupParticipants extends Respuesta{
    private Integer groupID;
    private String groupMembers;

    public GroupParticipants(@JsonProperty("groupID") Integer groupID,
                             @JsonProperty("groupMembers") String groupMembers) {
        super("GROUP_PARTICIPANTS");
        this.groupID = groupID;
        this.groupMembers = groupMembers;
    }

    public Integer getGroupID() {
        return groupID;
    }

    public String getGroupMembers() {
        return groupMembers;
    }
}
