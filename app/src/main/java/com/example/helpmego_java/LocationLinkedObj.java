package com.example.helpmego_java;

import java.util.ArrayList;

public class LocationLinkedObj {
    protected String BeaconID; //unique id for a ble beacon

    protected LocationLinkedObj nextLocation, prevLocation;
    protected ArrayList<String> roomIDs; //room # considered "at" this beacon

    protected enum listNavDirections{
        //forward and backward directions, turns, etc
    }


    /**
     *
     * BEGIN GETTERS AND SETTER FUNCTIONS
     *
     */


    public LocationLinkedObj getPrevLocation() {
        return prevLocation;
    }

    public void setPrevLocation(LocationLinkedObj prevLocation) {
        this.prevLocation = prevLocation;
    }

    public String getBeaconID() {
        return BeaconID;
    }

    public void setBeaconID(String beaconID) {
        BeaconID = beaconID;
    }

    public LocationLinkedObj getNextLocation() {
        return nextLocation;
    }

    public void setNextLocation(LocationLinkedObj nextLocation) {
        this.nextLocation = nextLocation;
    }

    public ArrayList<String> getRoomIDs() {
        return roomIDs;
    }

    public void setRoomIDs(ArrayList<String> roomIDs) {
        this.roomIDs = roomIDs;
    }
}
