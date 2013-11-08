/*******************************************************************************
 * Copyright (c) 2013 Matthias Niemann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * www.gnu.org/licenses/lgpl.txt
 * 
 * Contributors:
 *     Matthias Niemann - initial API and implementation
 ******************************************************************************/
package xApriori;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import datatypes.Missable;
import datatypes.Pattern;


/**
 * Checks a single pattern for being frequent.
 * @author mniemann
 *
 * @param <T>
 */
public class XAPrioriCheckPatternThread<T> implements Runnable {
	private Map<T, Pattern<Missable>> data;
	private Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> patternData;
	private Set<Pattern<Missable>> L_k, kPattern;
	private Pattern<Missable> c, superC;
	private Map<Pattern<Missable>, Integer> setSupport;
	private Semaphore barrier;
	private int transactionCount, pruned, discarded;
	private double minRep, minSup;
	private boolean stopped = false;
	public XAPrioriCheckPatternThread(
			Map<T, Pattern<Missable>> data,
			Pattern<Missable> c, Pattern<Missable> superC,
			Map<Pattern<Missable>, Integer> setSupport, Semaphore barrier,
			int transactionCount, double minRep,
			double minSup) {
		this.data = data;
		this.c = c;
		this.superC = superC;
		this.setSupport = setSupport;
		this.barrier = barrier;
		this.transactionCount = transactionCount;
		this.pruned = 0;
		this.discarded = 0;
		this.minRep = minRep;
		this.minSup = minSup;
	}
	
	public Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> getPatternData() {
		return patternData;
	}

	public void setPatternData(
			Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> patternData) {
		this.patternData = patternData;
	}

	public Set<Pattern<Missable>> getL_k() {
		return L_k;
	}

	public void setL_k(Set<Pattern<Missable>> l_k) {
		L_k = l_k;
	}

	public Set<Pattern<Missable>> getkPattern() {
		return kPattern;
	}

	public void setkPattern(Set<Pattern<Missable>> kPattern) {
		this.kPattern = kPattern;
	}

	public int getPruned() {
		return pruned;
	}

	public void setPruned(int pruned) {
		this.pruned = pruned;
	}

	public int getDiscarded() {
		return discarded;
	}

	public void setDiscarded(int discarded) {
		this.discarded = discarded;
	}

	@Override
	public void run() {
		if (!stopped){
			L_k = new HashSet<Pattern<Missable>>();
			kPattern = new HashSet<Pattern<Missable>>();
			patternData = new HashMap<Pattern<Missable>, Map<InterestingnessMeasure, Double>>();
	
			double support = setSupport.get(c) * 1.0 / setSupport.get(superC);
			double representativity = setSupport.get(superC) * 1.0 / transactionCount;
			double allConfidence = setSupport.get(c) * 1.0 / getMaxItemSupport(c, superC); 
			if (representativity >= minRep){
				L_k.add(c);
				if (support >= minSup){
					kPattern.add(c);
					HashMap<InterestingnessMeasure, Double> numbers = new HashMap<InterestingnessMeasure, Double>();
					numbers.put(InterestingnessMeasure.SUPPORT, support);
					numbers.put(InterestingnessMeasure.REPRESENTATIVITY, representativity);
					numbers.put(InterestingnessMeasure.ALLCONFIDENCE, allConfidence);
					patternData.put(c, numbers);
				}
				else{
					discarded++;
				}
			}
			else{
				pruned++;
			}
			
		}
		barrier.release();
	}
	private int getMaxItemSupport(Pattern<Missable> c, Pattern<Missable> superC) {
		Map<T, Pattern<Missable>> superCData = getReducedDataset(superC);
		int max = 0;
		for (Missable x_i : c){
			int localMax = 0;
			for (T tid : superCData.keySet()){
				boolean contained = true;
				for (Missable dat : superCData.get(tid)){
					if (dat.equals(x_i)){
						contained = true;
					}
				}
				if (contained){
					localMax++;
				}
			}
			
			max = Math.max(max, localMax);
		}
		return max;
	}
	private Map<T, Pattern<Missable>> getReducedDataset(Pattern<Missable> set){
		Map<T, Pattern<Missable>> reducedData = new HashMap<T, Pattern<Missable>>();
		for (T tid : data.keySet()){
			boolean contained = true;
			for (Missable item : set){
				boolean locallyContained = false;
				for (Missable dat : data.get(tid)){
					if (dat.equals(item)){
						locallyContained = true;
					}
				}
				contained &= locallyContained;
			}
			if (contained){
				reducedData.put(tid, data.get(tid));
			}
		}
		return reducedData;
	}
	public void stop(){
		stopped = true;
	}
}
