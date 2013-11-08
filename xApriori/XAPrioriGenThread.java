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

import java.util.HashSet;

import java.util.Set;
import java.util.concurrent.Semaphore;

import datatypes.Missable;
import datatypes.Pattern;



/**
 * Generates candidate patterns.
 * @author mniemann
 *
 */
public class XAPrioriGenThread implements Runnable {
	private Set<Pattern<Missable>> bigC_k;
	private Set<Pattern<Missable>> bigL_k_minus_1;
	/**
	 * barrier for jobs, acquire outside, release after run() has finished
	 */
	private Semaphore barrier;
	/**
	 * maximum number of threads
	 */
	private int threadMax;
	/**
	 * id of this thread (between 0 and threadMax)
	 */
	private int thread;

	private boolean stopped = false;
	
	public XAPrioriGenThread(Set<Pattern<Missable>> bigL_k_minus_1,
			Semaphore barrier, int threadMax, int thread) {
		this.bigL_k_minus_1 = bigL_k_minus_1;
		this.barrier = barrier;
		this.threadMax = threadMax;
		this.thread = thread;
	}
	@Override
	public void run() {
		bigC_k = new HashSet<Pattern<Missable>>();
		/**
		 * for each itemset l_1 in L_k-1
		 */
		for (Pattern<Missable> l_1 : bigL_k_minus_1){
			if (!stopped){
				if (l_1.hashCode() % threadMax == thread){
					/**
					 * for each itemset l_2 in L_k-1
					 */
					for (Pattern<Missable> l_2 : bigL_k_minus_1){
						/**
						 * if (l_1[1] == l_2[1]) & (l_1[2] == l_2[2]) & ... & (l_1[k-2] == l_2[k-2]) & (l_1[k-1] < l_2[k-1])
						 */
						if (itemSetsEqualExceptLast(l_1, l_2)){
							/**
							 * c = l_1 x l_2; //join step: generate candidates
							 */
							Pattern<Missable> c = joinCandidates(l_1, l_2);
							/**
							 * if has_infrequent_subset(c, L_k-1) then
							 */
							if (!has_infrequent_subset(c, bigL_k_minus_1)){
								/**
								 * add c to C_k;
								 */
								bigC_k.add(c);
							}
						}
					}
				}
			}
		}
		barrier.release();
	}
	public Set<Pattern<Missable>> getBigC_k() {
		return bigC_k;
	}
	private boolean has_infrequent_subset(Pattern<Missable> c, Set<Pattern<Missable>> bigL_k_minus_1){
		/**
		 * for each (k-1)-subset s of c
		 */
		for (Missable subtraction : c){
			Pattern<Missable> s = cloneSet(c);
			s.remove(subtraction);
			
			/**
			 * if s not in L_k-1 then
			 */
			if (!bigL_k_minus_1.contains(s)){
				/**
				 * return true
				 */
				return true;
			}
		}
		/**
		 * return false
		 */
		return false;
	}
	private boolean itemSetsEqualExceptLast(Pattern<Missable> l1, Pattern<Missable> l2){
		if (l1.getDimension() != l2.getDimension()){
			return false;
		}
		
		Missable missing = null;
		Pattern<Missable> l2clone = cloneSet(l2);
		for (Missable itemL1 : l1){
			if (!l2clone.contains(itemL1)){
				if (missing == null){
					missing = itemL1;
				}
				else{
					return false;
				}
			}
			else{
				l2clone.remove(itemL1);
			}
		}
		if (missing == null | l2clone.getDimension() > 1){
			return false;
		}
		if (missing.compareTo( (Missable) l2clone.toArray()[0]) < 0){
			return true;
		}
		return false;
	}
	private Pattern<Missable> joinCandidates(Pattern<Missable>l_1, Pattern<Missable> l_2){
		Pattern<Missable> result = cloneSet(l_1);
		for (Missable l_2_item : l_2){
			if (!result.contains(l_2_item)){
				result.add(l_2_item);
			}
		}
		return result;
	}
	private Pattern<Missable> cloneSet(Pattern<Missable> set){
		Pattern<Missable> newSet = new Pattern<Missable>();
		for (Missable item : set){
			newSet.add(item);
		}
		return newSet;
	}
	public void stop(){
		stopped = true;
	}

}
