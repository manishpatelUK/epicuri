# epicuri-online-ordering

> A simple web app for online ordering with Epicuri

## Build Setup

``` bash
# install dependencies
npm install

# serve with hot reload at localhost:8080
npm run dev

# build for production with minification
npm run build

# build for production and view the bundle analyzer report
npm run build --report

# run unit tests
npm run unit

# run all tests
npm test
```

For a detailed explanation on how things work, check out the [guide](http://vuejs-templates.github.io/webpack/) and [docs for vue-loader](http://vuejs.github.io/vue-loader).


CHANGING ROOT FOLDER CONFIG... change the folder in

`config/index.js`
And then:
`assetsPublicPath: '/online-ordering'`



# To Make a Client Package

If required, make sure the restaurant object in the database has a EPICURI_ONLINE_ORDERING integration & add a token (any random string). Also ensure it has a STRIPE integration with the appropriate token in the Token field.

Configure these in main.js:

- g_restaurantId (database ID)
- g_publicToken (integration Token on restaurant object) - this would be created manually in Management Portal
- g_defaultTelephoneNumber
- g_validLocations

Ensure the Stripe token in main.js is the correct one for dev vs prod:

```
// Vue.use(VueStripeCheckout, 'pk_test_0uLMayRAzxqdSIjWsmSfCDuo');
Vue.use(VueStripeCheckout, 'pk_live_5PFLgL2uoPK4pGF0aDaIB4q8');
```

Then in config/index.js change assetsSubDirectory to match the path to the app on the desired website. `/online-ordering-open/XXXXX` if on epicuri website where XXXXX is the staff facing ID.

Ensure `targetUrl` is set to `https://diner-api-prod.epicuri.co.uk/onlineorders`

Then run `npm run build`

The result in the `target/` folder is the build - that is deployable.
