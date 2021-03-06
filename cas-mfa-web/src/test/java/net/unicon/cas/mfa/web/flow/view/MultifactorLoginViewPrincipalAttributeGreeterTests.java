package net.unicon.cas.mfa.web.flow.view;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.binding.message.Message;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.Severity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class MultifactorLoginViewPrincipalAttributeGreeterTests {

    @Mock
    private MessageContext messageContext;

    @Mock
    private Message msg;

    public MultifactorLoginViewPrincipalAttributeGreeterTests() {
        MockitoAnnotations.initMocks(this);

        when(msg.getSeverity()).thenReturn(Severity.INFO);
        when(msg.getSource()).thenReturn(MultifactorLoginViewPrincipalAttributeGreeter.CODE);
        final Message[] values = new Message[1];
        values[0] = msg;

        when(messageContext.getMessagesBySource(any(Object.class))).thenReturn(values);
    }

    @Test
    public void testValidPrincipalMultivaluedAttributeToGreet() {
        final Map map = new HashMap();
        map.put("firstName", Arrays.asList("cas", "sso"));
        map.put("lastName", "user");

        final Principal p = new SimplePrincipal("userid", map);

        final MultifactorLoginViewPrincipalAttributeGreeter greeter = new MultifactorLoginViewPrincipalAttributeGreeter(
                "firstName");

        configureMessageContextForPrincipal("cas");
        final String value = greeter.getPersonToGreet(p, this.messageContext);
        assertTrue(value.contains("cas"));
    }

    @Test
    public void testValidPrincipalAttributeToGreet() {
        final Map map = new HashMap();
        map.put("firstName", "cas");
        map.put("lastName", "user");

        final Principal p = new SimplePrincipal("userid", map);

        final MultifactorLoginViewPrincipalAttributeGreeter greeter = new MultifactorLoginViewPrincipalAttributeGreeter(
                "firstName");

        configureMessageContextForPrincipal("cas");
        final String value = greeter.getPersonToGreet(p, this.messageContext);
        assertTrue(value.contains("cas"));
    }

    @Test
    public void testInvalidPrincipalAttributeToGreet() {
        final Map map = new HashMap();
        final Principal p = new SimplePrincipal("userid", map);

        final MultifactorLoginViewPrincipalAttributeGreeter greeter = new MultifactorLoginViewPrincipalAttributeGreeter(
                "firstName");
        configureMessageContextForPrincipal(p.getId());
        final String value = greeter.getPersonToGreet(p, this.messageContext);
        assertTrue(value.contains(p.getId()));
    }

    private void configureMessageContextForPrincipal(final String expectedId) {
        when(msg.getText()).thenReturn("Welcome " + expectedId);

    }
}
