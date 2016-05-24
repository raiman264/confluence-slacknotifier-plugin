package nl.mdlware.confluence.plugins.slacknotifier.config;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SlackConfigurationManager {
	public static final String SLACK_API_KEYS = "ext.slack.api.keys";
	
	private BandanaManager bandanaManager;
	private SpaceManager spaceManager;
	
	public SlackConfigurationManager() {
	}
	
	/**
	 * Returns a Slack ApiKeyPair for each Space in the system.
	 * 
	 * {@link SlackConnectionData#getApiKey()} might be null, if it hasn't been
	 * configured for the given space..
	 * 
	 * @return
	 */
	public List<SlackConnectionData> getSlackConnectionDataList() {
		List<SlackConnectionData> result = new ArrayList<SlackConnectionData>();
		Properties props = this.readApiKeyPropertiesObj();
		
		List<Space> spaces = this.spaceManager.getAllSpaces();
		for (Space space : spaces) {	
			String apiAndChannel = props.getProperty(space.getKey());
			if (apiAndChannel != null) {
				String apiKey = apiAndChannel.split(",")[0];
				String slackChannel = apiAndChannel.split(",")[1];
				result.add(new SlackConnectionData(space, apiKey, slackChannel));
			}
			else result.add(new SlackConnectionData(space, "",""));
		}
		
		return result;
	}
	
	/**
	 * Save listed ApiKeys.
	 * 
	 * @param slackConnectionDataList
	 */
	public void setSlackConnectionDataList(List<SlackConnectionData> slackConnectionDataList) {
		Properties props = new Properties();
		
		for (SlackConnectionData slackConnectionData : slackConnectionDataList) {
			props.put(slackConnectionData.getSpaceKey(), slackConnectionData.getApiKey() + "," + slackConnectionData.getSlackChannel());
		}
		
		OutputStream out = new ByteArrayOutputStream();
		try {
			props.store(out, null);
		} catch (IOException ioe) {} // seems unlikely to happen
		
		this.bandanaManager.setValue(new ConfluenceBandanaContext(),
				SLACK_API_KEYS, out.toString());
	}
	
	public String getApiKeyForSpace(Space space) {
		List<SlackConnectionData> pairs = this.getSlackConnectionDataList();
		for (SlackConnectionData pair : pairs) {
			if (pair.getSpace() == space) {
				return pair.getApiKey();
			}
		}
		
		return null;
	}
	
	// Bean configuration
	
	public void setBandanaManager(BandanaManager manager) {
		this.bandanaManager = manager;
	}
	
	public void setSpaceManager(SpaceManager manager) {
		this.spaceManager = manager;
	}
	
	// Helpers
	
	private Properties readApiKeyPropertiesObj() {
		ConfluenceBandanaContext context = new ConfluenceBandanaContext();
		String propsString = (String)this.bandanaManager.getValue(context, SLACK_API_KEYS);
		if (propsString == null) propsString = ""; // initially it doesn't exist
		
		Properties props = new Properties();
		InputStream in = new ByteArrayInputStream(propsString.getBytes());
		
		try {
			props.load(in);
		} catch (IOException ioe) {} // seems unlikely to happen
		
		return props;
	}
}
