/**
 * 
 */
package org.geworkbenchweb.plugins.citrus;

import org.geworkbenchweb.visualizations.CitrusDiagram;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * @author zji
 *
 */
public class GeneBasedQueryAndDataIntegration extends VerticalLayout {

	private static final long serialVersionUID = -713233350568178L;
	
	final private ComboBox cancerTypeComboBox = new ComboBox("TCGA cancer type");
	final private ComboBox geneSymbolComboBox = new ComboBox("Gene symbol");
	final private CitrusDiagram citrusDiagram = new CitrusDiagram();
	final private Button runButton = new Button("Run Citrus");

	private CitrusDatabase db = null;
	
	public GeneBasedQueryAndDataIntegration() {
		try {
			db = new CitrusDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final private ClickListener clickListener = new ClickListener() {

		private static final long serialVersionUID = 5141684198050379901L;

		@Override
		public void buttonClick(ClickEvent event) {
			String cancerType = (String) cancerTypeComboBox.getValue();
			String geneSymbol = (String) geneSymbolComboBox.getValue();
			String[] alteration = db.getAlterations(cancerType, geneSymbol);
			String[] samples = db.getSamples(cancerType);
			String[] presence = db.getPresence(cancerType, geneSymbol);
			Integer[] preppi = db.getPrePPI(cancerType, geneSymbol);
			Integer[] cindy = db.getCINDy(cancerType, geneSymbol);
			String[] pvalue = db.getPValue(cancerType, geneSymbol);
			String[] nes = db.getNES(cancerType, geneSymbol);
			citrusDiagram.setCitrusData(alteration, samples, presence, preppi, cindy, pvalue, nes);
		}
	};
	
	private void setTFComboBox(String cancerType) {
		geneSymbolComboBox.removeAllItems();
		for (String tf : db.getTF(cancerType)) {
			geneSymbolComboBox.addItem(tf);
		}
	}

	@Override
	public void attach() {
		super.attach();

		HorizontalLayout commandPanel = new HorizontalLayout();
		Panel diagramPanel = new Panel();
		this.setSpacing(true);
		this.addComponent(commandPanel);
		this.addComponent(diagramPanel);

		diagramPanel.addComponent(citrusDiagram);
		diagramPanel.getContent().setSizeUndefined();

		String[] cancerTypes = db.getCancerTypes();
		for (String s : cancerTypes)
			cancerTypeComboBox.addItem(s);
		cancerTypeComboBox.setNullSelectionAllowed(false);
		cancerTypeComboBox.addListener(new ValueChangeListener() {

			private static final long serialVersionUID = 246976645556960310L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				setTFComboBox((String) cancerTypeComboBox.getValue());
			}

		});

		final TextField pValueTextField = new TextField("p-value");
		
		runButton.addListener(clickListener);
		commandPanel.setSpacing(true);
		commandPanel.addComponent(cancerTypeComboBox);
		commandPanel.addComponent(geneSymbolComboBox);
		commandPanel.addComponent(pValueTextField);
		commandPanel.addComponent(runButton);
		commandPanel.setComponentAlignment(runButton, Alignment.BOTTOM_CENTER);
	}
}
