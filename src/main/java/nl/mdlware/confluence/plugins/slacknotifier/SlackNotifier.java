package nl.mdlware.confluence.plugins.slacknotifier;

import com.atlassian.confluence.event.events.content.ContentEvent;
import com.atlassian.confluence.event.events.content.comment.CommentCreateEvent;
import com.atlassian.confluence.event.events.content.comment.CommentEvent;
import com.atlassian.confluence.event.events.content.comment.CommentRemoveEvent;
import com.atlassian.confluence.event.events.content.comment.CommentUpdateEvent;
import com.atlassian.confluence.event.events.content.page.*;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import nl.mdlware.confluence.plugins.slacknotifier.config.SlackConfigurationManager;
import nl.mdlware.confluence.plugins.slacknotifier.config.SlackConnectionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.util.List;

public class SlackNotifier implements DisposableBean {
    private EventPublisher eventPublisher;
    private SlackSessionFactoryWrapper slackSessionFactory;
    private SlackConfigurationManager slackConfigurationManager;

//    private String slackChannel = "eduscrum";
//    private String spaceKey = "EDU";
//    private String slackBotAuthToken = "xoxb-44899160594-xpmNF8zXgPD7XHNcMVqeBf36";

    private Logger logger = LoggerFactory.getLogger(getClass());

    public SlackNotifier(EventPublisher eventPublisher, SlackSessionFactoryWrapper slackSessionFactory, SlackConfigurationManager slackConfigurationManager) {
        this.eventPublisher = eventPublisher;
        this.slackSessionFactory = slackSessionFactory;
        this.slackConfigurationManager = slackConfigurationManager;
        this.eventPublisher.register(this);
    }

    @EventListener
    public void pageUpdateEvent(PageUpdateEvent event) {
        this.sendNotification(event);
    }

    @EventListener
    public void pageCreateEvent(PageCreateEvent event) {
        this.sendNotification(event);
    }

    @EventListener
    public void pageTrashedEvent(PageTrashedEvent event) {
        this.sendNotification(event);
    }

    @EventListener
    public void pageRemoveEvent(PageRemoveEvent event) {
        this.sendNotification(event);
    }

    @EventListener
    public void pageRestoreEvent(PageRestoreEvent event) {
        this.sendNotification(event);
    }

    @EventListener
    public void commentCreateEvent(CommentCreateEvent event) {
        this.sendNotification(event);
    }

    @EventListener
    public void commentUpdateEvent(CommentUpdateEvent event) {
        this.sendNotification(event);
    }

    @EventListener
    public void commentRemoveEvent(CommentRemoveEvent event) {
        this.sendNotification(event);
    }

    private void sendNotification(final CommentEvent event) {
        if (event.getComment().getContainer() instanceof AbstractPage) {
            this.sendNotification((AbstractPage) event.getComment().getContainer(), event);
        } else {
            throw new RuntimeException("Unknown event type: " + event.getComment().getContainer().getType());
        }
    }

    private void sendNotification(final PageEvent event) {
        this.sendNotification(event.getPage(), event);
    }

    private void sendNotification(final AbstractPage page, final ContentEvent event) {
        List<SlackConnectionData> slackConnectionDataList = slackConfigurationManager.getSlackConnectionDataList();
        for (SlackConnectionData slackConnectionData : slackConnectionDataList) {
            SlackSession session =  slackSessionFactory.createWebSocketSlackSession(slackConnectionData.getApiKey());

            if (page.getSpaceKey().equals(slackConnectionData.getSpaceKey()) && slackConnectionData.getSlackChannel() != null && slackConnectionData.getSlackChannel() != "") {
                try {
                    session.connect();
                    SlackChannel channel = session.findChannelByName(slackConnectionData.getSlackChannel());
                    session.sendMessage(channel, event.getContent().getLastModifier().getFullName() + " performed a " + event.getClass().getSimpleName() + " on " + event.getContent().getLastModificationDate());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException("Cannot connect to Slack: ", e);
                }
            }
        }
    }

    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }
}