package uk.co.epicuri.serverapi.management.controllers;

import uk.co.epicuri.serverapi.management.webservice.WebService;

/**
 * Created by manish
 */
public abstract class BaseController {
    protected WebService webService = WebService.getWebService();
}
