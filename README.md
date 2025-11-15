A Java-based GUI application that visually simulates popular page replacement algorithms used in virtual memory. The goal of this project is to help students understand memory management concepts through clear visualizations, animations, and performance comparison.

Supported Algorithms

  * FIFO (First-In-First-Out) – Replaces the oldest page in memory

  * LRU (Least Recently Used) – Removes the page unused for the longest time

  * MRU (Most Recently Used) – Removes the most recently accessed page

  * Optimal – Removes the page used farthest in the future

  * LIFO (Last-In-First-Out) – Removes the most recently loaded page

  * Random – Replaces a page chosen at random
    
System Requirements

Java JDK 8+

Works on Windows, macOS, and Linux

Recommended screen resolution: 1300×800

How to Use
Step 1 — Configure

Enter Number of Frames

Choose an algorithm

Click Setup

Step 2 — Enter Reference String

Example input:

7 0 1 2 0 3 0 4

Step 3 — Choose an Action

Visualize

Shows each memory access step

Displays HIT/MISS in real time

Updates the frames visually

Provides a final summary

Analyse

Compares all algorithms

Shows page faults for each

Displays a bar chart of performance

Highlights the best algorithm

Output Shown to the User

Total Page Faults

Total Page Hits

Hit Ratio

Final Frame Content

Step-by-step explanation

Comparative bar chart (in Analyse mode)

Sample Algorithm Comparison (Example)

For sequence:
7 0 1 2 0 3 0 4 2 3 0

Algorithm	Faults
FIFO	9
LRU	7
MRU	8
Optimal	6
LIFO	10
Random	8
User Interface Layout

Left Panel:
Controls, frame input, algorithm selection, results, descriptions

Center Panel:
Visualization of frames, animation steps, bar charts

Bottom Panel:
Execution steps and logs

Technical Summary

Built with Java Swing

Uses Timer for animations

Data structures:

Queue (FIFO)

Deque (LIFO)

HashMap (LRU/MRU)

LinkedHashSet (frame set)

Custom ChartPanel for performance graphs

