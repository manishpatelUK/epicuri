package uk.co.epicuri.serverapi.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhoneNumberValidationService {
    @Autowired
    private MasterDataService masterDataService;

    private PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public String trimPhoneNumber(String phone) {
        if(phone == null) {
            return null;
        }

        phone = phone.trim();
        phone = phone.replaceAll("\\(", "");
        phone = phone.replaceAll("\\)", "");
        phone = phone.replaceAll("\\+", "");
        phone = phone.replaceAll("\\s", "");
        if(phone.startsWith("00")) {
            phone = phone.substring(2, phone.length());
        }

        if(phone.startsWith("0")) {
            phone = phone.substring(1, phone.length());
        }

        return phone;
    }

    public String getInternationalCode(String acronym) {
        return trimPhoneNumber(masterDataService.getDialingCode(acronym));
    }

    public Phonenumber.PhoneNumber parse(String suspectedCountryOrigin, String phone) throws NumberParseException {
        return phoneNumberUtil.parse(phone, suspectedCountryOrigin);
    }

    public Phonenumber.PhoneNumber concat(String internationalCode, String phone) throws NumberParseException {
        if(!internationalCode.startsWith("+")) {
            internationalCode = "+" + internationalCode;
        }

        return phoneNumberUtil.parse(internationalCode + phone, "");
    }

    public String getRegion(Phonenumber.PhoneNumber number) {
        return phoneNumberUtil.getRegionCodeForNumber(number);
    }

    public String getRegion(String internationalCode) throws NumberFormatException{
        return phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(internationalCode));
    }

    public boolean isPossibleNumber(Phonenumber.PhoneNumber number) {
        return phoneNumberUtil.isPossibleNumber(number);
    }
}
