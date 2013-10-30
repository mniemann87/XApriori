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
 * Introduces hierarchical information to inherited classes. This is essential
 * for the correct evaluation of missingness in the data mining algorithms.
 * 
 * @author mniemann
 * 
 */
public abstract class Missable extends Nameable implements Comparable<Missable> {

	private static final long serialVersionUID = 2323161175343371650L;

	/**
	 * Return the item, this object belongs to. I.e. an item "my value: low"
	 * must return "my value: null".
	 * 
	 * @return super item
	 */
	public abstract Missable getSuperset();

	public abstract Missable clone();

	public int compareTo(Missable other) {
		if (this.equals(other) || this.hashCode() == other.hashCode()) {
			return 0;
		} else {
			if (this.hashCode() < other.hashCode()) {
				return -1;
			} else {
				return 1;
			}
		}
	}

}
