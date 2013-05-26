package org.geworkbenchweb.events;

import java.io.Serializable;
import java.util.HashMap;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.ResultSet;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class AnalysisSubmissionEvent implements Event {

	private DSDataSet<?> dataSet;
	
	private ResultSet resultSet;
	
	private HashMap<Serializable, Serializable> params;
	
	final private AnalysisUI analysisUi;

	final private Long datasetId;
	
	// temporary solution: to have both versions available
	public AnalysisSubmissionEvent(Long datasetId, ResultSet resultSet, HashMap<Serializable, Serializable> params,
			AnalysisUI analysisUi) {
		this.datasetId 	= 	datasetId;
		this.dataSet 	= 	null;
		this.resultSet	=	resultSet;
		this.params		=	params;
		this.analysisUi   =   analysisUi;
	}
	
	public AnalysisSubmissionEvent(DSDataSet<?> dataSet, ResultSet resultSet, HashMap<Serializable, Serializable> params,
			AnalysisUI analysisUi) {
		this.datasetId = null;
		this.dataSet 	= 	dataSet;
		this.resultSet	=	resultSet;
		this.params		=	params;
		this.analysisUi   =   analysisUi;
	}
	
	public DSDataSet<?> getDataSet() {
		return dataSet;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}
	
	public HashMap<Serializable, Serializable> getParameters() {
		return params;
	}
	
	public interface AnalysisSubmissionEventListener extends Listener {
		public void SubmitAnalysis(final AnalysisSubmissionEvent event);
	}
	
	public AnalysisUI getAnalaysisUI() {
		return analysisUi;
	}

	public Long getDatasetId() {
		return datasetId;
	}
}