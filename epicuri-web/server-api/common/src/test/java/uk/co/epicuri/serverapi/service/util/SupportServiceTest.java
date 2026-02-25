package uk.co.epicuri.serverapi.service.util;

import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.AsyncCommunicationsService;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class SupportServiceTest extends BaseIT {
    @Autowired
    private SupportService supportService;

    @Test
    public void sendLogFile() {
        restaurant1.setInternalEmailAddress("blackhole@mailinator.com");
        restaurantRepository.save(restaurant1);

        AsyncCommunicationsService asyncCommunicationsService = mock(AsyncCommunicationsService.class);
        Whitebox.setInternalState(supportService, "asyncCommunicationsService", asyncCommunicationsService);
        expect(asyncCommunicationsService.sendInternalSupportEmail(restaurant1.getInternalEmailAddress(),"LOG FILE DUMP: " + restaurant1.getId() + ": " + restaurant1.getName(), "foo\\rbar\\r")).andReturn(null);
        replay(asyncCommunicationsService);

        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("bar");
        supportService.sendLogFile(restaurant1.getId(), list);
        verify(asyncCommunicationsService);
    }
}