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
import patternTools.SubsetGenerator;
import xApriori.InterestingnessMeasure;

public class XAPrioriAssociationProducerThread implements Runnable {
	private Map<XAPTransactionID, Pattern<Missable>> data;
	private Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> patternData;
	
	private Set<XAPRichAssociation<Missable>> associations;
	private Semaphore barrier;
	private SubsetGenerator<Missable> sg;
	private double confidence;

	public XAPrioriAssociationProducerThread(
			Map<XAPTransactionID, Pattern<Missable>> data,
			Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> patternData,
			Semaphore barrier, double confidence) {
		super();
		this.data = data;
		this.patternData = patternData;
		this.barrier = barrier;
		associations = new HashSet<XAPRichAssociation<Missable>>();
		sg = new SubsetGenerator<Missable>();
		this.confidence = confidence;
	}


	@Override
	public void run() {
		for (Pattern<Missable> curr : patternData.keySet()){
			Set<Pattern<Missable>> allSubsets = sg.getAllSubsets(curr, 1, 1);
			for (Pattern<Missable> antecedent : allSubsets){
				Pattern<Missable> consequent = sg.getDifference(antecedent, curr);
				Map<InterestingnessMeasure, Double> interestingness = getInterestingnessMeasures(antecedent, consequent, patternData.get(curr));
				if (interestingness.get(InterestingnessMeasure.CONFIDENCE) >= confidence){
					XAPRichAssociation<Missable> ra = new XAPRichAssociation<Missable>(antecedent, consequent, interestingness);
					associations.add(ra);
				}
			}
		}
		barrier.release();
	}


	public Set<XAPRichAssociation<Missable>> getAssociations() {
		return associations;
	}
	private Map<InterestingnessMeasure, Double> getInterestingnessMeasures(Pattern<Missable> antecedent, Pattern<Missable> consequent, Map<InterestingnessMeasure, Double> patternMeasures){
		Map<InterestingnessMeasure, Double> result = new HashMap<InterestingnessMeasure, Double>();
		result.putAll(patternMeasures);
		
		Pattern<Missable> consequentHat = getSuperset(consequent);
		
		int aUb = 0, a = 0, aNotBHat = 0;
		for (XAPTransactionID tid : data.keySet()){
			//determine, whether sets are contained
			boolean allItemsContainedA = data.get(tid).containsAll(antecedent);
			boolean allItemsContainedB = data.get(tid).containsAll(consequent);
			boolean allItemsContainedBHat = true;
			for (Missable item : consequentHat){
				boolean contained = false;
				for (Missable patItem : data.get(tid)){
					if (patItem.equals(item)){
						contained = true;
					}
				}
				allItemsContainedBHat &= contained;
			}
			//count
			if (allItemsContainedA && allItemsContainedB){
				aUb++;
			}
			if (allItemsContainedA){
				a++;
			}
			if (allItemsContainedA && !allItemsContainedBHat){
				aNotBHat++;
			}
			//calculate
			double bottom = a - aNotBHat;
			if (bottom > 0){
				result.put(InterestingnessMeasure.CONFIDENCE, aUb / bottom);
			}
			else{
				result.put(InterestingnessMeasure.CONFIDENCE, 0.);
			}
			
		}
		
		return result;
	}


	private Pattern<Missable> getSuperset(Pattern<Missable> set) {
		Pattern<Missable> result = new Pattern<Missable>();
		for (Missable item : set){
			result.add(item.getSuperset());
		}
		return result;
	}

}
