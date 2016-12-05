package org.geworkbenchweb.plugins.pbqdi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ResultView extends Window {

    private static final long serialVersionUID = -273679922042872864L;

    private static final String COLUMN_SAMPLE_NAME = "Sample Name";
    private static final String COLUMN_SUBTYPE = "Subtype";
    private static final String COLUMN_SAMPLE_PER_SUBTYPE = "samples";

    private Table resultTable = new Table();
    private Embedded image = new Embedded();
    private Table samplePerSubtype = new Table();

    public ResultView(String[] sampleNames, final String tumorType, Map<String, Integer> subtypes, final String drugReport,
            FileResource kaplanImage, final String htmlReport) {
        this.setModal(true);
        this.setClosable(true);
        ((AbstractOrderedLayout) this.getContent()).setSpacing(true);
        this.setResizable(true);
        this.setCaption("Columbia/CPTAC Patient Tumor Subtype Results");
        this.setImmediate(true);

        Container container = new IndexedContainer();
        container.addContainerProperty(COLUMN_SAMPLE_NAME, String.class, null);
        container.addContainerProperty(COLUMN_SUBTYPE, Integer.class, 0);
        Map<Integer, List<String>> summary = new HashMap<Integer, List<String>>();
        for (int i = 0; i < sampleNames.length; i++) {
            final String sampleName = sampleNames[i];
            Integer subtype = subtypes.get(sampleName);
            Item item = container.addItem(sampleName);
            item.getItemProperty(COLUMN_SAMPLE_NAME).setValue(sampleName);
            item.getItemProperty(COLUMN_SUBTYPE).setValue(subtype);

            List<String> s = summary.get(subtype);
            if (s == null) {
                s = new ArrayList<String>();
                summary.put(subtype, s);
            }
            s.add(sampleName);
        }
        resultTable.setContainerDataSource(container);
        resultTable.setPageLength(sampleNames.length);
        resultTable.setSizeFull();

        Button reportButton = new Button("Drug Prediction Report");
        reportButton.addListener(new ClickListener() {

            private static final long serialVersionUID = 345938285589568581L;

            @Override
            public void buttonClick(ClickEvent event) {
                Window mainWindow = ResultView.this.getApplication().getMainWindow();
                DrugReport v = new DrugReport(tumorType, drugReport, htmlReport);
                mainWindow.addWindow(v);
            }

        });

        Container container2 = new IndexedContainer();
        container2.addContainerProperty(COLUMN_SUBTYPE, Integer.class, 0);
        container2.addContainerProperty(COLUMN_SAMPLE_PER_SUBTYPE, Integer.class, null);
        for (Integer subtype : summary.keySet()) {
            Item item = container2.addItem(subtype);
            item.getItemProperty(COLUMN_SUBTYPE).setValue(subtype);
            item.getItemProperty(COLUMN_SAMPLE_PER_SUBTYPE).setValue(summary.get(subtype).size());
        }
        samplePerSubtype.setContainerDataSource(container2);
        samplePerSubtype.setPageLength(summary.size());
        samplePerSubtype.setSizeFull();

        image = new Embedded(null, kaplanImage);
        image.setSizeFull();

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setSizeFull();
        VerticalLayout leftSide = new VerticalLayout();
        leftSide.setSpacing(true);
        leftSide.setWidth("50%");
        VerticalLayout rightSide = new VerticalLayout();
        rightSide.setWidth("50%");
        leftSide.addComponent(new Label("<b>Subtypes</b>", Label.CONTENT_XHTML));
        leftSide.addComponent(resultTable);
        resultTable.setHeight("45%");
        leftSide.addComponent(new Label("<b>Summary of TCGA Samples per Subtype</b>", Label.CONTENT_XHTML));
        leftSide.addComponent(samplePerSubtype);
        samplePerSubtype.setHeight("45%");
        leftSide.addComponent(reportButton);
        rightSide.addComponent(new Label("<b>Survival Curves per Subtype</b>", Label.CONTENT_XHTML));
        rightSide.addComponent(image);
        layout.addComponent(leftSide);
        layout.addComponent(rightSide);
        this.addComponent(layout);

        this.setWidth("65%");
        this.setHeight("65%");
    }

}