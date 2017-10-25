package jackwtat.simplembta.data;

/**
 * Created by jackw on 10/22/2017.
 */

public class Alert {
    private String id;
    private String text;

    public Alert(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() { return id; }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Alert) {
            Alert anotherAlert = (Alert) obj;
            return this.id.equals(anotherAlert.getId());
        }

        return false;
    }
}
