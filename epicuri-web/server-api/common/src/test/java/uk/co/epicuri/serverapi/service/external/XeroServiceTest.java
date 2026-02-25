package uk.co.epicuri.serverapi.service.external;

import com.google.common.collect.Lists;
import com.xero.api.OAuthAccessToken;
import com.xero.api.OAuthAuthorizeToken;
import com.xero.api.OAuthRequestToken;
import com.xero.model.Account;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.KVDataConstants;
import uk.co.epicuri.serverapi.common.pojo.external.xero.XeroMappingRule;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XeroServiceTest extends BaseIT {
    @Autowired
    private XeroService xeroService;

    @Before
    public void setUpRestaurant() throws Exception {
        super.setUp();

        KVData data = new KVData();
        restaurant1.getIntegrations().put(ExternalIntegration.XERO, data);
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void convertToRules1() {
        Map<String,String> data = new HashMap<>();
        data.put("foo","bar");
        List<TaxRate> rates = Lists.newArrayList(tax1,tax2);
        List<AdjustmentType> adjustmentTypes = Lists.newArrayList(adjustmentType1, adjustmentType2);

        List<XeroMappingRule> xeroMappingRules = xeroService.convertToRules(data, adjustmentTypes, rates);
        assertEquals(0, xeroMappingRules.size());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void convertToRules2() {
        Map<String,String> data = new HashMap<>();
        data.put(xeroService.getKVDataConstantsKey(ItemType.FOOD, tax1),"1");
        data.put(xeroService.getKVDataConstantsKey(ItemType.DRINK, tax1),"2");
        data.put(xeroService.getKVDataConstantsKey(ItemType.OTHER, tax2),"3");
        data.put(xeroService.getKVDataConstantsKey(adjustmentType1, null),"4");
        data.put(xeroService.getKVDataConstantsKey(adjustmentType2, null),"5");
        tax1.setName("tax1");
        tax2.setName("tax2");
        List<TaxRate> rates = Lists.newArrayList(tax1,tax2);
        List<AdjustmentType> adjustmentTypes = Lists.newArrayList(adjustmentType1, adjustmentType2);

        List<XeroMappingRule> xeroMappingRules = xeroService.convertToRules(data, adjustmentTypes, rates);
        assertEquals(5, xeroMappingRules.size());
        assertEquals("1", filter(ItemType.FOOD, tax1, xeroMappingRules).getXeroAccountCode());
        assertEquals("2", filter(ItemType.DRINK, tax1, xeroMappingRules).getXeroAccountCode());
        assertEquals("3", filter(ItemType.OTHER, tax2, xeroMappingRules).getXeroAccountCode());
        assertEquals("4", filter(adjustmentType1, xeroMappingRules).getXeroAccountCode());
        assertEquals("5", filter(adjustmentType2, xeroMappingRules).getXeroAccountCode());
    }

    private XeroMappingRule filter(ItemType type, TaxRate tax, List<XeroMappingRule> xeroMappingRules) {
        for(XeroMappingRule xeroMappingRule : xeroMappingRules) {
            if(xeroMappingRule.getTaxId() != null
                    && xeroMappingRule.getTaxId().equals(tax.getId())
                    && xeroMappingRule.getTaxName().equals(tax.getName())
                    && xeroMappingRule.getTypeName() != null
                    && xeroMappingRule.getTypeName().equals(type.getName())
                    && xeroMappingRule.getTypeId().equals(type.getName())) {
                return xeroMappingRule;
            }
        }

        return null;
    }

    private XeroMappingRule filter(AdjustmentType type, List<XeroMappingRule> xeroMappingRules) {
        for(XeroMappingRule xeroMappingRule : xeroMappingRules) {
            if(xeroMappingRule.getTaxId() == null
                    && xeroMappingRule.getTaxName() == null
                    && xeroMappingRule.getTypeName() != null
                    && xeroMappingRule.getTypeName().equals(type.getName())
                    && xeroMappingRule.getTypeId().equals(type.getId())) {
                return xeroMappingRule;
            }
        }

        return null;
    }

    @Test
    public void getPreAuthentication() throws Exception{
        OAuthRequestToken token = mock(OAuthRequestToken.class);
        XeroInterface xeroInterface = mock(XeroInterface.class);
        when(xeroInterface.executeAuthRequest(anyString())).thenReturn(token);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        HashMap<String,String> info = new HashMap<>();
        info.put("tempToken", "A");
        info.put("tempTokenSecret", "B");
        when(token.getAll()).thenReturn(info);
        OAuthAuthorizeToken oauthAuthorizeToken = mock(OAuthAuthorizeToken.class);
        when(xeroInterface.createAuthorizeToken(token, restaurant1.getId())).thenReturn(oauthAuthorizeToken);

        OAuthAuthorizeToken preAuthentication = xeroService.getPreAuthentication(restaurant1);
        assertEquals(oauthAuthorizeToken, preAuthentication);

        KVData data = restaurantRepository.findOne(restaurant1.getId()).getIntegrations().get(ExternalIntegration.XERO);
        assertEquals("A", data.getToken());
        assertEquals("B", data.getSecret());
        assertNull(data.getData().get(KVDataConstants.XERO_REAL_TOKEN_AVAILABLE));
        assertEquals("true", data.getData().get(KVDataConstants.XERO_TEMP_TOKEN_AVAILABLE));
    }
    @Test
    public void getPreAuthenticationThrowsErrors() throws Exception{
        OAuthRequestToken token = mock(OAuthRequestToken.class);
        XeroInterface xeroInterface = mock(XeroInterface.class);
        when(xeroInterface.executeAuthRequest(anyString())).thenReturn(token);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        HashMap<String,String> info = new HashMap<>();
        when(token.getAll()).thenReturn(info);
        boolean errorThrown = false;
        try {
            xeroService.getPreAuthentication(restaurant1);
        } catch (IOException e) {
            errorThrown = true;
        }
        assertErrorsThrown(errorThrown);

        info.put("tempToken", "A");
        errorThrown = false;
        try {
            xeroService.getPreAuthentication(restaurant1);
        } catch (IOException e) {
            errorThrown = true;
        }
        assertErrorsThrown(errorThrown);

        info.clear();
        info.put("tempTokenSecret", "B");
        errorThrown = false;
        try {
            xeroService.getPreAuthentication(restaurant1);
        } catch (IOException e) {
            errorThrown = true;
        }
        assertErrorsThrown(errorThrown);
    }

    private void assertErrorsThrown(boolean errorThrown) {
        assertTrue(errorThrown);
        KVData data = restaurantRepository.findOne(restaurant1.getId()).getIntegrations().get(ExternalIntegration.XERO);
        assertNotNull(data.getData().get(KVDataConstants.XERO_ERROR));
        assertNull(data.getData().get(KVDataConstants.XERO_REAL_TOKEN_AVAILABLE));
        assertNull(data.getData().get(KVDataConstants.XERO_TEMP_TOKEN_AVAILABLE));
    }

    @Test
    public void verify() throws Exception {
        restaurant1.getIntegrations().get(ExternalIntegration.XERO).setToken("A");
        restaurant1.getIntegrations().get(ExternalIntegration.XERO).setSecret("B");
        restaurant1.setCountryId(country1.getId());
        tax1.setCountryId(country1.getId());
        taxRateRepository.save(tax1);
        restaurant1.getAdjustmentTypes().clear();
        restaurant1.getAdjustmentTypes().add(adjustmentType1.getId());
        restaurant1 = restaurantRepository.save(restaurant1);
        XeroInterface xeroInterface = mock(XeroInterface.class);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        OAuthAccessToken token = mock(OAuthAccessToken.class);
        when(xeroInterface.verify("abc", "A", "B")).thenReturn(token);
        when(token.isSuccess()).thenReturn(true);
        when(token.getToken()).thenReturn("a");
        when(token.getTokenSecret()).thenReturn("b");
        when(token.getSessionHandle()).thenReturn("c");
        when(token.getTokenTimestamp()).thenReturn("1000");

        xeroService.verify(restaurant1, "abc");

        Restaurant restaurant = restaurantRepository.findOne(restaurant1.getId());
        KVData data = restaurant.getIntegrations().get(ExternalIntegration.XERO);
        assertEquals("a", data.getToken());
        assertEquals("b", data.getSecret());
        assertEquals("c", data.getKey());
        assertEquals(1000, data.getTokenExpiration());
        assertEquals(data.getData().get("XERO_REVENUE_KEY_ITEM-Food-"+tax1.getId()), XeroMappingRule.NONE);
        assertEquals(data.getData().get("XERO_REVENUE_KEY_ITEM-Drink-"+tax1.getId()), XeroMappingRule.NONE);
        assertEquals(data.getData().get("XERO_REVENUE_KEY_ITEM-Other-"+tax1.getId()), XeroMappingRule.NONE);
        assertEquals(data.getData().get("XERO_REVENUE_KEY_ADJUSTMENT-"+adjustmentType1.getId()+"-"), XeroMappingRule.NONE);
        assertNull(data.getData().get(KVDataConstants.XERO_ERROR));
        assertNull(data.getData().get(KVDataConstants.XERO_TEMP_TOKEN_AVAILABLE));
        assertEquals("true", data.getData().get(KVDataConstants.XERO_REAL_TOKEN_AVAILABLE));
    }

    @Test
    public void verifyThrowsException() throws Exception {
        restaurant1.getIntegrations().get(ExternalIntegration.XERO).setToken("A");
        restaurant1.getIntegrations().get(ExternalIntegration.XERO).setSecret("B");
        Restaurant restaurant = restaurantRepository.save(restaurant1);
        XeroInterface xeroInterface = mock(XeroInterface.class);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        OAuthAccessToken token = mock(OAuthAccessToken.class);
        when(xeroInterface.verify("abc", "A", "B")).thenReturn(token);
        when(token.isSuccess()).thenReturn(false);

        boolean error = false;
        try {
            xeroService.verify(restaurant, "abc");
        } catch (IllegalStateException ex) {
            error = true;
        }

        assertTrue(error);
        assertNotNull(restaurantRepository.findOne(restaurant1.getId()).getIntegrations().get(ExternalIntegration.XERO).getData().get(KVDataConstants.XERO_ERROR));
    }

    @Test
    public void getAccounts() throws Exception {
        XeroInterface xeroInterface = mock(XeroInterface.class);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        when(xeroInterface.isTokenStale(anyString())).thenReturn(false);
        Account account = mock(Account.class);
        when(account.getAccountID()).thenReturn("1");
        List<Account> accounts = Lists.newArrayList(account);
        when(xeroInterface.getAccounts(anyString(),anyString())).thenReturn(accounts);

        assertEquals("1", xeroService.getAccounts(restaurant1).get(0).getAccountID());
    }

    @Test
    public void checkAndRefresh() throws Exception {
        XeroInterface xeroInterface = mock(XeroInterface.class);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        when(xeroInterface.isTokenStale(anyString())).thenReturn(true);
        KVData data = restaurant1.getIntegrations().get(ExternalIntegration.XERO);
        when(xeroInterface.refreshToken(data)).thenReturn(true);
        assertTrue(xeroService.checkAndRefreshToken(restaurant1, data));

        when(xeroInterface.refreshToken(data)).thenReturn(false);
        assertFalse(xeroService.checkAndRefreshToken(restaurant1, data));
    }

    @Test
    public void updateRules() {
        xeroService.createDefaultMappings(restaurant1.getIntegrations().get(ExternalIntegration.XERO).getData(),
                Lists.newArrayList(tax1), Lists.newArrayList(adjustmentType1));
        Restaurant restaurant = restaurantRepository.save(restaurant1);

        Map<String,String> data = restaurant.getIntegrations().get(ExternalIntegration.XERO).getData();
        for(String key : data.keySet()) {
            if(key.startsWith(KVDataConstants.XERO_KEY_PREFIX)) {
                assertEquals(XeroMappingRule.NONE, data.get(key));
            }
        }

        XeroMappingRule rule = new XeroMappingRule();
        rule.setRuleType(XeroMappingRule.ITEM_TYPE);
        rule.setTaxId(tax1.getId());
        rule.setTaxName(tax1.getName());
        rule.setTypeName(ItemType.FOOD.getName());
        rule.setTypeId(ItemType.FOOD.getName());
        rule.setOriginalKey(xeroService.getKVDataConstantsKey(ItemType.FOOD, tax1));
        rule.setXeroAccountCode("1001");

        xeroService.updateRules(Lists.newArrayList(rule), restaurant1);
        restaurant = restaurantRepository.save(restaurant1);
        data = restaurant.getIntegrations().get(ExternalIntegration.XERO).getData();
        assertEquals("1001", data.get(xeroService.getKVDataConstantsKey(ItemType.FOOD, tax1)));
    }
}