package nl.mdlware.confluence.plugins.slacknotifier.config;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static nl.mdlware.confluence.plugins.slacknotifier.SlackConnectionDataTestSet.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureSlackActionTest  {
    private ConfigureSlackAction configureSlackAction;
    private List<SlackConnectionData> slackConnectionDataList;

    @Mock
    private SlackConfigurationManager slackConfigurationManager;

    @Before
    public void setUp() throws Exception {
        final Properties pluginProperties = new Properties();
        pluginProperties.load(ClassLoader.getSystemResourceAsStream("confluence-slacknotifier-plugin.properties"));
        configureSlackAction = new ConfigureSlackAction() {
            @Override
            public String getText(String key) {
                return pluginProperties.getProperty(key);
            }
        };
        configureSlackAction.setSlackConfigurationManager(slackConfigurationManager);
        slackConnectionDataList = new ArrayList<SlackConnectionData>();
        slackConnectionDataList.add(new SlackConnectionData(SPACE_KEY, API_KEY, SLACK_CHANNEL));

        when(slackConfigurationManager.getSlackConnectionDataList()).thenReturn(slackConnectionDataList);
    }

    @Test
    public void alwaysPermitted()
    {
        assertTrue(configureSlackAction.isPermitted());
    }

    @Test
    public void actionNameCanBeLoadedFromi18N()
    {
        assertEquals("Configure Slack integration.", configureSlackAction.getActionName(""));
    }

    @Test
    public void input()
    {
        assertEquals(ConfluenceActionSupport.INPUT, configureSlackAction.input());
        assertEquals(slackConnectionDataList, configureSlackAction.getSlackConnectionDataList());
    }

    @Test
    public void savingEmptyPageDoesNotChangeContentOfEmptySlackConnectionDataList()
    {
        when(slackConfigurationManager.getSlackConnectionDataList()).thenReturn(new ArrayList<SlackConnectionData>());
        assertEquals(ConfluenceActionSupport.SUCCESS, configureSlackAction.save());
        assertEquals(0, configureSlackAction.getSlackConnectionDataList().size());
    }

    @Test
    public void savingNonEmptyPageDoesChangeContentOfEmptySlackConnectionDataList()
    {
        when(slackConfigurationManager.getSlackConnectionDataList()).thenReturn(new ArrayList<SlackConnectionData>());
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();

                ArrayList<SlackConnectionData> arg = (ArrayList<SlackConnectionData>) args[0];
                assertEquals(1, arg.size());

                assertEquals(API_KEY, arg.get(0).getApiKey());
                assertEquals(SPACE_KEY, arg.get(0).getSpaceKey());
                assertEquals(SLACK_CHANNEL, arg.get(0).getSlackChannel());

                return arg;
            }
        }).when(slackConfigurationManager).setSlackConnectionDataList(anyList());
        configureSlackAction.setApiKeys(new String[]{API_KEY});
        configureSlackAction.setSlackChannels(new String[]{SLACK_CHANNEL});
        configureSlackAction.setSpaceKeys(new String[]{SPACE_KEY});
        assertEquals(ConfluenceActionSupport.SUCCESS, configureSlackAction.save());
        verify(slackConfigurationManager, times(1)).setSlackConnectionDataList(anyList());
    }
}