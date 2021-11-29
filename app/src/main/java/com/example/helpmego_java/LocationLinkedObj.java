package com.example.helpmego_java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

public class LocationLinkedObj implements Serializable {
    protected String BeaconID; //unique id for a ble beacon
    public int UniqueInt; //identifies integer node to this beacon obj
    protected ArrayList<String> roomIDs; //room # considered "at" this beacon
    public HashMap<Integer, String> DirectionsTo; //details human directions to connected nodes

    /*default/null constructor*/
    public LocationLinkedObj() {
    }

    /*fully qualified constructor*/
    public LocationLinkedObj(String beaconID, int uniqueInt, ArrayList<String> roomIDs, HashMap<Integer, String> directionsTo) {
        BeaconID = beaconID;
        UniqueInt = uniqueInt;
        this.roomIDs = roomIDs;
        DirectionsTo = directionsTo;
    }
    public LocationLinkedObj(String beaconID, int uniqueInt){
        BeaconID = beaconID;
        UniqueInt = uniqueInt;
        roomIDs = new ArrayList<String>();
        DirectionsTo = new HashMap<Integer, String>();
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

    public void addRoomID(String room){
        roomIDs.add(room);
    }
    public void addDirection(int toNode, String dir){
        DirectionsTo.put(toNode, dir);
    }
}
