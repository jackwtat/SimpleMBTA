package jackwtat.simplembta.data;

/**
 * Created by jackw on 10/22/2017.
 */

public class ServiceAlert {

    public static final int ADVISORY = 0;
    public static final int URGENT_ALERT = 1;

    private String id;
    private String text;
    private String status;
    private int urgency;

    public ServiceAlert(String id, String text, String status) {
        this.id = id;
        this.text = text;
        this.status = status;

        if (status.toLowerCase().contains("ongoing")) {
            this.urgency = ADVISORY;
        } else {
            this.urgency = URGENT_ALERT;
        }
    }

    public String getId() { return id; }

    public String getText() {
        return text;
    }

    public String getStatus() { return status; }

    public int getUrgency() { return urgency; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceAlert) {
            ServiceAlert anotherAlert = (ServiceAlert) obj;
            return this.id.equals(anotherAlert.getId());
        }

        return false;
    }
}
