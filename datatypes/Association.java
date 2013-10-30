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
package datatypes;

/**
 * Basic type of association. Has an antecedent and consequence.
 * @author mniemann
 *
 * @param <T> denotes the type of items in the pattern.
 */
public class Association<T extends Missable> {
	/** Left side of the implication. */
	protected Pattern<T> antecedent;
	/** Right side of the implication. */
	protected Pattern<T> consequent;

	public Association(Pattern<T> antecedent, Pattern<T> consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
	}

	public Pattern<T> getAntecedent() {
		return antecedent;
	}

	public void setAntecedent(Pattern<T> antecedent) {
		this.antecedent = antecedent;
	}

	public Pattern<T> getConsequent() {
		return consequent;
	}

	public void setConsequent(Pattern<T> consequent) {
		this.consequent = consequent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		final int prime2 = 3733;
		int result = 1;
		result = prime * ((antecedent == null) ? 0 : antecedent.hashCode());
		result += prime2 * ((consequent == null) ? 0 : consequent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Association<T> other = (Association<T>) obj;
		if (antecedent == null) {
			if (other.antecedent != null)
				return false;
		} else if (!antecedent.equals(other.antecedent))
			return false;
		if (consequent == null) {
			if (other.consequent != null)
				return false;
		} else if (!consequent.equals(other.consequent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Association [antecedent=" + antecedent + ", consequent="
				+ consequent + "]";
	}
	
	
}
