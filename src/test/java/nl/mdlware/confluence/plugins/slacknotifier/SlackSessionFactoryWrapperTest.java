package nl.mdlware.confluence.plugins.slacknotifier;

import org.junit.Test;

import static org.junit.Assert.*;

public class SlackSessionFactoryWrapperTest {
    @Test
    public void createWebSocketSlackSession() throws Exception {
        SlackSessionFactoryWrapper slackSessionFactoryWrapper = new SlackSessionFactoryWrapper();
        assertNotNull(slackSessionFactoryWrapper.createWebSocketSlackSession("12345"));
    }

}