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
            //give a popup or direct the user to find a beacon, they gone wack
            //this gives us the path though.
            //return a null linked list to show it gone poopy
        }
        LinkedList<Integer> path = new LinkedList<Integer>();
        int crawl = dest;
        path.add(crawl);
        while(pred[crawl] != -1){
            path.add(pred[crawl]);
            crawl = pred[crawl];
        }
        return path;


    }

    public static void addEdge(ArrayList<ArrayList<Integer>> adj, Integer first, Integer second) {
        adj.get(first).add(second);
        adj.get(second).add(first);

    }

    private static boolean BreadthSearch(ArrayList<ArrayList<Integer>> graph, int source, int destination, int vertextes, int pred[], int dist[]) {
        LinkedList<Integer> queue = new LinkedList<Integer>();

        boolean visited[] = new boolean[vertextes];

        for (int i = 0; i < vertextes; i++) {
            visited[i] = false;
            dist[i] = Integer.MAX_VALUE;
            pred[i] = -1;

        }

        visited[source] = true;
        dist[source] = 0;
        queue.add(source);

        while (!queue.isEmpty()) {
            int u = queue.remove();
            for (int i = 0; i < graph.get(u).size(); i++) {
                if (visited[graph.get(u).get(i)] == false) {
                    visited[graph.get(u).get(i)] = true;
                    dist[graph.get(u).get(i)] = dist[u] + 1;
                    pred[graph.get(u).get(i)] = u;
                    queue.add(graph.get(u).get(i));

                    // stopping condition (when we find
                    // our destination)
                    if (graph.get(u).get(i) == destination)
                        return true;
                }
            }

        }
        return false;
    }
}
