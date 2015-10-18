package gokrimp;

import java.text.DecimalFormat;


/**
 *
 * @author hkosorus
 */
public class Experiments {
     
    
    static void experiment_with_gosemkrimp(String dataname, String test, int N, double alpha) {
        
        DataReader d = new DataReader();
        GoSemKrimp gs = d.readSemData(dataname);
        gs.goSemKrimp(test, N, alpha);
        String s = new DecimalFormat("#0.0000").format(alpha);
        s = s.substring(s.indexOf(",")+1, s.length());
        //gs.PrintPatternWeka(dataname+"_GSK_"+test+"_"+N+"_"+ s);
        //gs.printResultToFile(dataname+"_res_GSK_"+test+"_"+N+"_"+s);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	experiment_with_gosemkrimp("../data/reuters", "sign", 25, 0.01);
    	
    }
}
