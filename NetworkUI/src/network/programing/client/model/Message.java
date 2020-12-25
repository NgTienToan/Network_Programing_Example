package network.programing.client.model;

public class Message {
    private String fromUser;
    private String content;
    private boolean me;

    public Message(String fromUser, String content, boolean me) {
        this.fromUser = fromUser;
        this.content = content;
        this.me = me;
    }

    public String getContent() {
        return content;
    }

    public boolean isMe() {
        return me;
    }
}
