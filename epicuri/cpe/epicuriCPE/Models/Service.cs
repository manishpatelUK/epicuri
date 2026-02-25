using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class Service
    {
        public int Id;
        public int MenuId;
        public int SelfServiceMenuId;
        public string MenuName;
        public IEnumerable<ScheduleItem> Schedule;
        public IEnumerable<RecurringScheduleItem> RecurringScheduleItems;
        public IEnumerable<Course> Courses;

        [Required]
        public String ServiceName;
        [Required]
        public String Notes;

        public Service() { }

        public Service(Core.DatabaseModel.Service service)
        {
            Id = service.Id;
            MenuName = service.DefaultMenu.MenuName;
            MenuId = service.DefaultMenu.Id;
            ServiceName = service.ServiceName;
                            
            Notes = service.Notes;
            Courses = from course in service.Courses
                        select new Models.Course(course);
            RecurringScheduleItems = from recurringScheduleItem in service.RecurringScheduleItems
                                    select new Models.RecurringScheduleItem(recurringScheduleItem);

            Schedule = from scheduleItem in service.ScheduleItems
                       select new Models.ScheduleItem(scheduleItem);
        }

        public Core.DatabaseModel.Service ToService()
        {
            return new Core.DatabaseModel.Service
            {
                Updated = DateTime.UtcNow,
                ServiceName =  ServiceName,
                MenuId = MenuId,
                Notes = Notes,
                MenuId1 = SelfServiceMenuId == 0 ? (new int?()) : SelfServiceMenuId,
                IsTakeaway = false,
                Active = true
            };
        }
    }
}