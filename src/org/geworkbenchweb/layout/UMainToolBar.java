package org.geworkbenchweb.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.GeworkbenchRoot;
import org.geworkbenchweb.dataset.UDataSetUpload;
import org.geworkbenchweb.events.NodeAddEvent;
import org.geworkbenchweb.pojos.ActiveWorkspace;
import org.geworkbenchweb.pojos.Project;
import org.geworkbenchweb.pojos.Workspace;
import org.geworkbenchweb.utils.WorkspaceUtils;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.Window;

/**
 * Menu Bar class which will be common for all the Visual Plugins
 * @author Nikhil
 */
public class UMainToolBar extends MenuBar {

	private static final long serialVersionUID = 1L;

	public UMainToolBar() {
		
		setImmediate(true);
		setWidth("100%");
		
		this.addItem("Upload Data",  new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {

				UDataSetUpload dataWindow = new UDataSetUpload();
				getApplication().getMainWindow().addWindow(dataWindow);

			}

		});
		
		final MenuBar.MenuItem workspace = this.addItem("Workspaces",
				null);
		
		workspace.addItem("Create WorkSpace", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				
				final Window newWorkspace 		= 	new Window("Create New Workspace");
				
				newWorkspace.setModal(true);
				newWorkspace.setDraggable(false);
				newWorkspace.setResizable(false);
				newWorkspace.setWidth("300px");
				
				FormLayout workspaceForm 	= 	new FormLayout();
				
				final TextField name 	= 	new TextField();
				Button submit 			= 	new Button("Submit", new Button.ClickListener() {
					
					private static final long serialVersionUID = -6393819962372106745L;

					@Override
					public void buttonClick(ClickEvent event) {
		
						Workspace workspace = 	new Workspace();
						
						workspace.setOwner(SessionHandler.get().getId());	
						workspace.setName(name.getValue().toString());
					    FacadeFactory.getFacade().store(workspace);
					    
					    /* Creating default Project*/
					    Project project = 	new Project();
					    project.setOwner(SessionHandler.get().getId());	
					    project.setName("New Project");
					    project.setWorkspaceId(workspace.getId());
					    project.setDescription("Default Project created for the Workspace");
					    FacadeFactory.getFacade().store(project);
					    
					    Map<String, Object> param 		= 	new HashMap<String, Object>();
						param.put("owner", SessionHandler.get().getId());

						List<?> activeWorkspace =  FacadeFactory.getFacade().list("Select p from ActiveWorkspace as p where p.owner=:owner", param);
						FacadeFactory.getFacade().delete((ActiveWorkspace) activeWorkspace.get(0));
						
						/* Setting active workspace */
					    ActiveWorkspace active = new ActiveWorkspace();
					    active.setOwner(SessionHandler.get().getId());
					    active.setWorkspace(workspace.getId());
					    FacadeFactory.getFacade().store(active);
						
					    getApplication().getMainWindow().removeWindow(newWorkspace);
					    try {
					    	getApplication().getMainWindow().showNotification("New Workspace is created and set as Active Workspace");
					    	getApplication().getMainWindow().setContent(new UMainLayout());					    	

					    } catch(Exception e) {
					    	e.printStackTrace();
					    }
					}
				});
				
				name.setCaption("Enter Name");
				
				workspaceForm.setMargin(true);
				workspaceForm.setImmediate(true);
				workspaceForm.setSpacing(true);
				workspaceForm.addComponent(name);
				workspaceForm.addComponent(submit);
				
				newWorkspace.addComponent(workspaceForm);
				getApplication().getMainWindow().addWindow(newWorkspace);
				
			}
		});
		
		workspace.addItem("Switch Workspace", new Command() {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("deprecation")
			@Override
			public void menuSelected(MenuItem selectedItem) {
				
				final Window workspaceTable = new Window("Switch Workspace");
				
				((AbstractOrderedLayout) workspaceTable.getLayout()).setSpacing(true);
				workspaceTable.setModal(true);
				workspaceTable.setClosable(true);
				workspaceTable.setDraggable(false);
				workspaceTable.setResizable(false);
				workspaceTable.setWidth("200px");
				
				ListSelect workspaceSelect = new ListSelect("Select Workspace");
				workspaceSelect.setNullSelectionAllowed(false);
				workspaceSelect.setMultiSelect(false);
				workspaceSelect.setImmediate(true);
				
				/* Adding items to the combobox */
				List<Workspace> spaces = WorkspaceUtils.getAvailableWorkspaces();
				for(int i=0; i<spaces.size(); i++) {
					workspaceSelect.addItem(spaces.get(i).getId());
					workspaceSelect.setItemCaption(spaces.get(i).getId(), spaces.get(i).getName());
				}
				
				workspaceSelect.addListener(new ListSelect.ValueChangeListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						
						final Long selectedWorkspace = (Long) event.getProperty().getValue();

						Button activate = new Button("Activate", new Button.ClickListener() {

							private static final long serialVersionUID = 1L;

							@Override
							public void buttonClick(ClickEvent event) {

								Map<String, Object> param 		= 	new HashMap<String, Object>();
								param.put("owner", SessionHandler.get().getId());

								List<?> activeWorkspace =  FacadeFactory.getFacade().list("Select p from ActiveWorkspace as p where p.owner=:owner", param);
								FacadeFactory.getFacade().delete((ActiveWorkspace) activeWorkspace.get(0));

								/* Setting active workspace */
								ActiveWorkspace active = new ActiveWorkspace();
								active.setOwner(SessionHandler.get().getId());
								active.setWorkspace(selectedWorkspace);
								FacadeFactory.getFacade().store(active);

								getApplication().getMainWindow().removeWindow(workspaceTable);
								getApplication().getMainWindow().setContent(new UMainLayout());

							}
						});
						workspaceTable.addComponent(activate);
					}
					
				});
				workspaceTable.addComponent(workspaceSelect);
				getApplication().getMainWindow().addWindow(workspaceTable);
			}
		});
		
		/*workspace.addItem("Delete Workspace", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				
			}
		});*/
		
		final MenuBar.MenuItem project = this.addItem("Projects", null);
		
		project.addItem("Create New Project", new Command() {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("deprecation")
			@Override
			public void menuSelected(MenuItem selectedItem) {
				
				final Window project  = new Window("Create Project");
				
				((AbstractOrderedLayout) project.getLayout()).setSpacing(true);
				
				FormLayout projectForm 	= 	new FormLayout();
				
				project.setModal(true);
				project.setClosable(true);
				project.setDraggable(false);
				project.setResizable(false);
				project.setWidth("350px");
				
				final TextField projectName 	= 	new TextField("Project Name");
				final TextField projectDes 	= 	new TextField("Project Description");
				
				Button submitProject = new Button("Submit", new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						
						if(projectName.getValue() == null || projectDes.getValue() == null) {
							getApplication().getMainWindow().showNotification("Please Fill ProjectName and Description", 
									Notification.TYPE_ERROR_MESSAGE);
						} else {
							
							Project newData = new Project();
							
							newData.setName((String) projectName.getValue());
							newData.setDescription((String) projectDes.getValue());
							newData.setOwner(SessionHandler.get().getId());
							newData.setWorkspaceId(WorkspaceUtils.getActiveWorkSpace());
							
							FacadeFactory.getFacade().store(newData);
							
							NodeAddEvent resultEvent = new NodeAddEvent(newData.getId(), newData.getName(), null);
							GeworkbenchRoot.getBlackboard().fire(resultEvent);
							
							getApplication().getMainWindow().removeWindow(project);
						}
					}
					
				});
				
				projectForm.setMargin(true);
				projectForm.setImmediate(true);
				projectForm.setSpacing(true);
				
				projectForm.addComponent(projectName);
				projectForm.addComponent(projectDes);
				projectForm.addComponent(submitProject);
				
				project.addComponent(projectForm);
				getApplication().getMainWindow().addWindow(project);
			}
		});
		
		//project.addItem("Delete Project", null);
		
		this.addItem("Logout", new Command() {

			private static final long serialVersionUID = 1L;

			@Override
			public void menuSelected(MenuItem selectedItem) {
				SessionHandler.logout();
				getApplication().close();
			}
		});
		
	}
	
}
