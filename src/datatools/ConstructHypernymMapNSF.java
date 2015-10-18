package datatools;

import gokrimp.DataReader;
import gokrimp.DictionaryMapper;
import gokrimp.GoSemKrimp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import at.jku.faw.keywords.Lang;

public class ConstructHypernymMapNSF {

	public static void main(String[] args) {
		
		String dataname = "D:/Datasets/NSF_seq/red_awards_1990_new";
		DataReader d=new DataReader();
		GoSemKrimp gs = d.readSemData(dataname);
		gs.calculateStatistics();
		DictionaryMapper.init("D:/Datasets/NSF_seq/",Lang.EN);
		double SIM_THRESHOLD = 0.7;
    	HashMap<Integer,Integer> reverse_hypernym_map = DictionaryMapper.mapDictionaryIntern(gs, SIM_THRESHOLD);
    	HashMap<Integer,ArrayList<Integer>> hypernym_map = new HashMap<Integer, ArrayList<Integer>>();
    	for (Entry<Integer, Integer> e : reverse_hypernym_map.entrySet()) {
    		if (!hypernym_map.containsKey(e.getValue())) {
    			ArrayList<Integer> list = new ArrayList<Integer>();
    			list.add(e.getKey());
    			hypernym_map.put(e.getValue(), list);
    		}
    		else {
    			hypernym_map.get(e.getValue()).add(e.getKey());
    		}
    	}
    	
      for (Entry<Integer, ArrayList<Integer>> e : hypernym_map.entrySet()) {
      	String line = gs.getDictionary().get(e.getKey()).getBaseform()+" - [ ";
      	for (Integer i : e.getValue())
      		line += gs.getDictionary().get(i).getBaseform()+" ";
      	line += "]";
      	System.out.println(line);
      }
    	
		BufferedWriter bw;
		try{
			bw = new BufferedWriter(new FileWriter(dataname+"_"+SIM_THRESHOLD+".hyp"));
			for (Entry<Integer, ArrayList<Integer>> e : hypernym_map.entrySet()) {
				String s = ""+e.getKey();
				for (Integer id : e.getValue())
					s += " "+id;
				s += "\n";
				bw.write(s);
				bw.flush();
			}
			bw.close();
			
		} catch(IOException ex) {
			
		}
		
		BufferedWriter bw2;
    	try{
    		bw2 = new BufferedWriter(new FileWriter(dataname+"_"+SIM_THRESHOLD+".hyplab"));
    		for (Entry<Integer, ArrayList<Integer>> e : hypernym_map.entrySet()) {
    			String s = ""+gs.getDictionary().get(e.getKey()).getBaseform();
    			for (Integer id : e.getValue())
    				s += " "+gs.getDictionary().get(id).getBaseform();
    			s += "\n";
    			bw2.write(s);
    			bw2.flush();
    		}
    		bw2.close();
    		
    	} catch(IOException ex) {
    		
    	}
	}

}
