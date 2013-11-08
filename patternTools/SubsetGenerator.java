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
package patternTools;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import datatypes.Missable;
import datatypes.Pattern;

/**
 * Some set-computation-operations on pattern.
 * @author mniemann
 *
 */
public class SubsetGenerator<T extends Missable> {
	/**
	 * Returns the power set of a pattern.
	 * @param set the pattern
	 * @param bottomExcept levels, that will be ignored from the bottom of the pyramid
	 * @param topExcept levels, that will be ignored from the top of the pyramid
	 * @return set of subsets
	 */
	public Set<Pattern<T>> getAllSubsets (Pattern<T> set, int bottomExcept, int topExcept){
		Set<Pattern<T>> result = new HashSet<Pattern<T>>();
		//iterate over 0 to 2^set.size()
		if (set != null){
			for (int i = 0; i < Math.pow(2, set.getDimension()); i++){
				Pattern<T> temp = new Pattern<T>();
				//iterate over the set
				int n = i;
				for (int k = 0; k < set.getDimension(); k++){
					//if position is mod 2, add it to temp-set
					if (n % 2 == 1){
						temp.add((T) set.toArray()[k]);
					}
					n = n/2;
				}
				if (temp.getDimension() >= bottomExcept && temp.getDimension() <= set.getDimension() - topExcept){
					result.add(temp);
				}
			}
		}
		return result;
	}
	/**
	 * Subtracts pattern b from a.
	 * @param a
	 * @param b
	 * @return new object a-b
	 */
	public Pattern<T> getDifference(Pattern<T> a, Pattern<T> b){
		Pattern<T> res = new Pattern<T>();
		for (T si : b){
			if (!a.contains(si)){
				res.add(si);
			}
		}
		return res;
	}
}
