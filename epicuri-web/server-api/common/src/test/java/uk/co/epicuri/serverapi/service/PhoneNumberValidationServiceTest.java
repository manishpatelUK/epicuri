package uk.co.epicuri.serverapi.service;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.repository.BaseIT;

import static org.junit.Assert.*;

public class PhoneNumberValidationServiceTest extends BaseIT {
    @Autowired
    private PhoneNumberValidationService phoneNumberValidationService;

    @Test
    public void testTrimPhoneNumber() throws Exception {
        String number = "00444343";
        assertEquals("444343", phoneNumberValidationService.trimPhoneNumber(number));

        number = "000444343";
        assertEquals("444343", phoneNumberValidationService.trimPhoneNumber(number));

        number = "(0)444 34 3";
        assertEquals("444343", phoneNumberValidationService.trimPhoneNumber(number));
    }

    @Test
    public void testParse() throws Exception {
        String number1 = "07984444444";
        String number2 = "01923450466";

        Phonenumber.PhoneNumber phoneNumber1 = phoneNumberValidationService.parse("GB", number1);
        Phonenumber.PhoneNumber phoneNumber2 = phoneNumberValidationService.parse("GB", number2);

        assertEquals("7984444444", String.valueOf(phoneNumber1.getNationalNumber()));
        assertEquals("44", String.valueOf(phoneNumber1.getCountryCode()));

        assertEquals("1923450466", String.valueOf(phoneNumber2.getNationalNumber()));
        assertEquals("44", String.valueOf(phoneNumber2.getCountryCode()));

        String number3 = "022890 72 038";
        Phonenumber.PhoneNumber phoneNumber3 = phoneNumberValidationService.parse("GR", number3);
        assertEquals("2289072038", String.valueOf(phoneNumber3.getNationalNumber()));
        assertEquals("30", String.valueOf(phoneNumber3.getCountryCode()));

        System.out.print(phoneNumber1.toString());
    }

    @Test
    public void testGetInternationalCode() throws Exception {
        assertEquals("44", phoneNumberValidationService.getInternationalCode("GB"));
        assertEquals("30", phoneNumberValidationService.getInternationalCode("GR"));
        assertEquals("31", phoneNumberValidationService.getInternationalCode("NL"));
    }

    @Test
    public void testConcat() throws Exception {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        Phonenumber.PhoneNumber number1 = phoneNumberValidationService.concat("44", "7892929922");
        assertEquals(44, number1.getCountryCode());
        assertEquals(7892929922L, number1.getNationalNumber());
        assertEquals("GB", phoneNumberUtil.getRegionCodeForNumber(number1));

        Phonenumber.PhoneNumber number2 = phoneNumberValidationService.concat("30", "7892929922");
        assertEquals(30, number2.getCountryCode());
        assertEquals(7892929922L, number2.getNationalNumber());
        assertEquals("GR", phoneNumberUtil.getRegionCodeForNumber(number2));

        Phonenumber.PhoneNumber number3 = phoneNumberValidationService.concat("31", "7892929922");
        assertEquals(31, number3.getCountryCode());
        assertEquals(7892929922L, number3.getNationalNumber());
        assertEquals("NL", phoneNumberUtil.getRegionCodeForNumber(number3));
    }
}