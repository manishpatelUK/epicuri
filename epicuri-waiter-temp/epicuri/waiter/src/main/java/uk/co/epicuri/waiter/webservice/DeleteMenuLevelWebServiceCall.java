package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.ui.menueditor.MenuLevelFragment;

public class DeleteMenuLevelWebServiceCall implements WebServiceCall {
	final String path;
	final String menuId;
	final Uri[] uris;
	
	public DeleteMenuLevelWebServiceCall(String id, MenuLevelFragment.Level level, String menuId){
		switch(level){
		case MENU:
			path = String.format("/Menu/%s", id);
			break;
		case CATEGORY:
			path = String.format("/MenuCategory/%s", id);
			break;
		case GROUP:
			path = String.format("/MenuGroup/%s", id);
			break;
		case ITEM:
			path = String.format("/MenuItem/%s", id);
			break;
		default:
			throw new IllegalArgumentException("need cat, group or menu");
		}

		if(menuId != null && !menuId.equals("0") && !menuId.equals("-1")
				&& level != MenuLevelFragment.Level.MENU){
			uris = new Uri[]{Uri.withAppendedPath(EpicuriContent.MENU_URI, menuId)};
		} else {
			uris = new Uri[]{};
		}
		this.menuId = menuId;
	}

	@Override
	public String getMethod() {
		return "DELETE";
	}

	@Override
	public boolean requiresToken() {
		return true;
	}
	
	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getBody() {
		return null;
	}
	@Override
	public Uri[] getUrisToRefresh() {
		return uris;
	}
}
