package server;

import java.util.Comparator;
import java.util.HashMap;

public class ValueComparator implements Comparator
{

	  HashMap base;
	  public ValueComparator(HashMap base) 
	  {
	      this.base = base;
	  }

	  public int compare(Object a, Object b) 
	  {

	    if((Long)base.get(a) > (Long)base.get(b)) 	    
	    	return 1;	    
	    else if((Long)base.get(a) == (Long)base.get(b)) 
	    	return 0;	    
	    else 
	    	return -1;	    
	  }

}
