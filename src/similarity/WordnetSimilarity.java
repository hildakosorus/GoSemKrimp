package similarity;

import java.util.Arrays;

import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Resnik;
import edu.sussex.nlp.jws.HirstAndStOnge;
import edu.sussex.nlp.jws.JiangAndConrath;
import edu.sussex.nlp.jws.LeacockAndChodorow;
import edu.sussex.nlp.jws.WuAndPalmer;
import edu.sussex.nlp.jws.AdaptedLeskTanimoto;

/**
 * @author hkosorus
 *
 */
public class WordnetSimilarity {
	
		private JWS ws;
	    private Resnik resnik;
	    private Lin lin;
	    private HirstAndStOnge hirst;
	    private JiangAndConrath jiang;
	    private LeacockAndChodorow leacock;
	    private WuAndPalmer wu;
	    private AdaptedLeskTanimoto lesk;
	    private static WordnetSimilarity instance;
	    
	    private WordnetSimilarity() {
	    	
	    }
	    
	    public static WordnetSimilarity getInstance() {
	    	if (instance == null) instance = new WordnetSimilarity();
	    	return instance;
	    }

	    public void init(String WordnetPath, String Version) {
	        try {
	            ws = new JWS(WordnetPath,Version);
	            resnik = ws.getResnik();
	            lin = ws.getLin();
	            hirst = ws.getHirstAndStOnge();
	            jiang = ws.getJiangAndConrath();
	            leacock = ws.getLeacockAndChodorow();
	            wu = ws.getWuAndPalmer();
	            lesk = ws.getAdaptedLeskTanimoto();
	        } 
	        catch(Exception ex) {
	        }
	    }
	 
	    public double getResnikSimilarity(String word1, String word2) {
	    	if (word1.equals(word2)) return 1.0;
	        try {
	            double noun_sim = resnik.max(word1, word2, "n");
	            double verb_sim = resnik.max(word1, word2, "v");
	            double adj_sim = resnik.max(word1, word2, "a");
	            double adv_sim = resnik.max(word1, word2, "r");
	            double[] sims = {noun_sim,verb_sim,adj_sim,adv_sim};
	            Arrays.sort(sims);
	            return sims[3];
	        }
	        catch(Exception ex) {
	        	System.out.println(ex.getStackTrace());
	            return 0.0;
	        }
	    }
	    
	    public double getLinSimilarity(String word1, String word2) {
	    	if (word1.equals(word2)) return 1.0;
	        try {
	            double noun_sim = lin.max(word1, word2, "n");
	            double verb_sim = lin.max(word1, word2, "v");
	            double adj_sim = lin.max(word1, word2, "a");
	            double adv_sim = lin.max(word1, word2, "r");
	            double[] sims = {noun_sim,verb_sim,adj_sim,adv_sim};
	            Arrays.sort(sims);
	            return sims[3];
	        }
	        catch(Exception ex) {
	        	System.out.println(ex.getStackTrace());
	            return 0.0;
	        }
	    }
	    
	    public double getLinSimilarity(String word1, String word2, String tag) {
	    	if (word1.equals(word2)) return 1.0;
	        try {
	        	if (tag.contains("NN"))
	        		return lin.max(word1, word2, "n");
	        	else if (tag.contains("VB"))
	        			return lin.max(word1, word2, "v");
	        	else if (tag.contains("JJ"))
	        			return lin.max(word1, word2, "a");
	        	else if (tag.contains("RB"))
	        			return lin.max(word1, word2, "r");
	        	else return 0.0;
	        }
	        catch(Exception ex) {
	        	System.out.println(ex.getStackTrace());
	            return 0.0;
	        }
	    }
	    
	    public double getHirstSimilarity(String word1, String word2) {
	    	if (word1.equals(word2)) return 1.0;
	        try {
	            double noun_sim = hirst.max(word1, word2, "n");
	            double verb_sim = hirst.max(word1, word2, "v");
	            double adj_sim = hirst.max(word1, word2, "a");
	            double adv_sim = hirst.max(word1, word2, "r");
	            double[] sims = {noun_sim,verb_sim,adj_sim,adv_sim};
	            Arrays.sort(sims);
	            return sims[3];
	        }
	        catch(Exception ex) {
	        	System.out.println(ex.getStackTrace());
	            return 0.0;
	        }
	    }
	    
	    public double getJiangSimilarity(String word1, String word2) {
	    	if (word1.equals(word2)) return 1.0;
	        try {
	            double noun_sim = jiang.max(word1, word2, "n");
	            double verb_sim = jiang.max(word1, word2, "v");
	            double adj_sim = jiang.max(word1, word2, "a");
	            double adv_sim = jiang.max(word1, word2, "r");
	            double[] sims = {noun_sim,verb_sim,adj_sim,adv_sim};
	            Arrays.sort(sims);
	            return sims[3];
	        }
	        catch(Exception ex) {
	        	System.out.println(ex.getStackTrace());
	            return 0.0;
	        }
	    }
	    
	    public double getLeacockSimilarity(String word1, String word2) {
	    	if (word1.equals(word2)) return 1.0;
	        try {
	            double noun_sim = leacock.max(word1, word2, "n");
	            double verb_sim = leacock.max(word1, word2, "v");
	            double adj_sim = leacock.max(word1, word2, "a");
	            double adv_sim = leacock.max(word1, word2, "r");
	            double[] sims = {noun_sim,verb_sim,adj_sim,adv_sim};
	            Arrays.sort(sims);
	            return sims[3];
	        }
	        catch(Exception ex) {
	        	System.out.println(ex.getStackTrace());
	            return 0.0;
	        }
	    }
	    
	    public double getWuSimilarity(String word1, String word2) {
	    	if (word1.equals(word2)) return 1.0;
	        try {
	            double noun_sim = wu.max(word1, word2, "n");
	            double verb_sim = wu.max(word1, word2, "v");
	            double adj_sim = wu.max(word1, word2, "a");
	            double adv_sim = wu.max(word1, word2, "r");
	            double[] sims = {noun_sim,verb_sim,adj_sim,adv_sim};
	            Arrays.sort(sims);
	            return sims[3];
	        }
	        catch(Exception ex) {
	        	System.out.println(ex.getStackTrace());
	            return 0.0;
	        }
	    }
	    
	    public double getWuSimilarity(String word1, String word2, String tag) {
	    	if (word1.equals(word2)) return 1.0;
	        try {
	        	if (tag.contains("NN"))
	        		return wu.max(word1, word2, "n");
	        	else if (tag.contains("VB"))
	        			return wu.max(word1, word2, "v");
	        	else if (tag.contains("JJ"))
	        			return wu.max(word1, word2, "a");
	        	else if (tag.contains("RB"))
	        			return wu.max(word1, word2, "r");
	        	else return 0.0;
	        }
	        catch(Exception ex) {
	        	System.out.println(ex.getStackTrace());
	            return 0.0;
	        }
	    }
	    
	    public double getLeskSimilarity(String word1, String word2) {
	    	if (word1.equals(word2)) return 1.0;
	        try {
	            double noun_sim = lesk.max(word1, word2, "n");
	            double verb_sim = lesk.max(word1, word2, "v");
	            double adj_sim = lesk.max(word1, word2, "a");
	            double adv_sim = lesk.max(word1, word2, "r");
	            double[] sims = {noun_sim,verb_sim,adj_sim,adv_sim};
	            Arrays.sort(sims);
	            return sims[3];
	        }
	        catch(Exception ex) {
	        	System.out.println(ex.getStackTrace());
	            return 0.0;
	        }
	    }
}

