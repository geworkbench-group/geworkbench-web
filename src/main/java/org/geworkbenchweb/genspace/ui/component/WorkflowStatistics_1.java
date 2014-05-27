package org.geworkbenchweb.genspace.ui.component;

import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbenchweb.genspace.wrapper.WorkflowWrapper;
import org.vaadin.alump.fancylayouts.FancyCssLayout;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

public class WorkflowStatistics_1 extends AbstractGenspaceTab implements GenSpaceTab {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private VerticalLayout verticalLayout_1;
	@AutoGenerated
	private VerticalLayout individualToolStatisticsContainer;
	@AutoGenerated
	private Label lblToolStatistics;
	@AutoGenerated
	private Select cmbSelectedTool;
	@AutoGenerated
	private Label label_3;
	private Label label_4;
	@AutoGenerated
	private HorizontalLayout horizontalLayout_1;
	@AutoGenerated
	private Label label_2;
	private Label label_1;
	private Panel mainPanel = new Panel();
	private static FancyCssLayout mostPopularToolsCss = new FancyCssLayout();
	private static FancyCssLayout mostPopularToolsStartCss = new FancyCssLayout();
	private static FancyCssLayout mostPopularWorkflowsCss = new FancyCssLayout();

	@Override
	public void loggedIn() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void loggedOut() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void tabSelected() {
		
		
	}
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public WorkflowStatistics_1(final GenSpaceLogin_1 login) {
		super(login);
		buildMainLayout();
		setCompositionRoot(mainPanel);

		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				getDataAndSetupUI();
			}
		});
		t.start();

		cmbSelectedTool.addListener(new Property.ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				String ret = "";
				Tool tool = (Tool) cmbSelectedTool.getValue();
				String usageRate = "" + tool.getUsageCount();
				ret += "Total usage rate: " + usageRate + "<br>";

				String usageRateAsWFHead = "" + tool.getWfCountHead();
				ret += "Total usage rate at start of workflow: "
						+ usageRateAsWFHead + "<br>";
				Tool mostPopularNextTool = login.getGenSpaceServerFactory().getUsageOps().getMostPopularNextTool(tool.getId());
				if(mostPopularNextTool == null)
					ret += "No tools are used after this one"+ "<br>";
				else
					ret += "The most popular tool used next to this tool: "
						+ mostPopularNextTool.getName() + "<br>";

				Tool mostPopularPreviousTool = login.getGenSpaceServerFactory().getUsageOps().getMostPopularPreviousTool(tool.getId());
				if(mostPopularPreviousTool == null)
					ret += "No tools are used before this one"+ "<br>";
				else
					ret += "The most popular tool used before this tool: "
						+ mostPopularPreviousTool.getName() + "<br><br><br>";
				lblToolStatistics.setValue("<html>" + ret + "</html>");
			}
		});
	}

	private void getDataAndSetupUI()
	{
		
		for(Tool t : login.getGenSpaceServerFactory().getUsageOps().getAllTools())
		{
			cmbSelectedTool.addItem(t);
			cmbSelectedTool.setItemCaption(t, t.getName());
		}
		setImmediate(true);
	}
	
	private class ToolsLayout extends GridLayout {

		private static final long serialVersionUID = -2801145303701009347L;
		
		
		private ToolsLayout() {
			setColumns(2);
			setRows(2);
			setSizeFull();
			setImmediate(true);
			setColumnExpandRatio(1, 1.0f);

			mostPopularToolsCss.setSlideEnabled(true);
			mostPopularToolsCss.setWidth("95%");
			mostPopularToolsCss.addStyleName("lay");

		}

		private void addDescription() {
			int i =1;
			for(Tool t: login.getGenSpaceServerFactory().getUsageOps().getToolsByPopularity())
			{
				Label l = new Label(i+": " + t.getName());
				mostPopularToolsCss.addComponent(l);
				i++;
				if(i > 10)
					break;
			}
			
			addComponent(mostPopularToolsCss, 0, 1, 1, 1);
		}
		
		private void clearDescription() {
			mostPopularToolsCss.removeAllComponents();
			removeComponent(mostPopularToolsCss);
		}
	}
	
	private  class StartLayout extends GridLayout {

		private static final long serialVersionUID = -2801145303701009347L;
		
		
		private StartLayout() {
			setColumns(2);
			setRows(2);
			setSizeFull();
			setImmediate(true);
			setColumnExpandRatio(1, 1.0f);

			mostPopularToolsStartCss.setSlideEnabled(true);
			mostPopularToolsStartCss.setWidth("95%");
			mostPopularToolsStartCss.addStyleName("lay");

		}

		private void addDescription() {
			int i =1;
			for(Tool t: login.getGenSpaceServerFactory().getUsageOps().getMostPopularWFHeads())
			{
				Label l = new Label(i+": " + t.getName());
				mostPopularToolsStartCss.addComponent(l);
				i++;
				if(i > 10)
					break;
			}

			addComponent(mostPopularToolsStartCss, 0, 1, 1, 1);
		}
		
		private void clearDescription() {
			mostPopularToolsStartCss.removeAllComponents();
			removeComponent(mostPopularToolsStartCss);
		}
	}
	
	private class WorkflowLayout extends GridLayout {

		private static final long serialVersionUID = -2801145303701009347L;
		
		
		private WorkflowLayout() {
			setColumns(2);
			setRows(2);
			setSizeFull();
			setImmediate(true);
			setColumnExpandRatio(1, 1.0f);

			mostPopularWorkflowsCss.setSlideEnabled(true);
			mostPopularWorkflowsCss.setWidth("95%");
			mostPopularWorkflowsCss.addStyleName("lay");

		}

		private void addDescription() {

			int i =1;
			for(Workflow s: login.getGenSpaceServerFactory().getUsageOps().getWorkflowsByPopularity())
			{

				WorkflowWrapper w = new WorkflowWrapper(s);
				w.loadToolsFromCache();
				Label l = new Label(i+": " + w.toString());
				mostPopularWorkflowsCss.addComponent(l);
				i++;
				if(i > 10)
					break;
			}

			addComponent(mostPopularWorkflowsCss, 0, 1, 1, 1);
		}
		
		private void clearDescription() {
			mostPopularWorkflowsCss.removeAllComponents();
			removeComponent(mostPopularWorkflowsCss);
		}
	}
	
	private final ThemeResource ICON = new ThemeResource(
			"../custom/icons/icon_info.gif");
	private final ThemeResource CancelIcon = new ThemeResource(
			"../runo/icons/16/cancel.png");

	private void buildTools(VerticalLayout group,
			final String name) { //ComponentContainer

		final ToolsLayout toolsLayout = new ToolsLayout();
		final Button infoButton = new Button();
		final Button cancelButton = new Button();
		Button toolButton = new Button();		
		toolButton.setCaption(name);
		toolButton.setStyleName(Reindeer.BUTTON_LINK);	
		
		infoButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				toolsLayout.removeComponent(infoButton);
				toolsLayout.addComponent(cancelButton, 1, 0);
				toolsLayout.addDescription();
			}
		});
		cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				toolsLayout.removeComponent(cancelButton);
				toolsLayout.addComponent(infoButton, 1, 0);
				toolsLayout.clearDescription();
			}
		});

		infoButton.setStyleName(BaseTheme.BUTTON_LINK);
		infoButton.setIcon(ICON);
		cancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		cancelButton.setIcon(CancelIcon);
		toolsLayout.setSpacing(true);
		toolsLayout.addComponent(toolButton);
		toolsLayout.addComponent(infoButton);

		group.addComponent(toolsLayout);
	}
	
	private void buildStartTools(VerticalLayout group,
			final String name) { //ComponentContainer

		final StartLayout startLayout = new StartLayout();
		final Button infoButton = new Button();
		final Button cancelButton = new Button();
		Button toolButton = new Button();		
		toolButton.setCaption(name);
		toolButton.setStyleName(Reindeer.BUTTON_LINK);	
		
		infoButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				startLayout.removeComponent(infoButton);
				startLayout.addComponent(cancelButton, 1, 0);
				startLayout.addDescription();
			}
		});
		cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				startLayout.removeComponent(cancelButton);
				startLayout.addComponent(infoButton, 1, 0);
				startLayout.clearDescription();
			}
		});

		infoButton.setStyleName(BaseTheme.BUTTON_LINK);
		infoButton.setIcon(ICON);
		cancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		cancelButton.setIcon(CancelIcon);
		startLayout.setSpacing(true);
		startLayout.addComponent(toolButton);
		startLayout.addComponent(infoButton);

		group.addComponent(startLayout);
	}

	private void buildWorkflow(VerticalLayout group,
			final String name) { //ComponentContainer

		final WorkflowLayout workflowLayout = new WorkflowLayout();
		final Button infoButton = new Button();
		final Button cancelButton = new Button();
		Button toolButton = new Button();		
		toolButton.setCaption(name);
		toolButton.setStyleName(Reindeer.BUTTON_LINK);	
		
		infoButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				workflowLayout.removeComponent(infoButton);
				workflowLayout.addComponent(cancelButton, 1, 0);
				workflowLayout.addDescription();
			}
		});
		cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				workflowLayout.removeComponent(cancelButton);
				workflowLayout.addComponent(infoButton, 1, 0);
				workflowLayout.clearDescription();
			}
		});

		infoButton.setStyleName(BaseTheme.BUTTON_LINK);
		infoButton.setIcon(ICON);
		cancelButton.setStyleName(BaseTheme.BUTTON_LINK);
		cancelButton.setIcon(CancelIcon);
		workflowLayout.setSpacing(true);
		workflowLayout.addComponent(toolButton);
		workflowLayout.addComponent(infoButton);

		group.addComponent(workflowLayout);
	}
	@AutoGenerated
	private void buildMainLayout() {		
		mainPanel.setWidth("100%");
		mainPanel.setHeight("650px");
		mainPanel.setScrollable(true);
		
		// verticalLayout_1
		verticalLayout_1 = buildVerticalLayout_1();
		Panel contentPanel = new Panel();
		contentPanel.setScrollable(true);
		contentPanel.setWidth("100%");
		contentPanel.addComponent(verticalLayout_1);

		mainPanel.addComponent(contentPanel);
	}
	
	@AutoGenerated
	private VerticalLayout buildVerticalLayout_1() {
		// common part: create layout
		verticalLayout_1 = new VerticalLayout();
		verticalLayout_1.setImmediate(false);
		verticalLayout_1.setMargin(true);		
		Label emptyLabel = new Label();
		emptyLabel.setHeight("20px");
				
				
		// mostPopularWorkflowsContainer
		buildTools(verticalLayout_1, "Most Popular Tools");
		buildWorkflow(verticalLayout_1, "Most Popular Workflows");
		buildStartTools(verticalLayout_1, "Most Popular Tools at Start of Workflow");

		emptyLabel = new Label();
		emptyLabel.setHeight("10px");
		verticalLayout_1.addComponent(emptyLabel);
				
		// individualToolStatisticsContainer
		individualToolStatisticsContainer = buildIndividualToolStatisticsContainer();
		verticalLayout_1.addComponent(individualToolStatisticsContainer);

		return verticalLayout_1;
	}

	@AutoGenerated
	private VerticalLayout buildIndividualToolStatisticsContainer() {
		// common part: create layout
		individualToolStatisticsContainer = new VerticalLayout();
		individualToolStatisticsContainer.setImmediate(false);
		individualToolStatisticsContainer.setWidth("-1px");
		individualToolStatisticsContainer.setHeight("110px");
		individualToolStatisticsContainer.setMargin(false);
		
		// label_3
		label_3 = new Label();
		label_3.setStyleName("h4");
		label_3.setImmediate(false);
		label_3.setWidth("-1px");
		label_3.setHeight("-1px");
		label_3.setValue("Individual Tool Statistics");
		individualToolStatisticsContainer.addComponent(label_3);
		
		// cmbSelectedTool
		cmbSelectedTool = new Select();
		cmbSelectedTool.setImmediate(true);
		cmbSelectedTool.setWidth("-1px");
		cmbSelectedTool.setHeight("-1px");
		individualToolStatisticsContainer.addComponent(cmbSelectedTool);
		
		// lblToolStatistics
		lblToolStatistics = new Label();
		lblToolStatistics.setImmediate(false);
		lblToolStatistics.setWidth("-1px");
		lblToolStatistics.setHeight("-1px");
		lblToolStatistics.setValue("Select a tool to see its statistics");
		lblToolStatistics.setContentMode(3);
		individualToolStatisticsContainer.addComponent(lblToolStatistics);
		
		return individualToolStatisticsContainer;
	}

}
