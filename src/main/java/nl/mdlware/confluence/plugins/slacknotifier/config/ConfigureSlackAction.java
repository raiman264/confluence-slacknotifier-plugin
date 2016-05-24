package nl.mdlware.confluence.plugins.slacknotifier.config;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.xwork.RequireSecurityToken;

import java.util.ArrayList;
import java.util.List;

public class ConfigureSlackAction extends ConfluenceActionSupport {
	private static final long serialVersionUID = -5732284806136026379L;
	
	private SlackConfigurationManager slackConfigurationManager;
	
	private List<SlackConnectionData> slackConnectionDataList;
	
	private String[] spaceKeys;
	private String[] apiKeys;
    private String[] slackChannels;


    public String input() {
		this.updateTemplateData();
		return INPUT;
	}
	
	@RequireSecurityToken(true)
	public String save() {
		this.updateTemplateData();
		addActionMessage(getText("successfully.saved.api.keys"));
		
		List<SlackConnectionData> pairs = this.parseSlackConnectionData();
		this.slackConfigurationManager.setSlackConnectionDataList(pairs);
		
		return SUCCESS;
	}
	
	@Override
	public String getActionName(String fullClassName) {
		return getText("action.name");
	}

	@Override
	public boolean isPermitted() {
		return true;
	}
	
	public void setSlackConfigurationManager(SlackConfigurationManager manager) {
		this.slackConfigurationManager = manager;
	}
	
	public List<SlackConnectionData> getSlackConnectionDataList() {
		return this.slackConnectionDataList;
	}
	
	public void setSpaceKeys(String[] keys) {
		this.spaceKeys = keys;
	}

	public void setApiKeys(String[] keys) {
		this.apiKeys = keys;
	}

    public void setSlackChannels(String[] slackChannels) {
        this.slackChannels = slackChannels;
    }

	private void updateTemplateData() {
		this.updateSlackConnectionData();
	}

	private void updateSlackConnectionData() {
		this.slackConnectionDataList = this.slackConfigurationManager.getSlackConnectionDataList();
	}

	private List<SlackConnectionData> parseSlackConnectionData() {
		List<SlackConnectionData> result = new ArrayList<SlackConnectionData>();
		if (this.spaceKeys == null || this.apiKeys == null || this.slackChannels == null ||
                this.spaceKeys.length != this.apiKeys.length || this.spaceKeys.length != this.slackChannels.length) {
			return result;
		}

		for (int i=0; i<this.spaceKeys.length; i++) {
			if (apiKeys[i] != null && (apiKeys[i] != "") && slackChannels[i] != null && (slackChannels[i] != "")) {
				result.add(new SlackConnectionData(spaceKeys[i], apiKeys[i], slackChannels[i]));
			}
		}

		return result;
	}

}
