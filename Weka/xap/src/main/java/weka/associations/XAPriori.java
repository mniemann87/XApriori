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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datatypes.Missable;
import datatypes.Pattern;


import weka.associations.DefaultAssociationRule.METRIC_TYPE;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import xApriori.InterestingnessMeasure;


public class XAPriori extends AbstractAssociator implements OptionHandler,
		AssociationRulesProducer, TechnicalInformationHandler {

	private static final long serialVersionUID = 6064372461380663461L;
	/** The minimum support. */
	  protected double m_minSupport = 0.25;
	/** The minimum representativity. */
	  protected double m_minRepresentativity = 0.1;
	/** The minimum confidence. */
	  protected double m_minConfidence = 0.7;
	/** Maximum pattern size. */
	  protected int m_maxPatternSize = 5;
	/** Flag indicating whether data is a matrix or row based. */
	//  protected boolean m_matrix = false;
	/** Index of the column with the transaction id. */
	  protected int m_columnId = 0;
	/** Index of the column with the data type. */
	  protected String m_datatype = "1";
	/** Index of the column with the data. */
	  protected String m_data = "2";
	private Set<XAPRichAssociation<Missable>> associations;
	private Instances instances;
	private Map<Pattern<Missable>, Map<InterestingnessMeasure, Double>> patternData;
	  
	@Override
	public void buildAssociations(Instances data) throws Exception {
		instances = data;
		xApriori.XAPriori<XAPTransactionID> xap;
		/** Converted instances */
		HashMap<XAPTransactionID, Pattern<Missable>> transactions = new HashMap<XAPTransactionID, Pattern<Missable>>();
		
		//read in comma-separated list of columns
//		int[] families = divideStringToIntArray(m_datatype);
//		int[] values = divideStringToIntArray(m_data);
//		int length =  Math.min(families.length, values.length);
		int family = Integer.parseInt(m_datatype);
		int value = Integer.parseInt(m_data);
		
		//load data by modes
//		if (m_matrix){
//			//load data as matrix
//			for (Instance currentInstance : data){
//				XAPTransactionID currentID = new XAPTransactionID((int)currentInstance.value(m_columnId));
//				transactions.put(currentID, new Pattern<Missable>());
//				
//				//TODO: handle missing values... recognize "?" and don't add that value, therefore get index of "?" and compare
//				//for (int i = 0; i < length; i++){
//					XAPNominalMissable newItem = new XAPNominalMissable(currentInstance.value(family), currentInstance.value(value));
//					transactions.get(currentID).add(newItem);	
//				//}
//				
//			}
//		}
//		else{
			//load data row based
			for (Instance currentInstance : data){
				XAPTransactionID currentID = new XAPTransactionID((int)currentInstance.value(m_columnId));
				if (!transactions.containsKey(currentID)){
					transactions.put(currentID, new Pattern<Missable>());
				}
				
				//for (int i = 0; i < length; i++){
					XAPNominalMissable newItem = new XAPNominalMissable(currentInstance.value(family), currentInstance.value(value));
					transactions.get(currentID).add(newItem);	
				//}
				
			}
			
//		}
		xap = new xApriori.XAPriori<XAPTransactionID>(transactions, m_minSupport, m_minRepresentativity, m_maxPatternSize);
		System.out.println(m_minSupport + " " + m_minRepresentativity);
		// run frequent pattern mining
		xap.apriori();
		System.out.println("found: " + xap.getPatternData().size() + " checked: " + xap.getChecked() + " pruned: " + xap.getPruned() + " discarded: " + xap.getDiscarded());
		patternData = xap.getPatternData();
		
		System.out.println(patternData);
		//generate associations
		XAPrioriAssociationProducer xapap = new XAPrioriAssociationProducer(transactions, patternData, m_minConfidence, 4);
		associations = xapap.getAssociations();
	}
	public String globalInfo() {
		return "Class implementing an Apriori-type algorithm. Missing values doesn't distort support or confidence. "
	        + "For more information see:\n\n"
	        + getTechnicalInformation().toString();
	}
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();
	
		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);
		//enable numeric attributes in order to get the transaction id
		result.enable(Capability.NUMERIC_ATTRIBUTES);
	
		// class
	    result.enable(Capability.NO_CLASS);
	    result.enable(Capability.NOMINAL_CLASS);
	    result.enable(Capability.MISSING_CLASS_VALUES);
	
	    return result;
	}
	
	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
	    TechnicalInformation additional;

	    result = new TechnicalInformation(Type.INPROCEEDINGS);
	    result.setValue(Field.AUTHOR, "R. Agrawal and R. Srikant");
	    result.setValue(Field.TITLE,
	        "Fast Algorithms for Mining Association Rules in Large Databases");
	    result.setValue(Field.BOOKTITLE,
	        "20th International Conference on Very Large Data Bases");
	    result.setValue(Field.YEAR, "1994");
	    result.setValue(Field.PAGES, "478-499");
	    result.setValue(Field.PUBLISHER, "Morgan Kaufmann, Los Altos, CA");

	    additional = result.add(Type.INPROCEEDINGS);
	    additional.setValue(Field.AUTHOR, "Matthias Niemann");
	    additional.setValue(Field.TITLE,
	        "Development of a toolkit for association rule mining in the medical domain");
	    additional.setValue(Field.YEAR, "2013");
	    additional.setValue(Field.INSTITUTION, "Freie Universitaet zu Berlin");

	    return result;
	}

	@Override
	public AssociationRules getAssociationRules() {
		List<AssociationRule> rules = new ArrayList<AssociationRule>();
		int colFamily = Integer.parseInt(m_datatype);
		int colValue = Integer.parseInt(m_data);
		if (associations != null){
			for (XAPRichAssociation<Missable> currentAssociation : associations){ 
				Collection<Item> premise = new ArrayList<Item>();
				Collection<Item> consequence = new ArrayList<Item>();
				try{
					for (Missable item : currentAssociation.getAntecedent()){
						XAPNominalMissable nm = (XAPNominalMissable) item;
						Item newFamily = new NominalItem(instances.attribute(colFamily), (int)nm.getFamily());
						premise.add(newFamily);
						Item newValue = new NominalItem(instances.attribute(colValue), (int)nm.getValue());
						premise.add(newValue);
					}
					for (Missable item : currentAssociation.getConsequent()){
						XAPNominalMissable nm = (XAPNominalMissable) item;
						Item newFamily = new NominalItem(instances.attribute(colFamily), (int)nm.getFamily());
						consequence.add(newFamily);
						Item newValue = new NominalItem(instances.attribute(colValue), (int)nm.getValue());
						consequence.add(newValue);
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				DefaultAssociationRule dar = new DefaultAssociationRule(premise,
		            consequence, METRIC_TYPE.CONFIDENCE, 0, 0, 0, 0);
				rules.add(dar);
			}
		}
	    return new AssociationRules(rules, this);
	}

	@Override
	public String[] getRuleMetricNames() {
		String[] metricNames = new String[1];
		metricNames[0] = "confidence";
		return metricNames;
	}

	@Override
	public boolean canProduceRules() {
		return true;
	}

	@Override
	public Enumeration listOptions() {
		FastVector newVector = new FastVector(3);

	    newVector.addElement(new Option("minimum support a pattern has to have in contrast to all transactions, that can possibly have the pattern (incomplete transactions are omitted)", "S", 1,"-S <required minimum support>"));
	    newVector.addElement(new Option("minimum representativity a pattern has to have (possible transactions / all transactions)", "R", 1,"-R <required minimum representativity>"));
	    newVector.addElement(new Option("minimum confidence a rule has to have", "C", 1,"-C <required minimum confidence>"));
	    
	    return newVector.elements();
	}

	private void resetOptions(){
		m_minSupport = 0.2;
		m_minRepresentativity = 0.1;
		m_minConfidence = 0.7;
		m_maxPatternSize = 5;
//		m_matrix = false;
		m_columnId = 0;
		m_datatype = "1";
		m_data = "2";
	}
	@Override
	public void setOptions(String[] options) throws Exception {
		resetOptions();
		
		String supportString = Utils.getOption('S', options); 
		String representativityString = Utils.getOption('R', options); 
		String confidenceString = Utils.getOption('C', options);
		String maxPatternSizeString = Utils.getOption('P', options);
		String matrixString = Utils.getOption('M', options);
		String columnString = Utils.getOption('I', options);
		String datatypeString = Utils.getOption('T', options);
		String dataString = Utils.getOption('D', options);
		
		m_minSupport = (new Double(supportString)).doubleValue();
		m_minRepresentativity = (new Double(representativityString)).doubleValue();
		m_minConfidence = (new Double(confidenceString)).doubleValue();
		m_maxPatternSize = new Integer(maxPatternSizeString);
//		m_matrix = new Boolean(matrixString);
		m_columnId = new Integer(columnString);
		m_datatype = datatypeString;
		m_data = dataString;
	}

	@Override
	public String[] getOptions() {
		String[] options = new String[14];
		options[0] = "-S";
		options[1] = "" + m_minSupport;
		options[2] = "-R";
		options[3] = "" + m_minRepresentativity;
		options[4] = "-C";
		options[5] = "" + m_minConfidence;
		options[6] = "-P";
		options[7] = "" + m_maxPatternSize;
		options[8] = "-I";
		options[9] = "" + m_columnId;
		options[10] = "-T";
		options[11] = "" + m_datatype;
		options[12] = "-D";
		options[13] = "" + m_data;
//		options[14] = "-M";
//		options[15] = "" + m_matrix;
		return options;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
//	public boolean getMatrix(){
//		return m_matrix;
//	}
//	public void setMatrix(boolean m_matrix){
//		this.m_matrix = m_matrix;
//	}
	public double getMinSupport() {
		return m_minSupport;
	}

	public void setMinSupport(double m_minSupport) {
		this.m_minSupport = m_minSupport;
	}

	public double getMinRepresentativity() {
		return m_minRepresentativity;
	}

	public void setMinRepresentativity(double m_minRepresentativity) {
		this.m_minRepresentativity = m_minRepresentativity;
	}

	public double getMinConfidence() {
		return m_minConfidence;
	}
	public int getMaxPatternSize(){
		return m_maxPatternSize;
	}
	public void setMaxPatternSize(int m_maxPatternSize){
		this.m_maxPatternSize = m_maxPatternSize;
	}
	public void setMinConfidence(double m_minConfidence) {
		this.m_minConfidence = m_minConfidence;
	}
	public int getColumnId(){
		return m_columnId;
	}
	public void setColumnId(int m_columnId){
		this.m_columnId = m_columnId;
	}
	public String getColumnDatatype(){
		return m_datatype;
	}
	public void setColumnDatatype(String m_datatype){
		this.m_datatype = m_datatype;
	}
	public String getColumnData(){
		return m_data;
	}
	public void setColumnData(String m_data){
		this.m_data = m_data;
	}
	private int[] divideStringToIntArray(String s){
		String[] data = s.split(",");
		int[] result = new int[data.length];
		for (int i = 0; i < data.length; i++){
			result[i] = Integer.parseInt(data[i]);
		}
		return result;
	}
	@Override
	public String toString() {
		int colFamily = Integer.parseInt(m_datatype);
		int colValue = Integer.parseInt(m_data);
		
		StringBuffer text = new StringBuffer();
		text.append("Minimum support: " + m_minSupport + "\n");
		text.append("Minimum representativity: " + m_minRepresentativity + "\n");
		text.append("Minimum confidence: " + m_minConfidence + "\n");
		text.append("\n");
		text.append("large itemsets: " + patternData.keySet().size() + "\n");
		text.append("associations: " + associations.size() + "\n");
		text.append("\n");
		for (XAPRichAssociation<Missable> ra  : associations){
			String antedecent = "", consequence = "";
			for (Missable missable : ra.getAntecedent()){
				XAPNominalMissable nm = (XAPNominalMissable) missable;
				antedecent += (instances.attribute(colFamily).value((int)nm.getFamily()) + "=" + instances.attribute(colValue).value((int)nm.getValue()) + ", ");
			}
			text.append(antedecent.subSequence(0, antedecent.length() - 2));
			text.append(" ==> ");
			for (Missable missable : ra.getConsequent()){
				XAPNominalMissable nm = (XAPNominalMissable) missable;
				consequence += (instances.attribute(colFamily).value((int)nm.getFamily()) + "=" + instances.attribute(colValue).value((int)nm.getValue()) + ", ");
			}
			text.append(consequence.subSequence(0, consequence.length() - 2));
			text.append("      ");
			text.append(ra.getInterestingness() + "\n");
		}
		return text.toString();
	}
	
}
