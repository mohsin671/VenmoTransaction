

import java.util.Date;

public class Transaction {	
	Date created_time;
	String target, actor;
	
	public Transaction(Date created_time, String target, String actor){
		this.created_time = created_time;
		this.target = target;
		this.actor = actor;
	}    
}
