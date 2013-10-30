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

/**
 * Enumerates all available interestingness measures.
 * @author mniemann
 *
 */
public enum InterestingnessMeasure {
	SUPPORT, REPRESENTATIVITY, ALLCONFIDENCE, CONFIDENCE, SENSITIVITY, SPECIFICITY, LEVERAGE, ACCURACY, LIFT, ODDSRATIO
}
