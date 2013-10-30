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

import datatypes.Missable;


public class XAPNominalMissable extends Missable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6686340235402197464L;

	private double family;
	private double value;
	
	public XAPNominalMissable(double family, double value) {
		super();
		this.family = family;
		this.value = value;
	}

	@Override
	public Missable clone() {
		return new XAPNominalMissable(family, value);
	}

	@Override
	public Missable getSuperset() {
		if (value >= 0.0){
			return new XAPNominalMissable(family, -1);
		}
		else{
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime1 = 31;
		final int prime2 = 797;
		int result = 1;
		result += prime1 * (1 + (int) family);
		result += prime2 * (1 + (int) value);
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
		XAPNominalMissable other = (XAPNominalMissable) obj;

		if (Double.doubleToLongBits(family) != Double
				.doubleToLongBits(other.family))
			return false;
		if (Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value) && !(value < 0 || other.value < 0))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[f=" + family + ", v=" + value + "]";
	}

	public double getFamily() {
		return family;
	}

	public double getValue() {
		return value;
	}
	

}
