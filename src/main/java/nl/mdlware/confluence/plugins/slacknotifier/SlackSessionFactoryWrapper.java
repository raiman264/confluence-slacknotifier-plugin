package nl.mdlware.confluence.plugins.slacknotifier;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

public class SlackSessionFactoryWrapper {
    public SlackSession createWebSocketSlackSession(String slackBotAuthToken) {
        return SlackSessionFactory.createWebSocketSlackSession(slackBotAuthToken);
    }
}
