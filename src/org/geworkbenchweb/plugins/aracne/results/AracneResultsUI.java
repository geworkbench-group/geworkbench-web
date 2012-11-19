package org.geworkbenchweb.plugins.aracne.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.visualizations.Cytoscape;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.ui.VerticalLayout;

public class AracneResultsUI extends VerticalLayout {


	private static final long serialVersionUID = 1L;
	
	private AdjacencyMatrixDataSet adjMatrix = null;

	public AracneResultsUI(Long dataSetId) {
		
		Map<String, Object> parameters 	= 	new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list("Select p from ResultSet as p where p.id=:id", parameters);
		
		adjMatrix = (AdjacencyMatrixDataSet) ObjectConversion.toObject(data.get(0).getData());
		
		setImmediate(true);
		setSizeFull();
		
		/* Preparing data for cytoscape */
		ArrayList<String> nodes = new ArrayList<String>();
		ArrayList<String> edges = new ArrayList<String>();
		
		for(int i=0; i<adjMatrix.getMatrix().getEdges().size(); i++) {
			String id1;  
			String label1;	
			String id2;  
			String label2;	
			if (adjMatrix.getMatrix().getEdges().get(i).node1.type.equals(NodeType.MARKER))
			{
				id1 = adjMatrix.getMatrix().getEdges().get(i).node1.marker.getLabel();
				label1 = adjMatrix.getMatrix().getEdges().get(i).node1.marker.getGeneName();
				id2 = adjMatrix.getMatrix().getEdges().get(i).node2.marker.getLabel();
				label2 = adjMatrix.getMatrix().getEdges().get(i).node2.marker.getGeneName();
				
			}else 
			{
				id1 = adjMatrix.getMatrix().getEdges().get(i).node1.getStringId();
				label1 = id1;
				id2 = adjMatrix.getMatrix().getEdges().get(i).node2.getStringId();
				label2 = id2;
			} 			
			
			String edge = id1 + "," + id2;		 
			
			String node1 	= 	id1 + "," + label1 + ",0";
			String node2	=	id2 + "," + label2 + ",0";
			
			if(edges.isEmpty()) {
				edges.add(edge);
			}else if(!edges.contains(edge)) {
				edges.add(edge);
			}
			
			if(nodes.isEmpty()) {
				nodes.add(node1);
				nodes.add(node2);
			} else { 
				if(!nodes.contains(node1)) {
					nodes.add(node1);
				}
				if(!nodes.contains(node2)) {
					nodes.add(node2);
				}
			}
		}
		
		Cytoscape cy = new Cytoscape();
		cy.setImmediate(true);
		cy.setSizeFull();
		
		String[] nodeArray = new String[nodes.size()];
		String[] edgeArray = new String[edges.size()];

		nodeArray = nodes.toArray(nodeArray);
	    edgeArray = edges.toArray(edgeArray);
	    
		cy.setNodes(nodeArray);
		cy.setEdges(edgeArray);
		
		addComponent(cy);
	}
}
