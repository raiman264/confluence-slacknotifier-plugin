package nl.mdlware.confluence.plugins.slacknotifier;

import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.event.events.content.comment.CommentCreateEvent;
import com.atlassian.confluence.event.events.content.comment.CommentRemoveEvent;
import com.atlassian.confluence.event.events.content.comment.CommentUpdateEvent;
import com.atlassian.confluence.event.events.content.page.*;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.event.api.EventPublisher;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import nl.mdlware.confluence.plugins.slacknotifier.config.SlackConfigurationManager;
import nl.mdlware.confluence.plugins.slacknotifier.config.SlackConnectionData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static nl.mdlware.confluence.plugins.slacknotifier.SlackConnectionDataTestSet.API_KEY;
import static nl.mdlware.confluence.plugins.slacknotifier.SlackConnectionDataTestSet.SLACK_CHANNEL;
import static nl.mdlware.confluence.plugins.slacknotifier.SlackConnectionDataTestSet.SPACE_KEY;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SlackNotifierUnitTest
{
    public static final Date TODAY = Calendar.getInstance().getTime();
    public static final String FULL_USER_NAME = "Rody Middelkoop";

    @Mock
    EventPublisher eventPublisher;

    @Mock
    SlackSessionFactoryWrapper slackSessionFactoryWrapper= mock(SlackSessionFactoryWrapper.class);

    @Mock
    SlackSession slackSession;

    @Mock
    Page page;

    @Mock
    Comment comment;

    @Mock
    SlackChannel slackChannel;

    @Mock
    private ConfluenceUser confluenceUser;
    private SlackNotifier slackNotifier;

    @Mock
    private SlackConfigurationManager slackConfigurationManager;

    @Before
    public void setUp()
    {
        when(confluenceUser.getFullName()).thenReturn(FULL_USER_NAME);
        when(slackSessionFactoryWrapper.createWebSocketSlackSession(API_KEY)).thenReturn(slackSession);
        when(page.getSpaceKey()).thenReturn(SPACE_KEY);
        when(page.getLastModificationDate()).thenReturn(TODAY);
        when(page.getLastModifier()).thenReturn(confluenceUser);
        when(comment.getLastModificationDate()).thenReturn(TODAY);
        when(comment.getLastModifier()).thenReturn(confluenceUser);
        when(slackSession.findChannelByName(SLACK_CHANNEL)).thenReturn(slackChannel);
        when(comment.getContainer()).thenReturn(page);
        List<SlackConnectionData> slackConnectionDataList = new ArrayList<SlackConnectionData>();
        slackConnectionDataList.add(new SlackConnectionData(SPACE_KEY, API_KEY, SLACK_CHANNEL));
        when(slackConfigurationManager.getSlackConnectionDataList()).thenReturn(slackConnectionDataList);
        slackNotifier = new SlackNotifier(eventPublisher, slackSessionFactoryWrapper, slackConfigurationManager);
    }

    @Test
    public void aNewPageCreatedEventGetsPushedToTheEduscrumChannel()
    {
        PageCreateEvent event = new PageCreateEvent(this, page);
        slackNotifier.pageCreateEvent(event);
        verifyEventInformationDeliveredToChannel(event);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void aPageUpdatedEventGetsPushedToTheEduscrumChannel()
    {
        PageUpdateEvent event = new PageUpdateEvent(this, page);
        slackNotifier.pageUpdateEvent(event);
        verifyEventInformationDeliveredToChannel(event);
    }

    @Test
    public void aPageRemovedEventGetsPushedToTheEduscrumChannel()
    {
        PageRemoveEvent event = new PageRemoveEvent(this, page);
        slackNotifier.pageRemoveEvent(event);
        verifyEventInformationDeliveredToChannel(event);
    }

    @Test
    public void aPageTrashedEventGetsPushedToTheEduscrumChannel()
    {
        PageTrashedEvent event = new PageTrashedEvent(this, page, confluenceUser);
        slackNotifier.pageTrashedEvent(event);
        verifyEventInformationDeliveredToChannel(event);
    }

    @Test
    public void aPageRestoredEventGetsPushedToTheEduscrumChannel()
    {
        PageRestoreEvent event = new PageRestoreEvent(this, page);
        slackNotifier.pageRestoreEvent(event);
        verifyEventInformationDeliveredToChannel(event);
    }

    @Test
    public void aCommentAddedEventGetsPushedToTheEduscrumChannel()
    {
        CommentCreateEvent event = new CommentCreateEvent(this, comment);
        slackNotifier.commentCreateEvent(event);
        verifyEventInformationDeliveredToChannel(event);
    }

    @Test(expected = RuntimeException.class)
    public void aCommentAddedEventWithoutProperContainerGetsRejected()
    {
        when(comment.getContainer()).thenReturn(null);
        CommentCreateEvent event = new CommentCreateEvent(this, comment);
        slackNotifier.commentCreateEvent(event);
    }

    @Test
    public void aCommentUpdatedEventGetsPushedToTheEduscrumChannel()
    {
        CommentUpdateEvent event = new CommentUpdateEvent(this, comment);
        slackNotifier.commentUpdateEvent(event);
        verifyEventInformationDeliveredToChannel(event);
    }

    @Test
    public void aCommentRemovedEventGetsPushedToTheEduscrumChannel()
    {
        CommentRemoveEvent event = new CommentRemoveEvent(this, comment, confluenceUser);
        slackNotifier.commentRemoveEvent(event);
        verifyEventInformationDeliveredToChannel(event);
    }

    @Test
    public void destroyCalledOnObjectMakesTheObjectRemovedFromEventPublisher() throws Exception {
        slackNotifier.destroy();
        verify(eventPublisher).unregister(slackNotifier);
    }

    @Test(expected = RuntimeException.class)
    public void aRuntimeExceptionIsThrownWhenSlackCannotBeReached() throws IOException {
        doThrow(IOException.class).when(slackSession).connect();
        slackNotifier.pageCreateEvent(new PageCreateEvent(this, page));
    }

    private void verifyEventInformationDeliveredToChannel(ContentEvent event) {
        verify(slackSession).sendMessage(slackChannel, FULL_USER_NAME + " performed a " + event.getClass().getSimpleName() + " on " + TODAY);
    }
}