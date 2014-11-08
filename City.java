/******************************************************************************
City.java
Brian Tomasik
Nov./Dec. 2006

City objects are the verticies in the graph. They have fields for name,
code, difference from GMT, x coordinate, and y coordinate. They store an
adjacency list for other City objects, as well as an adjacency list for 
Flight objects. Finally, City objects have fields used for Dijkstra's
algorithm, and they implement Comparable so that they can be put into a
priority queue during the algorithm.
******************************************************************************/

import java.util.*;

public class City implements Comparable
{
    static final int INFINITY = Integer.MAX_VALUE;

    public String name;
    public String code;
    public int diffGMT;
    public double xCoord;
    public double yCoord;
    public List<City> adjacentCities;
    public List<Flight> adjacentFlights;
    public City prevOnShortestPath;
    public int timeDepartPrevCity; // On the shortest path by time, the time of
                                   // departure from the previous city
    public int timeArriveThisCity; // On the shortest path by time, the time 
                                   // of arrival at the current city
    public int costFromStart; // On the shortest path
    public int distFromStart; // On the shortest path
    public boolean alreadyVisited;

    // Constructor
    public City(String n, String c, int d, double x, double y)
    {
	name = n;
	code = c;
	diffGMT = d;
	xCoord = x;
	yCoord = y;
	adjacentCities = new LinkedList<City>();
	adjacentFlights = new LinkedList<Flight>();
	reset();
    }

    // Accessors
    public String getName()
    { return name; }

     public String getCode()
    { return code; }
    
    public int getDiffGMT()
    { return diffGMT; }
    
    public double getXCoord()
    { return xCoord; }

    public double getYCoord()
    { return yCoord; }

    public int getCostFromStart()
    { return costFromStart; }

    public int getDistFromStart()
    { return distFromStart; }
    
    public City getPrevOnShortestPath()
    { return prevOnShortestPath; }

    public City getACityToWhichThisCityHasAFlight()
    {
	if(!adjacentFlights.isEmpty())
	    { return (adjacentFlights.get(0)).getDest(); }
	else
	    { System.out.println("There are no such cities!"); return null; }
    }

    public int getTimeDepartPrevCity()
    { return timeDepartPrevCity; }

    public int getTimeArriveThisCity()
    { return timeArriveThisCity; }

    public boolean alreadyVisited()
    { return alreadyVisited; }

    public Iterator getAdjacentCitiesIterator()
    { return adjacentCities.iterator(); }

    public Iterator getAdjacentFlightsIterator()
    { return adjacentFlights.iterator(); }

    // Modifiers
    public void addFlight(Flight addMe)
    { adjacentFlights.add(addMe); }

    public void addAdjacentCity(City other)
    { adjacentCities.add(other); }

    public void setCostFromStart(int c)
    { costFromStart = c; }

    public void setDistFromStart(int d)
    { distFromStart = d; }
    
    public void setPrevOnShortestPath(City prev)
    { prevOnShortestPath = prev; }

    public void setTimeDepartPrevCity(int t)
    { timeDepartPrevCity = t; }

    public void setTimeArriveThisCity(int t)
    { timeArriveThisCity = t; }

    public void haveAlreadyVisited()
    { alreadyVisited = true; }

    // Other methods
    public double distOtherCity(City other)
    {
	double xSquared = Math.pow((xCoord - other.xCoord), 2);
	double ySquared = Math.pow((yCoord - other.yCoord), 2);
	return Math.sqrt(xSquared + ySquared);
    }

    public boolean alreadyHasEdgeToOtherCity(String otherCityCode)
    {
	boolean alreadyThere = false;
	ListIterator<City> itr = adjacentCities.listIterator();
	while(itr.hasNext() && !alreadyThere)
	    {
		if(((itr.next()).getCode()).equalsIgnoreCase(otherCityCode))
		    { alreadyThere = true; }
	    }

	return alreadyThere;
    }

    public String toString()
    {
	City nextCity;
	Flight nextFlight;
	String returnMe = "\nname = " + name + "\n" + "code = " + code + "\n" 
	    + "diffGMT = " + diffGMT + "\n" + "x = " + xCoord + "\n" +
	    "y = " + yCoord + "\n" + "adjacent cities:\n";
	Iterator cities = adjacentCities.iterator();
	while(cities.hasNext())
	    {
		nextCity = (City)cities.next();
		returnMe = returnMe.concat(nextCity.name + ", distance = " +
					   distOtherCity(nextCity) + "\n");
	    }
	returnMe = returnMe.concat("departing flights:\n");
	Iterator flights = adjacentFlights.iterator();
	while(flights.hasNext())
	    {
		nextFlight = (Flight)flights.next();
		returnMe = returnMe.concat(nextFlight.toString() + "\n");
	    }

	return returnMe;
    }

    public void printAllFlights()
    {
	Iterator flights = adjacentFlights.iterator();
	while(flights.hasNext())
	    {
		System.out.println((Flight)flights.next());
	    }	
    }

    public void printFlightsWithOtherCity(String otherCode)
    {
	Flight nextFlight;
	Iterator flights = adjacentFlights.iterator();
	while(flights.hasNext())
	    {
		nextFlight = (Flight)flights.next();
		if(((nextFlight.dest).getCode()).equals(otherCode))
		    { System.out.println(nextFlight); }
	    }	
    }

    public void reset()
    {
	costFromStart = INFINITY;
	distFromStart = INFINITY;
	prevOnShortestPath = null;
	timeDepartPrevCity = -1; // a nonsense value
	timeArriveThisCity = -1; // a nonsense value
	alreadyVisited = false;
    }

    // Cities should be comparable so that they can be put into the heap used
    // in Dijkstra's algorithm
    public int compareTo(Object other)
    { return costFromStart - ((City) other).costFromStart; }
}
