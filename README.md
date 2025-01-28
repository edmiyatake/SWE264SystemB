SWE261 System B
Edwin Miyatake

Task: Design and Construction

Your task is to use the existing frameworks as the basis for creating a new system. Modify System A by revising the Middle Filter to filter “wild jumps” out of the data stream for altitude. A
wild jump is a variation of more than 100 feet between two adjacent frames. For wild jumps encountered in the stream, replace it with the average of the previous two altitudes. Note that if the
wild jump occurs in the second frame (i.e., there is only one previous altitude available), simply replace the current altitude with the previous altitude. The Middle Filter should (1) write the records of wild
jumps (with the original value, before replacement) to a text file called WildPoints.csv using the same format as System A, and (2) send the updated data stream to the Sink Filter. Modify the Sink Filter of System A to write the output received from the Middle Filter to a text file called OutputB.csv.

Operating System: Windows 11 Version 23H2 
Computer Architecture: AMD64 
IDE: Visual Studio Code 1.96.4

Instructions:

1. unzip the folder and open a terminal with the directory containing "SWE264SystemB" 
2. Compile the necessary java files by running javac *.java into the designated terminal
3. Run the Plumber class by running "java Plumber"
4. The output is written onto a text file called "OutputB.csv"

 
