package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriMenu;

/**
 * Created by Home on 7/18/16.
 */
public interface SaveCategoryListener {
    void createCategory(String menuId, CharSequence name, String[] defaultCourses);
    void saveCategory(EpicuriMenu.Category category, String menuId, CharSequence name, String[] defaultCourses);
    void deleteCategory(String categoryId, String menuId);
}
