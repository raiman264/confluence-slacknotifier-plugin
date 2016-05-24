package nl.mdlware.confluence.plugins.slacknotifier.config;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static nl.mdlware.confluence.plugins.slacknotifier.SlackConnectionDataTestSet.*;
import static org.mockito.Mockito.when;

import com.atlassian.confluence.spaces.Space;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SlackConnectionDataTest {
    private SlackConnectionData slackConnectionData;

    @Mock
    private Space space;

    @Before
    public void setUp() throws Exception {
        when(space.getKey()).thenReturn(SPACE_KEY);
    }

    @Test
    public void createSlackConnectionDataWithSpaceKey()
    {
        slackConnectionData = new SlackConnectionData(SPACE_KEY, API_KEY, SLACK_CHANNEL);
        assertEquals(SPACE_KEY, slackConnectionData.getSpaceKey());
        assertEquals(API_KEY, slackConnectionData.getApiKey());
        assertEquals(SLACK_CHANNEL, slackConnectionData.getSlackChannel());
    }

    @Test
    public void createSlackConnectionDataWithSpaceObject()
    {
        slackConnectionData = new SlackConnectionData(space, API_KEY, SLACK_CHANNEL);
        assertEquals(SPACE_KEY, slackConnectionData.getSpaceKey());
        assertEquals(API_KEY, slackConnectionData.getApiKey());
        assertEquals(SLACK_CHANNEL, slackConnectionData.getSlackChannel());
        assertEquals(space, slackConnectionData.getSpace());
    }

    @Test
    public void slackConnectionDataOnlyAcceptsNonEmptyAPIKey()
    {
        slackConnectionData = new SlackConnectionData(SPACE_KEY, API_KEY, SLACK_CHANNEL);
        slackConnectionData.setApiKey(null);
        assertNull(slackConnectionData.getApiKey());

        slackConnectionData.setApiKey("");
        assertNull(slackConnectionData.getApiKey());
    }

}