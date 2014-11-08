/******************************************************************************
findFlight.java
By Brian Tomasik
Nov./Dec. 2006

This is the program that the user runs to find flights. The user is presented
with a list of options:
- list all cities in the Graph
- see if there is a direct flight between two given cities
- find all flights that depart from a given city
- find all flights that go to a given city
- add a city to the Graph
- add a flight to the Graph
- given two cities and a starting time, find a path from the first city
to the second that will require as few stops as possible
- given two cities and a starting time, find a path from the first city to 
the second that will require as little time as possible.
- write out the Graph to a file.
******************************************************************************/

import java.util.*;
import java.io.*;

public class findFlight
{
    static Scanner reader = new Scanner(System.in);

    // The followinig are used in keeping track of which option the user
    // has selected from the main list.
    final static String LIST_CITIES = "l";
    final static String DIRECT_FLIGHT = "d";
    final static String DEPART_FROM_CITY = "f";
    final static String GO_TO_CITY = "g";
    final static String ADD_CITY = "a";
    final static String ADD_FLIGHT = "t";
    final static String FEWEST_STOPS = "s";
    final static String QUICKEST = "u";
    final static String GRAPH_TO_FILE = "v";
    final static String SEE_AGAIN = "r";
    final static String QUIT = "q";

    // These keep track of whether the user says "yes" or "no."
    final static String YES = "y";
    final static String NO = "n";

    // The following specify modes for executing certain methods. I use them
    // to give slightly different output messages depending on what the method
    // is being used for. These are methods that perform a function that's
    // useful in more than one situation, but for which the function has
    // slightly different contexts. These variables keep track of which 
    // context is being used.
    final static String START_TRIP = "x";
    final static String SPECIFY_DEPART_TIME = "y";
    final static String SPECIFY_ARRIVE_TIME = "z";
    final static int DONT_REFER_TO_NUMBER = 0;

    // The x and y coordinates for cities that the input gives seem to have
    // no particular real-world significance. However, I used those numbers
    // to calculate the distance between two cities, Albuquerque and 
    // Boston. Then I found an approximate real value for that distance using
    // GoogleMaps. The scale factor given above is the number by which I
    // multiply all of the input coordinate values in order to make the
    // resulting distances represent the approximate actual distances in
    // miles.
    final static double SCALE_FACTOR = 4.9;

    public static void main(String[] args) throws IOException
    {
	printIntroMessage();
	String option;
	Graph graph = initializeGraph(new Scanner(new File(args[0])));

	while(!( (option = getUserOption()).equalsIgnoreCase(QUIT) ))
	    {
		if(option.equalsIgnoreCase(LIST_CITIES))
		    {
			System.out.println("\nHere they are:");
			System.out.println(graph);
		    }
		else if(option.equalsIgnoreCase(DIRECT_FLIGHT))
		    {
			checkIfDirectFlight(graph);
		    }
		else if(option.equalsIgnoreCase(DEPART_FROM_CITY))
		    {
			City departFromHere = askUserForCity(graph, 0);
			if(departFromHere != null)
			    { departFromHere.printAllFlights(); }
			else
			    {
				System.out.println("Sorry! It looks like " +
						   "there are no flights.");
			    }
		    }
		else if(option.equalsIgnoreCase(GO_TO_CITY))
		    {
			City arriveHere = askUserForCity(graph, 0);
			if(arriveHere != null)
			    { graph.printFlightsWithThisDestination
				  (arriveHere.getCode()); }
			else
			    {
				System.out.println("Sorry! It looks like " +
						   "there are no flights.");
			    }
		    }
		else if(option.equalsIgnoreCase(ADD_CITY))
		    {
			addACity(graph);
		    }
		else if(option.equalsIgnoreCase(ADD_FLIGHT))
		    {
			City depart = askUserForCity(graph, 1);
			if(depart == null) { continue; }
			City arrive = askUserForCity(graph, 2);
			if(arrive == null) { continue; }
			int departTime = askUserForTime(SPECIFY_DEPART_TIME);
			int arriveTime = askUserForTime(SPECIFY_ARRIVE_TIME);
			graph.addFlight
			    (depart, arrive, departTime, arriveTime);
			System.out.println("Your flight has been added.");
		    }
		else if(option.equalsIgnoreCase(FEWEST_STOPS))
		    {
			City start = askUserForCity(graph, 1);
			if(start == null) { continue; }
			City finish = askUserForCity(graph, 2);
			if(finish == null) { continue; }
			graph.fewestCities(start);
			graph.minimizeTimeGivenPath
			    (start, finish, askUserForTime(START_TRIP));
		    }
		else if(option.equalsIgnoreCase(QUICKEST))
		    {
			City start = askUserForCity(graph, 1);
			if(start == null) { continue; }
			City finish = askUserForCity(graph, 2);
			if(finish == null) { continue; }
			graph.dijkstra
			    (start, askUserForTime(START_TRIP), finish);
		    }
		else if(option.equalsIgnoreCase(GRAPH_TO_FILE))
		    {
			graphToFile(graph);
		    }
		else if(option.equalsIgnoreCase(SEE_AGAIN))
		    {
			// do nothing - let the menu repeat
		    }
	    }
	
	System.out.println("\nBye.\n");
    }

    /**
       printIntroMessage: Tells the user about the program.
    */
    public static void printIntroMessage()
    {
	System.out.println("\nWelcome. This program allows you to find out " +
			   "various information\nabout flights in the US. " +
			   "Here's a list of options:");
    }

    /**
       initializeGraph: Reads in data from a file and creates a corresponding
       Graph. (This method assumes that the user has supplied a correctly
       formatted text file.)
       @param fileIn: the Scanner for the input file
       @return the newly created and initialized Graph
    */
    public static Graph initializeGraph(Scanner fileIn)
    {
	Graph graph = new Graph();
	String currLine;
	
	// Skip introductory material, i.e., material starting with "#"
	while((currLine = fileIn.nextLine()).charAt(0) == '#')
	    {
		// skip ahead to the next line
	    }

	// Go until the "!" delimiter
	addCityToGraph(currLine, graph); /* Make sure to add the first good
					    line before getting a new one 
					    below. */
	while((currLine = fileIn.nextLine()).charAt(0) != '!')
	    {
		addCityToGraph(currLine, graph);
	    }
	
	// I'm not going to read in anything from the list of which cities
	// are connected to which other cities. I'll get a more complete
	// version of that information from the third section of input. So
	// I just skip over this section.
	while(!((currLine = fileIn.nextLine()).equals("")))
	    {
		// do nothing
	    }
		// StringTokenizer st = new StringTokenizer(currLine);
		//graph.addEdge(st.nextToken(), st.nextToken());

	// Go past further comments
	while((currLine = fileIn.nextLine()).charAt(0) == '#')
	    {
		// skip ahead
	    }

	// Finish with the rest of the input file
	processFlight(currLine, graph);
	while(fileIn.hasNext())
	    {
		currLine = fileIn.nextLine();
		processFlight(currLine, graph);
	    }

	return graph;
    }

    /**
       addCityToGraph: Uses data from the file to create a new City and
       add it to the graph
       @param line: the line of input from which to extract data
       @param graph: the graph into which to insert the City objects.
    */
    public static void addCityToGraph(String currLine, Graph graph)
    {
	StringTokenizer st = new StringTokenizer(currLine);
	String code;
	String cityName = "";
	int diffGMT;
	double xCoord, yCoord;
	code = st.nextToken();
	diffGMT = Integer.parseInt(st.nextToken());
	xCoord = SCALE_FACTOR * Integer.parseInt(st.nextToken());
	yCoord = SCALE_FACTOR * Integer.parseInt(st.nextToken());
	
	while(st.hasMoreTokens())
	    {
		cityName = cityName.concat(st.nextToken() + " ");
	    }
	cityName = cityName.trim();
	
	graph.addCity(new City(cityName, code, diffGMT, xCoord, yCoord));
    }

    /**
       processFlight: Given a line of input and a Graph, this method reads
       in data from the line and adds a Flight to the appropriate City.
       @param currLine: the current line of input
       @param graph: the Graph that stores the City objects
    */
    public static void processFlight(String currLine, Graph graph)
    {
	String startCityCode, destCityCode;
	String departTimeString, arriveTimeString;
	StringTokenizer st = new StringTokenizer(currLine);
	
	String airline;
	// The following code passes over unused lines for Airline and 
	// Flight number. The trick is that sometimes in the input file,
	// both of these fields appear together (e.g., "CO1594") and sometimes
	// they appear apart (e.g., "AA 748") depending on how many digits
	// the number has. In order to discriminate between these cases,
	// I look at the length of the first String token on the line. Since
	// the Airline is always two characters long, I'll know that this
	// is an instance of merged Airline and Flight codes if the length is
	// more than two characters. In that case, I won't read a second 
	// String token to be passed over.
	airline = st.nextToken();
	if(airline.length() <= 2)
	    { st.nextToken(); } // read another unused token

	startCityCode = st.nextToken();
	departTimeString = st.nextToken();
	destCityCode = st.nextToken();
	arriveTimeString = st.nextToken();

	int departClockTime = processTimeString(departTimeString);
	int arriveClockTime = processTimeString(arriveTimeString);
	
	City startCity, destCity;
	// Check that both Cities are already in the Graph. If not, print an
	// error. If so, get the City objects.
	if(graph.containsCityByCode(startCityCode))
	    { startCity = graph.getCity(startCityCode); }
	else
	    {
		System.out.println("Oops. The graph doesn't contain a city " +
				   " with code " + startCityCode + ".");
		return;
	    }
	
	if(graph.containsCityByCode(destCityCode))
	    { destCity = graph.getCity(destCityCode); }
	else
	    {
		System.out.println("Oops. The graph doesn't contain a city " +
				 " with code " + destCityCode + ".");
		return;
	    }

	graph.addFlight(startCity, destCity, 
			departClockTime, arriveClockTime);
    }

    /**
       processTimeString: This method takes a String reprentation of a time,
       including an "A" for "am" or "P" for "pm." It converts the time to
       24-hour clock time.
       @param timeString: the String to convert to 24-hour clock time
       @return the GMT time in minutes as an int.
    */
    public static int processTimeString(String processMe)
    {
	int stringLength = processMe.length();
	String amOrPm 
	    = Character.toString(processMe.charAt(stringLength - 1));
	String substring = processMe.substring(0, stringLength - 1);
	int clockTime = Integer.parseInt(substring);
	int hoursSinceStartOfDay = clockTime / 100;
	if(amOrPm.equalsIgnoreCase("P") && hoursSinceStartOfDay != 12)
	    { clockTime += 1200; }
	if(amOrPm.equalsIgnoreCase("A") && hoursSinceStartOfDay == 12)
	    { clockTime -= 1200; }

	return clockTime;
    }

    /**
       getUserOption: Ask for the user's next option
       @return a String representing the user's choice.
    */
    public static String getUserOption()
    {
	String choice;

	System.out.println
	    ("\nEnter\n" +
	     "\t" + LIST_CITIES +
	     " to see a list of the cities\n" +
	     "\t" + DIRECT_FLIGHT + 
	     " to see if there is a direct flight between two given cities\n" +
	     "\t" + DEPART_FROM_CITY + 
	     " to find all flights that depart from a given city\n" +
	     "\t" + GO_TO_CITY + 
	     " to find all flights that go to a given city\n" +
	     "\t" + ADD_CITY +
	     " to add a city to the Graph\n" +
	     "\t" + ADD_FLIGHT +
	     " to add a flight\n" +
	     "\t" + FEWEST_STOPS +
	     " to find a path from one city to another that will require\n" +
	     "\t  as few stops as possible\n" +
	     "\t" + QUICKEST +
	     " to find a path from one city to another that will require \n" +
	     "\t  as little time as possible\n" +
	     "\t" + GRAPH_TO_FILE +
	     " to write the graph out to a file\n" +
	     "\t" + SEE_AGAIN +
	     " to see this menu again\n" +
	     "\t" + QUIT +
	     " to quit.");
	
	choice = reader.nextLine();

	while
	    (!(choice.equalsIgnoreCase(LIST_CITIES)) &&
	     !(choice.equalsIgnoreCase(DIRECT_FLIGHT)) &&
	     !(choice.equalsIgnoreCase(DEPART_FROM_CITY)) &&
	     !(choice.equalsIgnoreCase(GO_TO_CITY)) &&
	     !(choice.equalsIgnoreCase(ADD_CITY)) &&
	     !(choice.equalsIgnoreCase(ADD_FLIGHT)) &&
	     !(choice.equalsIgnoreCase(FEWEST_STOPS)) &&
	     !(choice.equalsIgnoreCase(QUICKEST)) &&
	     !(choice.equalsIgnoreCase(GRAPH_TO_FILE)) &&
	     !(choice.equalsIgnoreCase(SEE_AGAIN)) &&
	     !(choice.equalsIgnoreCase(QUIT)))
	    {
		System.out.println("Hey! Your choice has to be one of " +
				   "these options:\n" +
				   LIST_CITIES + ", " +
				   DIRECT_FLIGHT + ", " + DEPART_FROM_CITY +
				   ", " + GO_TO_CITY + ", " + ADD_CITY +
				   ", " + ADD_FLIGHT + ", " + FEWEST_STOPS +
				   ", " + QUICKEST + ", " + SEE_AGAIN +
				   ", " + GRAPH_TO_FILE +
				   ", " + QUIT + ". Try again:");
		choice = reader.nextLine();
	    }
	
	return choice;
    }

    /**
       checkIfDirectFlight: This method carries out one of the user's options:
       checking to see if two cities have a direct flight between them. If
       it turns out they do, this method then asks the user if she wants to
       see all of the flights from the first city to the second.
       @param graph: the Graph that stores the Cities and Flights
    */
    public static void checkIfDirectFlight(Graph graph)
    {
	City firstCity = askUserForCity(graph, 1);
	if(firstCity == null)
	    { return; }
	City secondCity = askUserForCity(graph, 2);
	if(secondCity == null)
	    { return; }

	if(!graph.checkEdgeBetweenCities(firstCity, secondCity.getCode()))
	    { System.out.println("Nope, no direct flight."); }
	else
	    {
		System.out.println
		    ("Yes, there is at least one direct flight. Do you " +
		     "want to see all\n" + 
		     "flights from the first city to the second?");
		if(userSaysYes())
		    {
			firstCity.printFlightsWithOtherCity
			    (secondCity.getCode());
		    }
	    }
    }

    /**
       askUserForCity: Prompts the user to enter either the name or the code
       for a City in the Graph. If the City isn't in the Graph, prompts the
       user to try again, or else to quit. If the City is found, it's returned.
       @param graph: the Graph in which to look
       @param numberCity: how many Cities have been looked for (e.g., 
       numberCity = 2 if this is the second City that the user is entering).
       If the user doesn't want to make reference to this being the first,
       second, etc. City searched for, he enters DONT_REFER_TO_NUMBER for 
       numberCity.
       @return the found City, or null if not found
    */
    public static City askUserForCity(Graph graph, int numberCity)
    {
	City returnMe = null;
	String city;
	boolean found = false;
	boolean keepLooking = true;
	
	while(!found && keepLooking)
	    {
		if(numberCity != DONT_REFER_TO_NUMBER)
		    {
			System.out.println("Okay, enter either the name " +
					   "or the code for city #" + 
					   numberCity + ":");
		    }
		else
		    {
			System.out.println("Okay, enter either the name " +
					   "or the code for the city:");
		    }
		city = reader.nextLine();

		// Since the user may enter either the name of the city or 
		// of its code, the following checks both the TreeMap of 
		// names and the TreeMap of codes.
		if(graph.containsCityByName(city))
		    {
			returnMe = graph.getCity(graph.getCode(city)); 
			found = true;
		    }
		else if(graph.containsCityByCode(city))
		    {
			returnMe = graph.getCity(city);
			found = true;
		    }
		else
		    {
			System.out.println
			    ("That city isn't in the list. Note that the " +
			     "names you enter are\n" +
			     "case-sensitive. Try again?");
			if(!userSaysYes()) { keepLooking = false; }
		    }
	    }

	return returnMe;
    }

    /**
       userSaysYes: Prompts the user to enter "yes" or "no," and checks
       to see that the user has entered correct input.
       @return true if "yes," false if "no"
    */
    public static boolean userSaysYes()
    {
	boolean saysYes = false;
	String response;
	System.out.println("Enter " + YES + " for \"yes\" or " + NO + 
			   " for \"no\":");
	response = reader.nextLine();
	while(!response.equalsIgnoreCase(YES) &&
	      !response.equalsIgnoreCase(NO))
	    {
		System.out.println("Hey, you have to enter either " + YES +
				   " or " + NO + ":");
		response = reader.nextLine();
	    }

	if(response.equalsIgnoreCase(YES)) { saysYes = true; }

	return saysYes;
    }

    /**
       askUserForTime: Prompt the user to enter a time. This method may be
       called in three different contexts, and each one is identified with
       a modeForRunningMethod that determines what message the user sees.
       In order to perform error checking easily, this method has the user 
       enter am/pm, the hours digits, and the minutes digits separately. 
       To process the time, this method turns those inputs into a String 
       and calls the previous processTimeString() method.
       @param modeForRunningMethod: a String specifying which output message
       should be given to the user.
       @return the clock time the user enters, in 24-hour format
    */
    public static int askUserForTime(String modeForRunningMethod)
    {
	if(modeForRunningMethod.equals(START_TRIP))
	    {
		System.out.println("Now you'll specify the starting time " +
				   "for your trip.");
	    }
	else if(modeForRunningMethod.equals(SPECIFY_DEPART_TIME))
	    {
		System.out.println("Now you'll specify the depature time " +
				   "of the flight.\nMake sure that this " +
				   "time is *relative to* the time zone " +
				   "of the departing city.");
	    }
	else if(modeForRunningMethod.equals(SPECIFY_ARRIVE_TIME))
	    {
		System.out.println("Now you'll specify the arrival time " +
				   "of the flight.\nMake sure that this " +
				   "time is *relative to* the time zone " +
				   "of the city of arrival.");
	    }
	System.out.println("First, do you want the time to be " +
			   "in the evening (i.e., pm)?");
	boolean pm = userSaysYes();
	System.out.println("Now, enter the hours portion of your " +
			   "starting time\n(e.g., if you depart at 8:15 pm " +
			   "enter \"8\").");
	int hours = reader.nextInt();
	while(hours < 1 || hours > 12)
	    {
		System.out.println("Hey, the number of hours you enter has " +
				   "to be between 1 and 12. Try again:");
		hours = reader.nextInt();
	    }

	System.out.println("Now enter the minutes portion of the time\n" +
			   "(e.g., if you depart at 8:15 pm, enter \"15\").");
	int minutes = reader.nextInt();
	while(minutes < 0 || minutes > 59)
	    {
		System.out.println("Hey, the number of minutes has " +
				   "to be between 0 and 59. Try again:");
		minutes = reader.nextInt();
	    }
	
	String timeString = "" + hours;
	if(minutes < 10) { timeString = timeString.concat("0"); }
	timeString = timeString.concat("" + minutes);
	if(pm) { timeString = timeString.concat("P"); }
	else { timeString = timeString.concat("A"); }
	reader.nextLine(); // get rid of white space that was left over from
	                   // reading in the integers above

	// Farm out the work to a previous method.
	return processTimeString(timeString);
    }


    /**
       addACity: This method prompts the user to enter information about
       a City to add, and then adds the City.
       @param graph: the graph that holds the current cities
    */
    public static void addACity(Graph graph)
    {
	System.out.println("Okay, enter the city name:");
	String name = reader.nextLine();
	while(graph.containsCityByName(name))
	    {
		System.out.println("Oops. That city is already in the " +
				   "graph. Do you want to try again?");
		if(userSaysYes())
		    {
			System.out.println("Enter the city name:");
			name = reader.nextLine();
		    }
		else
		    { return; }
	    }

	System.out.println("Enter the three-digit code that will be used " +
			   "to identify the city.\nYou must use UPPER CASE " +
			   "letters:");
	String code = reader.nextLine();
	code = checkThatCodeIsValid(code);

	while(graph.containsCityByCode(code))
	    {
		System.out.println("Oops. That code is already taken. " +
				   "Do you want to try another?");
		if(userSaysYes())
		    {
			System.out.println("Enter the code:");
			code = reader.nextLine();
			code = checkThatCodeIsValid(code);
		    }
		else
		    { return; }
	    }

	System.out.println("How many hours off from Greenwich Mean Time is " +
			   "your city?\nIf your city is in a later time " +
			   "zone, make your number negative.\nAs an " +
			   "example, New York is 6 hours later than GMT, so " +
			   "you would type -6:");
	int diffGMT = reader.nextInt();
	while(diffGMT < -12 || diffGMT > 12)
	    {
		System.out.println("Hey, the hour difference can only " +
				   "be between -12 and 12. Try again:");
		diffGMT = reader.nextInt();
	    }
	diffGMT *= 100; // Convert the number of hours off to the usual format

	System.out.println("Enter the x-coordinate of the city:");
	int xCoord = reader.nextInt();
	System.out.println("Enter the y-coordinate of the city:");
	int yCoord = reader.nextInt();
	reader.nextLine(); // get rid of white space
	
	System.out.println("\nOkay, here's your information:\n" +
			   "name = " + name + "\n" +
			   "code = " + code + "\n" +
			   "difference from GMT = " + diffGMT + "\n" +
			   "x-coordinate = " + xCoord + "\n" +
			   "y-coordinate = " + yCoord + "\n\n" +
			   "Is this the way you want everything?");
	if(userSaysYes())
	    {
		graph.addCity(new City(name, code, diffGMT, xCoord, yCoord));
		System.out.println("Okay, your city has been entered.");
	    }
	else
	    {
		System.out.println("Do you want to start over?");
		if(userSaysYes())
		    { addACity(graph); } // rerun the method
		else
		    { return; }
	    }
    }

    /**
       checkThatCodeIsValid: This method takes a code and checks that it's
       valid according to (a) having only three letters, (b) having only
       capital letters, and (c) having only letters 'A' to 'Z'. Once a valid
       code is gotten, that code is returned.
       @param code: the code to check
       @return the valid code
    */
    public static String checkThatCodeIsValid(String code)
    {
	if(!(code.length() == 3))
	    {
		System.out.println
		    ("Hey, your code has to be three letters " +
		     "long. Try again:");
		code = reader.nextLine();
		code = checkThatCodeIsValid(code);
	    }
	
	if(!(isACapitalWord(code)))
	    {
		System.out.println
		    ("Hey, your code has to have all capital " +
		     "letters. Try again:");
		code = reader.nextLine();
		code = checkThatCodeIsValid(code);
	    }
	
	if(!(hasOnlyLetters(code)))
	    {
		System.out.println
		    ("Hey, your code can only consist of " +
		     "letters from A to Z. Try again:");
		code = reader.nextLine();
		code = checkThatCodeIsValid(code);
	    }
		
	return code;
    }

    /**
       isACapitalWord: Tells whether the given word consists of all capital
       letters.
       @param word: the word to check
    */
    public static boolean isACapitalWord(String word)
    {
	for(int i = 0; i < word.length(); i++)
	    {
		if(Character.isLowerCase(word.charAt(i)))
		    { return false; }
	    }

	return true;
    }

    /**
       hasOnlyLetters: Checks whether the given word consists only of letters/
       @param word: the word to check
    */
    public static boolean hasOnlyLetters(String word)
    {
	for(int i = 0; i < word.length(); i++)
	    {
		if(!(Character.isLetter(word.charAt(i))))
		    { return false; }
	    }

	return true;
    }

    /**
       graphToFile: This method write out the contents of the Graph to a 
       file.
       @param graph: the Graph whose contents will be written out
    */
    public static void graphToFile(Graph graph)
    {
	String writeMe;
	System.out.println("Do you want the output to be verbose\n" +
			   "(including all adjacent cities and flights)?");
	if(userSaysYes())
	    { writeMe = graph.verboseToString(true); }
	else
	    { writeMe = graph.verboseToString(false); }

    	System.out.println("What should the file be called?");
	String fileName = reader.nextLine();
	PrintWriter fileOut = null;
	
	try
	    {
		fileOut = new PrintWriter(new FileWriter(fileName));
		fileOut.println(writeMe);
	    } catch(IOException e) { e.printStackTrace(); }

	finally
	    {
		if(fileOut != null) { fileOut.close(); }
	    }

	System.out.println("Your file has been written.");
    }
}
