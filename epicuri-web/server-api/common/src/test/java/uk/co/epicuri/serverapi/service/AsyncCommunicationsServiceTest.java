package uk.co.epicuri.serverapi.service;

import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.external.EmailService;
import uk.co.epicuri.serverapi.service.external.IMailBuilder;

import java.io.File;
import java.io.InputStream;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class AsyncCommunicationsServiceTest extends BaseIT{
    @Autowired
    private AsyncCommunicationsService asyncCommunicationsService;

    @Test
    public void onNewCustomer() throws Exception {

    }

    @Test
    public void sendSMSCode() throws Exception {
    }

    @Test
    public void sendBookingConfirmations() throws Exception {
        EmailService emailServiceMock = mock(EmailService.class);
        EmailService.MultipartEmailBuilder iMailBuilder1 = mock(EmailService.MultipartEmailBuilder.class);
        EmailService.MultipartEmailBuilder iMailBuilder2 = mock(EmailService.MultipartEmailBuilder.class);

        Whitebox.setInternalState(asyncCommunicationsService,"emailService",emailServiceMock);

        booking1.setName("foo");
        booking1.setEmail("bar@mail.com");
        booking1.setCustomerId(null);
        bookingRepository.save(booking1);
        restaurant1.setIANATimezone("Europe/London");
        restaurant1.setAddress(new Address());
        restaurant1.setPosition(new LatLongPair(0,0));
        restaurant1.setPublicEmailAddress("epicuriblackhole@mailinator.com");
        restaurantRepository.save(restaurant1);

        expect(emailServiceMock.createBuilder(anyString(), anyString(), anyString(),anyString(),anyString(),anyString(),anyObject())).andReturn(iMailBuilder1);
        expect(emailServiceMock.createBuilder(anyString(), anyString(), anyString(),anyString(),anyString(),anyString(),anyObject())).andReturn(iMailBuilder2);
        expect(iMailBuilder1.build()).andReturn(true);
        expect(iMailBuilder2.build()).andReturn(true);
        replay(emailServiceMock, iMailBuilder1, iMailBuilder2);
        asyncCommunicationsService.sendBookingConfirmations(booking1.getId(), restaurant1.getId());
        verify(emailServiceMock);
    }

    @Test
    public void sendBookingConfirmationsSendActualEmails() throws Exception {
        booking1.setName("foo");
        booking1.setEmail("epicuriblackhole@mailinator.com");
        booking1.setCustomerId(null);
        bookingRepository.save(booking1);
        restaurant1.setIANATimezone("Europe/London");
        restaurant1.setAddress(new Address());
        restaurant1.setPosition(new LatLongPair(0,0));
        restaurant1.setPublicEmailAddress("epicuriblackhole@mailinator.com");
        restaurantRepository.save(restaurant1);

        asyncCommunicationsService.sendBookingConfirmations(booking1.getId(), restaurant1.getId());
    }

    @Test
    public void sendReceiptToCustomer() throws Exception {

    }

    @Test
    public void sendSimpleEmailNonAsync() throws Exception {
        try {
            assertTrue(asyncCommunicationsService.sendSimpleEmailNonAsync("epicuriblackhole@mailinator.com", "Epicuri", "foo", "bar"));
        } catch (Exception ex) {
            fail();
        }
    }
}