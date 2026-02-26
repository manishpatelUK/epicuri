package uk.co.epicuri.serverapi.service.external;

import com.xero.api.*;
import com.xero.model.Account;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.KVDataConstants;
import uk.co.epicuri.serverapi.common.pojo.external.xero.XeroAccountView;
import uk.co.epicuri.serverapi.common.pojo.external.xero.XeroMappingRule;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentTypeType;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class XeroService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XeroService.class);

    @Autowired
    private MasterDataService masterDataService;

    private XeroInterface xeroInterface = new XeroInterface();
    private static final String SEPARATOR = "-";

    public OAuthAuthorizeToken getPreAuthentication(Restaurant restaurant) throws IOException {
        OAuthRequestToken requestToken = xeroInterface.executeAuthRequest(restaurant.getId());

        KVData data = restaurant.getIntegrations().get(ExternalIntegration.XERO);

        String tempToken = requestToken.getAll().get("tempToken");
        String tempTokenSecret = requestToken.getAll().get("tempTokenSecret");
        if(StringUtils.isBlank(tempToken) || StringUtils.isBlank(tempTokenSecret)) {
            data.getData().put(KVDataConstants.XERO_ERROR, "Token could not be generated (Xero error)");
            data.getData().remove(KVDataConstants.XERO_REAL_TOKEN_AVAILABLE);
            data.getData().remove(KVDataConstants.XERO_TEMP_TOKEN_AVAILABLE);
            masterDataService.upsert(restaurant);
            throw new IOException("Temp token or secret is not readable");
        }
        data.setToken(tempToken);
        data.setSecret(tempTokenSecret);
        data.getData().put(KVDataConstants.XERO_TEMP_TOKEN_AVAILABLE, "true");
        data.getData().remove(KVDataConstants.XERO_REAL_TOKEN_AVAILABLE);
        masterDataService.upsert(restaurant);

        return xeroInterface.createAuthorizeToken(requestToken, restaurant.getId());
    }

    public void verify(Restaurant restaurant, String verifier) throws IllegalStateException, IOException {
        KVData data = restaurant.getIntegrations().get(ExternalIntegration.XERO);
        OAuthAccessToken accessToken = xeroInterface.verify(verifier, data.getToken(), data.getSecret());

        if(!accessToken.isSuccess()) {
            data.getData().put(KVDataConstants.XERO_ERROR, "Access token verification was not successful (cancelled or rejected)");
            masterDataService.upsert(restaurant);
            throw new IllegalStateException("Access token was not verified: " + accessToken.getProblem() + ": " + accessToken.getAdvice());
        }

        String token = accessToken.getToken();
        String tokenSecret = accessToken.getTokenSecret();
        String sessionHandle = accessToken.getSessionHandle();
        String tokenTimestamp = accessToken.getTokenTimestamp();

        data.setToken(token);
        data.setSecret(tokenSecret);
        data.setKey(sessionHandle);
        data.setTokenExpiration(Long.valueOf(tokenTimestamp));
        data.getData().remove(KVDataConstants.XERO_ERROR);
        data.getData().remove(KVDataConstants.XERO_TEMP_TOKEN_AVAILABLE);
        data.getData().put(KVDataConstants.XERO_REAL_TOKEN_AVAILABLE, "true");
        if(!isDefaultXeroMappingsCreated(data.getData())) {
            createDefaultMappings(data.getData(), masterDataService.getTaxRatesByCountry(restaurant.getCountryId()), masterDataService.getAdjustmentTypes(restaurant.getAdjustmentTypes()));
        }
        masterDataService.upsert(restaurant);
    }

    void createDefaultMappings(Map<String, String> data, List<TaxRate> taxRates, List<AdjustmentType> adjustmentTypes) {
        for(ItemType itemType : ItemType.values()) {
            for(TaxRate taxRate : taxRates) {
                data.put(getKVDataConstantsKey(itemType, taxRate), XeroMappingRule.NONE);
            }
        }
        for(AdjustmentType adjustmentType : adjustmentTypes) {
            data.put(getKVDataConstantsKey(adjustmentType, null), XeroMappingRule.NONE);
        }
    }

    private boolean isDefaultXeroMappingsCreated(Map<String, String> data) {
        return data.keySet().stream().anyMatch(k -> k != null
                && (k.startsWith(KVDataConstants.XERO_ITEM_TYPE_PREFIX) || k.startsWith(KVDataConstants.XERO_ADJUSTMENT_TYPE_PREFIX)));
    }

    public List<Account> getAccounts(Restaurant restaurant) throws IOException {
        KVData data = restaurant.getIntegrations().get(ExternalIntegration.XERO);
        if(!checkAndRefreshToken(restaurant, data)) {
            return new ArrayList<>();
        }
        return xeroInterface.getAccounts(data.getToken(), data.getSecret());
    }

    public boolean isTokenStale(Restaurant restaurant) {
        KVData data = restaurant.getIntegrations().get(ExternalIntegration.XERO);
        return xeroInterface.isTokenStale(String.valueOf(data.getTokenExpiration()));
    }

    public boolean isPublicApp() {
        return xeroInterface.isPublicApp();
    }

    public boolean checkAndRefreshToken(Restaurant restaurant) throws IOException {
        KVData data = restaurant.getIntegrations().get(ExternalIntegration.XERO);
        return checkAndRefreshToken(restaurant, data);
    }

    public boolean checkAndRefreshToken(Restaurant restaurant, KVData data) throws IOException {
        if(xeroInterface.isTokenStale(String.valueOf(data.getTokenExpiration()))) {
            boolean success = xeroInterface.refreshToken(data);
            if(success) {
                masterDataService.upsert(restaurant);
            } else {
                LOGGER.warn("Xero token refresh for restaurant {} was not successful", restaurant.getId());
                return false;
            }
        }

        return true;
    }

    public List<XeroMappingRule> convertToRules(Map<String,String> data, List<AdjustmentType> adjustmentTypes, List<TaxRate> taxRates) {
        Map<String, TaxRate> taxMap = taxRates.stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));
        Map<String, AdjustmentType> adjustmentTypeMap = adjustmentTypes.stream().collect(Collectors.toMap(AdjustmentType::getId, Function.identity()));
        List<XeroMappingRule> xeroMappingRules = new ArrayList<>();
        for(Map.Entry<String,String> entry : data.entrySet()) {
            String key = entry.getKey();
            if(key == null) {
                continue;
            }

            XeroMappingRule xeroMappingRule = new XeroMappingRule();
            xeroMappingRule.setOriginalKey(key);
            if(key.startsWith(KVDataConstants.XERO_ITEM_TYPE_PREFIX)) {
                xeroMappingRule.setRuleType(XeroMappingRule.ITEM_TYPE);
                String itemName = getItemName(key);
                xeroMappingRule.setTypeName(itemName);
                xeroMappingRule.setTypeId(itemName);
                String taxId = getItemTypeTaxId(key);
                xeroMappingRule.setTaxId(taxMap.get(taxId).getId());
                xeroMappingRule.setTaxName(taxMap.get(taxId).getName());
                xeroMappingRule.setXeroAccountCode(entry.getValue());
                xeroMappingRule.setId(String.valueOf(xeroMappingRules.size()));
                xeroMappingRules.add(xeroMappingRule);
            } else if(key.startsWith(KVDataConstants.XERO_ADJUSTMENT_TYPE_PREFIX)) {
                String itemName = getItemName(key);
                AdjustmentType adjustmentType = adjustmentTypeMap.get(itemName);
                if(adjustmentType.getType() == AdjustmentTypeType.PAYMENT) {
                    xeroMappingRule.setRuleType(XeroMappingRule.PAYMENT_TYPE);
                } else if(adjustmentType.getType() == AdjustmentTypeType.DISCOUNT) {
                    xeroMappingRule.setRuleType(XeroMappingRule.DISCOUNT_TYPE);
                } else if(adjustmentType.getType() == AdjustmentTypeType.GRATUITY) {
                    xeroMappingRule.setRuleType(XeroMappingRule.GRATUITY_TYPE);
                }
                xeroMappingRule.setTypeName(adjustmentType.getName());
                xeroMappingRule.setTypeId(itemName);
                xeroMappingRule.setXeroAccountCode(entry.getValue());
                xeroMappingRule.setId(String.valueOf(xeroMappingRules.size()));
                xeroMappingRules.add(xeroMappingRule);
            }
        }

        return xeroMappingRules;
    }

    public void updateRules(List<XeroMappingRule> rules, Restaurant restaurant) {
        Map<String, String> data = restaurant.getIntegrations().get(ExternalIntegration.XERO).getData();
        for (XeroMappingRule rule : rules) {
            String key = rule.getOriginalKey();
            if(data.containsKey(key)) {
                data.put(key, rule.getXeroAccountCode());
            }
        }
        masterDataService.upsert(restaurant);
    }

    public String getKVDataConstantsKey(ItemType type, TaxRate taxRate) {
        return KVDataConstants.XERO_ITEM_TYPE_PREFIX + type.getName() + SEPARATOR + taxRate.getId();
    }

    public String getKVDataConstantsKey(AdjustmentType adjustmentType, TaxRate taxRate) {
        return KVDataConstants.XERO_ADJUSTMENT_TYPE_PREFIX + adjustmentType.getId() + SEPARATOR + (taxRate == null ? "" : taxRate.getId());
    }

    public String getItemName(String key) {
        return key.split(SEPARATOR)[1];
    }

    public String getItemTypeTaxId(String key) {
        return key.split(SEPARATOR)[2];
    }
}
