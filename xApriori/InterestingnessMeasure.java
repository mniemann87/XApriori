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
package xApriori;

/**
 * Enumerates all available interestingness measures.
 * @author mniemann
 *
 */
public enum InterestingnessMeasure {
	SUPPORT, REPRESENTATIVITY, ALLCONFIDENCE, CONFIDENCE, SENSITIVITY, SPECIFICITY, LEVERAGE, ACCURACY, LIFT, ODDSRATIO
}
