// example of program that calculates the  median degree of a 
// venmo transaction graph

 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
 
public class median_degree {
	
	final static String currentDirectory = System.getProperty("user.dir");    	
	Transaction current_transaction;
	Date max_time_processed;
	JSONParser jsonParser;
	ArrayList<Transaction> window;
	
	public median_degree(){
		max_time_processed = new Date(Long.MIN_VALUE);
		jsonParser = new JSONParser();
		window = new ArrayList<Transaction>();
		
		//delete output.txt if it exists at beginning
		String path= currentDirectory + "/venmo_output/output.txt";
		File file = new File(path);
		if (file.exists()) file.delete();
	}
	
    public static void main(String[] args) {
    	median_degree md = new median_degree();    	    	
    	md.ReadInputLineAndWriteOutputMedian();   	   	    	         
    }
    
    private void ReadInputLineAndWriteOutputMedian() {
    	
    	try {        	        	            
            FileReader fileReader = new FileReader(currentDirectory + "/venmo_input/venmo-trans.txt");
            BufferedReader br = new BufferedReader(fileReader);
            String line = br.readLine();
            while(line != null) {

                JSONObject jsonObject = (JSONObject) jsonParser.parse(line);                
                
                String datetime = (String) jsonObject.get("created_time");
                if (datetime.length() != 20){
					line = br.readLine();
                	continue;
				}
                Date created_time = parseDate(datetime);            
                String target = (String) jsonObject.get("target");
                String actor = (String) jsonObject.get("actor");
                 
				//ignoring erroneous input transactions
                if (target.length()==0 || actor.length()==0){
					line = br.readLine();
                	continue; 
				}
                
                current_transaction = new Transaction(created_time, target, actor);
                             
                this.CreateWindow(current_transaction);
                
                //System.out.println(max_time_processed + " " + actor + " " + target);
                //System.out.println(linecounter + " " + window.size());
                
                line = br.readLine();
            }  
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}

	private void CreateWindow(Transaction t) {
		double median = 0.0;
		//handling in-order transaction		
        if(t.created_time.compareTo(max_time_processed) >= 0){
            max_time_processed = t.created_time;
            window.add(t);
            window = FilterWindow(window, max_time_processed);
            median = this.CalculateMedianDegree(window);                                                         
            WriteToFile(median);
        } 
        
        //handling out-of-order transaction
        else if (t.created_time.compareTo(max_time_processed) < 0 ){
        	if(((max_time_processed.getTime()-t.created_time.getTime())/1000) < 60){ //allow transactions in the past 60 seconds
        		window.add(t);
        		window = FilterWindow(window, max_time_processed);
                median = this.CalculateMedianDegree(window);                                             
                WriteToFile(median);
        	}
        	else{  //discard transactions older than 60s and do nothing
        		//print previous median degree               		            		
        		WriteToFile(median);
        	}
        }		
	}

	//writes the current median degree to output.txt
    private void WriteToFile(double median) {
    	try{
	        // create new file
			String content = String.format("%.2f", median);
			String path= currentDirectory + "/venmo_output/output.txt";
			File file = new File(path);
			
			   // if file doesnt exists, then create it			
			if (!file.exists()) {
			   file.createNewFile();
			}
			   			  			
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			// write in file
			bw.write(content + "\n");
			   
			// close connection
			bw.close();
        }catch(Exception e){
             System.out.println(e);
        }
		
	}

    //this method calculates median degree from the current updated window
	private double CalculateMedianDegree(ArrayList<Transaction> window) {   	
    	int PeopleID = 0;
    	Map<String, Integer> PeopleMap = new HashMap <String, Integer>();
    	
    	for (Transaction t : window) {    		//assign all distinct actors and targets in a hashmap with unique integer IDs
    		if(!PeopleMap.containsKey(t.actor)){    			
    			PeopleMap.put(t.actor, PeopleID++);
    		}
    		if(!PeopleMap.containsKey(t.target)){    			
    			PeopleMap.put(t.target, PeopleID++);
    		}    		
    	}
    	
    	boolean [][] PeopleMatrix2D = new boolean[PeopleID][PeopleID]; //2D boolean adjacency matrix for calculating degree
    	for (Transaction t : window) { 
    		PeopleMatrix2D[PeopleMap.get(t.actor)][PeopleMap.get(t.target)] = true;
    		PeopleMatrix2D[PeopleMap.get(t.target)][PeopleMap.get(t.actor)] = true;
    	}
    	
    	int [] degreelist = new int[PeopleID]; //calculating degree of each vertex (person) from PeopleMatrix2D 
    	for (int i = 0; i < PeopleID; i++){
    		int degree_count = 0;
    		for (int j = 0; j < PeopleID; j++){
    			if (PeopleMatrix2D[i][j] == true) degree_count++;
    		}
    		degreelist[i] = degree_count;
    	}
    	
    	//sorting the list containing all degrees and calculating median
    	//System.out.println(Arrays.toString(degreelist));
    	Arrays.sort(degreelist);
    	//System.out.println(linecounter + " sorted:" + Arrays.toString(degreelist));
    	double median;
    	if (degreelist.length % 2 == 0)
    	    median = ((double)degreelist[degreelist.length/2] + (double)degreelist[degreelist.length/2 - 1])/2.0;
    	else
    	    median = (double) degreelist[degreelist.length/2];
    	
    	return median;
	}
		
	//this method updates the 60s window and discards outdated transactions from the window
	private ArrayList<Transaction> FilterWindow(ArrayList<Transaction> window, Date max_time_processed) {
    	ArrayList<Transaction> toremove = new ArrayList<Transaction>();
    	for (Transaction t : window) {
    		if(((max_time_processed.getTime() - t.created_time.getTime())/1000) >= 60) toremove.add(t); //if t is outdated add it to toremove list
    	}
    	window.removeAll(toremove); //remove all outdated transactions
		return window;
	}

	//Parses a 20 character long ISO 8601 format date to Java Date object
	private static Date parseDate(String date) { 
		try {
		    return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").parse(date);
		} catch (ParseException e) {
		    return null;
		}             
     }
     
     public class Transaction {	
		 Date created_time;
		 String target, actor;
	  
		 public Transaction(Date created_time, String target, String actor){
			 this.created_time = created_time;
			 this.target = target;
			 this.actor = actor;
		 }    
     }        
}

