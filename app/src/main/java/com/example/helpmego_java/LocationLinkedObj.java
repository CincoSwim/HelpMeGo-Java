package com.example.helpmego_java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;

public class LocationLinkedObj implements Serializable {
    protected String BeaconID; //unique id for a ble beacon
    public int UniqueInt; //identifies integer node to this beacon obj
    protected ArrayList<String> roomIDs; //room # considered "at" this beacon
    public Dictionary<Integer, String> DirectionsTo; //details human directions to connected nodes

    /*default/null constructor*/
    public LocationLinkedObj() {
    }

    /*fully qualified constructor*/
    public LocationLinkedObj(String beaconID, int uniqueInt, ArrayList<String> roomIDs, Dictionary<Integer, String> directionsTo) {
        BeaconID = beaconID;
        UniqueInt = uniqueInt;
        this.roomIDs = roomIDs;
        DirectionsTo = directionsTo;
    }


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

    public String getBeaconID() {
        return BeaconID;
    }

    public void setBeaconID(String beaconID) {
        BeaconID = beaconID;
    }

    public ArrayList<String> getRoomIDs() {
        return roomIDs;
    }

    public void setRoomIDs(ArrayList<String> roomIDs) {
        this.roomIDs = roomIDs;
    }
}
