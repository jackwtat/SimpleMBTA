package jackwtat.simplembta.mbta.data;

import android.support.annotation.NonNull;

/**
 * Created by jackw on 10/22/2017.
 */

public class ServiceAlert implements Comparable<ServiceAlert> {

    public enum Urgency {
        ALERT,
        ADVISORY
    }

    public enum Severity {
        SEVERE,
        SIGNIFICANT,
        MODERATE,
        MINOR,
        INFORMATION
    }

    private String id;
    private String text;
    private String status;
    private Urgency urgency;
    private Severity severity;

    public ServiceAlert(String id, String text, String status, Severity severity) {
        this.id = id;
        this.text = text;
        this.status = status;
        this.severity = severity;

        if (status.toLowerCase().contains("ongoing")) {
            this.urgency = Urgency.ADVISORY;
        } else {
            this.urgency = Urgency.ALERT;
        }
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getStatus() {
        return status;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public Severity getSeverity() {
        return severity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceAlert) {
            ServiceAlert anotherAlert = (ServiceAlert) obj;
            return this.id.equals(anotherAlert.getId());
        }

        return false;
    }

    @Override
    public int compareTo(@NonNull ServiceAlert serviceAlert) {
        if (this.urgency != serviceAlert.getUrgency()) {
            return this.urgency.compareTo(serviceAlert.getUrgency());
        } else if (this.severity != serviceAlert.getSeverity()) {
            return this.severity.compareTo(serviceAlert.getSeverity());
        } else {
            return this.id.compareTo(serviceAlert.getId());
        }
    }
}
