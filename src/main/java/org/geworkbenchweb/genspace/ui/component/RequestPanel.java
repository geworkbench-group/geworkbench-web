package org.geworkbenchweb.genspace.ui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geworkbench.components.genspace.server.stubs.Network;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.geworkbenchweb.events.FriendStatusChangeEvent;
import org.geworkbenchweb.genspace.chat.BroadCaster;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.genspace.wrapper.UserWrapper;
import org.vaadin.addon.borderlayout.BorderLayout;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class RequestPanel extends SocialPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GenSpaceLogin_1 login;
	
	private String panelTitle;
	
	private Panel requestPanel;
		
	private VerticalLayout hLayout;
	
	private VerticalLayout fLayout;
	
	private VerticalLayout nLayout;
	
	private ListSelect friendSelect;
	
	private ListSelect networkSelect;
	
	private String friend = "Friends";
	
	private String network = "Network";
	
	private String aFriend = "Accept Friend";
	
	private String rFriend = "Reject Friend";
	
	private String accept = "Accept";
	
	private String reject = "Reject";
	
	private BeanItemContainer<User> userContainer;
	
	private BeanItemContainer<UserNetworkReqWrapper> networkContainer;
	
	private List<User> friendReqList;
	
	private LinkedList<UserNetwork> networkReqList;
	
	private BorderLayout blLayout;
	
	private int myID;
	private ICEPush pusher = new ICEPush();
	
	public RequestPanel(String panelTitle, GenSpaceLogin_1 login) {

		this.login = login;
		this.myID = login.getGenSpaceServerFactory().getUser().getId();
		
		this.panelTitle = panelTitle;
		this.blLayout = new BorderLayout();
		setCompositionRoot(blLayout);
		this.updatePanel();
	}
	
	public String getPanelTitle() {
		return this.panelTitle;
	}
	
	public void updatePanel() {
		if (blLayout.getComponentCount() > 0){
			blLayout.removeAllComponents();
		}
		this.retrieveBasicInfo();
		this.requestPanel = new Panel(this.panelTitle);
		this.requestPanel.setWidth("800px");
		this.createMainLayout();
		this.requestPanel.addComponent(hLayout);
		blLayout.addComponent(requestPanel, BorderLayout.Constraint.CENTER);
		
	}
	
	private void retrieveBasicInfo() {
		if(login.getGenSpaceServerFactory().isLoggedIn()) {
			this.friendReqList = login.getGenSpaceServerFactory().getFriendOps().getFriendRequests();
			
			this.networkReqList = new LinkedList<UserNetwork>();
			for (UserNetwork tmp: login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks()){
				Network nt = tmp.getNetwork();

				if ((new UserWrapper(nt.getOwner(), login)).equals(login.getGenSpaceServerFactory().getWrappedUser())) {
					networkReqList.addAll((login.getGenSpaceServerFactory().getNetworkOps().getNetworkRequests(nt.getId())));
				}
					
			}
		}
	}
	
	private void createMainLayout() {
		this.hLayout = new VerticalLayout();	
		this.hLayout.setSpacing(true);
		createFriendLayout();
		this.hLayout.addComponent(fLayout);
		Label emptyLabel = new Label();
		emptyLabel.setWidth("100px");
		this.hLayout.addComponent(emptyLabel);
		
		createNetworkLayout();
		this.hLayout.addComponent(nLayout);
		this.hLayout.addComponent(pusher);
	}
	

	public void attachPusher(){
		this.hLayout.addComponent(pusher);
	}
	
	private void createFriendLayout() {
		fLayout = new VerticalLayout();
		fLayout.setSpacing(true);
		this.loadFriends();
		
		fLayout.addComponent(friendSelect);
		HorizontalLayout buLayout = new HorizontalLayout();
		Button aButton = new Button(aFriend);
		aButton.addListener(this.createFriendListener(aFriend));
		
		Button rButton = new Button(rFriend);
		rButton.addListener(this.createFriendListener(rFriend));
		
		buLayout.addComponent(aButton);
		
		Label emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		buLayout.addComponent(emptyLabel);
		
		buLayout.addComponent(rButton);
		fLayout.addComponent(buLayout);
	}
	
	private void loadFriends() {
		if(friendSelect != null){
			friendSelect.removeAllItems();

			if (friendSelect.getParent() != null && friendSelect.getParent() == fLayout)
				fLayout.removeComponent(friendSelect);
		}
		
		userContainer = new BeanItemContainer<User>(User.class);
		
		Iterator<User> fIT = this.friendReqList.iterator();
		User tmpUser;
		while(fIT.hasNext()) {
			tmpUser = fIT.next();
			userContainer.addItem(tmpUser);
		}
		
		friendSelect = new ListSelect(this.friend, userContainer);
		friendSelect.setRows(10);
		friendSelect.setMultiSelect(true);
		friendSelect.setWidth("300px");
		friendSelect.setItemCaptionMode(ListSelect.ITEM_CAPTION_MODE_PROPERTY);
		friendSelect.setItemCaptionPropertyId("username");
		
		fLayout.addComponent(friendSelect);
		
	}
	
	private Button.ClickListener createFriendListener(String acrj) {
		final String ar = acrj;
		Button.ClickListener cListener = new Button.ClickListener() {
			private final static long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				Iterator fSelect = friendSelect.getItemIds().iterator();
				List<Integer> accList = new ArrayList<Integer>();
				List<Integer> rejList = new ArrayList<Integer>();
				
				Object tmpID;
				User tmpUser;
				int tmpUserID;
				
				/*Avoid concurrent modification exception, select friend id into lists first*/
				while(fSelect.hasNext()) {
					
					tmpID = fSelect.next();
					if(friendSelect.isSelected(tmpID)) {
						tmpUser = userContainer.getItem(tmpID).getBean();
						tmpUserID = tmpUser.getId();
						
						if(ar.equals(aFriend)) {
							accList.add(tmpUser.getId());
						} else if (ar.equals(rFriend)) {
							rejList.add(tmpUser.getId());
						}
					}
				}
				
				//Once user decide to accept/reject a friend, fire an event for notifying friend's ui
				if(accList.size()>0){
					for (int friendID: accList) {
						login.getGenSpaceServerFactory().getFriendOps().addFriend(friendID);
						FriendStatusChangeEvent e = new FriendStatusChangeEvent(myID, friendID);
						e.setOptType(FriendStatusChangeEvent.ADD_FRIEND);	
						GenSpaceWindow.getGenSpaceBlackboard().fire(e);
					}
					retrieveBasicInfo();
					loadFriends();
				}
				if(rejList.size()>0){
					for (int friendID: rejList) {
						login.getGenSpaceServerFactory().getFriendOps().rejectFriend(friendID);
						GenSpaceWindow.getGenSpaceBlackboard().fire(new FriendStatusChangeEvent(myID, friendID));
					}
					retrieveBasicInfo();
					loadFriends();
				}
				
				BroadCaster.broadcastPresence(login);
				pusher.push();
			}
		};
		
		return cListener;
	}
		
	private void createNetworkLayout() {
		nLayout = new VerticalLayout();
		nLayout.setSpacing(true);
		loadNetworks();
		
		nLayout.addComponent(networkSelect);
		
		HorizontalLayout buLayout = new HorizontalLayout();
		Button aButton = new Button(accept);
		aButton.addListener(this.createNetworkListener(accept));
		Button rButton = new Button(reject);
		rButton.addListener(this.createNetworkListener(reject));
		
		buLayout.addComponent(aButton);
		
		Label emptyLabel = new Label();
		emptyLabel.setWidth("20px");
		buLayout.addComponent(emptyLabel);
		
		buLayout.addComponent(rButton);
		nLayout.addComponent(buLayout);
	}
	
	private void loadNetworks() {
		if(networkSelect != null/* && networkSelect.size()!=0*/){
			networkSelect.removeAllItems();
		}
		networkContainer = new BeanItemContainer<UserNetworkReqWrapper>(UserNetworkReqWrapper.class);
		
		Iterator<UserNetwork> nIT = this.networkReqList.iterator();
		UserNetwork tmpNet;
		UserNetworkReqWrapper tmpNetReqWrapper;
		while(nIT.hasNext()) {
			tmpNet = nIT.next();
			tmpNetReqWrapper = new UserNetworkReqWrapper(tmpNet, login);
			networkContainer.addItem(tmpNetReqWrapper);
		}
		
		networkSelect = new ListSelect(this.network, networkContainer);
		networkSelect.setRows(10);
		networkSelect.setMultiSelect(true);
		networkSelect.setWidth("300px");
		networkSelect.setItemCaptionMode(ListSelect.ITEM_CAPTION_MODE_PROPERTY);
		networkSelect.setItemCaptionPropertyId("name");
	}
	
	private Button.ClickListener createNetworkListener(String acrj) {
		final String ar = acrj;
		Button.ClickListener cListener = new Button.ClickListener() {
			private final static long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				Iterator nSelect = networkSelect.getItemIds().iterator();
				List<Integer> accList = new ArrayList<Integer>();
				List<Integer> rejList = new ArrayList<Integer>();
				
				Object tmpID;
				UserNetworkReqWrapper tmpUnr;
				
				/*Avoid concurrent modificiation, select network into lists first*/
				while(nSelect.hasNext()) {
					tmpID = nSelect.next();
					if(networkSelect.isSelected(tmpID)) {
						tmpUnr = networkContainer.getItem(tmpID).getBean();
						
						if(ar.equals(accept)) {
							accList.add(tmpUnr.getId());
						} else if(ar.equals(reject)) {
							rejList.add(tmpUnr.getId());
						}
						
					}
				}
				if(accList.size()>0){
					for (int unID: accList) {
						login.getGenSpaceServerFactory().getNetworkOps().acceptNetworkRequest(unID);
						
						FriendStatusChangeEvent e = new FriendStatusChangeEvent(FriendStatusChangeEvent.NETWORK_EVENT, 
								FriendStatusChangeEvent.NETWORK_EVENT);
						e.setOptType(FriendStatusChangeEvent.ADD_FRIEND);						
						GenSpaceWindow.getGenSpaceBlackboard().fire(e);
					}
					loadNetworks();
				}
				
				if(rejList.size()>0){
					for (int unID: rejList) {
						login.getGenSpaceServerFactory().getNetworkOps().rejectNetworkRequest(unID);
					}
					loadNetworks();

				}				
				
				BroadCaster.broadcastPresence(login);
				pusher.push();
				
			}
		};
		
		return cListener;
	}
}
