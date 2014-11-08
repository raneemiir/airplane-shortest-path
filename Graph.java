/******************************************************************************
Graph.java
Brian Tomasik
Based on Weiss's Graph.java (p. 480).
Nov./Dec. 2006

A Graph stores a TreeMap<String, City> that maps city codes to city objects.
It also stores a codeMap<String, String> that maps city names to city codes.
Graphs allow users to enter new Cities, add Flights between existing Cities,
and check whether certain Cities are already present. In addition, Graphs
house all of the time-conversion methods, as well as the shortest-path
methods.
******************************************************************************/

import java.util.*;

public class Graph
{
    public final static int MINUTES_IN_A_DAY = 24 * 60;
    public final static int SHORTEST_TIME_TO_CHANGE_PLANES = 30;

    // Data fields
    private Map<String, City> cityMap;
    private Map<String, String> codeMap;
    // The airport codes will be used in the cityMap to identify City
    // objects, but we also need a way to keep track of which code goes
    // with which name. That's what the codeMap does.

    private int size;

    // Constructor
    public Graph()
    {
	cityMap = new TreeMap<String, City>();
	codeMap = new TreeMap<String, String>();
	size = 0;
    }

    // Accessors
    public int getSize()
    { return size; }
    
    public boolean containsCityByCode(String code)
    { return cityMap.containsKey(code); }

    public boolean containsCityByName(String name)
    { return codeMap.containsKey(name); }

    public String getCode(String name)
    { return codeMap.get(name); }

    public City getCity(String code)
    { return cityMap.get(code); }

    public Iterator getCitiesIterator()
    { return (cityMap.values()).iterator(); }

    public String toString()
    { return verboseToString(false); }

    public boolean checkEdgeBetweenCities(City firstCity, 
					  String secondCityCode)
    { return firstCity.alreadyHasEdgeToOtherCity(secondCityCode); }

    public String verboseToString(boolean verbose)
    {
	City currCity;
	String returnMe = "Num cities = " + size + "\n";
	Iterator cities = getCitiesIterator();
	while(cities.hasNext())
	    {
		if(verbose)
		    {
			returnMe = returnMe.concat
			    ((cities.next()).toString() + "\n");
		    }
		else
		    {
			currCity = (City)(cities.next());
			returnMe = returnMe.concat
			    ( currCity.getName() + ", " 
			      + currCity.getCode() + "\n");
		    }
	    }
	
	return returnMe;
    }

    /**
       printFlightsWithThisDestination: Looks through all Flights in the 
       Graph to see if any go to the given City.
       @param dest: the code for the City to which the desired Flights
       should point
    */
    public void printFlightsWithThisDestination(String destCode)
    {
	Iterator cities = getCitiesIterator();
	City nextCity;
	while(cities.hasNext())
	    {
		nextCity = (City)cities.next();
		if(nextCity.alreadyHasEdgeToOtherCity(destCode))
		{
		    System.out.println("\nFlights from " 
				       + nextCity.getName() + ":");
		    nextCity.printFlightsWithOtherCity(destCode);
		}
	    }
    }

    /************************* ADD METHODS **********************************/
    /**
       addCity: This method adds a new City to the Graph. It assumes that
       passed-in City objects have name and code values, but this should
       always be true, since the City constructor requires them.
       @param newCity: the new City to add
    */
    public void addCity(City newCity)
    {
	if(containsCityByCode(newCity.getCode()))
	    { System.out.println("Oops, that city is already in the graph."); }
	else
	    {
		cityMap.put(newCity.getCode(), newCity);
		codeMap.put(newCity.getName(), newCity.getCode());
		size ++;
	    }
    }

    /**
       addFlight: Given the necessary parameters, this method creates a 
       Flight object and adds it to the starting City's List<Flight>. It also 
       checks to see if the destination City is already in the given City's 
       adjacentCities list. If not, it adds the destination City to that list.
       @param startCity: the starting City
       @param destCity: the destination City
       @param clockTimeDepart: the departure time
       @param clockTimeArrive: the arrival time
    */
    public void addFlight(City startCity, City destCity, 
			  int clockTimeDepart, int clockTimeArrive)
    {
	int minuteTimeDepart = clockTimeToGMTMinuteTime
	    (clockTimeDepart, startCity.getDiffGMT());
	int minuteTimeArrive = clockTimeToGMTMinuteTime
	    (clockTimeArrive, destCity.getDiffGMT());
	int diffGMTStartingCityInMinutes 
	    = clockTimeToMinuteTime(startCity.getDiffGMT());
	Flight newFlight 
	    = new Flight(destCity, minuteTimeDepart, 
			 minuteTimeArrive, diffGMTStartingCityInMinutes);
	startCity.addFlight(newFlight);

	// Check to see if we need to add a new edge to startCity's
	// adjacentCities list
	if(!startCity.alreadyHasEdgeToOtherCity(destCity.getCode()))
	    { startCity.addAdjacentCity(destCity); }
    }

    /************************* TIME-CONVERSION METHODS ***********************/
    /**
       clockTimeToMinuteTime: Takes a clock time (e.g., 1734 for 5:34 pm)
       and converts it to minute time (e.g., 1054 minutes since the beginning
       of the day).
       @param clockTime: the time to convert
       @return the time in minutes
    */
    public static int clockTimeToMinuteTime(int clockTime)
    {
	int minutesDigits = clockTime % 100;
	int hoursDigits = clockTime / 100;
	return hoursDigits * 60 + minutesDigits;
    }

    /**
       clockTimeToGMTMinuteTime: Converts a regular clock time into
       GMT minute time.
       @param clockTime: the time in 24-hour clock format (e.g., 2015 for 
       8:15 pm)
       @param diffGMT: the difference from GMT of the time zone (e.g.,
       -800 for Albuquereque)
       @return the GMT time in minutes as an int.
    */
    public static int clockTimeToGMTMinuteTime(int clockTime, int diffGMT)
    {
	int minuteTime = clockTimeToMinuteTime(clockTime);
	int minuteDiffGMT = clockTimeToMinuteTime(diffGMT);
	int desiredTime = minuteTime - minuteDiffGMT;
	if(desiredTime >= MINUTES_IN_A_DAY)
	    { desiredTime = desiredTime % MINUTES_IN_A_DAY; }

	return desiredTime;
    }

    /**
       minuteTimeToClockTime: Takes a minute time and converts it to a String
       in regular time format. For instance, the minute time 1266 would become
       "9:06 pm".
       @param minuteTime: the time in minutes
       @return a String of the time in standard format
    */
    public static String minuteTimeToClockTime(int minuteTime)
    {
	// If minuteTime is negative, it refers to a time from the
	// previous day.
	while(minuteTime < 0)
	    { minuteTime += MINUTES_IN_A_DAY; }

	if(minuteTime > MINUTES_IN_A_DAY)
	    { minuteTime = minuteTime % (60*24); }

	String clockTime;
	boolean pm = false;
	int numHours = minuteTime / 60;
	int numMins = minuteTime % 60;
	
	if(numHours > 11)
	    { pm = true; }
	if(numHours > 12)
	    { numHours = numHours % 12; }
	if(numHours == 0)
	    { numHours = 12; }

	clockTime = "" + numHours + ":";
	if(numMins < 10) { clockTime = clockTime.concat("0"); }
	clockTime = clockTime.concat("" + numMins);
	if(!pm) { clockTime = clockTime.concat(" am"); }
	else { clockTime = clockTime.concat(" pm"); }
	
	return clockTime;
    }

    /**
       minuteTimeToHoursAndMinutes: converts a number of minutes into 
       a number of hours and minutes. For instance, 255 minutes becomes
       "4 hours, 15 minutes".
       @param minuteTime: the time in number of minutes
       @return a String telling the number of hours and minutes
    */
    public static String minuteTimeToHoursAndMinutes(int minuteTime)
    {
	if(minuteTime < 0)
	    {
		System.out.println("Oops. You can't have negative time!");
		return null;
	    }

	int numHours = minuteTime / 60;
	int numMins = minuteTime % 60;

	return "" + numHours + " hrs, " + numMins + " mins";
    }

    /**
       getWaitingTime: Returns the waiting time between arriving at an 
       airport and subsequently departing, given arrival and departure 
       times, adjusting for the possibility that departure might not happen
       until the next day.
       @param timeArrive: time when get to airport
       @param timeDepart: time when leave airport
       @return the waiting time at the airport
    */
    public static int getWaitingTime(int timeArrive, int timeDepart)
    {
	int waitingTime;
	if(timeDepart >= timeArrive)
	    { waitingTime = timeDepart - timeArrive; }
	else
	    { waitingTime = MINUTES_IN_A_DAY + timeDepart - timeArrive; }
	return waitingTime;
    }


    /*********************** SHORTEST-PATH METHODS ***************************/
    /**
       resetAllCities: Goes through each City and resets the values relevant
       to the shortest-path algorithms.
    */
    public void resetAllCities()
    {
	Iterator cities = getCitiesIterator();
	City nextCity;
	while(cities.hasNext())
	    { ((City)(cities.next())).reset(); }
    }

    /**
       fewestCities: This method applies the unweighted shortest-path
       algorithm in in finding the shortest distance from a given City
       to all other Cities.
       @param startCity: the City from which to find shortest paths
     */
    public void fewestCities(City startCity)
    {
	City currCity, nextCity;
	Iterator cities;
	resetAllCities();
	Queue<City> doTheseCities = new LinkedList<City>();
	startCity.setDistFromStart(0);
	doTheseCities.add(startCity);

	while(!doTheseCities.isEmpty())
	    {
		currCity = doTheseCities.remove();
		cities = currCity.getAdjacentCitiesIterator();	
		while(cities.hasNext())
		    {
			nextCity = (City)cities.next();

			if(nextCity.getDistFromStart() == City.INFINITY)
			    {
				nextCity.setDistFromStart
				    (currCity.getDistFromStart() + 1);
				nextCity.setPrevOnShortestPath(currCity);
				doTheseCities.add(nextCity);
			    }
		    }
	    }
    }

    /**
       minimizeTimeGivenPath: This method is invoked after fewestCities().
       The fewestCities() method successfully find the path from one City
       to another that minimizes the number of airports. However, once that
       path from airport to airport is established, there's still room
       to take Flights that happen at different times. It would be nice to
       have a path that minimized the time required once the airport path
       has been established. That's what this method does. It takes the 
       desired starting and ending Cities, and by tracing backwards from
       the ending City, it constructs a new temporary Graph that contains
       only those Cities that are on the shortest-airport path. Then this
       method applies dijkstra() to find a minimum time path given the
       existing path constraint.
       @param startCity: the City where the traveller begins
       @param finishCity: the City where the traveller would like to go
       @param startClockTime: the time (e.g., 1624 for 4:24 pm) when the 
       traveller starts off on the journey
    */
    public void minimizeTimeGivenPath(City startCity, City finishCity,
				      int startClockTime)
    {
	// Create new Graph on which to run dijkstra()
	Graph temp = new Graph();
	City currCity = finishCity;
	// Put all and only all the cities that are on the shortest path 
	// into temp. However, I don't want to copy the entire City, since
	// that would include adjacency lists to Cities that will not be
	// part of the new temp Graph. Hence, I just copy the constructor
	// parameters. Below that, I copy over the relevant Flights, too.
	while(currCity != null)
	    {
		City addMe = new City(currCity.getName(), currCity.getCode(),
				      currCity.getDiffGMT(), 
				      currCity.getXCoord(),
				      currCity.getYCoord());
		temp.addCity(addMe);
		currCity = currCity.getPrevOnShortestPath();
	    }
	// Now add all the relevant Flights to temp
	Iterator tempCities = temp.getCitiesIterator();
	City tempCity;
	while(tempCities.hasNext())
	    {
		tempCity = (City)tempCities.next();
		if((tempCity.getCode()).equalsIgnoreCase(finishCity.getCode()))
		    { continue; }
		else
		    {
		// We need only look at the Flights from tempCity to the
		// City that immediately follows it, for in fact, tempCity
		// has no Flights to Cities beyond that. For if it did,
		// we could take those Flights, and the City immediately
		// following tempCity would no longer be in the temp Graph,
		// as it would no longer be on the shortest path by number
		// of airports.
		City cityAfterTempCity 
		    = getCity(tempCity.getCode()).
		    getACityToWhichThisCityHasAFlight();
			
		// get the Flights adjacent to the old version of
		// what's called tempCity
		City oldVersionOfTempCity = getCity(tempCity.getCode());
		Iterator adjacentFlights 
		    = oldVersionOfTempCity.getAdjacentFlightsIterator();
		Flight flight;
		
		while(adjacentFlights.hasNext())
		    {
			flight = (Flight) adjacentFlights.next();
			if((flight.getDestCityCode()).equalsIgnoreCase
			   (cityAfterTempCity.getCode()))
			    {
				tempCity.addFlight(flight);
				if(!tempCity.alreadyHasEdgeToOtherCity
				   (cityAfterTempCity.getCode()))
				    { 
					tempCity.addAdjacentCity
					    (cityAfterTempCity);
				    }
			    }
		    }
		}
	    }
	
	System.out.println(temp.verboseToString(true));

	// Perform dijkstra() on this subset of the original Graph. Since
	// this subset contains only those Cities on the shortest path,
	// dijkstra() can only find the shortest-time itinerary *given the
	// constraint of being a minimum airport path*.
	temp.dijkstra(startCity, startClockTime, finishCity);
    }

    /**
       dijkstra: This method takes a starting City and a starting clock time.
       It then applies Dijkstra's algorithm, to find shortest-time paths
       from that starting City to any other City. One stipulation is that
       at least 30 minutes are required between the time one leaves one plane
       and boards another. The subsequent printPath method can be used to
       display the paths found by this method.
       @param startCity: the City from which all shortest-paths will start
       @param startClockTime: the time when the traveller starts at startCity
       @param finishCity: the City to which the traveller wants to go
    */
    public void dijkstra(City startCity, int startClockTime, City finishCity)
    {
	int citiesDone = 0;
	City currCity, nextCity;
	Flight nextFlight;
	Iterator flights;
	int nextCost, waitingTime, potentiallyFasterTime;
	boolean thisFlightIsAnIntermediateFlight = false;
	int GMTStartTimeMinutes 
	    = clockTimeToGMTMinuteTime(startClockTime, startCity.getDiffGMT());

	PriorityQueue<City> doTheseCities = new PriorityQueue<City>();
	resetAllCities();
	
	startCity.setCostFromStart(0);
	startCity.setTimeArriveThisCity(GMTStartTimeMinutes);
	doTheseCities.add(startCity);

	while(!doTheseCities.isEmpty() && citiesDone < size)
	    {
		if(citiesDone > 0)
		    { thisFlightIsAnIntermediateFlight = true; }

		currCity = doTheseCities.remove();
		if(currCity.alreadyVisited()) { continue; }
		currCity.haveAlreadyVisited();
		citiesDone ++;

		flights = currCity.getAdjacentFlightsIterator();
		while(flights.hasNext())
		    {
			nextFlight = (Flight)flights.next();
			nextCity = nextFlight.dest;
			nextCost = nextCity.getCostFromStart();
			waitingTime = getWaitingTime
			    (currCity.getTimeArriveThisCity(), 
			     nextFlight.getTimeDepart());
			if(waitingTime < SHORTEST_TIME_TO_CHANGE_PLANES &&
			   thisFlightIsAnIntermediateFlight)
			    { waitingTime += MINUTES_IN_A_DAY; }
			// If the traveller is still going to take this 
			// particular flight, he'll have to stay overnight.

			potentiallyFasterTime = currCity.getCostFromStart() + 
			    waitingTime + nextFlight.getFlightTime();
			if(potentiallyFasterTime < nextCost)
			    {
				nextCity.setCostFromStart
				    (potentiallyFasterTime);
				nextCity.setTimeArriveThisCity
				    (nextFlight.getTimeArrive());
				nextCity.setTimeDepartPrevCity
				    (nextFlight.getTimeDepart());
				nextCity.setPrevOnShortestPath(currCity);
				doTheseCities.add(nextCity);
			    }
		    }
	    }

	// Print the shortest path, calling printPath()
	System.out.println("\nHere's the shortest path:");
	if(printPath(finishCity)) // true if there is a path
	    {
		System.out.println("Total cost = " +
				   minuteTimeToHoursAndMinutes
				   (finishCity.getCostFromStart()));
	    }
    }

    /**
       printPath: Given two Cities, this method traces prevOnShortestPath
       references to print out the shortest path recursively.
       @param finishcity: the end city, from which references will be 
       followed backwards
       @param true if there was actually a shortest path to print out,
       false otherwise
    */
    public static boolean printPath(City finishCity)
    {
	if(finishCity.getCostFromStart() < City.INFINITY)
	    {
		int arrivalTime, timeDepartPrev, additionalCost;
		if(finishCity.getPrevOnShortestPath() == null)
		    {
			arrivalTime = finishCity.getTimeArriveThisCity() +
			    clockTimeToMinuteTime(finishCity.getDiffGMT());
			System.out.println("Start at " 
					   + finishCity.getName() + " at " +
					   minuteTimeToClockTime(arrivalTime));
		    }
		else
		    {
			printPath(finishCity.getPrevOnShortestPath());
			timeDepartPrev = finishCity.getTimeDepartPrevCity() +
			   clockTimeToMinuteTime
			   ((finishCity.getPrevOnShortestPath()).getDiffGMT());
			arrivalTime = finishCity.getTimeArriveThisCity() +
			    clockTimeToMinuteTime(finishCity.getDiffGMT());
			additionalCost = finishCity.getCostFromStart() -
			    (finishCity.getPrevOnShortestPath()).
			    getCostFromStart();
			System.out.println("depart at " + 
					   minuteTimeToClockTime
					   (timeDepartPrev) +
					   " to " + finishCity.getName() + 
					   ", arriving at " +
					   minuteTimeToClockTime(arrivalTime) +
					   "\n\twith additional cost of " +
					   minuteTimeToHoursAndMinutes
					   (additionalCost));
		    }
		
		return true;
	    }
	else
	    {
		System.out.println("Sorry! There is no such path.");
		return false;
	    }
    }
}
