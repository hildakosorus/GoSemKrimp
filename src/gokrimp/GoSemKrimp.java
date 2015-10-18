/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gokrimp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import datatools.DataStatistics;
import datatools.Keyword;

/**
 * implementation of GoSemKrimp and SeqSemKrimp algorithms
 * GoKrimp: direct look for compressing semantic sequential patterns from a database of sequences
 * @author hkosorus
 */
public class GoSemKrimp {
    ArrayList<Integer> characters; //map from characters to its indices in the dictionary
    int[] frequencies; // the frequencies of the labels in the database
    ArrayList<ArrayList<Event>> data;  // a database of sequences
    ArrayList<ArrayList<Event>> original_data;  // a database of sequences
    ArrayList<MyPattern> patterns; // the set of patterns, the dictionary in this implementation
    ArrayList<MyPattern> candidates; // the set of candidates
    HashMap<Integer,String> labels; // event labels
    ArrayList<Keyword> dictionary; // the dictionary of the database (i.e. list of keywords with baseform, hypernym, stem and tag)
    HashMap<Integer,ArrayList<Integer>> related_events; // map from events to related events
    HashMap<Integer,ArrayList<Integer>> hypernym_map; // map from rare events to their hypernym (frequent) event
    HashMap<Integer, Integer> reverse_hypernym_map; // the "inverse" of the hypernym map
    ArrayList<Integer> classlabels; // class labels of each sequence
    DataStatistics stat;
    String dataset;
    
    int Nword; // the number of encoded words   
    double comp_size; // size (in bits) of the compressed data 
    double uncomp_size; // the size (in bits) of the uncompressed data (representation by the Huffman codes)
    static final int NSTART = 1000; // the maximum number of candidate events as starting points for extending to find compressing patterns
    String testStatistics = "sign"; // the test statistics method used (possible values: "sign", "mann", "wilcoxon", "none")
    int N = 25;
    double alpha = 0.01;
    long runtime;
   
    /**
     *  find compression patterns by greedily extending initial candidate events 
     */ 
    void goSemKrimp(String test, int N, double alpha){
    	testStatistics = test;
    	this.N = N;
    	this.alpha = alpha;
    	calculateStatistics();
        initialization();
        long startTime = System.currentTimeMillis();
        ArrayList<MyPattern> ie = get_Initial_Patterns(); // get a set of initial event
        MyPattern maxp=new MyPattern(); double max;
        int no = 0;
        while(true){
            max = Double.NEGATIVE_INFINITY;
            for(int i=0; i<ie.size(); i++) {
                MyPattern mp = ie.get(i), prev = mp;
                while ((mp = extend(mp)) != null){
                   prev = mp;
                }
                if (prev.ben > max) {
                    maxp = prev;
                    max = prev.ben;
                }
            }
            if (max <= 0)
                break;
            else {
                addPattern(maxp);
                System.out.print(no+" "); no++;
                printMyPattern(maxp);
                remove(maxp);                 
                //printData();
                //printPattern();
            }            
        }
        System.out.println("Compressed size: "+comp_size+", uncompressed size: "+uncomp_size+", compression ratio: "+uncomp_size/(0.0+comp_size));
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        runtime = totalTime/1000;
        System.out.println("Running time: " + runtime + " seconds");
    }
    
    void goSemKrimp(){
    	calculateStatistics();
        initialization();
        long startTime = System.currentTimeMillis();
        ArrayList<MyPattern> ie = get_Initial_Patterns(); // get a set of initial event
        MyPattern maxp=new MyPattern(); double max;
        int no = 0;
        while(true){
            max = Double.NEGATIVE_INFINITY;
            for(int i=0; i<ie.size(); i++) {
                MyPattern mp = ie.get(i), prev = mp;
                while ((mp = extend(mp)) != null){
                   prev = mp;
                }
                if (prev.ben > max) {
                    maxp = prev;
                    max = prev.ben;
                }
            }
            if (max <= 0)
                break;
            else {
                addPattern(maxp);
                System.out.print(no+" "); no++;
                printMyPattern(maxp);
                remove(maxp);                 
                //printData();
                //printPattern();
            }            
        }
        System.out.println("Compressed size: "+comp_size+", uncompressed size: "+uncomp_size+", compression ratio: "+uncomp_size/(0.0+comp_size));
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        runtime = totalTime/1000;
        System.out.println("Running time: " + runtime + " seconds");
    }
    
    /**
     * candidate based algorithm, search for the best encoding of the data given the set of candidates
     */
    void seqkrimp(){
        
        MyPattern maxp=new MyPattern(); 
        double max;
        int no = 0;
        long startTime = System.currentTimeMillis();
        while(true) {
            max=Double.NEGATIVE_INFINITY;
            int mi=getBestPattern();
            if(candidates.get(mi).ben>max){
                    maxp=new MyPattern(candidates.get(mi));
                    max=candidates.get(mi).ben;
            }
            if(max<=0)
                break;
            else{
                addPattern(maxp);
                System.out.print(no+" "); no++;
                printMyPattern(maxp);
                remove(maxp);   
                //printData();
                //printPattern();
            } 
            // remove the best candidate from the candidate lists 
            candidates.remove(mi);  
            for(int i=0;i<candidates.size();i++){
                candidates.get(i).ben=0;
                candidates.get(i).freq=0;
                candidates.get(i).g_cost=0;             
            }
        }
        System.out.println("Compressed size: "+comp_size+", uncompressed size: "+uncomp_size+", compression ratio: "+uncomp_size/(0.0+comp_size));
        System.out.println("Compressed size: "+comp_size+", uncompressed size: "+uncomp_size+", compression ratio: "+uncomp_size/(0.0+comp_size));
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        runtime = totalTime/1000;
        System.out.println("Running time: " + runtime + " seconds");
    }
    
    /**
     * Initialization
     */
    void initialization(){
        patterns=new ArrayList<MyPattern>();
        candidates=new ArrayList<MyPattern>();
        related_events=new HashMap<Integer, ArrayList<Integer>>();
        comp_size = 0;
        uncomp_size = 0;
        characters=new ArrayList<Integer>();
        int[] chars = new int[labels.size()];
        
        for(int i=0; i<labels.size(); i++) {
        	int freq = frequencies[i] + 1; //plus 1 because counting also two occurrences of the singleton in the dictionary
        	uncomp_size += freq * (Math.log(Nword)/Math.log(2)- Math.log(freq)/Math.log(2));
        	if (!reverse_hypernym_map.containsKey(i)) { // only if the singleton is a "representative" event, a hypernym in the hypernym map
        		MyPattern mp=new MyPattern();
        		mp.ids.add(i);
		    	mp.ben = 0;
		    	mp.freq = freq;
		    	//mp.ic = ic[i];
		    	mp.g_cost = 0;
				patterns.add(mp); //add the given singleton to the dictionary
				chars[i] = patterns.size() - 1; // the characters map now contains event id and its index in the dictionary
        	}
        }
        
        for(int i=0; i<labels.size(); i++) {
        	if (reverse_hypernym_map.containsKey(i)) {
        		int hyp_id = reverse_hypernym_map.get(i);
        		int idx = chars[hyp_id];
        		patterns.get(idx).freq += frequencies[i];
        		chars[i] = idx;
        		Nword -= 1;
        	}
        	characters.add(chars[i]);
        }
        
        for(int i=0; i<labels.size(); i++) {
        	if (!reverse_hypernym_map.containsKey(i)) {
        		int cost = 0;
        		int freq = frequencies[i] + 1;
        		if (hypernym_map.containsKey(i)) {
        			ArrayList<Integer> list = hypernym_map.get(i);
        			for (int j=0; j<list.size(); j++) {
        				int f = frequencies[list.get(j)];
        				freq += f;
        				cost += f*bits(j+1);
        			}
        		}
        		patterns.get(chars[i]).hyp_cost = cost;
        		comp_size += freq * (Math.log(Nword)/Math.log(2)- Math.log(freq)/Math.log(2)) + cost;
        	}
        }
        
        //remove occurrences of rare events in the data, rare events are the ones having frequency less than SignTest.N (25 by default)
        for(int i=0;i<data.size();i++){
            for (Iterator<Event> it = data.get(i).iterator(); it.hasNext(); ) {
            	int id = it.next().id;
                if (frequencies[id] < stat.FreqLowerThreshold || frequencies[id] > stat.FreqUpperThreshold){
                	it.remove();
                }   
            }
        }
        original_data = new ArrayList<ArrayList<Event>>();
        for (int i=0;i<data.size();i++) {
        	ArrayList<Event> list = new ArrayList<Event>();
        	for (int j=0;j<data.get(i).size();j++) {
        		Event e = new Event(data.get(i).get(j));
        		list.add(e);
        	}
        	original_data.add(list);        
        }            
    }
    
    public void calculateStatistics() {
    	stat = new DataStatistics();
    	stat.NSequences = data.size();
    	stat.NLabels = labels.size();
    	stat.AvgSeqLength = 0.0;
    	stat.MaxSeqLength = 0;
    	stat.MinSeqLength = Integer.MAX_VALUE;
    	frequencies = new int[labels.size()];
    	Nword = 0;
    	for (ArrayList<Event> list : data) {
    		stat.AvgSeqLength += list.size();
    		if (list.size() < stat.MinSeqLength) stat.MinSeqLength = list.size();
    		if (list.size() > stat.MaxSeqLength) stat.MaxSeqLength = list.size();
    		for (Event e:list)
    			frequencies[e.id]++;
    		Nword += list.size();
    	}
//    	ic = new double[labels.size()];
//    	for (int i=0; i<frequencies.length; i++) {
//    		ic[i] = (Math.log(Nword) - Math.log(frequencies[i])) / Math.log(2);
//    	}
    	Nword += labels.size();
    	stat.AvgSeqLength /= data.size();
    	stat.MinLabFreq = Integer.MAX_VALUE;
    	stat.MaxLabFreq = 0;
		for (int freq : frequencies) {
			stat.AvgLabFreq += freq;
			if (stat.MaxLabFreq < freq) stat.MaxLabFreq = freq;
			if (stat.MinLabFreq > freq) stat.MinLabFreq = freq;
		}
		stat.AvgLabFreq = stat.AvgLabFreq / stat.NLabels;
		
		stat.StdDevFreq = 0.0;
		for (int freq : frequencies)
			stat.StdDevFreq += Math.pow(freq-stat.AvgLabFreq,2);
		stat.StdDevFreq = Math.sqrt(stat.StdDevFreq/stat.NLabels);
		
		stat.FreqLowerThreshold = SignTest.N;//stat.MinLabFreq + (int)Math.round(stat.StdDevFreq);
		stat.FreqUpperThreshold = stat.MaxLabFreq;//stat.MaxLabFreq - (int)Math.round(stat.StdDevFreq);
    }
    
    /**
     * add a new pattern to the dictionary
     * @param pattern 
     */
    void addPattern(MyPattern pattern){
        Nword = Nword - (pattern.freq-1)*pattern.ids.size() + pattern.freq + 1; //update the number of encoded words
        comp_size -= pattern.ben; //update the compression size
        HashMap<Integer,Integer> hm=new HashMap<Integer, Integer>(); // hm contains event id and the number of times the event occurs in the pattern.ids
        for(int j=0;j<pattern.ids.size();j++){
                if(!hm.containsKey(pattern.ids.get(j))){
                    hm.put(pattern.ids.get(j), 1);
                } else{
                    hm.put(pattern.ids.get(j), hm.get(pattern.ids.get(j))+1);
                }
        }
        for(int i=0;i<patterns.size();i++){ //update the frequency of the existing patterns 
            if(patterns.get(i).ids.size()==1 && hm.containsKey(patterns.get(i).ids.get(0))) { //singleton among the events of the pattern
                patterns.get(i).freq -= (pattern.freq-1)*hm.get(patterns.get(i).ids.get(0));
                patterns.get(i).freq += hm.get(patterns.get(i).ids.get(0)); 
            }            
        }
        patterns.add(pattern); // add the new pattern to the dictionary
    }    
    
   /**
    * extend the current pattern
    * @param pattern the pattern to be extended
    * @return null if no extension gives additional compression benefit or the extended pattern if otherwise
    */
    MyPattern extend(MyPattern pattern){
       ArrayList<Integer> ve;
       if (testStatistics.equals("none"))
    	   ve = get_Extending_Events(); // get the set of extending events
       else 
    	   ve = get_Extending_Events_Test(pattern.ids.get(pattern.ids.size()-1)); // get the set of extending events
       candidates.clear();
       //append the set of extending event to the pattern to create new candidates
       for(int i=0;i<ve.size();i++) {
           int id = ve.get(i);
           MyPattern can=new MyPattern(); //create a new candidate
           can.g_cost=0;
           can.freq=0;
           can.ben=0;
           can.hyp_cost = 0;
           can.ids=new ArrayList<Integer>(pattern.ids);
           can.ids.add(id);
           //can.ic = avgIC(can.ids);
           candidates.add(can);
       }
       if(candidates.isEmpty())
           return null;
       int best = getBestPattern(); //get the index of the best candidate
       if (candidates.get(best).ben > pattern.ben)
            return candidates.get(best);
       else 
           return null;
    }
    
    /**
     * get the set of initial patterns
     * @return return a set of initial patterns
     */
    ArrayList<MyPattern> get_Initial_Patterns(){
        ArrayList<MyPattern> ie=new ArrayList<MyPattern>();
        for(int i=0;i<patterns.size();i++){
            if (patterns.get(i).freq >= stat.FreqLowerThreshold && patterns.get(i).freq <= stat.FreqUpperThreshold) { //only consider unrare but also not too common events
                 ie.add(new MyPattern(patterns.get(i)));
            }
        }
        for(int i=0;i<ie.size();i++){
            ie.get(i).ben = ie.get(i).freq;
        }
        Collections.sort(ie);
        while (ie.size()>NSTART) {
            ie.remove(ie.size()-1);
        }
        for(int i=0;i<ie.size();i++){
            ie.get(i).ben = 0;
        }
        /*for(int i=0;i<patterns.size();i++)
            System.out.println("a "+patterns.get(i).ben+" "+patterns.get(i).ids+ie.get(i).ids);*/
        return ie;
    }
    
   
    /**
     * get the set of events being considered to extend a pattern
     * @return the set of events being considered to extend a pattern
     */
    ArrayList<Integer> get_Extending_Events(){
        ArrayList<Integer> ve=new ArrayList<Integer>();
        for (int i=0;i<patterns.size();i++){
            if (patterns.get(i).ids.size() == 1)
                ve.add(patterns.get(i).ids.get(0));
        }
        return ve;
    }
    
     /**
     * get the set of events being considered to extend a pattern, Signed Test is used to select such events
     * @return the set of events being considered to extend a pattern
     */
    ArrayList<Integer> get_Extending_Events_Test(Integer e){
       if(related_events.containsKey(e))
           return related_events.get(e);
       ArrayList<Integer> ve = new ArrayList<Integer>();
       if (testStatistics.equals("sign")) {
    	   SignTest.N = N;
    	   SignTest.alpha = alpha;
    	   ve = getRelatedEventsWithSignTest(e);
       }
       related_events.put(e, ve);
       return ve;
    }
   
    /**
     * get the best pattern among the set of candidates
     * @return index of the best pattern in the candidates ArrayList
     */
    int getBestPattern(){
       int index=0; double min=Double.POSITIVE_INFINITY;
       for (int i=0; i<candidates.size(); i++) {//for every candidate
           //get all the best matches of the candidate in every sequence
    	   MyPattern candidate = candidates.get(i);
           for (int j=0; j<data.size(); j++) {
        	   ArrayList<Event> sequence = data.get(j);
               HashMap<Integer,ArrayList<Integer>> hm=new HashMap<Integer, ArrayList<Integer>>();
               ArrayList<ArrayList<Integer>> pos = new ArrayList<ArrayList<Integer>>();
               ArrayList<ArrayList<Integer>> hyp_pos = new ArrayList<ArrayList<Integer>>();
               for(int k=0; k<candidate.ids.size(); k++){
                 if(!hm.containsKey(candidate.ids.get(k))){
                     ArrayList<Integer> a=new ArrayList<Integer>();
                     a.add(k);
                     hm.put(candidate.ids.get(k),a);
                 } else{
                      ArrayList<Integer> a=hm.get(candidate.ids.get(k));
                      a.add(k);
                      hm.put(candidate.ids.get(k),a);
                 }
                 pos.add(new ArrayList<Integer>());
                 hyp_pos.add(new ArrayList<Integer>());
               }
               for(int k=0; k<sequence.size(); k++) {
            	   Event e = sequence.get(k);
            	   int id = e.id;
            	   int h_pos = 0;
            	   if (reverse_hypernym_map.containsKey(id)) {
            		   id = reverse_hypernym_map.get(id);
            		   h_pos = hypernym_map.get(id).indexOf(e.id)+1;
            	   }
                   if (hm.containsKey(id)) {
                       for(int l=0; l<hm.get(id).size(); l++) {
                    	   pos.get(hm.get(id).get(l)).add(e.ts);
                    	   hyp_pos.get(hm.get(id).get(l)).add(h_pos);
                       }
                   }
               }
               HashMap<ArrayList<Integer>,Integer> matches = getBestMatches(pos,hyp_pos);
               if (matches.size() > 0) {
            	   candidate.freq += matches.size();
            	   candidate.g_cost += gap_cost(matches.keySet());
            	   candidate.hyp_cost += hyp_cost(new ArrayList<Integer>(matches.values()));
               }
           }
           if (candidate.freq == 0) //skip the candidate that has no occurrence in the data
               continue;
           candidate.freq += 1;//plus one because we also count its occurrence in the dictionary
           //candidate.ic = avgIC(candidate.ids);
           double com = get_Compress_Size_When_Adding(candidates.get(i));
           if (com < min){
               min = com;
               index = i;
           }           
       }
       candidates.get(index).ben = comp_size - min;
       return index;
    }    
    
    /**
     * remove all the best matches of the pattern in the data
     * @param pattern 
     */
    void remove(MyPattern pattern){
        for(int j=0;j<data.size();j++){
               HashMap<Integer,ArrayList<Integer>> hm=new HashMap<Integer, ArrayList<Integer>>();
               ArrayList<ArrayList<Integer>> pos=new ArrayList<ArrayList<Integer>>();
               ArrayList<ArrayList<Integer>> hyp_pos=new ArrayList<ArrayList<Integer>>();
               for(int k=0;k<pattern.ids.size();k++){
                 if(!hm.containsKey(pattern.ids.get(k))){
                     ArrayList<Integer> a=new ArrayList<Integer>();
                     a.add(k);
                     hm.put(pattern.ids.get(k),a);
                 } else{
                      ArrayList<Integer> a=hm.get(pattern.ids.get(k));
                      a.add(k);
                      hm.put(pattern.ids.get(k),a);
                 }
                 pos.add(new ArrayList<Integer>());
                 hyp_pos.add(new ArrayList<Integer>());
               }
               ArrayList<Event> sequence = data.get(j);
               for (int k=0; k<sequence.size(); k++) {
            	   Event e = sequence.get(k);
            	   int id = e.id;
            	   int h_cost = 0;
            	   if (reverse_hypernym_map.containsKey(id)) {
            		   id = reverse_hypernym_map.get(id);
            		   h_cost = hypernym_map.get(id).indexOf(e.id)+1;
            	   }
                   if (hm.containsKey(id)) {
                       for(int l=0; l<hm.get(id).size(); l++) {
                    	   pos.get(hm.get(id).get(l)).add(e.ts);
                    	   hyp_pos.get(hm.get(id).get(l)).add(h_cost);
                       }
                   }
               }
       
               HashMap<ArrayList<Integer>,Integer> matches = getBestMatches(pos,hyp_pos);    
               remove(matches.keySet(),j);
         }
    }
    /**
     * get the compress size of the data when the given @param pattern is added to the current dictionary
     * @param pattern
     * @return 
     */
    double get_Compress_Size_When_Adding(MyPattern pattern){
       // System.out.println(pattern.ids);
        int new_Nword = Nword - (pattern.freq-1)*pattern.ids.size() + pattern.freq + 1;
        double com = comp_size; //
        com += new_Nword*Math.log(new_Nword)/Math.log(2) - Nword*Math.log(Nword)/Math.log(2) - pattern.freq*Math.log(pattern.freq)/Math.log(2);
        HashMap<Integer,Integer> hm=new HashMap<Integer, Integer>(); // hm contains event id and the number of times the event occurs in the pattern.ids
        for (int i=0; i<pattern.ids.size(); i++) {
            if (!hm.containsKey(pattern.ids.get(i))) {
                hm.put(pattern.ids.get(i), 1);
            } else {
                hm.put(pattern.ids.get(i), hm.get(pattern.ids.get(i))+1);
            }
        }
        for(Integer key:hm.keySet()) {
            int new_freq = patterns.get(characters.get(key)).freq - hm.get(key)*pattern.freq + hm.get(key);
            int old_freq = patterns.get(characters.get(key)).freq;
            com -= new_freq*Math.log(new_freq)/Math.log(2) - old_freq*Math.log(old_freq)/Math.log(2);
        }
        com += pattern.g_cost + pattern.hyp_cost;
        return com;
    }
    
    /**
     * return the best matches of a pattern with positions stored in the @param pos
     * @param pos
     * @return 
     */
    HashMap<ArrayList<Integer>,Integer> getBestMatches(ArrayList<ArrayList<Integer>> pos, ArrayList<ArrayList<Integer>> hyp_pos){
        HashMap<ArrayList<Integer>,Integer> matches=new HashMap<ArrayList<Integer>,Integer>();
        while(true){
            ArrayList<ArrayList<Event> > matrix=new ArrayList<ArrayList<Event>>();
            for (int i=0; i<pos.size(); i++) {
                matrix.add(new ArrayList<Event>());
                if (pos.get(i).size() == 0 || (i < pos.size()-1 && min(pos.get(i)) > max(pos.get(i+1)))) return matches;
            }
            for (int i=0; i<pos.size(); i++) {
            	if(i==0) {
					for(int j=0; j<pos.get(0).size(); j++) {
						Event ww=new Event();
						ww.ts=0;
						ww.id=pos.get(0).get(j);
						ww.gap=0;
						ww.hyp_cost = bits(hyp_pos.get(0).get(j));
						matrix.get(0).add(ww);
					}
				} else {
					for (int j=0; j<pos.get(i).size(); j++) {
		                int index = 0, min = Integer.MAX_VALUE, mini = 0;
		                int min_cost = Integer.MAX_VALUE;
		                int current = pos.get(i).get(j);
						while (index < matrix.get(i-1).size() && matrix.get(i-1).get(index).id<current) {
							Event prev =  matrix.get(i-1).get(index);
							if (prev.ts==Integer.MAX_VALUE){
								index++;
								continue;
							}
							int g = prev.ts+bits(current-prev.id);
							int cost = prev.hyp_cost+bits(hyp_pos.get(i).get(j));
							if (g < min) {
								min = g;
								min_cost = cost;
								mini = index;
							}
							else if (min == g && cost < min_cost) {
								min_cost = cost;
								mini = index;
							}
							index++;
						}
						Event ww=new Event();
						ww.ts = min;
		                ww.id = pos.get(i).get(j);
						ww.gap = mini;
						ww.hyp_cost = min_cost;
						matrix.get(i).add(ww);				
					}
				}
            }
	
            int min = Integer.MAX_VALUE;
            int mini = 0;
            int min_cost = Integer.MAX_VALUE;
            ArrayList<Event> last = matrix.get(matrix.size()-1);
            for (int i=0; i<last.size(); i++) {
				if (min > last.get(i).ts) {
					min = last.get(i).ts;
					min_cost = last.get(i).hyp_cost;
					mini = i;
				}
				else if (min == last.get(i).ts){
					if (min_cost > last.get(i).hyp_cost) {
						min_cost = last.get(i).hyp_cost;
						mini = i;
					}
				}
            }
            
            if (min == Integer.MAX_VALUE)
                   break;
            ArrayList<Integer> match=new ArrayList<Integer>();
            HashMap<Integer,Integer> hm=new HashMap<Integer, Integer>();
            int match_hyp_cost = matrix.get(matrix.size()-1).get(mini).hyp_cost;
            
            //trace back to get the best match
            for (int i=matrix.size()-1;i>=0;i--) {
            	Event e = matrix.get(i).get(mini);
            	match.add(0,e.id);
                hm.put(e.id, 1); 
                mini = e.gap;
            }
            
            matches.put(match,match_hyp_cost);
            
            for (int i=0; i<pos.size(); i++) {
                for (Iterator<Integer> it = pos.get(i).iterator(); it.hasNext(); ) {
                	if (hm.containsKey(it.next())) {
                		it.remove();
                    }
                }  
                if (pos.get(i).isEmpty())
                     return matches;
            }
        }
        return matches;
    }
    
    /**
     * 
     * @param matches
     * @return the cost of encoding the gaps of the matches
     */
    int gap_cost(Set<ArrayList<Integer>> matches){
      int g = 0;
      for(ArrayList<Integer> match : matches){
          for(int j=1;j<match.size();j++)
              g += bits(match.get(j)-match.get(j-1));
      }
      return g;
    }
    
    int minimum(ArrayList<Integer> list, int after) {
    	int min = Integer.MAX_VALUE;
    	for (int i=0; i<list.size(); i++) {
    		int e = list.get(i);
    		if (e > after && e < min) {
    			min = e;
    		}
    	}
    	return min;
    }
    
    int max(ArrayList<Integer> list) {
    	int max = Integer.MIN_VALUE;
    	for (int i=0; i<list.size(); i++) {
    		int e = list.get(i);
    		if (e > max) {
    			max = e;
    		}
    	}
    	return max;
    }
    
    int min(ArrayList<Integer> list) {
    	int min = Integer.MAX_VALUE;
    	for (int i=0; i<list.size(); i++) {
    		int e = list.get(i);
    		if (e < min) {
    			min = e;
    		}
    	}
    	return min;
    }
    
    /**
     * 
     * @param hyp_costs
     * @return the cost of encoding the hypernym list positions of the matches
     */
    int hyp_cost(ArrayList<Integer> hyp_costs){
      int cost=0;
      for (Integer c : hyp_costs){
          cost += c;
      }
      return cost;
    }
    
//    double avgIC(ArrayList<Integer> ids) {
//    	double avg = 0.0;
//    	for (Integer i : ids)
//    		avg += ic[i];
//    	avg /= ids.size();
//    	return avg;
//    }
    
    /**
     * remove all the matches in the sequence with identifier index
     * @param matches
     * @param index the identifier of the sequence 
     */
    void remove(Set<ArrayList<Integer>> matches, int index){
      HashMap<Integer,Integer> hm=new HashMap<Integer, Integer>();
      for(ArrayList<Integer> match : matches){
          for(int j=0;j<match.size();j++){
             hm.put(match.get(j), 1); 
          }
      } 
      for (Iterator<Event> it = data.get(index).iterator(); it.hasNext(); ) {
            if(hm.containsKey(it.next().ts)){
                it.remove();
            }
      }   
    }
    
                 
    /**
     *  get related event of the event @param e
     * @param e 
     * @return  the set of related events
     */
    ArrayList<Integer> getRelatedEventsWithSignTest(Integer e) {
        HashMap<Integer,SignTest> me = new HashMap<Integer, SignTest>();//statistics
        HashMap<Integer,Integer> mc = new HashMap<Integer, Integer>();//counter
        ArrayList<Integer> nextdata = new ArrayList<Integer>();
        for (int i=0;i<data.size();i++) {
            int next=data.get(i).size();
            for (int j=0; j<data.get(i).size(); j++) {
            	int event = data.get(i).get(j).id;
                if (event==e.intValue()){
                    next=j;
                    break;
                }
            }
            next++;
            nextdata.add(next);
        }
        for(int i=0;i<data.size();i++){
            mc.clear();
            if(nextdata.get(i)>=data.get(i).size())
                continue;
            double middle = data.get(i).get(nextdata.get(i)).ts;
            middle = middle + (data.get(i).get(data.get(i).size()-1).ts-middle)/2;
            for (int j = nextdata.get(i).intValue(); j<data.get(i).size(); j++) {
            	Event event = data.get(i).get(j);
            	int id = event.id;
            	if (reverse_hypernym_map.containsKey(id)) id = reverse_hypernym_map.get(id);
            	//if (id != e) {
	                if (event.ts <= middle) { // in the first half
	                    if(!mc.containsKey(id)){ // the event has been seen for the first time
	                       mc.put(id, new Integer (1));
	                    } else { // the event has been already seen before
	                       mc.put(id, new Integer(mc.get(id)+1)); 
	                    }
	                } else { // in the second half
	                     if(!mc.containsKey(id)){ // the event has been seen for the first time
	                       mc.put(id, new Integer (-1));
	                    } else { // the event has been already seen before
	                       mc.put(id, new Integer(mc.get(id)-1)); 
	                    }
	                }
            	//}
            }
            for (Integer key : mc.keySet()) {
	                if (!me.containsKey(key)){ // see for the first time
	                    SignTest st = new SignTest(1,0);
	                    if (mc.get(key).intValue()>0)
	                        st.Nplus=st.Nplus+1;
	                    me.put(key, st);
	                } else { // have been already seen
	                    SignTest st;
	                    if (mc.get(key).intValue()!=0)
	                        st = new SignTest(me.get(key).Npairs+1,me.get(key).Nplus);
	                    else
	                        st = new SignTest(me.get(key).Npairs,me.get(key).Nplus);
	                    if (mc.get(key).intValue()>0)
	                        st.Nplus = st.Nplus+1;
	                    me.put(key, st);
	                }
            }
        }
        ArrayList<Integer> results =new ArrayList<Integer>();
        for (Integer key:me.keySet()) {
           if (me.get(key).sign_test()) {//pass the sign-test
                results.add(key);
           }
        }      
        return results;
    }
  
    /**
     * 
     * @param a
     * @return the number of bits in the binary representation of an integer a using the Elias code
     */
    int bits(Integer a){
        if(a.intValue()<=0)
            return 0;
        else{
            double x=Math.log(a)/Math.log(2);
            //return lowround(x)+2*lowround(Math.log(lowround(x)+1)/Math.log(2))+1; //ellias delta
            //return lowround(x);
            return 2*lowround(x)+1; //elias gamma code
        }
    }
    
   /**
    * 
    * @param x
    * @return the lower round value of x 
    */
    int lowround(double x){
       int y=(int) Math.round(x);
       if(y>x)
           y=y-1;
       return y;
    }
    
    /**
     * check if pattern p is occurred in the sequence index 
     * @param index
     * @return 
     */
    boolean isOccurred(MyPattern p, int index){
        int d=0;
        for(int i=0;i<original_data.get(index).size()&&d<p.ids.size();i++) {
        	int id = original_data.get(index).get(i).id;
        	if (reverse_hypernym_map.containsKey(id)) 
        		id = reverse_hypernym_map.get(id);
            if (p.ids.get(d)==id){
                d++;                
            }
        }
        if(d==p.ids.size())
            return true;
        else
            return false;
    }
    
    /**
     * print all patterns having positive compression benefit 
     */
    void printPattern(){
        Collections.sort(patterns); //sort patterns decreasingly by the compresion benefit
        int np=0;
        for(int i=0;i<patterns.size();i++){
           if(patterns.get(i).ben<=0)
               break;
           np++;
           if(labels.isEmpty()){
               System.out.print("[ ");
               for(int j=0;j<patterns.get(i).ids.size();j++)
                   System.out.print(patterns.get(i).ids.get(j)+" ");
               System.out.println("] "+patterns.get(i).g_cost+" "+patterns.get(i).freq+" "+patterns.get(i).ben);
           } else {
               System.out.print("[ "); 
               for(int j=0;j<patterns.get(i).ids.size();j++)
                   System.out.print(labels.get(patterns.get(i).ids.get(j))+" ");
               System.out.println("] "+patterns.get(i).g_cost+" "+patterns.get(i).freq+" "+patterns.get(i).ben);
           }          
        }
        System.out.println("# encoded words: "+Nword+ ", # patterns: "+np);
    }
    
    /**
     * print the sequence database
     */
    void printData(){
       System.out.println("o--------------------------------o");
       for(int i=0;i<data.size();i++){
               for(int j=0;j<data.get(i).size();j++)
                   System.out.print("("+data.get(i).get(j).id+","+data.get(i).get(j).ts +") ");
                System.out.println();
       }
       System.out.println("o--------------------------------o");
   }
   
    /**
     * print a pattern
     * @param pattern 
     */
   void printMyPattern(MyPattern pattern) {
        if(labels==null||labels.isEmpty()){
               System.out.print("[ ");
               for(int j=0;j<pattern.ids.size();j++)
                   System.out.print(pattern.ids.get(j)+" ");
               System.out.println("] "+pattern.ben);
        } else {
               System.out.print("[ "); 
               for(int j=0;j<pattern.ids.size();j++)
                   System.out.print(labels.get(pattern.ids.get(j))+" ");
               System.out.println("] "+pattern.ben);
        }     
   }
   
   void printResultToFile(String dataname) {
	   try {
		   String a = ""+alpha;
	       a = a.substring(a.indexOf(".")+1, a.length());
		   BufferedWriter bw = new BufferedWriter(new FileWriter(dataname + ".txt"));
		   for (MyPattern pattern : patterns)
			   if (pattern.ids.size() > 1) {
			              bw.write("[ ");
			              for(int j=0;j<pattern.ids.size();j++)
			            	  bw.write(labels.get(pattern.ids.get(j))+" ");
			              bw.write("] "+pattern.ben+"\n");
			   }
		   bw.write("Compressed size: "+comp_size+", uncompressed size: "+uncomp_size+", compression ratio: "+uncomp_size/(0.0+comp_size)+"\n");
		   bw.write("Running time: " + runtime + " seconds \n");
		   bw.close();
	   } catch(IOException ex) {
		   
	   }
  }
   
   /**
    * print binary representation of the data in Weka type
    * @param dataname 
    */
   void PrintPatternWeka(String dataname){
       try{
          HashSet<Integer> classes = new HashSet<Integer>();
          for(int i=0;i<classlabels.size();i++){
              classes.add(classlabels.get(i));
          }
          // Create file 
          FileWriter fstream = new FileWriter(dataname+".arff");
          PrintWriter out = new PrintWriter(fstream);
              out.println("@RELATION "+dataname);
              for(int i=0;i<patterns.size();i++){
                  if(patterns.get(i).ids.size()==1)
                      out.println("@ATTRIBUTE s"+ i+  " NUMERIC");
                  else
                      out.println("@ATTRIBUTE m"+ i+  " NUMERIC");
              }
              String s="@ATTRIBUTE class {";
              Iterator<Integer> iter = classes.iterator();
              for (int i=0; i < classes.size() && iter.hasNext(); i++) {
            	  if (i==0) 
            		  s += iter.next();
            	  else
            		  s += "," + iter.next();
              }
              s+="}";
              out.println(s);
              out.println("@DATA");
              for(int i=0;i<original_data.size();i++){
                  out.print("{");
                  for(int j=0;j<patterns.size();j++){
                      if(isOccurred(patterns.get(j),i))
                         out.print(j+" 1,");                        
                  }
                  out.println(patterns.size()+" "+classlabels.get(i)+"}");
              }
              out.close();
          }catch (Exception e){//Catch exception if any
              System.err.println("Error: " + e.getMessage());
          }
   }
   
    
    
    /**
     * copy data
     */
    ArrayList<ArrayList<Event>> copy(){
        ArrayList<ArrayList<Event>> cd=new ArrayList<ArrayList<Event>>();
        for(int i=0;i<data.size();i++){
            ArrayList<Event> s=new ArrayList<Event>();
            for(int j=0;j<data.get(i).size();j++){
                Event e=new Event();
                e.gap=data.get(i).get(j).gap;
                e.id=data.get(i).get(j).id;
                e.ts=data.get(i).get(j).ts;
                s.add(e);
            }
            cd.add(s);
        }
        return cd;
    }

	public ArrayList<Keyword> getDictionary() {
		return dictionary;
	}

	public void setDictionary(ArrayList<Keyword> dictionary) {
		this.dictionary = dictionary;
	}

	public HashMap<Integer, ArrayList<Integer>> getHypernym_map() {
		return hypernym_map;
	}

	public void setHypernym_map(HashMap<Integer, ArrayList<Integer>> hypernym_map) {
		this.hypernym_map = hypernym_map;
	}
}
