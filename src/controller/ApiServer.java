package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.Edge;
import model.Graph;
import model.KnapsackLineItem;
import model.KnapsackResult;
import model.Node;
import model.NodeRouteInfo;
import model.RouteResult;
import model.SupplyItem;
import model.VehicleProfile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Lightweight HTTP server (pure JDK, no external dependencies) that:
 *   1. Serves the existing static front-end pages
 *      (UserInterface.html, rescue-console.html, give-info-console.html, app.js)
 *   2. Exposes a small JSON API that those pages call to execute the
 *      Dijkstra and Fractional Knapsack algorithms against live data
 *      held in {@link database.FloodDatabase}.
 *
 * Routes:
 *   GET  /                       -> UserInterface.html
 *   GET  /api/graph              -> nodes, edges, vehicle profiles (JSON)
 *   GET  /api/route?source=&dmax= -> Dijkstra result for all nodes (JSON)
 *   GET  /api/items              -> current supply item set I (JSON)
 *   GET  /api/knapsack?capacity= -> Fractional Knapsack result (JSON)
 *   POST /api/field-report       -> apply a field report to the database
 */
public class ApiServer {

    private final FloodController controller;
    private final HttpServer server;
    private final Path webRoot;

    public ApiServer(FloodController controller, int port, String webRootPath) throws IOException {
        this.controller = controller;
        this.webRoot = Paths.get(webRootPath).toAbsolutePath().normalize();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        registerRoutes();
    }

    private void registerRoutes() {
        server.createContext("/api/graph", this::handleGraph);
        server.createContext("/api/route", this::handleRoute);
        server.createContext("/api/items", this::handleItems);
        server.createContext("/api/knapsack", this::handleKnapsack);
        server.createContext("/api/field-report", this::handleFieldReport);
        server.createContext("/", this::handleStatic);
    }

    public void start() {
        server.start();
        int port = server.getAddress().getPort();
        System.out.println("First Call backend running:");
        System.out.println("  Portal           http://localhost:" + port + "/UserInterface.html");
        System.out.println("  Rescue console   http://localhost:" + port + "/rescue-console.html");
        System.out.println("  Field report     http://localhost:" + port + "/give-info-console.html");
    }

    /* =========================================================
       Static file serving
       ========================================================= */

    private void handleStatic(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/UserInterface.html";
        }
        Path file = webRoot.resolve(path.substring(1)).normalize();
        if (!file.startsWith(webRoot) || !Files.exists(file) || Files.isDirectory(file)) {
            send(ex, 404, "text/plain; charset=utf-8", "404 Not Found: " + path);
            return;
        }
        byte[] bytes = Files.readAllBytes(file);
        ex.getResponseHeaders().add("Content-Type", guessContentType(file.toString()));
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String guessContentType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (lower.endsWith(".json")) return "application/json; charset=utf-8";
        return "application/octet-stream";
    }

    /* =========================================================
       GET /api/graph
       ========================================================= */

    private void handleGraph(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }
        Graph graph = controller.getGraph();
        StringBuilder json = new StringBuilder("{");

        json.append("\"nodes\":[");
        boolean first = true;
        for (Node n : graph.getAllNodes()) {
            if (!first) json.append(",");
            first = false;
            json.append("{")
                    .append("\"id\":").append(q(n.getId())).append(",")
                    .append("\"name\":").append(q(n.getName())).append(",")
                    .append("\"priority\":").append(q(n.getPriority().getLabel())).append(",")
                    .append("\"floodDepth\":").append(n.getFloodDepthMm())
                    .append("}");
        }
        json.append("],");

        json.append("\"edges\":[");
        first = true;
        for (Edge e : graph.getEdges()) {
            if (!first) json.append(",");
            first = false;
            json.append("{")
                    .append("\"from\":").append(q(e.getFrom())).append(",")
                    .append("\"to\":").append(q(e.getTo())).append(",")
                    .append("\"weight\":").append(num(e.effectiveWeight()))
                    .append("}");
        }
        json.append("],");

        json.append("\"vehicles\":[");
        first = true;
        for (VehicleProfile v : controller.getVehicleProfiles()) {
            if (!first) json.append(",");
            first = false;
            json.append("{")
                    .append("\"name\":").append(q(v.getName())).append(",")
                    .append("\"payloadCapacityKg\":").append(num(v.getPayloadCapacityKg())).append(",")
                    .append("\"safeFloodDepthMm\":").append(num(v.getSafeFloodDepthMm()))
                    .append("}");
        }
        json.append("]");

        json.append("}");
        send(ex, 200, "application/json; charset=utf-8", json.toString());
    }

    /* =========================================================
       GET /api/route?source=UNI&dmax=550
       ========================================================= */

    private void handleRoute(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }
        Map<String, String> params = queryParams(ex.getRequestURI());
        String source = params.getOrDefault("source", "UNI");
        double dMax = parseDoubleOr(params.get("dmax"), 550);

        Graph graph = controller.getGraph();
        if (graph.getNode(source) == null) {
            send(ex, 400, "application/json; charset=utf-8", "{\"error\":\"unknown source node\"}");
            return;
        }

        RouteResult result = controller.getRouteAnalysis(source, dMax);

        StringBuilder json = new StringBuilder("{");
        json.append("\"source\":").append(q(result.getSource())).append(",");
        json.append("\"dmax\":").append(num(result.getDMax())).append(",");

        json.append("\"results\":[");
        boolean first = true;
        for (Node n : graph.getAllNodes()) {
            if (n.getId().equals(source)) {
                continue; // do not list the source hub itself in the destination table
            }
            NodeRouteInfo info = result.get(n.getId());
            if (!first) json.append(",");
            first = false;
            json.append("{")
                    .append("\"id\":").append(q(n.getId())).append(",")
                    .append("\"name\":").append(q(n.getName())).append(",")
                    .append("\"priority\":").append(q(n.getPriority().getLabel())).append(",")
                    .append("\"reachable\":").append(info.isReachable()).append(",")
                    .append("\"eta\":").append(info.isReachable() ? num(info.getEta()) : "null").append(",")
                    .append("\"path\":").append(q(info.isReachable() ? info.getPathString() : ""))
                    .append("}");
        }
        json.append("]}");

        send(ex, 200, "application/json; charset=utf-8", json.toString());
    }

    /* =========================================================
       GET /api/items
       ========================================================= */

    private void handleItems(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (SupplyItem s : controller.getSupplyItems()) {
            if (!first) json.append(",");
            first = false;
            json.append("{")
                    .append("\"id\":").append(q(s.getId())).append(",")
                    .append("\"name\":").append(q(s.getName())).append(",")
                    .append("\"weight\":").append(num(s.getWeightKg())).append(",")
                    .append("\"priority\":").append(num(s.getPriorityValue())).append(",")
                    .append("\"density\":").append(String.format(Locale.US, "%.4f", s.density()))
                    .append("}");
        }
        json.append("]");
        send(ex, 200, "application/json; charset=utf-8", json.toString());
    }

    /* =========================================================
       GET /api/knapsack?capacity=1000
       ========================================================= */

    private void handleKnapsack(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }
        Map<String, String> params = queryParams(ex.getRequestURI());
        double capacity = parseDoubleOr(params.get("capacity"), 1000);
        if (capacity < 0) capacity = 0;

        KnapsackResult result = controller.getLoadOptimization(capacity);

        StringBuilder json = new StringBuilder("{");
        json.append("\"capacity\":").append(num(result.getCapacity())).append(",");
        json.append("\"totalWeight\":").append(String.format(Locale.US, "%.2f", result.getTotalWeight())).append(",");
        json.append("\"totalScore\":").append(String.format(Locale.US, "%.2f", result.getTotalScore())).append(",");

        json.append("\"manifest\":[");
        boolean first = true;
        for (KnapsackLineItem li : result.getManifest()) {
            if (!first) json.append(",");
            first = false;
            json.append("{")
                    .append("\"id\":").append(q(li.getItem().getId())).append(",")
                    .append("\"name\":").append(q(li.getItem().getName())).append(",")
                    .append("\"weightCapacity\":").append(num(li.getItem().getWeightKg())).append(",")
                    .append("\"priority\":").append(num(li.getItem().getPriorityValue())).append(",")
                    .append("\"weightLoaded\":").append(String.format(Locale.US, "%.2f", li.getWeightLoaded())).append(",")
                    .append("\"scoreAdded\":").append(String.format(Locale.US, "%.2f", li.getScoreAdded())).append(",")
                    .append("\"fraction\":").append(String.format(Locale.US, "%.4f", li.getFraction()))
                    .append("}");
        }
        json.append("]}");

        send(ex, 200, "application/json; charset=utf-8", json.toString());
    }

    /* =========================================================
       POST /api/field-report
       ========================================================= */

    private void handleFieldReport(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseFormBody(body);

        String nodeId = emptyToNull(form.get("nodeTarget"));
        Double floodDepth = parseDoubleOrNull(form.get("floodDepth"));
        Double baseWeight = parseDoubleOrNull(form.get("baseWeight"));
        String itemId = emptyToNull(form.get("itemCategory"));
        Double itemWeight = parseDoubleOrNull(form.get("itemWeight"));
        Double itemPriority = parseDoubleOrNull(form.get("priorityValue"));

        boolean changed = controller.submitFieldReport(nodeId, floodDepth, baseWeight, itemId, itemWeight, itemPriority);

        String json = "{\"status\":" + q(changed ? "ok" : "no-change") + "}";
        send(ex, 200, "application/json; charset=utf-8", json);
    }

    /* =========================================================
       Helpers
       ========================================================= */

    private Map<String, String> queryParams(URI uri) {
        Map<String, String> map = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            map.put(urlDecode(pair.substring(0, idx)), urlDecode(pair.substring(idx + 1)));
        }
        return map;
    }

    private Map<String, String> parseFormBody(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isBlank()) return map;
        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            map.put(urlDecode(pair.substring(0, idx)), urlDecode(pair.substring(idx + 1)));
        }
        return map;
    }

    private String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private double parseDoubleOr(String s, double fallback) {
        if (s == null || s.isEmpty()) return fallback;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    /** JSON string literal, quoted and escaped. */
    private String q(String s) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.append("\"").toString();
    }

    /** JSON number literal; renders infinities as null. */
    private String num(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) return "null";
        if (d == Math.rint(d)) {
            return Long.toString((long) d);
        }
        return String.format(Locale.US, "%.2f", d);
    }

    private void send(HttpExchange ex, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", contentType);
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
