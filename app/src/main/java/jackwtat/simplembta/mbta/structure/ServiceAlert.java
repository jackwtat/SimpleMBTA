package jackwtat.simplembta.mbta.structure;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jackw on 12/21/2017.
 */

public class ServiceAlert implements Comparable<ServiceAlert> {

    public enum Lifecycle {NEW, ONGOING, UPCOMING, ONGOING_UPCOMING, UNKNOWN}

    private String id = "";
    private String header = "";
    private String effect = "";
    private int severity = 0;
    private int lifecycleStage = -1;
    private Lifecycle lifecycle = Lifecycle.UNKNOWN;
    private ArrayList<String> affectedRoutes = new ArrayList<>();
    private ArrayList<Mode> blanketModes = new ArrayList<>();
    private ArrayList<ActivePeriod> activePeriods = new ArrayList<>();

    public ServiceAlert(String id, String header, String effect, int severity, String lifecycle) {
        this.id = id;
        this.header = header;
        this.effect = effect;
        this.severity = severity;

        if (lifecycle.toUpperCase().equals("NEW")) {
            this.lifecycle = Lifecycle.NEW;
            this.lifecycleStage = 0;
        } else if (lifecycle.toUpperCase().equals("ONGOING")) {
            this.lifecycleStage = 1;
            this.lifecycle = Lifecycle.ONGOING;
        } else if (lifecycle.toUpperCase().equals("UPCOMING")) {
            this.lifecycleStage = 2;
            this.lifecycle = Lifecycle.UPCOMING;
        } else if (lifecycle.toUpperCase().equals("ONGOING_UPCOMING")) {
            this.lifecycleStage = 3;
            this.lifecycle = Lifecycle.ONGOING_UPCOMING;
        } else {
            this.lifecycleStage = 4;
            this.lifecycle = Lifecycle.UNKNOWN;
        }
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

    public ArrayList<String> getAffectedRoutes() {
        return affectedRoutes;
    }

    public void addAffectedRoute(String routeId) {
        affectedRoutes.add(routeId);
    }

    public ArrayList<Mode> getBlanketModes() {
        return blanketModes;
    }

    public void addBlanketMode(Mode mode) {
        blanketModes.add(mode);
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
        } else if (this.lifecycleStage != serviceAlert.lifecycleStage){
            return Integer.compare(this.lifecycleStage, serviceAlert.lifecycleStage);
        } else if (this.severity != serviceAlert.getSeverity()) {
            return Integer.compare(serviceAlert.getSeverity(), this.severity);
        } else {
            return this.id.compareTo(serviceAlert.getId());
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
