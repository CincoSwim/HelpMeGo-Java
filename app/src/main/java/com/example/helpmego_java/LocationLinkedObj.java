package com.example.helpmego_java;

import java.util.ArrayList;
import java.util.Dictionary;

public class LocationLinkedObj {
    protected String BeaconID; //unique id for a ble beacon
    public int UniqueInt; //identifies integer node to this beacon obj
    protected ArrayList<String> roomIDs; //room # considered "at" this beacon
    public Dictionary<LocationLinkedObj, String> DirectionsTo; //details human directions to connected nodes











    /**
     *
     * BEGIN GETTERS AND SETTER FUNCTIONS
     *
     */


    public int getUniqueInt() {
        return UniqueInt;
    }

    public void setUniqueInt(int uniqueInt) {
        UniqueInt = uniqueInt;
    }


    /*
    public LocationLinkedObj getPrevLocation() {
        return prevLocation;
    }

    public void setPrevLocation(LocationLinkedObj prevLocation) {
        this.prevLocation = prevLocation;
    }
    */
    public String getBeaconID() {
        return BeaconID;
    }

    public void setBeaconID(String beaconID) {
        BeaconID = beaconID;
    }

    /*public LocationLinkedObj getNextLocation() {
        return nextLocation;
    }

    public void setNextLocation(LocationLinkedObj nextLocation) {
        this.nextLocation = nextLocation;
    }
    */
    public ArrayList<String> getRoomIDs() {
        return roomIDs;
    }

    public void setRoomIDs(ArrayList<String> roomIDs) {
        this.roomIDs = roomIDs;
    }
}
