/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package preferencebasedretrieval_v1;

import java.util.ArrayList;
import java.util.HashMap;
import static preferencebasedretrieval_v1.PreferenceBasedRetrieval_v1.*;

/**
 *
 * @author balu
 */


public class Query {
    public ArrayList<String> normalisedQuery;
    public ArrayList<String> query;
    public ArrayList<Integer> positions;
    public ArrayList<Integer> normalisedPositions;
    
    public void initialise(){
        this.normalisedQuery = new ArrayList<>();
        this.query = new ArrayList<>();
        this.positions = new ArrayList<>();
        this.normalisedPositions = new ArrayList<>();
    }
    
    public void normaliseQueryOld(){
        
        
        for(int i=0;i<positions.size();i++){
            int qPosition = normalisedIndex.get(positions.get(i));
            if(featureTypes.get(positions.get(i)).equalsIgnoreCase("Numeric")){              
                Limits lim = numericLimits.get(qPosition);
                Double val = Double.parseDouble(query.get(i));
                if(utilTypes.get(qPosition).equalsIgnoreCase("MIB")){
                    val = (val - lim.min)/(lim.max - lim.min);
                }
                else{
                    val = (lim.max - val)/(lim.max - lim.min);
                }
                if(val>1.0){
                    val = 1.0;
                }
                else if(val<0.0){
                    val = 0.0;
                }
                normalisedQuery.add(val+"");
                
                
                //normlaised Position Calculation
//                int normPos = 0;
//                
//                for(int j=0;j<normalisedIndex.size();j++){
//                    int normInd = normalisedIndex.get(j);
//                    if(normInd == qPosition && featureTypes.get(j).equalsIgnoreCase("Numeric")){
//                        break;
//                    }
//                    else if(featureTypes.get(j).equalsIgnoreCase("Numeric")){
//                        normPos += 1;
//                    }
//                    else if(featureTypes.get(j).equalsIgnoreCase("Nominal")){
//                        normPos += nominalTypes.get(normInd).size();
//                    }
//                }
//                normalisedPositions.add(normPos);
                normalisedPositions.add(normalisedFeatureSpace.indexOf(featureNames.get(positions.get(i))));
            }
            else{
                int nominalTypePosition = nominalTypes.get(qPosition).indexOf(query.get(i));
                for(int ntp=0;ntp<nominalTypes.get(qPosition).size();ntp++){
                    
                    int normalPosition = normalisedFeatureSpace.indexOf(nominalTypes.get(qPosition).get(ntp));
                    normalisedPositions.add(normalPosition);
                    
                    if(ntp==nominalTypePosition){
                        normalisedQuery.add(1.0+"");                       
                    }
                    else{
                        normalisedQuery.add(0.0+"");
                    }
                }
                
                //If that nominal attr is absent
//                if(nominalTypePosition == -1){
//                    nominalTypePosition = 0;
//                }
//                int normPos = 0;
//                
//                for(int j=0;j<normalisedIndex.size();j++){
//                    int normInd = normalisedIndex.get(j);
//                    if(normInd == qPosition && featureTypes.get(j).equalsIgnoreCase("Nominal")){
//                        normPos += nominalTypePosition;
//                        break;
//                    }
//                    else if(featureTypes.get(j).equalsIgnoreCase("Numeric")){
//                        normPos += 1;
//                    }
//                    else if(featureTypes.get(j).equalsIgnoreCase("Nominal")){
//                        normPos += nominalTypes.get(normInd).size();
//                    }
//                }
//                normalisedPositions.add(normPos);
//                normalisedQuery.add(1.0);
                
            }

        }
    }
    public void normaliseQuery(){
        
        for(int i=0;i<positions.size();i++){
            int qPosition = normalisedIndex.get(positions.get(i));
            if(featureTypes.get(positions.get(i)).equalsIgnoreCase("Numeric")){              
                Limits lim = numericLimits.get(qPosition);
                Double val = Double.parseDouble(query.get(i));
                if(utilTypes.get(qPosition).equalsIgnoreCase("MIB")){
                    val = (val - lim.min)/(lim.max - lim.min);
                }
                else{
                    val = (lim.max - val)/(lim.max - lim.min);
                }
                if(val>1.0){
                    val = 1.0;
                }
                else if(val<0.0){
                    val = 0.0;
                }
                normalisedQuery.add(val+"");
            }
            else{
                normalisedQuery.add(query.get(i));
            }
        }
        double sum = positions.size();
        
        for(int i=0;i<normalisedCases.get(0).size();i++){
            if(positions.indexOf(i)!=-1){
                normalisedFeatureWeights.set(i, (1/sum));
            }
            else{
                normalisedFeatureWeights.set(i, 0.0);
            }
                
        }
        
        for(int i=0;i<normalisedCases.get(0).size();i++){
            if(positions.indexOf(i)!=-1 && featureTypes.get(i).equalsIgnoreCase("Nominal")){
              int qPosition = normalisedIndex.get(i);  
              for(String s:nominalUtilValues.get(qPosition).keySet()){
                  if(s.equalsIgnoreCase(query.get(positions.indexOf(i))))
                    nominalUtilValues.get(qPosition).put(s, 1.0);
                  else{
                    nominalUtilValues.get(qPosition).put(s, 0.0); 
                  }
                }
            }
        }
        
        for(int i=0;i<nominalUtilValues.size();i++){
            System.out.println(nominalUtilValues.get(i).values());
        }
        
        
            
        
        
    }
    
}
