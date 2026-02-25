package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.CredentialsGrantResponse;
import uk.co.epicuri.serverapi.service.AsyncCommunicationsService;
import uk.co.epicuri.serverapi.service.external.StripeService;

@RestController
@RequestMapping(value = "/External/Stripe")
@CrossOrigin
public class StripeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StripeController.class);

    @Autowired
    private AsyncCommunicationsService asyncCommunicationsService;

    @Autowired
    private StripeService stripeService;

    /**
     * https://prod-api.epicuri.co.uk/External/Stripe?scope=read_write&code={AUTHORIZATION_CODE}
     If the authorization was denied by the user, they'll still be redirected back to your site, but the URL includes an error instead of the authorization code:

     https://prod-api.epicuri.co.uk/External/Stripe?error=access_denied&error_description=The%20user%20denied%20your%20request

     See https://stripe.com/docs/connect/standard-accounts#token-request
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public RedirectView confirm(@RequestParam(value = "code", required = false) String code,
                                @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "error_description", required = false) String error_description) {
        LOGGER.info("New Stripe accounts response: code: {}, error: {}, description: {}", code, error, error_description);
        if(StringUtils.isNotBlank(code)) {
            CredentialsGrantResponse response = stripeService.connectAccount(code);
            if(response == null) {
                sendErrorEmail("Could not connect Stripe account for code: " + code + ", see if you can do it manually: https://stripe.com/docs/connect/standard-accounts#token-request (STEP 4)");
                return new RedirectView("https://epicuri.co.uk/stripe/stripe_error.html");
            } else if(StringUtils.isNotBlank(response.getError())) {
                sendErrorEmail(response.getError() + ":" + response.getErrorDescription());
                return new RedirectView("https://epicuri.co.uk/stripe/stripe_error.html");
            } else {

                asyncCommunicationsService.sendInternalEmail("Stripe and Epicuri Accounts", "New Stripe account to connect",
                        "Response: " + response.toString()
                                + "\\nCheck Step 4 at https://stripe.com/docs/connect/standard-accounts#integrating-oauth"
                                + "\\nOR go to the dashboard and review recently attached accounts; you'll need to add the appropriate token to the restaurant: https://dashboard.stripe.com/applications/users/overview");
            }
        } else if(StringUtils.isNotBlank(error)) {
            sendErrorEmail(error + "\\nError Description: " + error_description);
            return new RedirectView("https://epicuri.co.uk/stripe/stripe_error.html");
        }
        return new RedirectView("https://epicuri.co.uk/stripe/index.html");
    }

    private void sendErrorEmail(String body) {
        asyncCommunicationsService.sendInternalEmail("Stripe and Epicuri Accounts", "New Stripe account to connect: FAILED",body);
    }
}
