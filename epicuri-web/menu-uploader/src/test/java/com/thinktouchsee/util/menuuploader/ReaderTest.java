package com.thinktouchsee.util.menuuploader;

import org.junit.Test;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Authentication;
import uk.co.epicuri.api.core.pojo.ItemType;
import uk.co.epicuri.api.core.pojo.MenuItem;

import java.util.List;

import static org.junit.Assert.*;

public class ReaderTest {

    @Test
    public void testGetMenuItemList() throws Exception {
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.PROD);
        Authentication authentication = api.login("8","epicuriadmin","keshavroshan");
        List<MenuItem> items = api.getMenuItems(authentication.getAuthKey());
        for (MenuItem item : items) {
            if(item.getMenuItemTypeId() == ItemType.OTHER.getId()) {
                System.out.println(item.getName());
            }
        }
    }
}