package similarity;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;


/**
 * @author hkosorus
 *
 */
public class WSimilarity {
	
	 private static WSimilarity instance;
	 private static ILexicalDatabase db = new NictWordNet();
     private static RelatednessCalculator[] rcs = {new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db), new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)};

	 private WSimilarity() {
	    	
	 }
	    
	 public static WSimilarity getInstance() {
	    if (instance == null) instance = new WSimilarity();
	    return instance;
	 }
	 
	 public double getSimilarity(String word1, String word2, RelatednessType type) {
		 WS4JConfiguration.getInstance().setMFS(true);
         double s = rcs[type.getId()].calcRelatednessOfWords(word1, word2);
         return s;
	 }
    
}
