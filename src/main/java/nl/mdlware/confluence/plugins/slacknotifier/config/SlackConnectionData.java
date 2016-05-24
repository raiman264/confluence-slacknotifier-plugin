package nl.mdlware.confluence.plugins.slacknotifier.config;

import com.atlassian.confluence.spaces.Space;

public class SlackConnectionData {
	private Space space;
	private String spaceKey; // when Space is not available, use this as an ID
	private String apiKey; // slack API token
	private String slackChannel;
	
	public SlackConnectionData() {
	}
	
	public SlackConnectionData(Space space, String key, String slackChannel) {
        this();
		this.setSpace(space);
		this.setApiKey(key);
		this.setSlackChannel(slackChannel);
	}
	
	public SlackConnectionData(String spaceKey, String apiKey, String slackChannel) {
        this();
        this.spaceKey = spaceKey;
		this.setApiKey(apiKey);
		this.setSlackChannel(slackChannel);
	}

	public void setSpace(Space space) {
		this.space = space;
		this.spaceKey = space.getKey();
	}

	public Space getSpace() {
		return space;
	}

	public void setApiKey(String key) {
		if (key == null || key == "") {
			this.apiKey = null;
		} else {
			this.apiKey = key;
		}
	}

	public String getApiKey() {
		return apiKey;
	}
	
	public String getSpaceKey() {
		return this.spaceKey;
	}

	public String getSlackChannel() {
		return slackChannel;
	}

	public void setSlackChannel(String slackChannel) {
		this.slackChannel = slackChannel;
	}
}
