package jackwtat.simplembta.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jackw on 12/21/2017.
 */

public class ServiceAlert implements Comparable<ServiceAlert> {

    public enum Lifecycle {
        NEW(0),
        ONGOING(1),
        UPCOMING(2),
        ONGOING_UPCOMING(3),
        UNKNOWN(4);

        private int stage;

        Lifecycle(int stage) {
            this.stage = stage;
        }

        public int getStage() {
            return stage;
        }
    }

    private String id;
    private String header = "";
    private String effect = "";
    private int severity = 0;
    private Lifecycle lifecycle = Lifecycle.UNKNOWN;
    private boolean[] affectedModes = {false, false, false, false, false};
    private ArrayList<String> affectedRoutes = new ArrayList<>();
    private ArrayList<ActivePeriod> activePeriods = new ArrayList<>();

    public ServiceAlert(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getHeader() {
        return header;
    }

    public String getEffect() {
        return effect;
    }

    public int getSeverity() {
        return severity;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public void setLifecycle(String lifecycle) {
        if (lifecycle.equals("NEW")) {
            this.lifecycle = Lifecycle.NEW;
        } else if (lifecycle.equals("ONGOING")) {
            this.lifecycle = Lifecycle.ONGOING;
        } else if (lifecycle.equals("UPCOMING")) {
            this.lifecycle = Lifecycle.UPCOMING;
        } else if (lifecycle.equals("ONGOING_UPCOMING")) {
            this.lifecycle = Lifecycle.ONGOING_UPCOMING;
        } else {
            this.lifecycle = Lifecycle.UNKNOWN;
        }
    }

    public ArrayList<String> getAffectedRoutes() {
        return affectedRoutes;
    }

    public void addAffectedRoute(String routeId) {
        affectedRoutes.add(routeId);
    }

    public void addAffectedMode(int mode) {
        affectedModes[mode] = true;
    }

    public boolean isAffectedMode(int mode) {
        try {
            return affectedModes[mode];
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public void addActivePeriod(Date startTime, Date endTime) {
        activePeriods.add(new ActivePeriod(startTime, endTime));
    }

    public boolean isActive() {
        Date currentTime = new Date();

        for (ActivePeriod ap : activePeriods) {
            if (currentTime.compareTo(ap.startTime) >= 0 &&
                    (ap.endTime == null || currentTime.compareTo(ap.endTime) <= 0)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int compareTo(@NonNull ServiceAlert serviceAlert) {
        if (this.isActive() && !serviceAlert.isActive()) {
            return -1;
        } else if (!this.isActive() && serviceAlert.isActive()) {
            return 1;
        } else if (this.lifecycle.getStage() != serviceAlert.getLifecycle().getStage()) {
            return this.lifecycle.getStage() - serviceAlert.getLifecycle().getStage();
        } else if (this.severity != serviceAlert.getSeverity()) {
            return serviceAlert.getSeverity() - this.severity;
        } else {
            return serviceAlert.getId().compareTo(this.id);
        }
    }

    private class ActivePeriod {
        private Date startTime;
        private Date endTime;

        ActivePeriod(Date startTime, Date endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
