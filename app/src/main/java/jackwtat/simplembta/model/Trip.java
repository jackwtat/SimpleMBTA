package jackwtat.simplembta.model;

import jackwtat.simplembta.model.routes.Route;

public class Trip {
    private String id;
    private int direction = Direction.NULL_DIRECTION;
    private String destination = "null";
    private String name = "null";
    private Route route;
    private Vehicle vehicle;
    private Shape shape;

    public Trip(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public int getDirection() {
        return direction;
    }

    public String getDestination() {
        return destination;
    }

    public String getName() {
        return name;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Shape getShape() {
        return shape;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
}
