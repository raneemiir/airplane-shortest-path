/******************************************************************************
Flight.java
By Brian Tomasik.
Based on Weiss's Edge class (p. 479).
Nov./Dec. 2006

Flight objects give information about flights: time of departure, time of
arrival, the flight time in between, and the differences from GMT of the
departure and arrival Cities. Flight objects are stored in an adjacency list
of the departing City, so they only need to have a field for the destination
City.
******************************************************************************/

public class Flight
{
    public City dest;
    public int timeDepart;
    public int timeArrive;
    public int flightTime;
    public int diffGMTStartingCityInMinutes;
    public int diffGMTEndingCityInMinutes;

    public Flight(City d, int dep, int arr, int diff)
    {
	dest = d;
	timeDepart = dep;
	timeArrive = arr;
	flightTime = Graph.getWaitingTime(dep, arr);
	diffGMTStartingCityInMinutes = diff;
	diffGMTEndingCityInMinutes 
	    = Graph.clockTimeToMinuteTime(dest.getDiffGMT());
    }

    public City getDest()
    { return dest; }

    public String getDestCityCode()
    { return dest.getCode(); }

    public int getTimeDepart()
    { return timeDepart; }
    
    public int getTimeArrive()
    { return timeArrive; }

    public int getFlightTime()
    { return flightTime; }

    public String toString()
    {
	String depart = Graph.minuteTimeToClockTime
	    (timeDepart + diffGMTStartingCityInMinutes);
	String arrive = Graph.minuteTimeToClockTime
	    (timeArrive + diffGMTEndingCityInMinutes);

	return "to " + dest.getName() + 
	    "; " + depart + " to " + arrive + "; takes " +
	    Graph.minuteTimeToHoursAndMinutes(flightTime);
    }
	  

}
