package nl.mdlware.confluence.plugins.slacknotifier.config;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import static nl.mdlware.confluence.plugins.slacknotifier.SlackConnectionDataTestSet.*;
import static nl.mdlware.confluence.plugins.slacknotifier.config.SlackConfigurationManager.SLACK_API_KEYS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SlackConfigurationManagerTest {
    private DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());

    private SlackConfigurationManager slackConfigurationManager;

    @Spy
    Properties propertyStorage = new Properties();

    @Mock
    BandanaManager bandanaManager;

    @Mock
    SpaceManager spaceManager;

    @Mock
    ConfluenceBandanaContext bandanaContext;

    @Before
    public void setUp() throws Exception {
        slackConfigurationManager = new SlackConfigurationManager();
        slackConfigurationManager.setBandanaManager(bandanaManager);
        slackConfigurationManager.setSpaceManager(spaceManager);
        slackConfigurationManager.setBandanaContext(bandanaContext);
        slackConfigurationManager.setPropertyStorage(propertyStorage);
    }

    @Test
    public void slackConnectionDataListIsEmptyWhenNoSpacesAvailable()
    {
        when(spaceManager.getAllSpaces()).thenReturn(new ArrayList<Space>());
        assertEquals(0, slackConfigurationManager.getSlackConnectionDataList().size());
    }

    @Test
    public void slackConnectionDataContainsSpacesForSlackChannelAndAPIKeyWhenSpaceIsNotConnectedToSlack()
    {
        when(spaceManager.getAllSpaces()).thenReturn(new ArrayList<Space>() {{
            add(new Space(SPACE_KEY));
        }});
        when(bandanaManager.getValue(bandanaContext, SLACK_API_KEYS)).thenReturn("");
        assertEquals(1, slackConfigurationManager.getSlackConnectionDataList().size());
        SlackConnectionData slackConnectionData = slackConfigurationManager.getSlackConnectionDataList().get(0);
        assertEquals(SPACE_KEY, slackConnectionData.getSpaceKey());
        assertEquals(EMPTY, slackConnectionData.getSlackChannel());

        assertNull(slackConnectionData.getApiKey());
    }

    @Test
    public void slackConnectionDataContainsDataForSlackChannelAndAPIKeyWhenSpaceIsConnectedToSlack()
    {
        when(spaceManager.getAllSpaces()).thenReturn(new ArrayList<Space>() {{
            add(new Space(SPACE_KEY));
        }});
        when(bandanaManager.getValue(bandanaContext, SLACK_API_KEYS)).thenReturn(SLACKCONNECTIONDATA_AS_PROPERTYLINEITEM);
        assertEquals(1, slackConfigurationManager.getSlackConnectionDataList().size());
        SlackConnectionData slackConnectionData = slackConfigurationManager.getSlackConnectionDataList().get(0);
        assertEquals(SPACE_KEY, slackConnectionData.getSpaceKey());
        assertEquals(SLACK_CHANNEL, slackConnectionData.getSlackChannel());
        assertEquals(API_KEY, slackConnectionData.getApiKey());
    }

    @Test
    public void emptySlackConnectionDataLeadsToSavingEmptyPropertyDataSucceededWithDateTimeStampInBandana()
    {
        slackConfigurationManager.setSlackConnectionDataList(new ArrayList<SlackConnectionData>());
        verify(bandanaManager).setValue(eq(bandanaContext), eq(SLACK_API_KEYS), anyString());
    }

    @Test
    public void singleSlackConnectionDataLeadsToSavingNonEmptyPropertyDataProceededWithDateTimeStampInBandana()
    {
        slackConfigurationManager.setSlackConnectionDataList(new ArrayList<SlackConnectionData>() {{
            add(new SlackConnectionData(SPACE_KEY, API_KEY, SLACK_CHANNEL));
        }});

        verify(bandanaManager).setValue(eq(bandanaContext), eq(SLACK_API_KEYS), anyString());
    }

    @Test(expected = RuntimeException.class)
    public void anyIOExceptionDuringPropertyStorageGetsTranslatedIntoRuntimeException() throws IOException {
        doThrow(IOException.class).when(propertyStorage).store(any(OutputStream.class), anyString());
        slackConfigurationManager.setSlackConnectionDataList(new ArrayList<SlackConnectionData>());
        verifyZeroInteractions(bandanaManager);
    }

    @Test(expected = RuntimeException.class)
    public void anyIOExceptionDuringPropertyAccessGetsTranslatedIntoRuntimeException() throws IOException {
        doThrow(IOException.class).when(propertyStorage).load(any(InputStream.class));
        slackConfigurationManager.getSlackConnectionDataList();
    }

}