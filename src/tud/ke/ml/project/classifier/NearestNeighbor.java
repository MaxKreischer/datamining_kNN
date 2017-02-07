package tud.ke.ml.project.classifier;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
//import java.util.Arrays;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tud.ke.ml.project.util.Pair;

/**
 * This implementation assumes the class attribute is always available (but probably not set).
 * 
 */
public class NearestNeighbor extends INearestNeighbor implements Serializable {
	private static final long serialVersionUID = 1L;

	protected double[] scaling;
	protected double[] translation;
	protected List<List<Object>> SavedModel = new ArrayList<List<Object>>();
	
	@Override
	public String getMatrikelNumbers() {
		return "2073175" + "," + "1704677" + "," + "2104399";
	}

	@Override
	protected void learnModel(List<List<Object>> data) {
		//Initialize Attributes to save training data
		//SavedModel = new ArrayList<List<Object>>();
		for(int i=0; i<data.size(); i++){
			SavedModel.add(data.get(i));
		}
	}

	@Override
	protected Map<Object, Double> getUnweightedVotes(List<Pair<List<Object>, Double>> subset) {
		//subset of NN with their distance
		Map<Object,Double> voteMap = new HashMap<Object, Double>();
		for(Pair<List<Object>, Double> pair : subset){
			int	classIndex = pair.getA().size()-1;
			Object classKey =  pair.getA().get(classIndex);
			if(voteMap.containsKey(classKey)){
				voteMap.put(classKey, (double)voteMap.get(classKey)+1);
			}else{
				voteMap.put(classKey, (double)1);
			}
		}
		return voteMap;
	}

	@Override
	protected Map<Object, Double> getWeightedVotes(List<Pair<List<Object>, Double>> subset) {
		Map<Object, Double> voteMap = new HashMap<Object, Double>();
		double totalWeight = 0;
		int classIndex;
		Object classKey;
		double classWeight;
		for(Pair<List<Object>, Double> pair : subset){
			totalWeight += (double)(1/pair.getB()); 
		}
		for(Pair<List<Object>, Double> pair : subset){
			classIndex = pair.getA().size()-1;
			classKey = pair.getA().get(classIndex);
			classWeight = (double)(1/pair.getB());
			if(voteMap.containsKey(classKey)){
				voteMap.put(classKey, (double)(voteMap.get(classKey)+classWeight/totalWeight));
			}else{
				voteMap.put(classKey, classWeight/totalWeight);
			}
		}
		return voteMap;
	}

	@Override
	protected Object getWinner(Map<Object, Double> votes) {
		Object currMaxKey= "" ;
		double currMaxValue = 0;
		for(Object key : votes.keySet()){
			if(votes.get(key) > currMaxValue){
				currMaxValue = votes.get(key);
				currMaxKey = key;
			}
		}
		System.out.println("Values :"+votes.values());
		double best=votes.get(currMaxKey);
		
		for(Object keys : votes.keySet()){
			if(votes.get(keys) == votes.get(currMaxKey)){
				int sum = 0;
				int bestsum = 0;
				for(List<Object> instance : SavedModel){
					if(instance.get(instance.size()-1) == currMaxKey){
						bestsum++;
					}
					if(instance.get(instance.size()-1) == keys){
						sum++;
					}
				}
				if( sum > bestsum){
				currMaxKey = keys;
				}
			}
		}
		return currMaxKey;
	}

	@Override
	protected Object vote(List<Pair<List<Object>, Double>> subset) {
		Object winningClass = "";
		Map<Object, Double> votes = new HashMap<Object, Double>();
		if(isInverseWeighting()){
			votes = getWeightedVotes(subset);
		}else{votes = getUnweightedVotes(subset);}
		winningClass = getWinner(votes);
		return winningClass;
	}

	@Override
	protected List<Pair<List<Object>, Double>> getNearest(List<Object> data) {
		List<Pair<List<Object>, Double>> Nearest;
		List<Pair<List<Object>, Double>> EvenNearer;
		//Pair<List<Object>, Double> tempPair = new Pair<List<Object>, Double>();
		List<Object> tmpList = new ArrayList<Object>();
		
		Nearest = new ArrayList<Pair<List<Object>, Double>>();
		
		//TODO remove test thing
		//int k = getkNearest();
		int k = 4;
		int j;
		double distance;
		List<Object> tempL;
		for(List<Object> trainInstance : SavedModel){
			
			tempL = trainInstance.subList(0, (int)trainInstance.size()-1);
			if(getMetric() == 0){
				distance = determineManhattanDistance(data.subList(0, data.size()-1), tempL);
			}else{
				distance = determineEuclideanDistance(data.subList(0, data.size()-1), tempL);
			}
			
			Pair<List<Object>, Double> tempPair = new Pair<List<Object>, Double>(trainInstance, distance);
			Nearest.add(tempPair);
		}
		
		for(int i=1; i<Nearest.size(); i++){
			j = i;
			while((j > 0) && (Nearest.get(j-1).getB() > Nearest.get(j).getB())){
				Pair<List<Object>, Double> tempPair = new Pair<List<Object>, Double>(new ArrayList<Object>(),0.0);
				tempPair.setA(Nearest.get(j).getA());
				tempPair.setB(Nearest.get(j).getB());
				Nearest.set(j, Nearest.get(j-1));
				Nearest.set(j-1, tempPair);
				j = j -1;
			}
		}
		
		
		EvenNearer = Nearest.subList(0, k); 
		
		
		return EvenNearer;
	}

	@Override
	protected double determineManhattanDistance(List<Object> instance1, List<Object> instance2) {
		//takes two pts in n-space: sum(abs(pt1 - pt2))
		List<Object> diffVec = new ArrayList<Object>(instance1.size());
		double distance = 0;
		for(int i=0; i<instance1.size(); i++){
			if((instance1.get(i)instanceof Double) && (instance2.get(i)instanceof Double)){
				diffVec.add(Math.abs((double)instance1.get(i)-(double)instance2.get(i)));
			}else{
				if(instance1.get(i).equals(instance2.get(i))){
					diffVec.add((double)0);
				}else{diffVec.add((double)1);}
			}
		}
		for(int i=0; i<instance1.size(); i++){
			distance += (double)diffVec.get(i);
		}
		return distance;
	}

	@Override
	protected double determineEuclideanDistance(List<Object> instance1, List<Object> instance2) {
		//takes two pts in n-space: sqrt(sum(pow((pt1-pt2),2)))
		List<Object> diffVec = new ArrayList<Object>(instance1.size());
		double distance = 0;
		for(int i=0; i<instance1.size(); i++){
			if((instance1.get(i) instanceof Double) && (instance2.get(i) instanceof Double)){
			diffVec.add(Math.pow(((double)instance1.get(i)-(double)instance2.get(i)), 2));
			}else{
				if(instance1.get(i).equals(instance2.get(i))){
					diffVec.add((double)0);
				}else{diffVec.add((double)1);}
			}
		}
		for(int i=0; i<instance1.size(); i++){
			distance += (double)diffVec.get(i);
		}
		distance = Math.sqrt(distance);
		return distance;
	}

	@Override
	protected double[][] normalizationScaling() {
		//takes SavedModel and normalizes it's numerical attributes
		//ignores class Attributes and nominal attributes (!!build that check!!)
		//returns scaling and translation factors -> use these outside this function
		// ---> to normalize SavedModel values
		throw new NotImplementedException();
	}

}
