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

import datatypes.ID;
import datatypes.Missable;
import datatypes.Pattern;

/**
 * Provides an Extensible Apriori algorithm.
 * 
 * Makes use of interestingness measure definitions by RAG98.
 * The extensibility measure of CAL07 is used to prune rules.
 * 
 * Literature:
 * RAG98: "Treatment of Missing Values for Association Rules" (Arnaud Ragel, Bruno Cremilleux, Universite de Caen, 1998)
 * CAL07: "Mining Itemsets in the Presence of Missing Values" (T. Calders, B. Goethals, M. Mampaey, 2007)
 * 
 * @author mniemann
 *
 * @param <T> Identifier of the transaction (i.e. transaction-id)
 */
public class XAPriori<T extends ID> {
	/** Complete data set. */
	private Map<T, Pattern<Missable>> data;
	/** Additional information about frequent pattern after the algorithm finished. */
	private Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> patternData;
	/** Minimum value for support of a pattern. Based on the definition of RAG98 */
	private double minSup;
	/** Minimum value for representativity of a pattern. Based on the definition of RAG98 */
	private double minRep;
	/** Maximum size of a pattern */
	private int patternSize;
	/** Very poor statistics of the data mining process. */ 
	private int pruned, discarded, checked;
	/** Number of threads used by the data mining */
	private final int THREAD_COUNT = 4;
	
	/** Concurrent scans of the data set */
	private Map<Integer, XAPrioriCheckTransactionThread<T>> scanJobs;
	/** Concurrent scans of the data set for checking pattern for being frequent. */
	private Map<Pattern<Missable>, XAPrioriCheckPatternThread<T>> jobs;
	/** Concurrent generation of candidate pattern. */
	private Map<Integer, XAPrioriGenThread> genJobs;
	
	/** Stop flag safely shuts down algorithm. */
	private boolean stopped;
	
	public XAPriori(HashMap<T, Pattern<Missable>> data, double minSup,
			double minRep, int patternSize) {
		this.data = data;
		this.minSup = minSup;
		this.minRep = minRep;
		this.patternSize = patternSize;
		patternData = new HashMap<Pattern<Missable>, Map<InterestingnessMeasure, Double>>();
	}
	
	public Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> getPatternData() {
		return patternData;
	}

	/**
	 * Runs extensible apriori.
	 * @return Set of frequent pattern.
	 */
	public Set<Pattern<Missable>> apriori(){
		Set<Pattern<Missable>> bigL = new HashSet<Pattern<Missable>>();
		Set<Pattern<Missable>> pattern = new HashSet<Pattern<Missable>>();
		int transactionCount = data.size();
		pruned = 0;
		discarded = 0;
		checked = 0;
		/**
		 * L_1 = find_frequent_1-itemsets(D)
		 */
		Set<Pattern<Missable>> bigL_1 = get_extensible_1_itemsets();
		Set<Pattern<Missable>> bigL_k_minus_1 = bigL_1;
		/**
		 * for (k = 2; L_k-1 != emptySet; k++){
		 * alternatively limit pattern-size
		 */
		for (int k = 2; !bigL_k_minus_1.isEmpty() && k <= patternSize; k++){
			System.out.println("xapriori pattern-size: " + k);
			/**
			 * C_k = apriori_gen(L_k-1);
			 */
			Set<Pattern<Missable>> C_k = apriori_gen(bigL_k_minus_1);
			/**
			 * for each transaction t in D { //scan D for counts
			 */
			Map<Pattern<Missable>, Integer> setSupport = new HashMap<Pattern<Missable>, Integer>();
			//scan in multiple jobs
			scanJobs = new HashMap<Integer, XAPrioriCheckTransactionThread<T>>();
			Semaphore scanBarrier = new Semaphore(THREAD_COUNT);
			for (int tNum = 0; tNum < THREAD_COUNT; tNum++){
				scanBarrier.acquireUninterruptibly();
				XAPrioriCheckTransactionThread<T> job = new XAPrioriCheckTransactionThread<T>(C_k, data, scanBarrier, THREAD_COUNT, tNum);
				scanJobs.put(tNum, job);
				Thread thread = new Thread(job);
				thread.start();
			}
			
			scanBarrier.acquireUninterruptibly(THREAD_COUNT);
			//integrate data from jobs
			for (Integer jobID : scanJobs.keySet()){
				Map<Pattern<Missable>, Integer> currentMap = scanJobs.get(jobID).getSetSupport();
				for (Pattern<Missable> set : currentMap.keySet()){
					if (setSupport.containsKey(set)){
						setSupport.put(set, setSupport.get(set) + currentMap.get(set));
					}
					else{
						setSupport.put(set, currentMap.get(set));
					}
				}
			}
			/**
			 * L_k = {c in C_k | c.count >= min_sup} -> extensibility 
			 */
			Set<Pattern<Missable>> L_k = new HashSet<Pattern<Missable>>();
			Set<Pattern<Missable>> kPattern = new HashSet<Pattern<Missable>>();
			
			Semaphore patternBarrier = new Semaphore(THREAD_COUNT);
			
			checked += C_k.size();
			//check each candidate in a separate job
			jobs = new HashMap<Pattern<Missable>, XAPrioriCheckPatternThread<T>>();
			for (Pattern<Missable> c : C_k){
				if (!stopped){
					Pattern<Missable> superC = getSupersetItem(c);
					if (superC != null){
						patternBarrier.acquireUninterruptibly();
						//run as thread
						XAPrioriCheckPatternThread<T> job = new XAPrioriCheckPatternThread<T>(data, c, superC, setSupport, patternBarrier, transactionCount, minRep, minSup);
						jobs.put(c, job);
						Thread thread = new Thread(job);
						thread.start();
					}
				}
			}
			patternBarrier.acquireUninterruptibly(THREAD_COUNT);
			//integrate data from jobs
			for (Pattern<Missable> c : jobs.keySet()){
				L_k.addAll(jobs.get(c).getL_k());
				kPattern.addAll(jobs.get(c).getkPattern());
				patternData.putAll(jobs.get(c).getPatternData());
				pruned += jobs.get(c).getPruned();
				discarded += jobs.get(c).getDiscarded();
			}
			bigL.addAll(L_k);
			pattern.addAll(kPattern);
			bigL_k_minus_1 = L_k;
		}
		
		/**
		 * return L = U_k L_k;
		 */
		return pattern;
	}

	/**
	 * Generates candidate pattern.
	 * @param bigL_k_minus_1
	 * @return
	 */
	private Set<Pattern<Missable>> apriori_gen(Set<Pattern<Missable>> bigL_k_minus_1){
		Set<Pattern<Missable>> bigC_k = new HashSet<Pattern<Missable>>();
		Semaphore barrier = new Semaphore(THREAD_COUNT);
		/**
		 * for each itemset l_1 in L_k-1
		 */
		//create thread for every set
		genJobs = new HashMap<Integer, XAPrioriGenThread>();
		for (int i = 0; i < THREAD_COUNT; i++){
			barrier.acquireUninterruptibly();
			XAPrioriGenThread job = new XAPrioriGenThread(bigL_k_minus_1, barrier, THREAD_COUNT, i);
			genJobs.put(i, job);
			Thread thread = new Thread(job);
			thread.start();
		}
		barrier.acquireUninterruptibly(THREAD_COUNT);
		//integrate data
		for (Integer key : genJobs.keySet()){
			bigC_k.addAll(genJobs.get(key).getBigC_k());
		}
		/**
		 * return C_k;
		 */
		return bigC_k;
	}
	
	
	
	/**
	 * Identifies frequent 1-itemsets. 
	 * @return set of 1-itemsets which are extensible
	 */
	private Set<Pattern<Missable>> get_extensible_1_itemsets(){
		/** frequent-1-itemsets */
		Set<Pattern<Missable>> result = new HashSet<Pattern<Missable>>();
		/** save support of items and superset-items */
		HashMap<Missable, Integer> itemSupport = new HashMap<Missable, Integer>();
		/** iterate over transactions */
		for (T tid : data.keySet()){
			/** iterate over items */
			for (Missable item : data.get(tid)){
				/** generate item eventually */
				if (!itemSupport.containsKey(item)){
					itemSupport.put(item, 0);
				}
				/** add one to support */
				itemSupport.put(item, itemSupport.get(item) + 1);
				/** generate superset-item eventually */
				Missable supersetItem = item.getSuperset();
				if (supersetItem != null){
					if (!itemSupport.containsKey(supersetItem)){
						itemSupport.put(supersetItem, 0);
					}
					/** add one to support */
					itemSupport.put(supersetItem, itemSupport.get(supersetItem) + 1);
				}
			}
		}
		
		/** evaluate extensibility */
		for (Missable item : itemSupport.keySet()){
			/** only evaluate non-superset-items */
			Missable supersetItem = item.getSuperset();
			if (supersetItem != null){
				double support = itemSupport.get(item) * 1.0 / itemSupport.get(supersetItem);
				double representativity = itemSupport.get(supersetItem) * 1.0 / data.keySet().size();
				
				/** if item is extensible */
				//if (itemSupport.get(item) / (minRep * data.size()) >= minSup){
				if (representativity >= minRep){
					Pattern<Missable> frequent_one_set = new Pattern<Missable>();
					frequent_one_set.add(item);
					result.add(frequent_one_set);
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns superset of a pattern. Equivalent to *-operator of CAL07
	 * @param set pattern
	 * @return superset pattern
	 */
	public Pattern<Missable> getSupersetItem(Pattern<Missable> set){
		Pattern<Missable> result = new Pattern<Missable>();
		for (Missable item : set){
			Missable superItem = item.getSuperset();
			if (superItem == null){
				return null;
			}
			result.add(superItem);
		}
		return result;
	}


	public int getPruned() {
		return pruned;
	}

	public int getDiscarded() {
		return discarded;
	}

	public int getChecked() {
		return checked;
	}
	/**
	 * Safely stops the algorithm.
	 */
	public void stop(){
		if (genJobs != null){
			for (Integer key : genJobs.keySet()){
				genJobs.get(key).stop();
			}
		}
		if (scanJobs != null){
			for (Integer key : scanJobs.keySet()){
				scanJobs.get(key).stop();
			}
		}
		if (jobs != null){
			for (Pattern<Missable> key : jobs.keySet()){
				jobs.get(key).stop();
			}
		}
	}
}
