package org.geworkbenchweb.genspace.ui.component;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserNetwork;
import org.geworkbenchweb.events.ChatStatusChangeEvent;
import org.geworkbenchweb.events.FriendStatusChangeEvent;
import org.geworkbenchweb.events.FriendStatusChangeEvent.FriendStatusChangeListener;
import org.geworkbenchweb.genspace.chat.BroadCaster;
import org.geworkbenchweb.genspace.chat.ChatReceiver;
import org.geworkbenchweb.genspace.ui.GenSpaceWindow;
import org.geworkbenchweb.genspace.ui.chat.RosterFrame;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Runo;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class SocialNetworkHome extends AbstractGenspaceTab implements GenSpaceTab, FriendStatusChangeListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AbsoluteLayout mainLayout;
	
	private VerticalLayout sbcLayout;
	
	private HorizontalLayout searchLayout;
	
	private HorizontalLayout bcLayout;
	
	private VerticalLayout buttonLayout;
	
	private VerticalLayout contentLayout;
	
	private String GenSpace = "GenSpace";
	
	private String Search = "Search";
	
	private String Go = "Go";
	
	private String title = "My Profile";
	
	private String myNet = "My Networks";
	
	private String myFriends = "My Friends";
	
	private String chat = "Chat";
	
	private String settings = "Settings";
	
	private String vRequests = "View Requests";
	
	private String pRequests = "Pending Requests";
	
	private String findNoUserMsg = "Error: Could not find xxx's profile";
	
	private String back = "Back";
	
	private List<User> friendList;
	
	private List<UserNetwork> uNetworkList;
	
	private ComboBox search;
	
	private SocialPanel current;
	
	private SocialPanel proPanel;
	
	private SocialPanel netPanel;
	
	private SocialPanel friendPanel;
	
	private SocialPanel privacyPanel;
	
	private SocialPanel viewPanel;
	
	private ChatReceiver chatHandler;

	private Stack<SocialPanel> last;
	
	private SocialNetworkHome instance;
	
	public Integer backTo  = -1;
	
	public final static int BACK_TO_MYNET = 1;
	
	
	private Label infoLabel = new Label(
			"Please login to genSpace to access this area.");
	
	{
		init();
	}
	
	
	public SocialNetworkHome(GenSpaceLogin_1 login)
	{
		super(login);
	}
	
	private void init() {
		instance = this;
		this.last = new Stack<SocialPanel>();
		makeLayout();
		setCompositionRoot(this.sbcLayout);
	}
	
	public SocialNetworkHome getInstance() {
		return instance;
	}
	
	private void createDefaultPanel() {
		ProfilePanel pPanel = new ProfilePanel(title, login);
		contentLayout.removeAllComponents();
		contentLayout.addComponent(pPanel);
		last.push(pPanel);
	}
	
	private void makeLayout() {
		mainLayout = new AbsoluteLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setMargin(false);
		
		setWidth("100.0%");
		setHeight("100.0%");
		
		sbcLayout = new VerticalLayout();
		
		this.makeSearchLayout();
		this.makeContentLayout();
		this.makeButtonLayout();
		
		bcLayout = new HorizontalLayout();
		bcLayout.addComponent(buttonLayout);
		Label emptyLabel = new Label();
		emptyLabel.setWidth("40px");
		bcLayout.addComponent(emptyLabel);
		bcLayout.addComponent(contentLayout);
		sbcLayout.setSpacing(true);
		sbcLayout.addComponent(searchLayout);
		emptyLabel = new Label();
		sbcLayout.addComponent(emptyLabel);
		sbcLayout.addComponent(bcLayout);
		
		if (!login.getGenSpaceServerFactory().isLoggedIn()) {
			//return ;
		}else{
			//searchRosterFrame();
		}
		
	
		mainLayout.addComponent(sbcLayout);
	}
	
	private void makeSearchLayout() {
		searchLayout = new HorizontalLayout();
		Label genSpaceLabel = new Label("<b>" + this.GenSpace + "</b>", Label.CONTENT_XHTML);
		Label emptyLabel = new Label();
		emptyLabel.setWidth("70px");
		search = new ComboBox(this.Search);
		search.setTextInputAllowed(true);
		search.setNewItemsAllowed(true);
		search.setInputPrompt("Please select or input username for search");

		Button go = new Button(this.Go);
		go.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				if (!login.getGenSpaceServerFactory().isLoggedIn()) {
					return ;
				}
				
				Object comboObject = search.getValue();
				User f;
				if (comboObject instanceof User) {
					f = (User) comboObject;
				} else if (comboObject != null){
					String comboValue = comboObject.toString();
					f = login.getGenSpaceServerFactory().getUserOps().getProfile(comboValue);
					
					if (f == null) {
						String errorMsg = findNoUserMsg.replace("xxx", comboValue);
						getApplication().getMainWindow().showNotification(errorMsg);
						return ;
					}
				} else {
					return ;
				}
				UserSearchWindow usw = new UserSearchWindow(f, login, SocialNetworkHome.this);
				getApplication().getMainWindow().addWindow(usw);
			}
		});
		searchLayout.setSpacing(true);
		searchLayout.addComponent(genSpaceLabel);
		searchLayout.addComponent(emptyLabel);
		searchLayout.addComponent(search);
		searchLayout.addComponent(go);
		searchLayout.setComponentAlignment(go, Alignment.BOTTOM_CENTER);
	}
	
	private void makeButtonLayout() {
		this.buttonLayout = new VerticalLayout();
		buttonLayout.setSpacing(true);
		Button profile = new Button(this.title);
		profile.addListener(new Button.ClickListener(){
			
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {				
				proPanel.updatePanel();
				setContent(proPanel);
			}
		});
		profile.setWidth("100px");
		
		Button network = new Button(this.myNet);
		network.addListener(new Button.ClickListener(){

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if (!login.getGenSpaceServerFactory().isLoggedIn())
					return ;
				
				netPanel.updatePanel();
				setContent(netPanel);
			}
		});
		network.setWidth("100px");
		
		Button friend = new Button(this.myFriends);
		friend.addListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent event) {
					if (!login.getGenSpaceServerFactory().isLoggedIn()) {
						return ;
					}
					
					friendPanel.updatePanel();
					setContent(friendPanel);
				}
		});
		friend.setWidth("100px");
	
		
		Button bSettings = new Button(this.settings);
		bSettings.setWidth("100px");
		bSettings.addListener(new Button.ClickListener(){
				private static final long serialVersionUID = 1L;
				
				public void buttonClick(ClickEvent event) {
					if (!login.getGenSpaceServerFactory().isLoggedIn()) {
						return ;
					}
					privacyPanel.updatePanel();
					setContent(privacyPanel);
				}			
		});
		
		Button vRequest = new Button(this.vRequests);
		vRequest.setWidth("100px");
		vRequest.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				if (!login.getGenSpaceServerFactory().isLoggedIn()) {
					return ;
				}		
				viewPanel.updatePanel();
				setContent(viewPanel);
				viewPanel.attachPusher();
			}
		});
		
		Button back = new Button(this.back);
		back.addListener(new Button.ClickListener(){
			private static final long serialVersionUID = 1L;
			
			public void buttonClick(ClickEvent event) {
				
				if (!login.getGenSpaceServerFactory().isLoggedIn()) {
					return ;
				}
				
				if(last.isEmpty())
					return ;
				
				SocialPanel sp;

				switch(backTo) {
				case BACK_TO_MYNET:
					sp = SocialNetworkHome.this.netPanel;
					sp.updatePanel();
					backTo = -1;
					break;				
				default:
					sp = last.pop();
					break;
				}
				
				setRealContent(sp);
			}
		});
		back.setWidth("100px");
		
		buttonLayout.addComponent(profile);
		buttonLayout.addComponent(network);
		buttonLayout.addComponent(friend);
		buttonLayout.addComponent(bSettings);
		buttonLayout.addComponent(vRequest);
		buttonLayout.addComponent(back);
	}
	
	private void makeContentLayout() {
		this.contentLayout = new VerticalLayout();
		proPanel = new ProfilePanel(title, login);
		this.setRealContent(proPanel);
	}
	
	private void setContent(SocialPanel sp) {
		if(sp != null) {
			last.push(current);
			this.setRealContent(sp);
		}
	}
	
	private void setRealContent(SocialPanel sp) {
		current = sp;
		if(sp.getPanelTitle().equals(title)) {
			ProfilePanel pp = (ProfilePanel)sp;
			contentLayout.removeAllComponents();
			contentLayout.addComponent(pp);
		} else if(sp.getPanelTitle().equals(myNet)) {
			NetworkPanel np = (NetworkPanel)sp;
			np.setRf(this.chatHandler.rf);
			np.setCr(this.chatHandler);
			contentLayout.removeAllComponents();
			contentLayout.addComponent(np);
		} else if(sp.getPanelTitle().equals(myFriends)) {
			FriendPanel fp = (FriendPanel)sp;
			fp.addStyleName(Runo.PANEL_LIGHT);
			contentLayout.removeAllComponents();
			contentLayout.addComponent(fp);
		} else if(sp.getPanelTitle().equals(settings)) {
			PrivacyPanel pr = (PrivacyPanel)sp;
			contentLayout.removeAllComponents();
			contentLayout.addComponent(pr);
		} else if(sp.getPanelTitle().equals(pRequests)) {
			RequestPanel rp = (RequestPanel)sp;
			contentLayout.removeAllComponents();
			contentLayout.addComponent(rp);
		}	
	}
	
	public boolean pendingFriendRequestTo(User u) {
		if(u.isFriendsWith())
			return false;
		for(User tmpU : friendList)
		{
			if(tmpU.getUsername().equals(u.getUsername()))
				return true;
		}
		return false;
	}
	
	@Override
	public void tabSelected() {
		ProfilePanel tmp = (ProfilePanel)this.proPanel;
		tmp.createProfileForm();
	}

	@Override
	public void loggedIn() {
		// TODO Auto-generated method stub
		this.initForm();
		this.chatHandler = this.login.getChatHandler();
	}

	@Override
	public void loggedOut() {
		// TODO Auto-generated method stub
		this.last.clear();
		this.clearSettings();
		GenSpaceWindow.getGenSpaceBlackboard().fire(new ChatStatusChangeEvent(login.getGenSpaceServerFactory().getUsername()));
	}
	
	private void clearSettings() {
		this.chatHandler.getConnection().disconnect();
		this.search.removeAllItems();
		this.proPanel.updatePanel();
		this.contentLayout.removeAllComponents();
		this.contentLayout.addComponent(this.proPanel);
		
		//Remove all chat window here
		Iterator<String> chatIT = this.chatHandler.chats.keySet().iterator();
		while(chatIT.hasNext()) {
			this.chatHandler.chats.get(chatIT.next()).removeAllComponents();
		}
	}
	
	public void loadSearchItems() {
		search.removeAllItems();
		
		if (!friendList.isEmpty() && friendList != null) {
			Iterator<User> friendIT = friendList.iterator();
			User f;
			while (friendIT.hasNext()) {
				f = friendIT.next();
				this.search.addItem(f);
				this.search.setItemCaption(f, f.getUsername());
			}
		}
	}
	
	public void initForm() {
		if (login.getGenSpaceServerFactory().isLoggedIn()) {
			this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
			this.uNetworkList = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
			this.proPanel.updatePanel();
			this.friendPanel = new FriendPanel(this.myFriends, this.login);
			this.netPanel = new NetworkPanel(this.myNet, this.login, this);
			NetworkPanel np = (NetworkPanel)this.netPanel;
			this.privacyPanel = new PrivacyPanel(this.settings, this.login);
			this.viewPanel = new RequestPanel(this.pRequests, this.login);
						
			this.loadSearchItems();
		}
	}
	
	public void updateForm(boolean needBroadcast) {
		this.friendList = login.getGenSpaceServerFactory().getFriendOps().getFriends();
		this.uNetworkList = login.getGenSpaceServerFactory().getNetworkOps().getMyNetworks();
		this.proPanel.updatePanel();
		this.friendPanel.updatePanel();
		this.netPanel.updatePanel();
		this.privacyPanel.updatePanel();
		this.viewPanel.updatePanel();
		
		if (needBroadcast) {			
			//Sleep for roster updated
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				//Do nothing if we cannot sleep
			}
			
			BroadCaster.broadcastPresence(this.login);
			/*if (this.chatHandler.rf != null) {
				this.chatHandler.rf.refresh(false);
			}*/
		}
		
		this.loadSearchItems();
	}
	
	@Override
	public void changeFriendStatus(FriendStatusChangeEvent evt) {
		if (login.getGenSpaceServerFactory().isLoggedIn()) {
			int myID = login.getGenSpaceServerFactory().getUser().getId();
			
			if (myID == evt.getMyID() || myID == evt.getFriendID()) {
				updateForm(true);
			}
		}
	}
	
	public RosterFrame getRf() {
		return this.chatHandler.rf;
	}
}
