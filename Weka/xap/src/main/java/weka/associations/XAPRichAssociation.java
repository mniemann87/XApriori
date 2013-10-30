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

import java.util.Map;

import datatypes.Association;
import datatypes.Missable;
import datatypes.Pattern;
import xApriori.InterestingnessMeasure;


public class XAPRichAssociation<T extends Missable> extends Association<T> {
	private Map<InterestingnessMeasure, Double> interestingness;
	
	public XAPRichAssociation(Pattern<T> antedecent, Pattern<T> consequent) {
		super(antedecent, consequent);
	}

	public XAPRichAssociation(Pattern<T> antedecent, Pattern<T> consequent,
			Map<InterestingnessMeasure, Double> interestingness) {
		super(antedecent, consequent);
		this.interestingness = interestingness;
	}

	public Map<InterestingnessMeasure, Double> getInterestingness() {
		return interestingness;
	}

	public void setInterestingness(
			Map<InterestingnessMeasure, Double> interestingness) {
		this.interestingness = interestingness;
	}

	@Override
	public String toString() {
		return antedecent + " ==> " + consequent + "   " + interestingness;
	}

	
	

}
