package org.example;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Main {
    static class Edge {
        String from;
        String to;
        int weight;
        Edge(String from, String to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    static class Graph {
        int id;
        List<String> nodes;
        List<Edge> edges;
    }

    static class DisjointSet {
        Map<String, String> parent = new HashMap<>();
        int operations = 0;
        public void makeSet(Collection<String> vertices) {
            for (String v : vertices) parent.put(v, v);
        }
        public String find(String v) {
            operations++;
            if (!v.equals(parent.get(v))) parent.put(v, find(parent.get(v)));
            return parent.get(v);
        }
        public void union(String a, String b) {
            operations++;
            String rootA = find(a);
            String rootB = find(b);
            if (!rootA.equals(rootB)) parent.put(rootB, rootA);
        }
    }

    static Map<String, Object> kruskal(Graph graph) {
        long start = System.nanoTime();
        List<Edge> result = new ArrayList<>();
        int totalCost = 0;
        int operations = 0;
        List<Edge> edges = new ArrayList<>(graph.edges);
        edges.sort(Comparator.comparingInt(e -> e.weight));
        DisjointSet ds = new DisjointSet();
        ds.makeSet(graph.nodes);
        for (Edge e : edges) {
            operations++;
            if (!ds.find(e.from).equals(ds.find(e.to))) {
                ds.union(e.from, e.to);
                result.add(e);
                totalCost += e.weight;
            }
        }
        long end = System.nanoTime();
        double timeMs = (end - start) / 1_000_000.0;
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("mst_edges", result);
        res.put("total_cost", totalCost);
        res.put("operations_count", operations + ds.operations);
        res.put("execution_time_ms", timeMs);
        return res;
    }

    static Map<String, Object> prim(Graph graph) {
        long start = System.nanoTime();
        Map<String, List<Edge>> adj = new HashMap<>();
        for (String node : graph.nodes) adj.put(node, new ArrayList<>());
        for (Edge e : graph.edges) {
            adj.get(e.from).add(e);
            adj.get(e.to).add(new Edge(e.to, e.from, e.weight));
        }
        String startNode = graph.nodes.get(0);
        Set<String> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
        List<Edge> result = new ArrayList<>();
        int totalCost = 0;
        int operations = 0;
        visited.add(startNode);
        if (adj.get(startNode) != null) pq.addAll(adj.get(startNode));
        while (!pq.isEmpty() && result.size() < graph.nodes.size() - 1) {
            Edge e = pq.poll();
            operations++;
            if (visited.contains(e.to)) continue;
            visited.add(e.to);
            result.add(e);
            totalCost += e.weight;
            for (Edge next : adj.get(e.to)) if (!visited.contains(next.to)) pq.add(next);
        }
        long end = System.nanoTime();
        double timeMs = (end - start) / 1_000_000.0;
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("mst_edges", result);
        res.put("total_cost", totalCost);
        res.put("operations_count", operations);
        res.put("execution_time_ms", timeMs);
        return res;
    }

    public static void main(String[] args) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String inputPath = "src/main/resources/ass_3_input.json";
        String outputPath = "src/main/resources/ass_3_output.json";
        JsonObject jsonObject = JsonParser.parseReader(new FileReader(inputPath)).getAsJsonObject();
        Type graphListType = new TypeToken<List<Graph>>(){}.getType();
        List<Graph> graphs = gson.fromJson(jsonObject.get("graphs"), graphListType);
        List<Map<String, Object>> results = new ArrayList<>();
        for (Graph g : graphs) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("graph_id", g.id);
            Map<String, Object> inputStats = new LinkedHashMap<>();
            inputStats.put("vertices", g.nodes.size());
            inputStats.put("edges", g.edges.size());
            entry.put("input_stats", inputStats);
            entry.put("prim", prim(g));
            entry.put("kruskal", kruskal(g));
            results.add(entry);
        }
        Map<String, Object> finalOutput = new LinkedHashMap<>();
        finalOutput.put("results", results);
        try (FileWriter writer = new FileWriter(outputPath)) {
            gson.toJson(finalOutput, writer);
        }
        System.out.println("Results written to " + outputPath);
    }
}
