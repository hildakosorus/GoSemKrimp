package gokrimp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import at.jku.faw.keywords.Lang;
import similarity.WordnetSimilarity;
import datatools.Keyword;

public class DictionaryMapper {
	
	private static WordnetSimilarity wsim = WordnetSimilarity. getInstance();
	public static HashSet<String> commonWords;
	
	public static void init(String path, Lang lang) {
		BufferedReader br;
		try{
			br = new BufferedReader(new FileReader(path+"common_words_"+lang.name()+".txt"));
			String s = br.readLine();
			commonWords = new HashSet<String>();
			while (s!=null) {
				commonWords.add(s);
				s = br.readLine();
			}
			br.close();
		}
		catch(IOException ex) {
		}
		wsim.init("C:\\Program Files (x86)\\WordNet", "2.1");
	}
	
	public static HashMap<Integer, Integer> mapDictionaryIntern(GoSemKrimp krimp, double SIM_THRESHOLD) {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		int FREQ_THRESHOLD = (int)Math.round(krimp.stat.AvgLabFreq)+(int)Math.round(krimp.stat.StdDevFreq);
		HashSet<Integer> hypSet = new HashSet<Integer>();
		for (int i = 0; i < krimp.dictionary.size(); i++) {
    		Keyword key = krimp.dictionary.get(i);
    		String hyp = key.getHypernym();
    		int idx = i;
    		if (!key.getBaseform().equals(hyp) && 
    				!hypSet.contains(i) && 
    				krimp.frequencies[i] < FREQ_THRESHOLD && 
    				!commonWords.contains(key.getBaseform()+" "+key.getTag()) &&
    				wsim.getLinSimilarity(key.getBaseform(), hyp, key.getTag()) >= SIM_THRESHOLD) {
    			for (int j=0; j<krimp.dictionary.size(); j++) {
    				Keyword hypkey = krimp.dictionary.get(j);
					if (i != j && !map.keySet().contains(j) && 
							(krimp.frequencies[j] >= FREQ_THRESHOLD || commonWords.contains(hypkey.getBaseform()+" "+hypkey.getTag())) && 
							hypkey.getBaseform().equals(hyp) && 
							hypkey.getTag().equals(key.getTag())) {
						idx = j;
						break;
					}
    			}
    		}
    		if (i != idx) {
    			map.put(i, idx);
    			hypSet.add(idx);
    		}
    	}
		return map;
	}
	
	public static HashMap<Integer, Integer> mapDictionaryExtern(GoSemKrimp krimp, double SIM_THRESHOLD) {
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		HashMap<Integer,ArrayList<Integer>> hypMap = new HashMap<Integer, ArrayList<Integer>>();
		HashMap<Integer, Integer> inverseHypMap = new HashMap<Integer, Integer>();
		ArrayList<String> labelTags = new ArrayList<String>();
		int initial_size = krimp.dictionary.size();
		for (int i = 0; i < krimp.dictionary.size(); i++)
			labelTags.add(krimp.dictionary.get(i).getBaseform()+" "+krimp.dictionary.get(i).getTag());
		for (int i = 0; i < initial_size; i++) {
    		Keyword key = krimp.dictionary.get(i);
    		String hyp = key.getHypernym();
    		String tag = key.getTag();
	    	int id = labelTags.indexOf(hyp+" "+tag);
	    	if (i != id && wsim.getLinSimilarity(key.getBaseform(), hyp, tag) >= SIM_THRESHOLD) {
	    		if (id < 0) {
	    			id = krimp.dictionary.size();
	    			krimp.dictionary.add(new Keyword(id, hyp, hyp, key.getHypernymstem(), hyp, key.getHypernymstem(), key.getTag()));
	    		}
	    		if (!hypMap.containsKey(id)) {
	    			ArrayList<Integer> list = new ArrayList<Integer>();
	    			list.add(i);
	    			hypMap.put(id, list);
	    			inverseHypMap.put(i, id);
	    		}
	    		else {
	    			hypMap.get(id).add(i);
	    			inverseHypMap.put(i, id);
	    		}
    		}
    	}
		// eliminate cycles
		for (Integer  e : hypMap.keySet()) {
			ArrayList<Integer> trace = getTrace(e, inverseHypMap); 
			if (trace.size() > 2) {
				int n = trace.size();
				int last = trace.get(n-1);
				int idx = trace.subList(0, n-1).indexOf(last);
				if (idx >= 0)
					if (n-idx == 3) {
						int freq1 = 0;
						int freq2 = 0;
						if (krimp.frequencies.length > trace.get(idx)) freq1 = krimp.frequencies[trace.get(idx)];
						if (krimp.frequencies.length > trace.get(n-2)) freq2 = krimp.frequencies[trace.get(n-2)];
						if (freq1 >= freq2)
							inverseHypMap.remove(trace.get(idx));
						else inverseHypMap.remove(trace.get(n-2));
					}
					else {
						int mini = -1;
						double minsim = Double.MAX_VALUE;
						for (int j=idx; j<n-1; j++) {
							String word1 = krimp.dictionary.get(trace.get(j)).getBaseform();
							String word2 = krimp.dictionary.get(trace.get(j+1)).getBaseform();
							double sim = wsim.getLinSimilarity(word1, word2);
							if (sim < minsim) {
								mini = trace.get(j);
								minsim = sim;
							}
						}
						inverseHypMap.remove(mini);
					}
			}
		}
		
		for (Integer id : inverseHypMap.keySet()) {
    		Integer hypId = inverseHypMap.get(id);
    		Integer idx = hypId;
    		while (idx != null) {
    			hypId = idx;
    			idx = inverseHypMap.get(idx);
    		}
    		result.put(id, hypId);
    	}
		
		if (initial_size != krimp.dictionary.size()) saveDictionary(krimp.dictionary, krimp.dataset);
		
		return result;
	}
	
	public static void saveDictionary(ArrayList<Keyword> dictionary, String dataset) {
		BufferedWriter bw;
		BufferedWriter bw2;
		try{
			bw = new BufferedWriter(new FileWriter(dataset+".dictn"));
			bw2 = new BufferedWriter(new FileWriter(dataset+".labn"));
			for (int i =0; i < dictionary.size(); i++) {
				bw.write(dictionary.get(i)+"\n");
				bw.flush();
				bw2.write(dictionary.get(i).getBaseform()+"\n");
				bw2.flush();
			}
			bw.close();
			bw2.close();
			
		} catch(IOException ex) {
			
		}
	}
	
	public static ArrayList<Integer> getTrace(Integer e, HashMap<Integer, Integer> inverseHypMap) {
		ArrayList<Integer> trace = new ArrayList<Integer>();
		trace.add(e);
		Integer curr = inverseHypMap.get(e);
		while (curr != null && !trace.contains(curr)) {
			trace.add(curr);
			curr = inverseHypMap.get(curr);
		}
		if (curr != null && trace.contains(curr)) trace.add(curr);
		return trace;
	}
	
	public static double similarity(String word, ArrayList<String> words, String tag) {
		double sim = 0.0;
		for (String w : words)
			sim += wsim.getLinSimilarity(word, w, tag);
		sim /= words.size();
		return sim;
	}

}
