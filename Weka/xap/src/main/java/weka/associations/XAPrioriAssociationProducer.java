/*******************************************************************************
 * Copyright (c) 2013 Matthias Niemann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Matthias Niemann - initial API and implementation
 ******************************************************************************/
package weka.associations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import datatypes.Missable;
import datatypes.Pattern;
import xApriori.InterestingnessMeasure;

public class XAPrioriAssociationProducer {
	private Map<XAPTransactionID, Pattern<Missable>> data;
	private Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> frequentPattern;
	
	private Set<XAPRichAssociation<Missable>> associations;
	
	private double confidence;
	private int threads;

	public XAPrioriAssociationProducer(
			Map<XAPTransactionID, Pattern<Missable>> data,
			Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> frequentPattern, double confidence,
			int threads) {
		super();
		this.data = data;
		this.frequentPattern = frequentPattern;
		this.confidence = confidence;
		this.threads = threads;
	}
	public Set<XAPRichAssociation<Missable>> getAssociations(){
		associations = new HashSet<XAPRichAssociation<Missable>>();
		
		Semaphore barrier = new Semaphore(threads);
		Set<XAPrioriAssociationProducerThread> threadSet = new HashSet<XAPrioriAssociationProducerThread>();
		
		Map<Integer, Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>>> patternForThread = new HashMap<Integer, Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>>>();
		
		//divide set of patterns
		int i = 0;
		for (Pattern<Missable> curr : frequentPattern.keySet()){
			int thread = i % threads;
			//create new set eventually
			if (!patternForThread.containsKey(thread)){
				patternForThread.put(thread, new HashMap<Pattern<Missable>, Map<InterestingnessMeasure, Double>>());
			}
			patternForThread.get(thread).put(curr, frequentPattern.get(curr));
			i++;
		}
		//create individual threads and run
		for (int t = 0; t < threads; t++){
			barrier.acquireUninterruptibly();
			//copy data for each thread
			Map<XAPTransactionID, Pattern<Missable>> dataClone = new HashMap<XAPTransactionID, Pattern<Missable>>();
			dataClone.putAll(data);
			
			XAPrioriAssociationProducerThread xapt = new XAPrioriAssociationProducerThread(dataClone, patternForThread.get(t), barrier, confidence);
			threadSet.add(xapt);
			Thread thr = new Thread(xapt);
			thr.start();
		}
		barrier.acquireUninterruptibly(threads);
		
		//integrate data
		for (XAPrioriAssociationProducerThread xapt : threadSet){
			associations.addAll(xapt.getAssociations());
		}
		
		return associations;
	}
	
}
