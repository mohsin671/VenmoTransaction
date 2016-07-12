// example of program that calculates the  median degree of a 
// venmo transaction graph

 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 

 
public class median_degree {
	
	final static String currentDirectory = System.getProperty("user.dir");    
	private static DecimalFormat df2 = new DecimalFormat(".##");
	
	public median_degree(){
		
	}
	
    public static void main(String[] args) {
    	Date max_time_processed = new Date(Long.MIN_VALUE);
    	JSONParser jsonParser = new JSONParser();
    	ArrayList<Transaction> window = new ArrayList<Transaction>();
    	Transaction t;
    	int linecounter = 0;
    	
    	String path= currentDirectory + "/venmo_output/output.txt";
		File file = new File(path);
		if (file.exists()) file.delete();
    	
        try {
        	        	            
            FileReader fileReader = new FileReader(currentDirectory + "/venmo_input/venmo-trans.txt");
            BufferedReader br = new BufferedReader(fileReader);
            String line = br.readLine();
            while(line != null) {

                JSONObject jsonObject = (JSONObject) jsonParser.parse(line);

                String datetime = (String) jsonObject.get("created_time");
                if (datetime.length() != 20) continue;
                Date created_time = parseDate(datetime.substring(0, 10) + "-" + datetime.substring(11, 19));            
                String target = (String) jsonObject.get("target");
                String actor = (String) jsonObject.get("actor");
                
                linecounter++;
                
                if (target.length()==0 || actor.length()==0) continue;
                
                t = new Transaction(created_time, target, actor);
                double median = 0.0;
                
                
                if(created_time.compareTo(max_time_processed) == 0 || created_time.compareTo(max_time_processed) > 0){
                    max_time_processed = created_time;
                    window.add(t);
                    window = FilterWindow(window, max_time_processed);
                    median = calculate_median_degree(window, linecounter);                                      
                    //System.out.printf("%.2f\n", median);
                    WriteToFile(median);
                } 
                
                else if (created_time.compareTo(max_time_processed) < 0 ){
                	if(((max_time_processed.getTime()-created_time.getTime())/1000) < 60){
                		window.add(t);
                        window = FilterWindow(window, max_time_processed);
                        median = calculate_median_degree(window, linecounter);                       
                       // System.out.printf("%.2f\n", median);
                        WriteToFile(median);
                	}
                	else{
                		//print previous median degree               		
                		//System.out.printf("%.2f\n", (calculate_median_degree(window)));
                		WriteToFile(calculate_median_degree(window, linecounter));
                	}
                }
                //System.out.println(max_time_processed + " " + actor + " " + target);
                System.out.println(linecounter + " " + window.size());
                
                line = br.readLine();
            }
           
 
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    private static void WriteToFile(double median) {
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

	private static double calculate_median_degree(ArrayList<Transaction> window, int linecounter) {   	
    	int PeopleID = 0;
    	Map<String, Integer> PeopleMap = new HashMap <String, Integer>();
    	
    	for (Transaction t : window) {    		
    		if(!PeopleMap.containsKey(t.actor)){    			
    			PeopleMap.put(t.actor, PeopleID++);
    		}
    		if(!PeopleMap.containsKey(t.target)){    			
    			PeopleMap.put(t.target, PeopleID++);
    		}
    		
    	}
    	
    	boolean [][] PeopleMatrix2D = new boolean[PeopleID][PeopleID];
    	for (Transaction t : window) { 
    		PeopleMatrix2D[PeopleMap.get(t.actor)][PeopleMap.get(t.target)] = true;
    		PeopleMatrix2D[PeopleMap.get(t.target)][PeopleMap.get(t.actor)] = true;
    	}
    	
    	int [] degreelist = new int[PeopleID];
    	for (int i = 0; i < PeopleID; i++){
    		int degree_count = 0;
    		for (int j = 0; j < PeopleID; j++){
    			if (PeopleMatrix2D[i][j] == true) degree_count++;
    		}
    		degreelist[i] = degree_count;
    	}
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

	private static ArrayList<Transaction> FilterWindow(ArrayList<Transaction> window, Date max_time_processed) {
    	ArrayList<Transaction> toremove = new ArrayList<Transaction>();
    	for (Transaction t : window) {
    		if(((max_time_processed.getTime() - t.created_time.getTime())/1000) >= 60) toremove.add(t);
    	}
    	window.removeAll(toremove);
		return window;
	}

	public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss").parse(date);
        } catch (ParseException e) {
            return null;
        }
        
        
     }
     
     public static class Transaction {	
	  Date created_time;
	  String target, actor;
	  
	  public Transaction(Date created_time, String target, String actor){
		  this.created_time = created_time;
		  this.target = target;
		  this.actor = actor;
	}    
    }
    
    
}

