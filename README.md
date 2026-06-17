# Flash Flood Relief Planner - CCS4202 Group Project

Java Swing desktop app for Selangor flash flood relief planning.

## Algorithms
- **Dijkstra** — shortest safe route from each relief hub to affected areas (respects flood depth Dmax, flooded roads, road weight limits)
- **Fractional Knapsack** — optimal truck loading by priority/weight ratio

## Run in IntelliJ
1. Open this folder as a project
2. Run `Main.java`

## GUI tabs
1. **Map & Roads** — interactive graph: add/delete places and roads, edit flood levels
2. **Supplies** — relief items and truck capacity
3. **Delivery Plan** — route results and cargo manifest

Use **Load Selangor Sample** to reset the demo map, then **Calculate Delivery Plan**.
