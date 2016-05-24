package nl.mdlware.confluence.plugins.slacknotifier.config;

import com.atlassian.bandana.BandanaContext;
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

    private BandanaContext bandanaContext = new ConfluenceBandanaContext();
    private Properties propertyStorage = new Properties();

    public SlackConfigurationManager() {
	}
	
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

	public void setSlackConnectionDataList(List<SlackConnectionData> slackConnectionDataList) {
        propertyStorage.clear();

		for (SlackConnectionData slackConnectionData : slackConnectionDataList) {
			propertyStorage.put(slackConnectionData.getSpaceKey(), slackConnectionData.getApiKey() + "," + slackConnectionData.getSlackChannel());
		}
		
		OutputStream out = new ByteArrayOutputStream();
		try {
			propertyStorage.store(out, null);
		} catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe);
        }
		
		this.bandanaManager.setValue(bandanaContext, SLACK_API_KEYS, out.toString());
	}

	private Properties readApiKeyPropertiesObj() {
        propertyStorage.clear();

        String propsString = (String)this.bandanaManager.getValue(bandanaContext, SLACK_API_KEYS);
		if (propsString == null) propsString = "";

		InputStream in = new ByteArrayInputStream(propsString.getBytes());

		try {
			propertyStorage.load(in);
		} catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe);
        }

		return propertyStorage;
	}

    public void setBandanaManager(BandanaManager manager) {
        this.bandanaManager = manager;
    }

    public void setSpaceManager(SpaceManager manager) {
        this.spaceManager = manager;
    }

    public void setBandanaContext(BandanaContext bandanaContext) {
        this.bandanaContext = bandanaContext;
    }

    public void setPropertyStorage(Properties propertyStorage) {
        this.propertyStorage = propertyStorage;
    }
}
