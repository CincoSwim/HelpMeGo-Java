package com.example.helpmego_java;

import android.location.Location;

import java.util.ArrayList;
import java.util.LinkedList;

public class PathGraph {

    /*
    * Function to return a linked list containing the path to travel along the graph.
    * Takes in:
    * adj: the adjacency graph (list<list<int>>)
    * start: int representation of start node
    * dest: int representation of destination node
    * vertexes: number of points in the graph
    * */

    public static LinkedList<Integer> findShortestPath(
            ArrayList<ArrayList<Integer>> adj, int start,
            int dest, int vertexes){

        int pred[] = new int[vertexes];
        int dist[] = new int[vertexes];

        if (BreadthSearch(adj, start, dest, vertexes, pred, dist) == false){
            //The call of BreadthSearch() here does the path calculation, storing the list of beacon indexes
            //in pred[].

            //no path between beacons - this area is not expected as a possible state
        }
        LinkedList<Integer> path = new LinkedList<Integer>();
        int crawl = dest;
        //iterate through pred[] to setup the LinkedList of integers
        path.add(crawl);
        while(pred[crawl] != -1){
            path.add(pred[crawl]);
            crawl = pred[crawl];
        }
        return path;


    }

    public static void addEdge(ArrayList<ArrayList<Integer>> adj, Integer first, Integer second) {
        //Add an edge between the two nodes detailed.
        adj.get(first).add(second); //connect second to first
        adj.get(second).add(first); //connect first to second

    }
    /**
     * Performs a breadth-first search on the beacon graph to determine shortest path between the start and end of a route
     * */
    private static boolean BreadthSearch(ArrayList<ArrayList<Integer>> graph, int source, int destination, int vertextes, int pred[], int dist[]) {
        LinkedList<Integer> queue = new LinkedList<Integer>();

        boolean visited[] = new boolean[vertextes];

        for (int i = 0; i < vertextes; i++) {
            //initialize all nodes as unchecked
            visited[i] = false;
            dist[i] = Integer.MAX_VALUE;
            pred[i] = -1;

        }

        visited[source] = true; //we're starting here, so this is checked off first
        dist[source] = 0;
        queue.add(source);

        while (!queue.isEmpty()) {
            int u = queue.remove();
            for (int i = 0; i < graph.get(u).size(); i++) {
                //if this hasn't been checked yet
                if (visited[graph.get(u).get(i)] == false) {
                    //check it!
                    visited[graph.get(u).get(i)] = true;
                    dist[graph.get(u).get(i)] = dist[u] + 1;
                    pred[graph.get(u).get(i)] = u;
                    queue.add(graph.get(u).get(i)); //queue next node to check

                    // stopping condition (when we find
                    // our destination)
                    if (graph.get(u).get(i) == destination)
                        return true; //end funct
                }
            }

        }
        return false;

    }

}
