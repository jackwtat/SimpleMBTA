package jackwtat.simplembta.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jackw on 12/21/2017.
 */

public class ServiceAlert implements Comparable<ServiceAlert>, Serializable {

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
    private String description = "";
    private String url = "";
    private String effect = "";
    private int severity = 0;
    private Lifecycle lifecycle = Lifecycle.UNKNOWN;
    private boolean[] affectedModes = {false, false, false, false, false};
    private ArrayList<String> affectedRoutes = new ArrayList<>();
    private ArrayList<String> affectedStops = new ArrayList<>();
    private ArrayList<String> affectedFacilities = new ArrayList<>();
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

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getEffect() {
        return effect;
    }

    public int getSeverity() {
        return severity;
    }

    public List<String> getAffectedRoutes() {
        return affectedRoutes;
    }

    public List<String> getAffectedStops() {
        return affectedStops;
    }

    public List<String> getAffectedFacilities() {
        return affectedFacilities;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public boolean affectsRoute(String routeId) {
        return affectedRoutes.contains(routeId);
    }

    public boolean affectsStop(String stopId) {
        return affectedStops.contains(stopId);
    }

    public boolean affectsFacility(String facilityId) {
        return affectedFacilities.contains(facilityId);
    }

    public boolean affectsMode(int mode) {
        return affectedModes[mode];
    }

    public void addAffectedRoute(String routeId) {
        affectedRoutes.add(routeId);
    }

    public void addAffectedStop(String stopId) {
        affectedStops.add(stopId);
    }

    public void addAffectedFacility(String facilityId) {
        affectedFacilities.add(facilityId);
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

    public boolean isUrgent() {
        return isActive() &&
                (lifecycle == ServiceAlert.Lifecycle.NEW ||
                        lifecycle == ServiceAlert.Lifecycle.UNKNOWN ||
                        effect.equalsIgnoreCase("ELEVATOR_CLOSURE"));
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceAlert) {
            ServiceAlert otherServiceAlert = (ServiceAlert) obj;
            return id.equals(otherServiceAlert.id);
        } else {
            return false;
        }
    }

    private class ActivePeriod implements Serializable {
        private Date startTime;
        private Date endTime;

        ActivePeriod(Date startTime, Date endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
