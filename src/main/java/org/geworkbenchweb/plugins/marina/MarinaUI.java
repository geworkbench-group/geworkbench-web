package org.geworkbenchweb.plugins.marina;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.events.AnalysisSubmissionEvent;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.plugins.AnalysisUI;
import org.geworkbenchweb.pojos.MraResult;
import org.geworkbenchweb.pojos.ResultSet;
import org.geworkbenchweb.pojos.SubSet;
import org.geworkbenchweb.utils.SubSetOperations;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;

import de.steinwedel.vaadin.MessageBox;
import de.steinwedel.vaadin.MessageBox.ButtonType;

public class MarinaUI extends VerticalLayout implements Upload.SucceededListener,Upload.FailedListener,Upload.Receiver, AnalysisUI {

	private static final long serialVersionUID = 845011602285963638L;
	private Log log = LogFactory.getLog(MarinaUI.class);

	protected Form form = new Form();
	private Upload upload = null;
	private CheckBox priorBox = new CheckBox("Retrieve Prior Result");
	protected Button submitButton = new Button("Submit", form, "commit");
	private ClassSelector classSelector = null;	 
	protected MarinaParamBean bean = null;
	protected BeanItem<MarinaParamBean> item = null;
	protected HashMap<String, String> arraymap = null;
	protected boolean allpos = true;
	private final ByteArrayOutputStream os = new ByteArrayOutputStream(10240);
	private final String[] order = {"network", "gseaPValue", 
			"minimumTargetNumber", "minimumSampleNumber", "gseaPermutationNumber",
			"gseaTailNumber", "shadowPValue", "synergyPValue", "retrievePriorResultWithId"};
	private static final String analysisName = "MARINa";

	protected Long dataSetId = null;
	
	public MarinaUI() {
		this(0L);
	}
	
	public MarinaUI(Long dataId){
		this.dataSetId = dataId;
		
		arraymap = new HashMap<String, String>();
		classSelector = new ClassSelector(dataSetId,  SessionHandler.get().getId(), "MarinaUI", this);
	 

		setDataSetId(dataSetId);	 

		form.getLayout().addComponent(classSelector.getArrayContextCB());
		form.getLayout().addComponent(classSelector.getH1());
		form.getLayout().addComponent(classSelector.getH2());
		
		//TODO: allow network to be loaded from adjacency matrix data node
		upload = new Upload("Upload Network File", this);
		upload.setButtonCaption("Upload");
		upload.addListener((Upload.SucceededListener)this);
        upload.addListener((Upload.FailedListener)this);
		form.getLayout().addComponent(upload);

		bean = new MarinaParamBean();
		DefaultFieldFactory.createCaptionByPropertyId(bean);
		item = new BeanItem<MarinaParamBean>(bean, order);
		form.setImmediate(true);
		form.setFormFieldFactory(new DefaultFieldFactory(){
			private static final long serialVersionUID = 4805200657491765148L;
			public Field createField(Item item, Object propertyId, Component uiContext) {
				Field f = super.createField(item, propertyId, uiContext);
				fieldTitleToUppercase(f, "Gsea");
				fieldTitleToUppercase(f, "Id");
				if (propertyId.equals("minimumTargetNumber") || propertyId.equals("gseaPermutationNumber") ||
	            	propertyId.equals("minimumSampleNumber") || propertyId.equals("gseaTailNumber")) {
					TextField tf = (TextField) f;
					if (propertyId.equals("gseaTailNumber"))
						tf.addValidator(new PositiveIntValidator("Please enter 1 or 2", 2));
					else tf.addValidator(new PositiveIntValidator("Please enter a positive integer"));
				} else if (propertyId.equals("shadowPValue") || propertyId.equals("synergyPValue") ||
	            		propertyId.equals("gseaPValue")) {
					TextField tf = (TextField) f;
					tf.addValidator(new PvalueValidator("P value must be in the range of 0 to 1"));
				} else if (propertyId.equals("retrievePriorResultWithId")) {
					TextField tf = (TextField) f;
					tf.addValidator(new RegexpValidator("^[mM][rR][aA]\\d+$", 
							"MRA Result ID must be 'mra' followed by an integer"));
					}
	            return f;
	        }
		});
		form.setItemDataSource(item);

		form.getField("network").setWidth("270px");
		form.getField("network").setReadOnly(true);
		form.getField("network").setEnabled(false);
		form.getField("retrievePriorResultWithId").setEnabled(false);

		priorBox.setImmediate(true);
		priorBox.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -5548846734511323624L;
			public void buttonClick(ClickEvent event) {
				if (priorBox.booleanValue()){
					for (String item : order)
						form.getField(item).setEnabled(false);
					upload.setEnabled(false);
					classSelector.getArrayContextCB().setEnabled(false);
					classSelector.getH1().setEnabled(false);
				    classSelector.getH2().setEnabled(false);
					form.getField("retrievePriorResultWithId").setEnabled(true);
					if (form.isValid()) submitButton.setEnabled(true);
				}else {
					for (String item : order)
						form.getField(item).setEnabled(true);
					upload.setEnabled(true);
					classSelector.getArrayContextCB().setEnabled(true);
					classSelector.getH1().setEnabled(true);
					classSelector.getH2().setEnabled(true);
					form.getField("retrievePriorResultWithId").setEnabled(false);
					if (form.getField("network").getValue().equals("")) form.getField("network").setEnabled(false);
					if (classSelector.getTf1().isEnabled() && form.getField("network").isEnabled())
						submitButton.setEnabled(true);
					else submitButton.setEnabled(false);
				}
			}
		});
		form.getLayout().addComponent(priorBox);

		submitButton.setEnabled(false);
		submitButton.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1085633263164082701L;

			@Override
			public void buttonClick(ClickEvent event) {			 
			    String warnMsg = validInputClassData(classSelector.getClass1ArraySet(), classSelector.getClass2ArraySet());
				if( warnMsg != null ) 
				{ 
					MessageBox mb = new MessageBox(getWindow(), 
				 
						"Warning", 
						MessageBox.Icon.INFO, 
						warnMsg,
						new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
				    mb.show();
				    return;
				}
					
					
				ResultSet resultSet = storePendingResultSet();
				
				HashMap<Serializable, Serializable> params = new HashMap<Serializable, Serializable>(); 
				params.put("bean", bean);

				AnalysisSubmissionEvent analysisEvent = new AnalysisSubmissionEvent(dataSetId, resultSet, params, MarinaUI.this);
				GeworkbenchRoot.getBlackboard().fire(analysisEvent);
			}
		});
		form.getFooter().addComponent(submitButton);

		addComponent(form);
	}
	
	/* 
	 * convert abbrev in field title to uppercase
	 */
	private void fieldTitleToUppercase(Field f, String abbrev){
		String caption = f.getCaption();
		if (caption.contains(abbrev))
			f.setCaption(caption.replace(abbrev, abbrev.toUpperCase()));
	}
	
	 
	
	private ResultSet storePendingResultSet() {

		ResultSet resultSet = new ResultSet();
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
		resultSet.setDateField(date);
		String dataSetName = analysisName + " - Pending";
		resultSet.setName(dataSetName);
		resultSet.setType(getResultType().getName());
		resultSet.setParent(dataSetId);
		resultSet.setOwner(SessionHandler.get().getId());
		FacadeFactory.getFacade().store(resultSet);

		NodeAddEvent resultEvent = new NodeAddEvent(resultSet);
		GeworkbenchRoot.getBlackboard().fire(resultEvent);

		return resultSet;
	}
	
	// Callback method to begin receiving the upload.
	public OutputStream receiveUpload(String filename, String mimeType) {
		item.getItemProperty("network").setValue(filename);
		os.reset();
		return os;
	}

	// This is called if the upload fails.
	public void uploadFailed(FailedEvent event) {
		String fname = event.getFilename();
		log.info("Failed to upload "+fname);
		networkNotLoaded("Network file " + fname + " failed to upload.");
	}

	// This is called if the upload is finished.
	public void uploadSucceeded(SucceededEvent event) {
		bean.setNetworkBytes(os.toByteArray());
		NetworkDialog dialog = new NetworkDialog(this);
		dialog.openDialog();
	}

	protected void networkNotLoaded(String msg){
		bean.setNetworkBytes(null);
		item.getItemProperty("network").setValue("");
		form.getField("network").setEnabled(false);
		submitButton.setEnabled(false);
		if (msg != null){
			MessageBox mb = new MessageBox(getWindow(), 
					"Network Problem", MessageBox.Icon.ERROR, msg, 
					new MessageBox.ButtonConfig(ButtonType.OK, "Ok"));
			mb.show();
		}
	}

	protected void networkLoaded(byte[] networkBytes){
		bean.setNetworkBytes(networkBytes);
		form.getField("network").setEnabled(true);
		if (classSelector.getTf1().isEnabled())
			submitButton.setEnabled(true);
	}
	
	public class PositiveIntValidator extends IntegerValidator {
		private static final long serialVersionUID = -8205632597275359667L;
		private int max = 0;
		public PositiveIntValidator(String message){
			super(message);
		}
		public PositiveIntValidator(String message, int max){
			this(message);
			this.max = max;
		}
		protected boolean isValidString(String value){
			try {
				int n = Integer.parseInt(value);
				if (n <= 0) return false;
				if (max > 0 && n > max) return false;
			} catch (Exception e) {
				return false;
			}
			submitButton.setComponentError(null);
			return true;
		}
	}
	public class PvalueValidator extends DoubleValidator {
		private static final long serialVersionUID = -815490638929041408L;
		public PvalueValidator(String errorMessage) {
			super(errorMessage);
		}
		protected boolean isValidString(String value){
			try {
				double n = Double.parseDouble(value);
				if (n < 0 || n > 1) return false;
			} catch (Exception e) {
				return false;
			}
			submitButton.setComponentError(null);
			return true;
		}
	}
	
	 
	
	// FIXME most of the null checkings should be designed out of the process
	// meaning if they are allowed to be null, it should be very clear when we expect them to be null
	@Override
	public void setDataSetId(Long dataId) {
		this.dataSetId = dataId;
		
		if(dataSetId==null || dataSetId==0) return;

		classSelector.setData(dataSetId, SessionHandler.get().getId());
	 
		arraymap.put(null, "");
	}
	
	private String validInputClassData(String[] selectedClass1Sets, String[] selectedclass2Sets)
	{    
	  
		List<String> microarrayPosList = new ArrayList<String>();
		List<String> caseSetList = new ArrayList<String>();
		/* for each group */
		if (selectedClass1Sets != null)
		{
			for (int i = 0; i < selectedClass1Sets.length; i++) {			
			    caseSetList.add(selectedClass1Sets[i].trim());
			    ArrayList<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedClass1Sets[i].trim()));	 
			 
			    for (int j = 0; j < arrays.size(); j++) {
				   if (microarrayPosList.contains(arrays.get(j)))  				
					 return "Same array (" + arrays.get(j) + ") exists in class1 array groups.";				 
				   microarrayPosList.add(arrays.get(j));				 
			    }
		    }
		}
		microarrayPosList.clear();
		if (selectedclass2Sets != null)
		{ 
			for (int i = 0; i < selectedclass2Sets.length; i++) {		 
			   if (caseSetList.contains(selectedclass2Sets[i].trim()))
			   {   SubSet subset = (SubSet)SubSetOperations.getArraySet(Long
							.parseLong(selectedclass2Sets[i].trim())).get(0);
				    return "Class1 and class2 groups have same array set " + subset.getName() + ".";
			   }
				 ArrayList<String> arrays = SubSetOperations.getArrayData(Long
						.parseLong(selectedclass2Sets[i].trim()));	 			 
			   for (int j = 0; j < arrays.size(); j++) {
				  if (microarrayPosList.contains(arrays.get(j)))  				
					 return "Same array (" + arrays.get(j) + ") exists in class2 array groups.";				 
				   microarrayPosList.add(arrays.get(j));				 
			   }
		    }
		}
		
		return null;
		
	}	
	

	@Override
	public Class<?> getResultType() {
		return org.geworkbenchweb.pojos.MraResult.class;
	}

	@Override
	public String execute(Long resultId, Long datasetId,
			HashMap<Serializable, Serializable> parameters, Long userId) throws IOException,
			Exception {
		MarinaAnalysis analyze = new MarinaAnalysis(datasetId, parameters);

		MraResult result = analyze.execute();
		FacadeFactory.getFacade().store(result);
		ResultSet resultSet = FacadeFactory.getFacade().find(ResultSet.class, resultId);
		resultSet.setDataId(result.getId());
		FacadeFactory.getFacade().store(resultSet);

		return analysisName + " - " + result.getLabel();
	}
}
