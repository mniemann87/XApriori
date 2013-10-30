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

import java.io.Serializable;

/**
 * Provides a user-visible description/name for any inherited class.
 * 
 * @author mniemann
 * 
 */
public abstract class Nameable implements Serializable {
	private static final long serialVersionUID = 2260492601721275792L;
	/** Description for the user. */

	protected String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Nameable(String description) {
		this.description = description;
	}

	public Nameable() {
		description = "";
	}

}
