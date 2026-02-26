package uk.co.epicuri.serverapi.service.external;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.repository.BaseIT;

import static org.junit.Assert.assertTrue;

@Ignore
public class SMSServiceTest extends BaseIT {
    @Autowired
    private SMSService smsService;

    @Ignore
    @Test
    public void send() throws Exception {
        assertTrue(smsService.send("This is a test", "447803293799"));
    }

}