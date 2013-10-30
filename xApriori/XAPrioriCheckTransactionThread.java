/*******************************************************************************
 * Copyright (c) 2013 Matthias Niemann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
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
 * Scans the data set for support counts for the given set of pattern.
 * @author mniemann
 *
 * @param <T>
 */
public class XAPrioriCheckTransactionThread<T extends ID> implements Runnable {
	private Set<Pattern<Missable>> C_k;
	private Map<T, Pattern<Missable>> data;
	private Map<Pattern<Missable>, Integer> setSupport;
	private Semaphore barrier;
	private int threadMax, thread;
	private boolean stopped = false;
	public XAPrioriCheckTransactionThread(Set<Pattern<Missable>> c_k,
			Map<T, Pattern<Missable>> data, Semaphore barrier, int threadMax, int thread) {
		C_k = c_k;
		this.data = data;
		this.barrier = barrier;
		this.thread = thread;
		this.threadMax = threadMax;
	}
	
	public Map<Pattern<Missable>, Integer> getSetSupport() {
		return setSupport;
	}

	@Override
	public void run() {
		setSupport = new HashMap<Pattern<Missable>, Integer>();
		for (T tid : data.keySet()){	
			if (!stopped){
				if (tid.getValue() % threadMax == thread){
					/**
					 * C_t = subset(C_k, t); //get the subsets of t that are candidates
					 * for each candidate c in C_t
					 * 	c.count++;
					 */
					Set<Pattern<Missable>> alreadyCounted = new HashSet<Pattern<Missable>>();
					for (Pattern<Missable> c : C_k){
						//get superset
						Pattern<Missable> superC = getSupersetItem(c);
						//create items in support-map eventually
						if (!setSupport.containsKey(c)){
							setSupport.put(c, 0);
						}
						if (!setSupport.containsKey(superC)){
							setSupport.put(superC, 0);
						}
						//check c for being in set
						if (data.get(tid).containsAll(c)){
							setSupport.put(c, setSupport.get(c) + 1);
							//if c is in, superC is in, too
							if (!alreadyCounted.contains(superC)){
								setSupport.put(superC, setSupport.get(superC) + 1);
								alreadyCounted.add(superC);
							}
						}
						else{
							//check super c for being in set (more complicate, due to hashing)
							boolean superCcontained = true;
							for (Missable superCItem : superC){
								boolean itemContained = false;
								for (Missable dataItem : data.get(tid)){
									if (dataItem.equals(superCItem)){
										itemContained = true;
									}
								}
								superCcontained &= itemContained;
							}
							if (superCcontained && !alreadyCounted.contains(superC)){
								setSupport.put(superC, setSupport.get(superC) + 1);
								alreadyCounted.add(superC);
							}
						}
					}
				}
			}
		}
		barrier.release();
	}
	private Pattern<Missable> getSupersetItem(Pattern<Missable> set){
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
	public void stop(){
		stopped = true;
	}
}
