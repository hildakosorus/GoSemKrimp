/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gokrimp;

/**
 * event class
 * @author thoang
 */
class Event implements Comparable<Event> {
    int id; // id of the event
    int ts; // timestamp
    int gap; //gap to previous timestamp
    int hyp_cost; // position in the list of its hypernym (if this is the case)
    
    public Event() {
    	
    }
    
    public Event(Event e) {
    	this.id = e.id;
    	this.ts = e.ts;
    	this.gap = e.gap;
    	this.hyp_cost = e.hyp_cost;
    }
    
    @Override
    /**
     * compare two events by timestamp
     */
    public int compareTo(Event o) {
        return this.ts - o.ts ;
    }

	@Override
	public String toString() {
		return "(#"+id+","+ts+","+gap+","+hyp_cost+")";
	}
	
}

