package jackwtat.simplembta.data;

import android.support.annotation.NonNull;

/**
 * Created by jackw on 10/22/2017.
 */

public class ServiceAlert implements Comparable<ServiceAlert> {

    public static final int URGENT = 0;
    public static final int ADVISORY = 1;

    private static final String[] SEVERITIES =
            {"Severe", "Significant", "Moderate", "Minor", "Information"};

    private String id;
    private String text;
    private String status;
    private int urgency;
    private int severity;

    public ServiceAlert(String id, String text, String status, String severity) {
        this.id = id;
        this.text = text;
        this.status = status;

        if (status.toLowerCase().contains("ongoing")) {
            this.urgency = ADVISORY;
        } else {
            this.urgency = URGENT;
        }

        for (int i = 0; i < SEVERITIES.length; i++) {
            if (severity.equals(SEVERITIES[i])) {
                this.severity = i;
            }
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

    public int getUrgency() {
        return urgency;
    }

    public int getSeverity() {
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
            return Integer.compare(this.urgency, serviceAlert.getUrgency());
        } else if (this.severity != serviceAlert.getSeverity()) {
            return Integer.compare(this.severity, serviceAlert.getSeverity());
        } else {
            return this.id.compareTo(serviceAlert.getId());
        }
    }
}
