import java.util.Hashtable;

class GroupChat<UserThread> {
    public String groupName;
    public Hashtable<String, UserThread> userMember = new Hashtable<>();
    public Hashtable<String, UserThread> adminMember = new Hashtable<>();
    public boolean adminAccept = true;
    public GroupChat(String groupName) {
        this.groupName = groupName;
    }
}
