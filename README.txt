README.txt
By Brian Tomasik (I worked by myself on this project.)
Nov. 2006

findFlight.java runs the program. It requires one command-line argument for
the input file from which the Graph will be constructed. The input file must
be in the same format as the file airplane.txt. To run the program on that
file, type

java findFlight airport.txt

From there, the program will provide all further instructions.

script1.txt gives a script of output from trying out a number of operations
on the file airplane.txt.

Since the Cities in airplane.txt had so many connections with one another,
it was often hard to tell whether the shortest-path-by-airports and shortest-
path-by-time algorithms were really doing different things. To test that out,
I created a made-up airport schedule in the file "serpentine.txt." This
schedule had the feature that the shortest path from "a" to "z" by distance
was not the shortest path by time. I tried running my program on this file
(see the results in script2.txt), and the result was that the two shortest-
path algorithms did indeed give different results.