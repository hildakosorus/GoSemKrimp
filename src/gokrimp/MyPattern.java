/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gokrimp;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author thoang
 */
public class MyPattern implements Comparable<MyPattern> {
 ArrayList<Integer> ids;
 double ben; // the compression benefit of using this pattern
 int freq; // the number of times this pattern is used
 int g_cost; // the cost of encoding the gaps
 int hyp_cost; // the cost of encoding with hypernyms
 double ic; // the information content of the pattern
 
 /**
  * print the set of patterns
  */
 void print(){
     System.out.print(ids);
     System.out.println(" " + ben);
 }
 
 /**
  * constructor
  */
 MyPattern(){
     ids=new ArrayList<Integer>();
 }
 
 /**
  * copy constructor 
  * @param p 
  */
 MyPattern(MyPattern p){
     ben = p.ben;
     freq = p.freq;
     g_cost = p.g_cost;
     hyp_cost = p.hyp_cost;
     ids = new ArrayList<Integer>(p.ids);
     ic = p.ic;
 }
 
  @Override
  /**
   * compare two patterns by benefits
   */
   public int compareTo(MyPattern o) {
        return (int)(o.ben-this.ben);
   }
  
  public boolean contains(MyPattern p) {
	  return ids.containsAll(p.ids);
  }
  
  @Override
  public String toString() {
	  String print = "([";
	  for (int i=0; i<ids.size(); i++)
		  if (i==ids.size()-1) print += ids.get(i)+"]";
		  else print += ids.get(i)+",";
	  print += ","+ben+","+freq+","+ic+","+g_cost+","+hyp_cost+")";
	  return print;
  }

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MyPattern))
	        return false;
	    if (obj == this)
	        return true;
	
	    MyPattern rhs = (MyPattern) obj;
	    return new EqualsBuilder().
	        // if deriving: appendSuper(super.equals(obj)).
	        append(ids, rhs.ids).
	        isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
	            // if deriving: appendSuper(super.hashCode()).
	            append(ids).
	            toHashCode();
	}

	public ArrayList<Integer> getIds() {
		return ids;
	}

	public double getBen() {
		return ben;
	}

	public int getFreq() {
		return freq;
	}

	public int getG_cost() {
		return g_cost;
	}

	public int getHyp_cost() {
		return hyp_cost;
	}

	public double getIc() {
		return ic;
	}
	
}
