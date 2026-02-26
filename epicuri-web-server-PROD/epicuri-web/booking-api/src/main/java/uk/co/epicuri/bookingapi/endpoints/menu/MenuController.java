package uk.co.epicuri.bookingapi.endpoints.menu;

import com.exxeleron.qjava.QConnection;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Menu;
import uk.co.epicuri.api.core.pojo.MenuItem;
import uk.co.epicuri.bookingapi.endpoints.auth.AbstractSecurityConnectingResource;
import uk.co.epicuri.bookingapi.pojo.MenuResponse;
import uk.co.epicuri.bookingapi.pojo.menustructure.Category;
import uk.co.epicuri.bookingapi.pojo.menustructure.Group;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * 26/05/2015
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("menu/{id}")
public class MenuController extends AbstractSecurityConnectingResource {
    private final EpicuriAPI api;

    public MenuController(EpicuriAPI api, QConnection securityConnection) {
        super(securityConnection);
        this.api = api;
    }

    @GET
    @Path("menuitems")
    public MenuResponse items(@NotNull @PathParam("id") String id,
                              @NotNull @HeaderParam("X-Auth-Token") String token) {
        if(StringUtils.isBlank(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        String realToken = getToken(token);
        List<Menu> menus = api.getMenus(realToken);
        List<MenuItem> items = api.getMenuItems(realToken);

        //id->item lookup
        Map<Integer,MenuItem> itemMap = new HashMap<>();
        for(MenuItem item : items) {
            itemMap.put(item.getId(),item);
        }

        // list of all menus
        final List<uk.co.epicuri.bookingapi.pojo.menustructure.Menu> simpleMenus = new ArrayList<>();
        for(Menu menu : menus) {
            if(!menu.isActive()) {
                continue;
            }

            uk.co.epicuri.bookingapi.pojo.menustructure.Menu simpleMenu = new uk.co.epicuri.bookingapi.pojo.menustructure.Menu();
            simpleMenu.setName(menu.getMenuName());
            simpleMenus.add(simpleMenu);

            for(Menu.MenuCategory category : menu.getMenuCategories()) {
                Category simpleCategory = new Category();
                simpleCategory.setName(category.getCategoryName());
                simpleCategory.setOrder(category.getOrder());
                simpleMenu.getCategories().add(simpleCategory);

                for(Menu.MenuCategory.MenuGroup menuGroup : category.getMenuGroups()) {
                    Group simpleGroup = new Group();
                    simpleGroup.setName(menuGroup.getGroupName());
                    simpleGroup.setOrder(menuGroup.getOrder());
                    simpleCategory.getGroups().add(simpleGroup);

                    for(int menuItemId : menuGroup.getMenuItemIds()) {
                        if(itemMap.containsKey(menuItemId)) {
                            MenuItem item = itemMap.get(menuItemId);
                            uk.co.epicuri.bookingapi.pojo.menustructure.MenuItem simpleItem = new uk.co.epicuri.bookingapi.pojo.menustructure.MenuItem();
                            simpleItem.setName(item.getName());
                            simpleItem.setDescription(item.getDescription());
                            simpleItem.setId(item.getId());
                            simpleItem.setPrice(item.getPrice());
                            simpleItem.setUnavailable(item.isUnavailable());

                            simpleGroup.getItems().add(simpleItem);
                        }
                    }
                }
            }
        }

        //sort stuff
        for(uk.co.epicuri.bookingapi.pojo.menustructure.Menu simpleMenu : simpleMenus) {
            Collections.sort(simpleMenu.getCategories());
            for(Category category : simpleMenu.getCategories()) {
                Collections.sort(category.getGroups());
            }
        }

        return new MenuResponse(){
            {
                setMenus(simpleMenus);
            }
        };
    }
}
