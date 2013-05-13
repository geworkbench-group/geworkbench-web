package org.geworkbenchweb.plugins.cnkb.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.model.clusters.CSHierClusterDataSet;
import org.geworkbench.components.interactions.cellularnetwork.InteractionsConnectionImpl;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.CellularNetworkPreference;
import org.geworkbench.util.network.InteractionDetail;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.utils.ObjectConversion;
import org.geworkbenchweb.utils.UserDirUtils;
import org.geworkbenchweb.visualizations.Cytoscape; 
import org.geworkbenchweb.plugins.PluginEntry;
import org.geworkbenchweb.plugins.Visualizer;
import org.geworkbenchweb.plugins.cnkb.CNKBParameters;
import org.geworkbenchweb.plugins.cnkb.NetworkCreation;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.cnkb.CNKBParameters;
import org.vaadin.appfoundation.authentication.SessionHandler;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.ChartSVGAvailableEvent;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.Grid;
import com.invient.vaadin.charts.InvientChartsConfig.DataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.Margin;
import com.invient.vaadin.charts.InvientChartsConfig.LineConfig;
import com.invient.vaadin.charts.InvientChartsConfig.NumberXAxis;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.addon.tableexport.CsvExport;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.Reindeer;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

import org.geworkbenchweb.plugins.cnkb.CNKBResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class displays CNKB results in a Table and also a graph
 * 
 * @author Nikhil Reddy
 */
@SuppressWarnings("unused")
public class CNKBResultsUI extends VerticalLayout implements Visualizer { // TabSheet {

	private static final long serialVersionUID = 1L;
	
	private static Log log = LogFactory.getLog(CNKBResultsUI.class);

	private VerticalSplitPanel tabPanel;

	private ArrayList<Double> totalInteractionConfidence = new ArrayList<Double>();

	private Map<String, List<Double>> ConfidentDataMap = new HashMap<String, List<Double>>();

	private static InvientCharts plot;

	private static CNKBResultsUI menuBarInstance;

	private static Table dataTable;
	private static Map<String, String> confidentTypeMap = null;

	private Cytoscape cy;

	final private Long datasetId;

	public CNKBResultsUI(Long dataSetId) {
		datasetId = dataSetId;
		if(dataSetId==null) return;

		final CNKBResultSet  resultSet = (CNKBResultSet) ObjectConversion
				.toObject(UserDirUtils.getResultSet(dataSetId));
		final Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
		final Short confidentType  = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		
		if (confidentTypeMap == null)
			loadConfidentTypeMap();
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("id", dataSetId);
		List<ResultSet> data = FacadeFactory.getFacade().list(
				"Select p from ResultSet as p where p.id=:id", parameters);
		Long parentId = data.get(0).getParent();

		setSizeFull();
		setImmediate(true);
		setStyleName(Reindeer.TABSHEET_SMALL);

		tabPanel = new VerticalSplitPanel();
		tabPanel.setSizeFull();
		tabPanel.setSplitPosition(400, Sizeable.UNITS_PIXELS);
		tabPanel.setStyleName("small");
		tabPanel.setLocked(false);

		// Results Table Code
		dataTable = new Table();
		dataTable.setColumnCollapsingAllowed(true);
		dataTable.setColumnReorderingAllowed(true);
		dataTable.setSizeFull();
		dataTable.setImmediate(true);

		IndexedContainer dataIn = new IndexedContainer();

		List<String> selectedTypes = getInteractionTypes(resultSet);

		for (int j = 0; j < hits.size(); j++) {
			Item item = dataIn.addItem(j);
			ArrayList<InteractionDetail> interactionDetail = hits.get(j)
					.getSelectedInteractions(selectedTypes, confidentType);
			if (interactionDetail != null) {
				for (InteractionDetail interaction : interactionDetail) {
					totalInteractionConfidence.add(interaction
							.getConfidenceValue(interaction
									.getConfidenceTypes().get(0)));
					String interactionType = interaction.getInteractionType();
					if (ConfidentDataMap.get(interactionType) == null) {
						List<Double> confidenceList = new ArrayList<Double>();
						ConfidentDataMap.put(interactionType, confidenceList);
					}
					ConfidentDataMap.get(interactionType).add(
							interaction.getConfidenceValue(interaction
									.getConfidenceTypes().get(0)));
				}
			}

			dataIn.addContainerProperty("Marker", String.class, null);
			dataIn.addContainerProperty("Gene", String.class, null);
			dataIn.addContainerProperty("Gene Type", String.class, null);
			dataIn.addContainerProperty("Annotation", String.class, null);
			for (String selectedType : selectedTypes)
				dataIn.addContainerProperty(selectedType + " #", Integer.class,
						null);

			item.getItemProperty("Marker").setValue(
					hits.get(j).getdSGeneMarker().getLabel());
			if (hits.get(j).getdSGeneMarker().getShortName() == hits.get(j)
					.getdSGeneMarker().getGeneName()) {
				item.getItemProperty("Gene").setValue("--");
			} else {
				item.getItemProperty("Gene").setValue(
						hits.get(j).getdSGeneMarker().getGeneName());
			}

			item.getItemProperty("Gene Type").setValue(
					hits.get(j).getGeneType());
			item.getItemProperty("Annotation").setValue(
					hits.get(j).getGoInfoStr());

			for (String selectedType : selectedTypes)
				item.getItemProperty(selectedType + " #").setValue(
						(hits.get(j).getSelectedInteractions(selectedType, confidentType))
								.size());

		}

		dataTable.setContainerDataSource(dataIn);
		dataTable.setColumnWidth("Marker", 300);
		dataTable.setColumnWidth("Annotation", 150);
		dataTable.setStyleName(Reindeer.TABLE_STRONG);

		plot = drawPlot(resultSet);
		tabPanel.setFirstComponent(plot);
		tabPanel.setSecondComponent(dataTable);

		Button createNetworkButton;
		createNetworkButton = new Button("Create Network");
		createNetworkButton.addListener(new CreateNetworkListener(parentId,
				resultSet, selectedTypes));

		addComponent(createNetworkButton);
		addComponent(tabPanel);
		setExpandRatio(tabPanel, 1);

	}

	/**
	 * This method draws the Throttle Graph using Invient Charts Add-on.
	 * 
	 */
	private InvientCharts drawPlot(CNKBResultSet resultSet) {

		InvientChartsConfig chartConfig = new InvientChartsConfig();
		chartConfig.getGeneralChartConfig().setMargin(new Margin());
		chartConfig.getGeneralChartConfig().getMargin().setRight(30);

		chartConfig.getTitle().setText(resultSet.getCellularNetworkPreference().getTitle());

		NumberXAxis numberXAxis = new NumberXAxis();
		Short confidenceType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		String axisTile = null;
		if (confidenceType != null)
			axisTile = confidentTypeMap.get(confidenceType.toString());
		if (axisTile != null)
			numberXAxis.setTitle(new AxisTitle(axisTile));
		else			
		   numberXAxis.setTitle(new AxisTitle("Likelihood"));

		LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
		xAxesSet.add(numberXAxis);
		chartConfig.setXAxes(xAxesSet);		
		
		NumberYAxis numberYAxis = new NumberYAxis();
		numberYAxis.setGrid(new Grid());
		numberYAxis.getGrid().setLineWidth(1);
		numberYAxis.setMin(0d);
		
		numberYAxis.setTitle(new AxisTitle("# Interactions"));
		LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
		yAxesSet.add(numberYAxis);
		chartConfig.setYAxes(yAxesSet);

		chartConfig.getTooltip().setEnabled(true);
		// Series data label formatter
		LineConfig lineCfg = new LineConfig();
		chartConfig.addSeriesConfig(lineCfg);
       
	    double maxX  = 1.00001d;
	    double smallestIncrement = 0.01d;
		Double maxConfidenceValue = resultSet.getCellularNetworkPreference().getMaxConfidenceValue(confidenceType);
		if (maxConfidenceValue != null && maxConfidenceValue > 1) {
			int a = (int) Math.log10(maxConfidenceValue);
			double b = maxConfidenceValue / (Math.pow(10, a));
			maxX  = Math.ceil(b);
			maxX = maxX * (Math.pow(10, a));
			smallestIncrement =  maxX / 100;	
			log.debug("maxConfidenceValue is " + maxConfidenceValue);
			log.debug("maxX is " + maxX + ", smallestIncrement is " + smallestIncrement);
		}
		else		
		    numberXAxis.setMax(maxX);
		
         
		/* Tooltip formatter */
		if (maxConfidenceValue != null && maxConfidenceValue <= 1 )
		    chartConfig.getTooltip().setFormatterJsFunc(
				"function() { "
						+ " return '<b>' + this.series.name + '</b><br/>' +  "
						+ "Math.round(((this.x+0.005)*100))/100 + ' '+ "
						+ "' to 1 - ' + " + "this.y + ' interactions'" + "}");

		else //this need to be fixed. Don't know how to pass maxX to function
			 chartConfig.getTooltip().setFormatterJsFunc(
						"function() { "
								+ " return '<b>' + this.series.name + '</b><br/>' +  "
								+ "Math.round(((this.x+0.005)*100))/100 + ' to max value - '+ "
								+ "this.y + ' interactions'" + "}");

		InvientCharts chart = new InvientCharts(chartConfig);

		XYSeries seriesData = new XYSeries("Total Distribution");
		seriesData.setSeriesPoints(getTotalDistribution(seriesData, smallestIncrement));
		chart.addSeries(seriesData);

		for (String interactionType : ConfidentDataMap.keySet()) {
			seriesData = new XYSeries(interactionType);			 
			seriesData.setSeriesPoints(getDistribution(interactionType, smallestIncrement));
			chart.addSeries(seriesData);
		}

		return chart;
	}

	/**
	 * Method is used to calculate the graph points for Protein-Protein
	 * Interactions
	 */
	private LinkedHashSet<DecimalPoint> getDistribution(String interactionType, double smallestIncrement) {

		XYSeries seriesData = new XYSeries(interactionType);
		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
		Double x = null;
		Double y = null;
		int[] distribution = new int[101];

		List<Double> confidenceList = ConfidentDataMap.get(interactionType);
		for (int m = 0; m < confidenceList.size(); m++) {
			int confidence = (int) ((confidenceList.get(m))  / smallestIncrement);	
			if (confidence >= distribution.length )
			{				
				log.warn("This shall not happen: confidence = " + confidence );
				confidence = distribution.length - 1;				
			}
			if (confidence >= 0) {
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.0;
		for (int j = 0; j < distribution.length; j++) {
			y = (double) (distribution[j]);
			points.add(new DecimalPoint(seriesData, x, y));
			x = x + smallestIncrement;
		}
		return points;
	}

	/**
	 * Method is used to calculate the graph points for all Interactions
	 */
	private LinkedHashSet<DecimalPoint> getTotalDistribution(XYSeries seriesData, double smallestIncrement) {

		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();

		Double x = null;
		Double y = null;
		int[] distribution = new int[101];

		for (int m = 0; m < totalInteractionConfidence.size(); m++) {
			int confidence = (int) ((totalInteractionConfidence.get(m))   / smallestIncrement);		 		
			if (confidence >= distribution.length)
			{
				confidence = distribution.length - 1;
				log.warn("This shall not happen: confidence = " + confidence );
			}
			if (confidence >= 0) {				 
				for (int i = 0; i <= confidence; i++) {
					distribution[i]++;
				}
			}
		}
		x = 0.0d;
		for (int j = 0; j < distribution.length; j++) {
			y = (double) (distribution[j]);
			points.add(new DecimalPoint(seriesData, x, y));
			x = x + smallestIncrement;
		}
		return points;
	}

	/**
	 * Called to Export SVG of the Throttle Graph
	 */
	public void plotExportSVG() {
		plot.addListener(new InvientCharts.ChartSVGAvailableListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void svgAvailable(
					ChartSVGAvailableEvent chartSVGAvailableEvent) {

				System.out.println(chartSVGAvailableEvent.getSVG());
			}
		});
	}

	/**
	 * Used to print the Throttle Graph of the CNKB Component
	 */
	public void plotPrint() {

		plot.print();

	}

	/**
	 * Called to export Table to Excel sheet or CSV
	 */
	public static void exportInteractionTable(String format) {

		if (format.equalsIgnoreCase("excel")) {

			ExcelExport excelExport = new ExcelExport(dataTable);
			excelExport.excludeCollapsedColumns();
			excelExport.setExportFileName("CNKBInteractions.xls");
			excelExport.export();

		} else {

			CsvExport csvExport = new CsvExport(dataTable);
			csvExport.excludeCollapsedColumns();
			csvExport.setExportFileName("CNKBInteractions.csv");
			csvExport.export();

		}

	}

	private class CreateNetworkListener implements ClickListener {

		private static final long serialVersionUID = 831124091338570481L;

		private Long parentId;		
		private CNKBResultSet resultSet;		 
	 
		public CreateNetworkListener(Long parentId,
				CNKBResultSet resultSet,
				List<String> selectedTypes) {

			this.parentId = parentId;
			this.resultSet = resultSet;            
		}

		private int getInteractionTotalNum(short confidentType) {

			int interactionNum = 0;
			Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
			List<String> selectedTypes = resultSet.getCellularNetworkPreference().getDisplaySelectedInteractionTypes();
			for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

				ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
						.getSelectedInteractions(selectedTypes, confidentType);

				interactionNum = interactionNum + arrayList.size();

			}

			return interactionNum;

		}

		@Override
		public void buttonClick(ClickEvent event) {

			Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
			if (hits == null || getInteractionTotalNum(resultSet.getCellularNetworkPreference().getSelectedConfidenceType()) == 0) {

				MessageBox mb = new MessageBox(getWindow(), "Warning",
						MessageBox.Icon.INFO,
						"There is no interaction to create a network. ",
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				mb.show();
				return;
			}
			HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>();
			params.put(CNKBParameters.CNKB_RESULTSET, resultSet);
			 
			ResultSet resultSet = new ResultSet();
			java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
			resultSet.setDateField(date);
			String dataSetName = "Cytoscape - Pending";
			resultSet.setName(dataSetName);
			resultSet.setType(AdjacencyMatrixDataSet.class.getName());
			resultSet.setParent(parentId);
			resultSet.setOwner(SessionHandler.get().getId());
			FacadeFactory.getFacade().store(resultSet);

			NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
			GeworkbenchRoot.getBlackboard().fire(resultEvent);

			AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(
					null, resultSet, params, new NetworkCreation());
			GeworkbenchRoot.getBlackboard().fire(analysisEvent);
		}
	}

	private List<String> getInteractionTypes(CNKBResultSet  resultSet)
	{

		Vector<CellularNetWorkElementInformation> hits = resultSet.getCellularNetWorkElementInformations();
		short confidentType = resultSet.getCellularNetworkPreference().getSelectedConfidenceType();
		List<String> interactionTypes = resultSet.getCellularNetworkPreference().getDisplaySelectedInteractionTypes();
	 
		List<String> selectedTypes = new ArrayList<String>();

		for (int j = 0; j < hits.size(); j++) {

			ArrayList<InteractionDetail> interactionDetail = hits.get(j)
					.getSelectedInteractions(interactionTypes, confidentType);
			if (interactionDetail != null) {
				for (InteractionDetail interaction : interactionDetail) {
					String interactionType = interaction.getInteractionType();
					if (selectedTypes.contains(interactionType))
						continue;
					else
						selectedTypes.add(interactionType);

				}
			}
		}

		return selectedTypes;
	} 
	
	private void loadConfidentTypeMap()
	{
		if (ResultSetlUtil.getUrl() == null || ResultSetlUtil.getUrl().trim().equals(""))
		{
			String interactionsServletUrl = "http://cagridnode.c2b2.columbia.edu:8080/cknb/InteractionsServlet_new/InteractionsServlet";			 
			ResultSetlUtil.setUrl(interactionsServletUrl);
			ResultSetlUtil.setTimeout(3000);
		}
		
		InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
		try{
		   confidentTypeMap =  interactionsConnection.getConfidenceTypeMap();
		}
		   catch(Exception ex)
		{
			   ex.printStackTrace();
		}
		
		 
	}

	@Override
	public PluginEntry getPluginEntry() {
		return GeworkbenchRoot.getPluginRegistry().getVisualizerPluginEntry(this.getClass());
	}

	@Override
	public Long getDatasetId() {
		return datasetId;
	}

}
