package com.example.helpmego_java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Class used to store information about Beacons detailed in the navigational graph, including Name, index assigned
 * in the navigation graph, rooms associated with that beacon, and direction strings associated with connected nodes
 * */
public class LocationLinkedObj implements Serializable {
    protected String BeaconID; //unique id/name for a ble beacon
    public int UniqueInt; //identifies integer node to this beacon obj
    protected ArrayList<String> roomIDs; //room # considered "at" this beacon
    public HashMap<Integer, String> DirectionsTo; //details human directions to connected nodes

    /*empty constructor*/
    public LocationLinkedObj() {
    }

    /*fully qualified constructor*/
    public LocationLinkedObj(String beaconID, int uniqueInt, ArrayList<String> roomIDs, HashMap<Integer, String> directionsTo) {
        BeaconID = beaconID;
        UniqueInt = uniqueInt;
        this.roomIDs = roomIDs;
        DirectionsTo = directionsTo;
    }
    //constructor to create object and instantiate new Arrays/HashMaps to be filled later.
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
