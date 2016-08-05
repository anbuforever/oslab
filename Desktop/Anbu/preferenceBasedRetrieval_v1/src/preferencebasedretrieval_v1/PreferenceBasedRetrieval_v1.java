/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package preferencebasedretrieval_v1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jacop.constraints.XplusYeqZ;
import org.jacop.core.Domain;
import org.jacop.core.Store;
import org.jacop.floats.constraints.LinearFloat;
import org.jacop.floats.constraints.PeqQ;
import org.jacop.floats.constraints.PminusQeqR;
import org.jacop.floats.constraints.PmulQeqR;
import org.jacop.floats.core.FloatDomain;
import org.jacop.floats.core.FloatInterval;
import org.jacop.floats.core.FloatVar;
import org.jacop.floats.search.LargestDomainFloat;
import org.jacop.floats.search.LargestMinFloat;
import org.jacop.floats.search.Optimize;
import org.jacop.floats.search.SplitSelectFloat;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.OneSolution;
import org.jacop.search.PrintOutListener;
import org.jacop.search.SimpleSolutionListener;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LinearProgram;
import scpsolver.problems.MathematicalProgram;

/**
 *
 * @author balu
 */
public class PreferenceBasedRetrieval_v1 {

    /**
     * @param args the command line arguments
     */
    
    public static ArrayList<String> featureNames;
    public static ArrayList<String> featureTypes;
    public static ArrayList<String> utilTypes;
    public static ArrayList<ArrayList<String>> cases;
    public static ArrayList<Limits> numericLimits;
    public static ArrayList<ArrayList<String>> nominalTypes;
    public static ArrayList<HashMap<String,Double>> nominalUtilValues;
    public static ArrayList<ArrayList<String>> normalisedCases;
    public static ArrayList<ArrayList<Double>> distances;
    public static ArrayList<ArrayList<Integer>> nNearest;
    public static ArrayList<ArrayList<Integer>> nonDominant;
    public static ArrayList<Integer> normalisedIndex;
    public static ArrayList<Double> normalisedFeatureWeights;
    public static ArrayList<String> currentQuery;
    public static int currentQueryIndex;
    public static ArrayList<String> target;
    public static ArrayList<Integer> currentNNearest;
    public static int numNeighs =25;
    public static ArrayList<String> normalisedFeatureSpace;
    public static HashMap<ArrayList<String>,ArrayList<Integer>> staticNonDominant = new HashMap<>();
    public static ArrayList<ArrayList<String>> staticNormalisedCases = new ArrayList<>();
    public static Random r = new Random();
    
    
    //special variables for running a new thread
    
    public static int SpecialOptimal;
    public static ArrayList<String> SpecialQuery = new ArrayList<>();
    public static ArrayList<Integer> SpecialKNearest = new ArrayList<>();
    
    
    
    public static int[] COUNT = new int[3];
    
    public static void main(String[] args) throws IOException, FileNotFoundException, InterruptedException {
        // TODO code application logic here
        initializeVariables();
        readSpecifications();
        readInput();
        normalise();   
        initialiseWeights();
//        calculateDistances();
//        findNeighbours();
//        compromiseBasedNeighbours();
        distanceBasedNeighbours();
        
        
        
//        solveConstraints();
//        check();
    }

    private static void initializeVariables() {
        featureNames = new ArrayList<>();
        featureTypes = new ArrayList<>();
        utilTypes = new ArrayList<>();
        cases = new ArrayList<>();
        numericLimits = new ArrayList<>();
        nominalTypes = new ArrayList<>();
        normalisedCases = new ArrayList<>();
        distances = new ArrayList<>();
        nNearest = new ArrayList<>();
        nonDominant = new ArrayList<>();
        normalisedIndex = new ArrayList<>();
        normalisedFeatureWeights = new ArrayList<>();
        currentNNearest = new ArrayList<>();
        currentQuery = new ArrayList<>();
        normalisedFeatureSpace = new ArrayList<>();
        target = new ArrayList<>();
        nominalUtilValues = new ArrayList<>();
    }

    private static void readSpecifications() throws FileNotFoundException, IOException {
        BufferedReader br =new BufferedReader(new FileReader("spec.txt"));
        String line = br.readLine();
        
        String[] lineSplit = line.split("&");
        for(int i=0;i<lineSplit.length;i++){
            lineSplit[i]=lineSplit[i].trim();
            featureNames.add(lineSplit[i]);
        }
        
        line = br.readLine();
        
        String[] line2Split = line.split("&");
        for(int i=0;i<line2Split.length;i++){
            line2Split[i]=line2Split[i].trim();
            featureTypes.add(line2Split[i]);
        }
        
        line = br.readLine();
        
        String[] line3Split = line.split("&");
        for(int i=0;i<line3Split.length;i++){
            line3Split[i]=line3Split[i].trim();
            utilTypes.add(line3Split[i]);
        }
    }

    private static void readInput() throws FileNotFoundException, IOException {
        BufferedReader br =new BufferedReader(new FileReader("input.txt"));
        String line = br.readLine();
        
        while(line!=null){
            String[] lineSplit = line.split("&");
            ArrayList<String> tempCase = new ArrayList<String>();
            for(int i=0;i<lineSplit.length;i++){
                lineSplit[i]=lineSplit[i].trim();
                tempCase.add(lineSplit[i]);
            }
            cases.add(tempCase);
            line = br.readLine();
        }
    }

    private static void normalise() {
        

        for(String type:featureTypes){
            if(type.equalsIgnoreCase("Numeric")){
                normalisedIndex.add(numericLimits.size());
                Limits l =new Limits();
                numericLimits.add(l);
            }
            else{
                normalisedIndex.add(nominalTypes.size());
                ArrayList<String> nom = new ArrayList<>();
                nominalTypes.add(nom);
            }
        }       
        
        for(int i=1,j=0;i<featureTypes.size();i++){
            if(featureTypes.get(i-1).equalsIgnoreCase("Numeric")){
                double val = Double.parseDouble(cases.get(0).get(i));
                numericLimits.get(j).max = val;
                numericLimits.get(j).min = val;
                j++;
             }
        }
        
        
        
        for(ArrayList<String> tempCase:cases){
            for(int i=1,j=0,k=0;i<=featureTypes.size();i++){
                if(featureTypes.get(i-1).equalsIgnoreCase("Numeric")){
                    double max = numericLimits.get(j).max;
                    double min = numericLimits.get(j).min;
                    double val = Double.parseDouble(tempCase.get(i));
                    
                    if(val>max){
                        numericLimits.get(j).max=val;
                    }
                    if(val<min){
                        numericLimits.get(j).min=val;
                    }
                    j++;
                }
                else{
                    String nomVal = tempCase.get(i);
                    if(nominalTypes.get(k).indexOf(nomVal)==-1){
                        nominalTypes.get(k).add(nomVal);
                    }
                    k++;
                }
            }
        }
        
        for(int i=0,j=0;i<featureTypes.size();i++){
            if(featureTypes.get(i).equalsIgnoreCase("Numeric")){
                normalisedFeatureSpace.add(featureNames.get(i));
            }
            else{
                for(String s:nominalTypes.get(j)){
                    normalisedFeatureSpace.add(s);
                }
                j++;
            }
        }
        
        //Normalise values and set vectors for nominal values
        for(ArrayList<String> tempCase: cases){
            ArrayList<String> tempNormalCase = new ArrayList<>();
            for(int i=1,j=0,k=0;i<=featureTypes.size();i++){
                if(featureTypes.get(i-1).equalsIgnoreCase("Numeric")){
                   double max = numericLimits.get(j).max;
                   double min = numericLimits.get(j).min;
                   double val = Double.parseDouble(tempCase.get(i)); 
                   if(utilTypes.get(j).equalsIgnoreCase("MIB")){
                       val = val - min;
                       val = val/(max - min);
                       tempNormalCase.add(val+"");
                   }
                   else{
                       val = max - val;
                       val = val/(max - min);
                       tempNormalCase.add(val+"");
                   }
                   j++;
                }
                else{
                    tempNormalCase.add(tempCase.get(i));
                }
            }
            normalisedCases.add(tempNormalCase);
        }
        
        for(int i=0;i<nominalTypes.size();i++){
            HashMap<String,Double> temp  = new HashMap<>();
            for(int j=0;j<nominalTypes.get(i).size();j++){
                temp.put(nominalTypes.get(i).get(j), 1.0);
            }
            nominalUtilValues.add(temp);
        }
        
        
        
    }

    private static void check() {
        for(int i=0;i<cases.size();i++){
            System.out.println(nonDominant.get(i));
        }
    }

    private static void calculateDistances() {
        
        distances = new ArrayList<>();
        for(ArrayList<String> vecA:normalisedCases){
            ArrayList<Double> tempDistances = new ArrayList<>();
            for(ArrayList<String> vecB:normalisedCases){
                double dist = calculateEuclidean(vecA,vecB);
                tempDistances.add(dist);
            }
            distances.add(tempDistances);
        }
    }

    private static double calculateEuclidean(ArrayList<String> vecA, ArrayList<String> vecB) {
        
        double dist = 0.0;
        for(int i=0;i<vecB.size();i++){
            if(featureTypes.get(i).equalsIgnoreCase("Numeric")){
                double x2 = Double.parseDouble(vecB.get(i));
                double x1 = Double.parseDouble(vecA.get(i));
                x2 = x2 -x1;
                dist += (normalisedFeatureWeights.get(i) * Math.pow(x2, 2.0));
            }
            else{
                if(vecB.get(i).equalsIgnoreCase("dontCare")||vecA.get(i).equalsIgnoreCase("dontCare"))
                    dist+=0;
                else{
                    double x2 = nominalUtilValues.get(normalisedIndex.get(i)).get(vecB.get(i));
                    double x1 = nominalUtilValues.get(normalisedIndex.get(i)).get(vecA.get(i));
                    x2 = x2 -x1;
                    dist += (normalisedFeatureWeights.get(i) * Math.pow(x2, 2.0));
                }
                
            }
                
        }
        dist = Math.sqrt(dist);
        return dist;
    }

    private static void findNeighbours() {
       
        for(ArrayList<Double> tempDistances:distances){
            ArrayList<String> stringDistances = new ArrayList<>();
            ArrayList<Integer> positions = new ArrayList<>();
            for(double d:tempDistances){
                stringDistances.add(d+"");
            }
            Collections.sort(tempDistances);
            for(int i=1;i<tempDistances.size();i++){
                int pos =stringDistances.indexOf(tempDistances.get(i)+"");
                while(positions.indexOf(pos)!=-1){
                    pos = pos + stringDistances.subList(pos+1, stringDistances.size()).indexOf(tempDistances.get(i)+"") + 1;
                }
                positions.add(pos);
            }
            nNearest.add(positions);
        }
    }

    private static void findNonDominatingNeighbours() {
        
        for(int i=0;i<normalisedCases.size();i++){
            ArrayList<String> queryCase = normalisedCases.get(i);
            ArrayList<Integer> tempNonDominant = new ArrayList<>();
            ArrayList<Integer> candidate = new ArrayList<>();
            candidate.addAll(nNearest.get(i));
//            System.out.println(candidate.size());
            tempNonDominant.addAll(findNonDominatingCases(queryCase,candidate));            
            nonDominant.add(tempNonDominant);
        }
    }
    
    private static ArrayList<Integer> findNonDominatingCases(ArrayList<String> queryCase, ArrayList<Integer> candidate) {
        
        ArrayList<Integer> tempNonDominant = new ArrayList<>();
        
        while(candidate.size()>0){
                ArrayList<String> tempCase = normalisedCases.get(candidate.get(0)); 
                Integer currentCandidate = candidate.get(0);
                candidate.remove(0);
                ArrayList<String> compromiseSet = findCompromiseSet(queryCase,tempCase);
                int flag=0;
                while(compromiseSet.isEmpty()){
                    if(candidate.isEmpty()){
                        flag=1;
                        break;
                    }
                    tempCase = normalisedCases.get(candidate.get(0));
                    currentCandidate = candidate.get(0);
                    candidate.remove(0);
                    compromiseSet = findCompromiseSet(queryCase,tempCase);
                }
                if(flag==1)
                    break;
                ArrayList<Integer> coverageSet = new ArrayList<>();
                tempNonDominant.add(currentCandidate);
                for(Integer tempCandidate:candidate){
                    ArrayList<String> candidateCompromiseSet = findCompromiseSet(queryCase,normalisedCases.get(tempCandidate));
                    if(candidateCompromiseSet.containsAll(compromiseSet)){
                        coverageSet.add(tempCandidate);
                    }
                }
                for(Integer removeCandidate:coverageSet){
                    candidate.remove(removeCandidate);
                }
            }
        
        return tempNonDominant;
    }
    
        private static ArrayList<Integer> compromiseDrivenRetrieval(ArrayList<String> queryCase, ArrayList<Integer> candidate) {
        
        ArrayList<Integer> tempNonDominant = new ArrayList<>();
        
        while(candidate.size()>0){
                ArrayList<String> tempCase = normalisedCases.get(candidate.get(0)); 
                tempNonDominant.add(candidate.get(0));
                candidate.remove(0);
                ArrayList<String> compromiseSet = findCompromiseSet(queryCase,tempCase);
                ArrayList<Integer> coverageSet = new ArrayList<>();              
                for(Integer tempCandidate:candidate){
                    ArrayList<String> candidateCompromiseSet = findCompromiseSet(queryCase,normalisedCases.get(tempCandidate));
                    if(candidateCompromiseSet.containsAll(compromiseSet)){
                        coverageSet.add(tempCandidate);
                    }
                }
                for(Integer removeCandidate:coverageSet){
                    candidate.remove(removeCandidate);
                }
            }
        
        return tempNonDominant;
    }
    

    private static ArrayList<String> findCompromiseSet(ArrayList<String> queryCase, ArrayList<String> tempCase) {
        
        ArrayList<String> compromiseSet = new ArrayList<>();
        
        for(int i=0;i<queryCase.size();i++){
            if(featureTypes.get(i).equalsIgnoreCase("Numeric")){
                if(Double.parseDouble(tempCase.get(i))<Double.parseDouble(queryCase.get(i)))
                    compromiseSet.add(i+"");
            }
            else{
                if(!tempCase.get(i).equalsIgnoreCase(queryCase.get(i))){
                    compromiseSet.add(i+"");
                }
            }
        }
        
        return compromiseSet;
    }

//    private static void solvLinear() {
//        LinearProgram lp = new LinearProgram(new double[]{5.0,10.0});
//        lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{3.0,1.0}, 8.0, "c1"));
//        lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{0.0,4.0}, 4.0, "c2"));
//        lp.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{2.0,0.0}, 2.0, "c3"));
//
//        lp.setMinProblem(true);
//        LinearProgramSolver solver  = SolverFactory.newDefault();
//        double[] sol = solver.solve(lp);
//        
//        for(int i=0;i<sol.length;i++){
//            System.out.println(sol[i]);
//        }
//    }
    
    private static void getRegionsByRREF(){
        double[] optimize = new double[normalisedCases.get(0).size()];
        for(int i=0;i<optimize.length;i++){
            optimize[i]=1.0;
        }
        
        for(int i=0;i<normalisedCases.size();i++){
            ArrayList<String> tempNormalisedCase = normalisedCases.get(i);
            ArrayList<Integer> neighbours = nonDominant.get(i);
            for(int j=0;j<neighbours.size();j++){
                ArrayList<String> neighbourCase = normalisedCases.get(neighbours.get(j));
                double[] deltas = calculateDeltas(tempNormalisedCase,neighbourCase);
            }
        }
        
    }

    private static void solveConstraints() {
        
        double[] optimize = new double[normalisedCases.get(0).size()];
        System.out.println(optimize.length+" "+normalisedCases.get(0).size());
        for(int i=0;i<optimize.length;i++){
            optimize[i]=1.0;
        }
        for(int i=0;i<normalisedCases.size();i++){
            ArrayList<String> tempNormalisedCase = normalisedCases.get(i);
            ArrayList<Integer> neighbours = nonDominant.get(i);
            LinearProgram lp = new LinearProgram(optimize);
            for(int j=0;j<neighbours.size();j++){
                ArrayList<String> neighbourCase = normalisedCases.get(neighbours.get(j));
                double[] deltas = calculateDeltas(tempNormalisedCase,neighbourCase);
                lp.addConstraint(new LinearBiggerThanEqualsConstraint(deltas, 0.0, "c"+j));
            }
            lp.addConstraint(new LinearEqualsConstraint(optimize, 1.0, "g"));

//            lp.setMinProblem(true);
            LinearProgramSolver solver  = SolverFactory.newDefault();
            double[] sol = solver.solve(lp);
            
//            solver.

        
            for(int j=0;j<sol.length;j++){
                System.out.print(sol[j]+",");
            }
            System.out.println();
        }
    }

    private static double[] calculateDeltas(ArrayList<String> tempNormalisedCase, ArrayList<String> neighbourCase) {
        double[] deltas = new double[neighbourCase.size()];
        
        for(int i=0;i<deltas.length;i++){
            if(featureTypes.get(i).equalsIgnoreCase("Numeric")){
                deltas[i] = Double.parseDouble(tempNormalisedCase.get(i))-Double.parseDouble(neighbourCase.get(i));
            }
            else{
                deltas[i] = nominalUtilValues.get(normalisedIndex.get(i)).get(tempNormalisedCase.get(i)) - nominalUtilValues.get(normalisedIndex.get(i)).get(neighbourCase.get(i));
            }
        }
        
        return deltas;
    }
    
    private static ArrayList<Integer> findQueryKNearest(int k, ArrayList<String> vecA, ArrayList<Integer> queryPositions) {

        ArrayList<Double> tempDistances = new ArrayList<>();
        for(ArrayList<String> vecB:normalisedCases){
            double dist = calculateEuclidean(vecA,vecB,queryPositions);
            tempDistances.add(dist);
        }
        ArrayList<String> stringDistances = new ArrayList<>();
        ArrayList<Integer> positions = new ArrayList<>();
        for(double d:tempDistances){
            stringDistances.add(d+"");
        }
        Collections.sort(tempDistances);
        for(int i=0;i<tempDistances.size() && i<k;i++){
            int pos =stringDistances.indexOf(tempDistances.get(i)+"");
            while(positions.indexOf(pos)!=-1){
                pos = pos + stringDistances.subList(pos+1, stringDistances.size()).indexOf(tempDistances.get(i)+"") + 1;
            }
            positions.add(pos);
        }
        return positions;
    }

    private static double calculateEuclidean(ArrayList<String> vecA, ArrayList<String> vecB, ArrayList<Integer> queryPositions) {
        
//        System.out.println("Nrmalised Index "+normalisedIndex);
        double dist = 0.0;
        for(int i=0;i<vecA.size();i++){
            if(queryPositions.indexOf(i)!=-1){
            if(featureTypes.get(queryPositions.get(i)).equalsIgnoreCase("Numeric")){
                double x2 = Double.parseDouble(vecB.get(queryPositions.get(i)));
                double x1 = Double.parseDouble(vecA.get(i));
                x2 = x2 -x1;
                dist += (normalisedFeatureWeights.get(queryPositions.get(i)) * Math.pow(x2, 2.0));
            }
            else{
                if(vecB.get(queryPositions.get(i)).equalsIgnoreCase("dontCare")||vecA.get(i).equalsIgnoreCase("dontCare"))
                    dist+=0;
                else{
                    double x2 = nominalUtilValues.get(normalisedIndex.get(queryPositions.get(i))).get(vecB.get(queryPositions.get(i)));
                    double x1 = nominalUtilValues.get(normalisedIndex.get(queryPositions.get(i))).get(vecA.get(i));
                    x2 = x2 -x1;
                    dist += (normalisedFeatureWeights.get(queryPositions.get(i)) * Math.pow(x2, 2.0));
                }
                
            } 
          }
        }
        dist = Math.sqrt(dist);
        return dist;  
    }

    private static void readQuery() throws FileNotFoundException, IOException, InterruptedException {
        
        int queryCount = 0;
        BufferedReader br =new BufferedReader(new FileReader("query.txt"));
        String line = br.readLine();
        
        while(line!=null){
            String[] lineSplit = line.split("&");
            Query queryLine = new Query();
            queryLine.initialise();
            for(int i=0;i<lineSplit.length;i++){
                lineSplit[i]=lineSplit[i].trim();
                String[] positionSplit = lineSplit[i].split(":");
                positionSplit[0] = positionSplit[0].trim();
                positionSplit[1] = positionSplit[1].trim();
//                System.out.println(positionSplit[0]+" "+positionSplit[1]+queryLine.positions);
                queryLine.positions.add(Integer.parseInt(positionSplit[0]));
                queryLine.query.add(positionSplit[1]);              
            }
            queryLine.normaliseQuery();
            
//            ArrayList<Double> tempList = new ArrayList<>();
//            double sum = 0.0;
//            
//            for(int j=0,m=0;j<normalisedCases.get(0).size();j++){
//                    if(queryLine.normalisedPositions.indexOf(j)==-1){
////                        filledQuery.add(normalisedCases.get(optimalCase).get(j));
//                        tempList.add(1.0);
//                        normalisedFeatureWeights.set(j,0.0);
//                    }
//                    else{
//                        Double sd=queryLine.normalisedQuery.get(m);
//                        if(sd!=0){
//                            normalisedFeatureWeights.set(j,1.0);
//                            sum+=1.0;
//                        }
//                        else{
//                            normalisedFeatureWeights.set(j,0.0);
//                        }
//                        tempList.add(sd);
//                        m++;
//                    }
//                }
//            for(int j=0;j<normalisedCases.get(0).size();j++){
//                double nVal = normalisedFeatureWeights.get(j)/sum;
//                normalisedFeatureWeights.set(j, nVal);
//            }
//            
//            queryLine.normalisedPositions=new ArrayList<>();
//            queryLine.normalisedQuery=new ArrayList<>();
//            for(int j=0;j<normalisedCases.get(0).size();j++){
//                queryLine.normalisedPositions.add(j);
//                queryLine.normalisedQuery.add(tempList.get(j));
//            }
            
            //number of cases in a retrieval
            
            
            
            
            
            
            
            
            int k=normalisedCases.size();
//            int k=5;
            System.out.println(queryLine.normalisedQuery.size()+" "+queryLine.positions.size());
            System.out.println(queryLine.normalisedQuery+" "+queryLine.positions);
            ArrayList<Integer> kNearest = findQueryKNearest(k,queryLine.normalisedQuery, queryLine.positions);
            ArrayList<String> filledQuery = new ArrayList<>();
            for(int j=0;j<featureTypes.size();j++){
                    if(queryLine.positions.indexOf(j)==-1){
//                        filledQuery.add(normalisedCases.get(optimalCase).get(j));
                        
                        if(featureTypes.get(j).equalsIgnoreCase("Numeric"))
                            filledQuery.add(1.0+"");
                        else{
                            filledQuery.add("dontCare");
                        }
                    }
                    else{
                        filledQuery.add(queryLine.normalisedQuery.get(queryLine.positions.indexOf(j)));
                    }
                }
            
            ArrayList<Integer> topCDR = findNonDominatingCases(filledQuery, kNearest);
            
            k = 5;
            if(topCDR.size()>k){
                kNearest.clear();
                kNearest.addAll(topCDR.subList(0, k));
            }
            else{
                kNearest.clear();
                kNearest.addAll(topCDR);
            }
            
            ArrayList<ArrayList<String>> kNearestCases = new ArrayList<>();
            
            for(int kn=0;kn<kNearest.size();kn++){
                kNearestCases.add(normalisedCases.get(kNearest.get(kn)));
//                System.out.println(normalisedCases.get(kNearest.get(kn)));
            }
            
            System.out.println("Target "+target);
            
            int conv = 0;
            while(kNearestCases.indexOf(target)==-1){
                System.out.println(kNearest);
                conv++;
                
                int optimalCase = pickOptimalCase(kNearest);
//                ArrayList<String> filledQuery = new ArrayList<>();
//                for(int j=0;j<normalisedCases.get(optimalCase).size();j++){
//                    if(queryLine.positions.indexOf(j)==-1){
////                        filledQuery.add(normalisedCases.get(optimalCase).get(j));
//                        
//                        if(featureTypes.get(j).equalsIgnoreCase("Numeric"))
//                            filledQuery.add(1.0+"");
//                        else{
//                            filledQuery.add("dontCare");
//                        }
//                    }
//                    else{
//                        filledQuery.add(queryLine.normalisedQuery.get(queryLine.positions.indexOf(j)));
//                    }
//                }
                
                
                System.out.println(normalisedCases.get(optimalCase));
                System.out.println(filledQuery);
                
                SpecialOptimal = optimalCase;
                SpecialQuery = new ArrayList<>();
                SpecialQuery.addAll(filledQuery);
                SpecialKNearest = new ArrayList<>();
                SpecialKNearest.addAll(kNearest);
                
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<?> future = executorService.submit(new Runnable() {
                public void run() {
                    UpdateWeightsLinearProgrammingJacop(SpecialOptimal,SpecialQuery,SpecialKNearest);
                }
                });
                
                executorService.shutdown();            //        <-- reject all further submissions
                

            try {
                future.get(10, TimeUnit.SECONDS);  //     <-- wait 8 seconds to finish
                } catch (InterruptedException e) {    //     <-- possible error cases
                System.out.println("job was interrupted");
                } 
            catch (ExecutionException e) {
                    System.out.println("caught exception: " + e.getCause());
            } 
            catch (TimeoutException e) {
                future.cancel(true);              //     <-- interrupt the job
                System.out.println("Timeout: Optimizer takes too long");
            }

            // wait all unfinished tasks for 2 sec
            if(!executorService.awaitTermination(2, TimeUnit.SECONDS)){
                // force them to quit by interrupting
                executorService.shutdownNow();
            }
                
                
                
                
                
                //UpdateWeightsLinearProgrammingJacop(optimalCase,filledQuery,kNearest);

                
                
//                UpdateWeightsLinearProgramming(optimalCase,filledQuery,kNearest);
                
                queryLine = new Query();
                queryLine.initialise();
                for(int j=0;j<normalisedCases.get(optimalCase).size();j++){
                    queryLine.normalisedQuery.add(normalisedCases.get(optimalCase).get(j));
                    queryLine.positions.add(j);
                }
                
                ArrayList<Integer> delKNN = new ArrayList<>();
                delKNN.addAll(kNearest);
                
                while(!kNearest.isEmpty()){
                    Integer del = kNearest.remove((int) 0);
                    safelyRemoveCase(del);
                    for(int ik = 0;ik < kNearest.size();ik++){
                        if(kNearest.get(ik) >del){
                            kNearest.set(ik, kNearest.get(ik) - 1);
                        }
                    }
                }
//                findDistanceBasedNeighbours(numNeighs);
                
                kNearest = new ArrayList<>();
                k=normalisedCases.size();
//                k=5;
                kNearest = findQueryKNearest(k,queryLine.normalisedQuery, queryLine.positions);
                topCDR = findNonDominatingCases(queryLine.normalisedQuery, kNearest);
            
                k = 5;
                if(topCDR.size()>k){
                    kNearest.clear();
                    kNearest.addAll(topCDR.subList(0, k));
                }
                else{
                    kNearest.clear();
                    kNearest.addAll(topCDR);
                }
            
                kNearestCases = new ArrayList<>();
            
                for(int kn=0;kn<kNearest.size();kn++){
                    kNearestCases.add(normalisedCases.get(kNearest.get(kn)));
//                    System.out.println(normalisedCases.get(kNearest.get(kn)));
                }
                System.out.println("Number of Cycles: "+conv);
                filledQuery = new ArrayList<>();
                filledQuery.addAll(queryLine.normalisedQuery);
                
            }
            restoreCases();
            for(String val:normalisedCases.get(nNearest.get(currentQueryIndex).get(0))){
                target.add(val);
            }
            safelyRemoveCase(currentQueryIndex);
//            initialiseWeights();
//            findDistanceBasedNeighbours(numNeighs);
            System.out.println("Number of Cycles !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1: "+conv);
            COUNT[queryCount]+=conv;
            queryCount++;
            
            for(int i=0;i<3;i++){
                System.out.println("Query Cycle Count "+i+": "+COUNT[i]);
            }
            
//            System.out.println(queryLine.query);
//            System.out.println(cases.get(kNearest.get(0)));
//            System.out.println(queryLine.normalisedPositions);
//            System.out.println(queryLine.normalisedQuery);
            line = br.readLine();
            
        }
    }
    
    private static int pickOptimalCase(ArrayList<Integer> kNearest) {
        

        int minIndex=0;
        double min=10000;
        for(Integer i:kNearest){
            ArrayList<String> vecB = normalisedCases.get(i);
            double dist = calculateEuclidean(target,vecB);
            if(dist<min){
                min = dist;
                minIndex = i;
            }
        }
        
        System.out.println("Optimal Case choice: "+minIndex);
        
        return minIndex;
    }

    private static void initialiseWeights() {
        
        for(int i=0;i<normalisedIndex.size();i++){
            double w = (1.0/normalisedIndex.size());
//            if(featureTypes.get(i).equalsIgnoreCase("Numeric")){              
                normalisedFeatureWeights.add(w);
//            }
//            else{
//                for(int j=0;j<nominalTypes.get(normalisedIndex.get(i)).size();j++)
//                    normalisedFeatureWeights.add(w*0.5);
//            }
        }
        System.out.println(normalisedFeatureWeights);
    }

    private static void UpdateWeightsLinearProgramming(int optimal, ArrayList<String> query, ArrayList<Integer> kNearest) {
        
        double[] optimize = new double[normalisedCases.get(0).size()];
        System.out.println(optimize.length+" "+normalisedCases.get(0).size());
        System.out.println("Here");
        
        LinearProgram lp = new LinearProgram(optimize);
        
            ArrayList<String> tempNormalisedCase = normalisedCases.get(optimal);
            for(int i=0;i<optimize.length;i++){
                double difference;
                if(featureTypes.get(i).equalsIgnoreCase("Numeric"))
                    difference = Double.parseDouble(query.get(i)) - Double.parseDouble(tempNormalisedCase.get(i));
                else{
                    difference = nominalUtilValues.get(i).get(query.get(i)) - nominalUtilValues.get(i).get(tempNormalisedCase.get(i));
                }
                optimize[i]= normalisedFeatureWeights.get(i) * (Math.pow(difference, 2.0));
                System.out.print(optimize[i]+"*");
            }
            lp.addConstraint(new LinearBiggerThanEqualsConstraint(optimize, 0.0, "distance"));
            
            ArrayList<Integer> neighbours = staticNonDominant.get(tempNormalisedCase);
            for(int i=0;i<neighbours.size();i++){
                 ArrayList<String> neighbourCase = staticNormalisedCases.get(neighbours.get(i));
                double[] deltas = calculateDeltas(tempNormalisedCase,neighbourCase);
                for(int j=0;j<optimize.length;j++){
//                    optimize[j]+= (normalisedFeatureWeights.get(j) * deltas[j]);
                    optimize[j]+= (normalisedFeatureWeights.get(j) * (Math.pow(deltas[j], 2.0)));
                    deltas[j] *= normalisedFeatureWeights.get(j);
                }
                lp.addConstraint(new LinearBiggerThanEqualsConstraint(deltas, 0.0, "static"));
            }
            
            neighbours.clear();
            kNearest.remove((Integer)optimal);
            neighbours.addAll(kNearest);
            for(int i=0;i<neighbours.size();i++){
                 ArrayList<String> neighbourCase = normalisedCases.get(neighbours.get(i));
                double[] deltas = calculateDeltas(neighbourCase,tempNormalisedCase);
                for(int j=0;j<optimize.length;j++){
                    optimize[j]+= (normalisedFeatureWeights.get(j) * deltas[j]);
                    deltas[j] *= normalisedFeatureWeights.get(j);
                }
                lp.addConstraint(new LinearBiggerThanEqualsConstraint(deltas, 0.0, "dynamic"));
            }
            
            
            for(int i=0;i<optimize.length;i++){
                optimize[i]=1.0;
            }
            lp.addConstraint(new LinearEqualsConstraint(optimize, 1.0, "weight"));

            lp.setMinProblem(true);
            LinearProgramSolver solver  = SolverFactory.newDefault();
            double[] sol = solver.solve(lp);
            
//            solver.

        
            for(int j=0;j<sol.length;j++){
                System.out.print(sol[j]+",");
            }
            
            for(int j=0;j<sol.length;j++){
                normalisedFeatureWeights.set(j, sol[j]);
            }
            System.out.println();
        
        
    }
    
    public static void UpdateWeightsLinearProgrammingJacop(int optimal, ArrayList<String> query,ArrayList<Integer> kNearest) {
        
        double[] optimize = new double[normalisedCases.get(0).size()];
        System.out.println(optimize.length+" "+normalisedCases.get(0).size());
        System.out.println("Here");
        
        Store store = new Store();
        FloatDomain.setPrecision(1E-3);
        
        FloatVar[] W = new FloatVar[optimize.length];
        FloatVar[] PW = new FloatVar[optimize.length];
        FloatVar[] conf = new FloatVar[optimize.length];
        for(int i=0;i<optimize.length;i++){
            W[i] = new FloatVar(store,"temp"+i, 0.0, 1);
//            conf[i] = new FloatVar(store, "conf"+i, -0.2,0.2);
//            PW[i] = new FloatVar(store,"temp1"+i, normalisedFeatureWeights.get(i), normalisedFeatureWeights.get(i));
        }
        //sum of weights is one
        for(int i=0;i<optimize.length;i++){
            optimize[i]=1.0;
//            store.impose(new PminusQeqR(W[i], PW[i], conf[i]));
        }
        
        double sum=1.0;
        
        store.impose(new LinearFloat(store, W, optimize, "==", sum ));

        

        
            ArrayList<String> tempNormalisedCase = normalisedCases.get(optimal);
             System.out.println(query+" "+tempNormalisedCase);
            int nozeros=0;
            for(int i=0;i<optimize.length;i++){
                double difference;
                if(featureTypes.get(i).equalsIgnoreCase("Nominal")){
                if(query.get(i).equalsIgnoreCase("dontCare"))
                    difference = 0.0;
                else{
                    difference = nominalUtilValues.get(normalisedIndex.get(i)).get(query.get(i)) - nominalUtilValues.get(normalisedIndex.get(i)).get(tempNormalisedCase.get(i));
                    }
                }
                
                else{
                    difference = Double.parseDouble(query.get(i)) - Double.parseDouble(tempNormalisedCase.get(i));
                }
                
                System.out.print(difference+"*");
                if(difference==0.0)
                    nozeros+=1;
                
                optimize[i]= normalisedFeatureWeights.get(i) * Math.pow(difference, 2.0);
//                System.out.println(normalisedFeatureWeights);
//                optimize[i]= normalisedFeatureWeights.get(i) * difference;
//                optimize[i]= Math.pow(difference, 2.0);
//                System.out.print(optimize[i]+"*");
//                System.out.println();
            }
            
            
            
            if(nozeros==optimize.length){
                System.out.println("Stuck in Local Minima ");
                System.exit(-1);
                return;
            }
            
             sum = 0.0;
             
             //moving towards user's preference
            
            FloatVar[] Function = new FloatVar[optimize.length+1];
            double[] weightSlack = new double[optimize.length+1];
            for(int i=0;i<optimize.length;i++){
                Function[i] = W[i];
                weightSlack[i] = optimize[i];
            }
            
            Function[optimize.length] = new FloatVar(store,"slack", 0.0, 1e150);
            weightSlack[optimize.length] = -1.0;
            
            
            store.impose(new LinearFloat(store, Function, weightSlack, "==", sum));
            
            
            //Static neighbourhood
            ArrayList<Integer> neighbours = staticNonDominant.get(tempNormalisedCase);
            int neighCount = neighbours.size();
            
            FloatVar[] neighsSlack = new FloatVar[neighbours.size()+kNearest.size()-1];
            
            for(int j=0;j<neighbours.size();j++){
                
                neighsSlack[j] = new FloatVar(store,"slacki"+j, 0.0, 1e150);
                FloatVar[] tempFunction = new FloatVar[optimize.length+1];
                double[] tempweightSlack = new double[optimize.length+1];

                
                ArrayList<String> neighbourCase = staticNormalisedCases.get(neighbours.get(j));
                double[] deltas = calculateDeltas(neighbourCase,tempNormalisedCase);
                sum = 0.0;

                
                for(int i=0;i<optimize.length;i++){
                    tempFunction[i] = W[i];
                    tempweightSlack[i] = deltas[i]*normalisedFeatureWeights.get(i);
//                    tempweightSlack[i] = (normalisedFeatureWeights.get(i) * (Math.pow(deltas[i], 2.0)));
                }
                tempFunction[optimize.length] = neighsSlack[j];
                tempweightSlack[optimize.length] = -1.0;
                
                
                
                store.impose(new LinearFloat(store, tempFunction, tempweightSlack, "==", sum));
//                store.impose(new LinearFloat(store, W, deltas, ">=", sum));
            }
            
            
            //dynamic surroundings
//            FloatVar[] neighsSlack = new FloatVar[kNearest.size()-1];
//            ArrayList<Integer> neighbours = new ArrayList<>();
            neighbours.clear();
            kNearest.remove((Integer)optimal);
            neighbours.addAll(kNearest);
            for(int j=0;j<neighbours.size();j++){
                neighsSlack[neighCount+j] = new FloatVar(store,"slackj"+j, 0.0, 1e150);
//                neighsSlack[j] = new FloatVar(store,"slackj"+j, 0.0, 1e150);
                 FloatVar[] tempFunction = new FloatVar[optimize.length+1];
                double[] tempweightSlack = new double[optimize.length+1];
                
                
                ArrayList<String> neighbourCase = normalisedCases.get(neighbours.get(j));
                double[] deltas = calculateDeltas(neighbourCase,tempNormalisedCase);
                sum = 0.0;
//                for(int i=0;i<optimize.length;i++){
//                    
//                }
//                store.impose(null);
                
                for(int i=0;i<optimize.length;i++){
                    tempFunction[i] = W[i];
                    tempweightSlack[i] = deltas[i]*normalisedFeatureWeights.get(i);
//                    tempweightSlack[i] = (normalisedFeatureWeights.get(i) * (Math.pow(deltas[i], 2.0)));
                }
                tempFunction[optimize.length] = neighsSlack[neighCount+j];
//                tempFunction[optimize.length] = neighsSlack[j];
                tempweightSlack[optimize.length] = -1.0;
                
                store.impose(new LinearFloat(store, tempFunction, tempweightSlack, "==", sum));
//                store.impose(new LinearFloat(store, W, deltas, ">=", sum));
            }
            
            
            
           
            
            //combining slack vars
            
            FloatVar cost = new FloatVar(store,"cost", 0, 1e150);
            FloatVar[] reduceCost = new FloatVar[neighsSlack.length+2];
            double[] totalWeigh = new double[neighsSlack.length+2];
            for(int i=0;i<neighsSlack.length;i++){
                reduceCost[i] = neighsSlack[i];
                totalWeigh[i] = 1.0;
            }
            reduceCost[neighsSlack.length] = Function[optimize.length];
            totalWeigh[neighsSlack.length] = 1.0;
            
            reduceCost[neighsSlack.length+1] = cost;
            totalWeigh[neighsSlack.length+1] = -1.0;
            
            store.impose(new LinearFloat(store, reduceCost, totalWeigh, "==", sum));
            
            
            
            DepthFirstSearch<FloatVar> search = new DepthFirstSearch<FloatVar>();
//            SplitSelectFloat<FloatVar> select = new SplitSelectFloat<FloatVar>(store, W, new LargestDomainFloat<FloatVar>());
            
//            SplitSelectFloat<FloatVar> select = new SplitSelectFloat<FloatVar>(store, W, new LargestMinFloat<FloatVar>());
            SplitSelectFloat<FloatVar> select = new SplitSelectFloat<FloatVar>(store, W, null);
//            search.setSolutionListener(new PrintOutListener<FloatVar>());
//            search.setSolutionListener(new OneSolution<FloatVar>());
            SimpleSolutionListener<FloatVar> ssl = new SimpleSolutionListener<FloatVar>();
            ssl.setSolutionLimit(1);
            search.setSolutionListener(ssl);
//             boolean result = search.labeling(store, select,cost);
        
            search.setTimeOut(5);
//            boolean result = search.labeling(store, select, Function[optimize.length]);
            Optimize min = new Optimize(store, search, select, cost);
            boolean result = min.minimize();
            
            if(result==true){
            
        FloatInterval[] finalVarValues = min.getFinalVarValues();
            
                


            System.out.println("Here Opt Done: ");
            
//            solver.

            double valueSum = 0.0;
            for(int j=0;j<W.length;j++){
                valueSum+=finalVarValues[j].max;
            }
            
            for(int j=0;j<W.length;j++){
                normalisedFeatureWeights.set(j, finalVarValues[j].max/valueSum);
            }
            System.out.println("Updated Weights: "+normalisedFeatureWeights);
        }
        
        
    }

    private static void randomisedQueryFixing() throws FileNotFoundException, IOException {
        
        
        int Low = 0;
        int High = cases.size();      
        int randomCase = r.nextInt(High-Low) + Low;
        
        currentQueryIndex = randomCase;
        ArrayList<String> tempCurrent = new ArrayList<>();
        for(int i=1;i<=featureTypes.size();i++){
            tempCurrent.add(cases.get(randomCase).get(i));
        }
        

        
        //write in to query file
        DataOutputStream fos = new DataOutputStream(new FileOutputStream("query.txt"));
        
        ArrayList<Integer> randomIndex = new ArrayList<>();
        String content = "";
        for(int i=0;i<1;i++){
            Low = 0;
            High = featureTypes.size(); 
            randomIndex.add(r.nextInt(High-Low) + Low);
            content+=randomIndex.get(i);
            content+=":";
            content+=tempCurrent.get(randomIndex.get(i));           
        }
        fos.writeBytes(content);
        fos.writeBytes("\n");
        
        randomIndex.clear();
        content = "";
        for(int i=0;i<3;i++){
            Low = 0;
            High = featureTypes.size(); 
            int pos = r.nextInt(High-Low) + Low;
            while(randomIndex.indexOf(pos)!=-1){
                pos = r.nextInt(High-Low) + Low;
            }
            randomIndex.add(pos);
            content+=randomIndex.get(i);
            content+=":";
            content+=tempCurrent.get(randomIndex.get(i));
            content+="&";
        }
        content = content.substring(0, content.length()-1);
        fos.writeBytes(content);
        fos.writeBytes("\n");
        
        randomIndex.clear();
        content = "";
        for(int i=0;i<5;i++){
            Low = 0;
            High = featureTypes.size(); 
            int pos = r.nextInt(High-Low) + Low;
            while(randomIndex.indexOf(pos)!=-1){
                pos = r.nextInt(High-Low) + Low;
            }
            randomIndex.add(pos);
            content+=randomIndex.get(i);
            content+=":";
            content+=tempCurrent.get(randomIndex.get(i));
            content+="&";
        }
        content = content.substring(0, content.length()-1);
        fos.writeBytes(content);
        fos.writeBytes("\n");
        
        currentQuery = new ArrayList<>();
        for(String val:normalisedCases.get(randomCase)){
            currentQuery.add(val);
        }
        
        System.out.println("Target: "+nNearest.get(randomCase).get(0));
        
        for(String val:normalisedCases.get(nNearest.get(randomCase).get(0))){
            target.add(val);
        }
        
        safelyRemoveCase(randomCase);
    }

    private static void restoreCases() throws IOException {
        initializeVariables();
        readSpecifications();
        readInput();
        normalise();   
        initialiseWeights(); 
        calculateDistances();
        findNeighbours();
        findNonDominatingNeighbours();
        
    }

    private static void compromiseBasedNeighbours() throws IOException, FileNotFoundException, InterruptedException {
        calculateDistances();
        findNeighbours();
        findNonDominatingNeighbours();
        findNonDominatingNeighboursStatic();
        for(int i=0;i<100;i++){
            
//            for(Double val:normalisedCases.get(nNearest.get(131).get(0))){
//                target.add(val);
//            }
            randomisedQueryFixing();
//            currentQueryIndex = 131;
//            safelyRemoveCase(131);
            readQuery();
            staticNonDominant = new HashMap<>();
            staticNormalisedCases = new ArrayList<>();
        initializeVariables();
        readSpecifications();
        readInput();
        normalise();   
        initialiseWeights();
        calculateDistances();
        findNeighbours();
        findNonDominatingNeighbours();
        findNonDominatingNeighboursStatic();
        System.out.println("********************");
        System.out.println("Done with Query: "+ i);
        System.out.println("********************");
        }
        
        for(int i=0;i<3;i++){
            System.out.println("Query Cycle Count "+i+": "+COUNT[i]);
        }
    }

    private static void distanceBasedNeighbours() throws IOException, FileNotFoundException, InterruptedException {
        calculateDistances();
        findNeighbours();
        findDistanceBasedNeighbours(numNeighs);
        findNonDominatingNeighboursStatic();
        for(int i=0;i<100;i++){
            
            randomisedQueryFixing();            
            readQuery();
            staticNonDominant = new HashMap<>();
            staticNormalisedCases = new ArrayList<>();
        initializeVariables();
        readSpecifications();
        readInput();
        normalise();   
        initialiseWeights();
        calculateDistances();
        findNeighbours();
        findDistanceBasedNeighbours(numNeighs);
        findNonDominatingNeighboursStatic();
        System.out.println("********************");
        System.out.println("Done with Query: "+ i);
        System.out.println("********************");
        }
        
        for(int i=0;i<3;i++){
            System.out.println("Query Cycle Count "+i+": "+COUNT[i]);
        }
    }

    private static void findDistanceBasedNeighbours(int k) {
        for(int i=0;i<normalisedCases.size();i++){         
            nonDominant.add(nNearest.get(i));
        }
    }

    private static void safelyRemoveCase(int optimalCase) {
        normalisedCases.remove((int) optimalCase);
        cases.remove((int) optimalCase);
        
        distances.remove((int) optimalCase);
        
        nNearest.remove((int) optimalCase);    
        nonDominant.remove((int) optimalCase);
        
        for(int i=0;i<normalisedCases.size();i++){
            distances.get(i).remove((int) optimalCase);
        }
        
        for(int i=0;i<nNearest.size();i++){
            if(nNearest.get(i).indexOf(optimalCase)!=-1){
                nNearest.get(i).remove((Integer)optimalCase);
            }
            for(int j=0;j<nNearest.get(i).size();j++){
                if(nNearest.get(i).get(j)>optimalCase){
                    nNearest.get(i).set(j, nNearest.get(i).get(j)-1);
                }
            }          
        }
        
        for(int i=0;i<nonDominant.size();i++){
            if(nonDominant.get(i).indexOf(optimalCase)!=-1){
                nonDominant.get(i).remove((Integer)optimalCase);
            }
            for(int j=0;j<nonDominant.get(i).size();j++){
                if(nonDominant.get(i).get(j)>optimalCase){
                    nonDominant.get(i).set(j, nonDominant.get(i).get(j)-1);
                }
            }
        }
    }

    private static void findNonDominatingNeighboursStatic() {
        staticNonDominant.clear();
        staticNormalisedCases.clear();
        for(int i=0;i<normalisedCases.size();i++){
            staticNormalisedCases.add(normalisedCases.get(i));
            ArrayList<Integer> temp = nNearest.get(i);
            ArrayList<Integer> candidate = new ArrayList<>();
            candidate.addAll(temp.subList(0, numNeighs));
            ArrayList<String> queryCase = normalisedCases.get(i);
            staticNonDominant.put(normalisedCases.get(i), findNonDominatingCases(queryCase,candidate));
//            staticNonDominant.put(normalisedCases.get(i), nonDominant.get(i));
        }
    }
    
    private static ArrayList<String> convertDoubleToString(ArrayList<Double> inputTemp){
        ArrayList<String> outputTemp = new ArrayList<>();
        for(int i=0;i<inputTemp.size();i++){
            outputTemp.add(inputTemp.get(i)+"");
        }
        return outputTemp;
    }




    
    
    
}
