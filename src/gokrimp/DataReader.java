/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gokrimp;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import datatools.Keyword;


/**
 * reads data with different formats
 * @author hkosorus
 */
public class DataReader {
    
    public GoSemKrimp readSemData(String dataname){
        GoSemKrimp gk=new GoSemKrimp();
        gk.labels = readLabel(dataname+".lab");
        gk.dictionary = readDictionary(dataname+".dict");
        gk.hypernym_map = readHypernymMap(dataname+".hyp");
        gk.reverse_hypernym_map = generateReverseHypernymMap(gk.hypernym_map);
        gk.data = new ArrayList<ArrayList<Event>>();
        gk.classlabels = new ArrayList<Integer>();
        gk.dataset = dataname;
        try{
            DataInputStream in;
            FileInputStream fstream = new FileInputStream(dataname+".dat");
            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            int size=0;
            while((strLine = br.readLine()) != null){
                String[] temp;
                // Separate class ID from the rest of the sequence (if necessary)
                if (strLine.contains(":")) {
                	temp = strLine.split(":");
                	gk.classlabels.add(size, (new Integer(temp[0])/10));
                	if (temp.length > 0) strLine = temp[1].trim();
                }
                // Parse sequence
                String delimiter = " ";
                temp = strLine.split(delimiter);
                ArrayList<Event> s=new ArrayList<Event>();
                gk.data.add(s);
                int ts=0,prev=0;
                size++;
                for(int i=0;i<temp.length;i++){
                    Event e=new Event();
                    e.id=Integer.parseInt(temp[i]);
                    e.ts=ts;
                    e.gap=ts-prev;
                    prev=ts;
                    gk.data.get(gk.data.size()-1).add(e);
                    ts++;
                }
               
            }
            System.err.println("data size:"+ size);
            in.close();
        }catch (IOException e){
                System.err.println("Error: " + e.getMessage());
        }
        
        return gk;
    }
    
    
    HashMap<Integer,String> readLabel(String dataname){
        HashMap<Integer,String> labels= new HashMap();
        File file = new File(dataname+".lab");
        if(file.exists()){ //the label file with such name does not exist
            return labels;
        }
        try{
            DataInputStream in;
            FileInputStream fstream = new FileInputStream(dataname);
                in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                int k=0;
                while((strLine = br.readLine()) != null){
                	String[] l = strLine.trim().split(" ");
                    labels.put(k, l[0]);
                    k++;
                }
                in.close();
        }catch (IOException e){
                System.err.println("Error: " + e.getMessage());
        }
        return labels;
     }
    
    ArrayList<Keyword> readDictionary(String dataname) {
    	ArrayList<Keyword> dictionary = new ArrayList<Keyword>();
    	try{
    		BufferedReader dict_in = new BufferedReader(new FileReader(dataname));
			String s = dict_in.readLine();
			while (s != null) {
				s = s.trim();
				String[] l = s.split(" ");
				Integer ID = new Integer(l[0]);
				String orig = l[1];
				String baseform = l[2];
				String stem =l[3];
				String hyp = l[4];
				String hypstem = l[5];
				String tag = l[6];
				Keyword key = new Keyword(ID, orig, baseform, stem, hyp, hypstem, tag);
				dictionary.add(key);
				s = dict_in.readLine();
			}
    	}
    	catch(IOException ex) {
    		
    	}
    	return dictionary;
    }
    
    HashMap<Integer, ArrayList<Integer>> readHypernymMap(String dataname) {
    	HashMap<Integer, ArrayList<Integer>> hyp_map = new HashMap<Integer, ArrayList<Integer>>();
    	try{
    		BufferedReader dict_in = new BufferedReader(new FileReader(dataname));
			String s = dict_in.readLine();
			while (s != null) {
				s = s.trim();
				String[] l = s.split(" ");
				Integer hypID = new Integer(l[0]);
				ArrayList<Integer> ids = new ArrayList<Integer>();
				for (int j=1; j<l.length; j++)
					ids.add(new Integer(l[j]));
				hyp_map.put(hypID, ids);
				s = dict_in.readLine();
			}
    	}
    	catch(IOException ex) {
    		
    	}
    	return hyp_map;
    }
    
    HashMap<Integer, Integer> generateReverseHypernymMap(HashMap<Integer, ArrayList<Integer>> hyp_map) {
    	HashMap<Integer, Integer> rev_map = new HashMap<Integer, Integer>();
    	for (Entry<Integer, ArrayList<Integer>> e : hyp_map.entrySet()) {
			Integer hypID = e.getKey();
			for (Integer id : e.getValue())
				rev_map.put(id, hypID);
		}
    	return rev_map;
    }
     
}

