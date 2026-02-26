package uk.co.epicuri.waiter.ui.menueditor;

import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriService;

/**
 * Created by Home on 7/18/16.
 */
public class ServiceCourse {
    public final EpicuriService service;
    public EpicuriMenu.Course course;
    public ServiceCourse(EpicuriService service){
        this.service = service;
    }

    @Override
    public String toString() {
        if(null == course){
            return service.toString();
        } else {
            return service.toString() + " (" + course.toString() + ")";
        }
    }
}
