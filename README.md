![Flash Flood Relief Banner](image/Project%20banner.png)
# Flash Flood Relief Logistic Optimization in Selangor - CCS4202 Group Project

![Java](https://img.shields.io/badge/Language-Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/Status-Completed-28A745?style=for-the-badge)

Managing flood risk information across multiple zones can be overwhelming. Flash Flood Tracker brings clarity to real-time flood data with organized dashboards and automated alerts. There is also a feature that analyze the best combination of supply based on its weight and value that need to be transported to affected victims. It is a time saving since during disaster time, many unexpected events can be happen.

## Table of Contents

- [Problem Statement](#problem-statement)
- [Our Solution](#our-solution)
- [Core Features](#core-features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [How to Install and Run](#how-to-install-and-run)
- [Team](#team)

## Problem Statement

Flash floods are among the most dangerous natural disasters. Without early warning systems, communities cannot respond fast enough to protect lives and property. Then, our group, AZIS invented a project to help solve this issue. We build a computer system that turns the disaster areas into a graph that consist of node that represent places and edges represent roads. We apply 3 algorithm to solve the problems at the same time. This system was trained to find a shortest route  to each location and choosing the best combination of supplies to load into a rescue truck without exceeding the weight limit. Model-View-Contoller(MVC) structure is applied in the system, so the logic part and interface part are being split and more manageable. 

## Our Solution

-Convert a disaster area as a directed weighted graph, where the nodes are locations and edges are roads with travel time as weight.
-Use Dijkstra’s Algorithm to find the shortest distance and safest route from base area to affected areas, if a certain road is block, the Dijkstra’s Algorithm will find another shortest and safest route.
-Implementing Fractional Knapsack Algorithm to choose the best combination of supplies that can be split into smaller parts because we want to used a truck capacity as fully as possible.

## Core Features
1) Map and Roads
![Flash Flood Relief Banner](image/UI(1).png)
- Add Place > Can be used to add new place in the map to deliver the emergency supplies
- Delete Selected > Can delete the selected place in the map
- Add Road > Can connect between 2 places, also can add the flood depth and expected travel time
- Re-arrange Map >
- Load Selangor Sample >
-Additional info: The table below show the route based on the map,estimated travel time, limit supply that can be carried on and check box to determine whether the road is flooded or not

2) Supplies
![Flash Flood Relief Banner](image/UI(2).png)
- This UI shows the list of supplies,its weight,priority score and available stock
- There is also feature to add and remove item at bottom left

3) Delivery Plan
![Flash Flood Relief Banner](image/UI(3).png)
-This UI shows information about delivery plan
-It is mainly to show the information about the transportation of supply from UPM (main base) and UNITEN (sub base) to the affected area
   
  
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
