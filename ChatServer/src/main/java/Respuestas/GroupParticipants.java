package Respuestas;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class GroupParticipants extends Respuesta{
    private Integer groupID;
    private String groupName;
    private ArrayList<String> groupMembers;

    public GroupParticipants(@JsonProperty("groupID") Integer groupID,
                             @JsonProperty("groupMembers") ArrayList<String> groupMembers,
                             @JsonProperty("groupName") String groupName) {
        super("GROUP_PARTICIPANTS");
        this.groupID = groupID;
        this.groupMembers = groupMembers;
        this.groupName = groupName;
    }

    public Integer getGroupID() {
        return groupID;
    }

    public ArrayList<String> getGroupMembers() {
        return groupMembers;
    }

    public String getGroupName() {
        return groupName;
    }
}
