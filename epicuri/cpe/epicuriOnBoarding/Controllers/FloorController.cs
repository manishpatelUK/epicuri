using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using epicuri.Core.DatabaseModel;
using epicuriOnBoarding.Filters;
namespace epicuriOnBoarding.Controllers
{
    [Authorize]
    [InitializeSimpleMembership]
    public class FloorController : Controller
    {
        private epicuriContainer db = new epicuriContainer();

        //
        // GET: /Floor/

        public ActionResult Index()
        {
            var floors = db.Floors.Include("Restaurant");
            return View(floors.ToList());
        }

        //
        // GET: /Floor/Details/5

        public ActionResult Details(int id = 0)
        {
            Floor floor = db.Floors.Single(f => f.Id == id);
            if (floor == null)
            {
                return HttpNotFound();
            }
            return View(floor);
        }

        //
        // GET: /Floor/Create

        public ActionResult Create(string id)
        {
            int rid = int.Parse(id);
 

            Restaurant r = db.Restaurants.Single(a => a.Id == rid);

            try
            {
                
                if (r == null)
                {
                    return HttpNotFound();
                }
            }
            catch { return HttpNotFound(); }

            ViewData["name"] = r.Name;

            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name");
            return View();
        }

        //
        // POST: /Floor/Create

        [HttpPost]
        public ActionResult Create(int id, Floor floor)
        {
            Restaurant r = db.Restaurants.Single(a => a.Id == id);
            if (r == null)
            {
                return HttpNotFound();
            }
            


            try
            {
                System.IO.Directory.CreateDirectory(Server.MapPath("~/static/"));
            }
            catch { }


            HttpPostedFileBase image = Request.Files[0];
            
            Guid g = Guid.NewGuid();

            System.IO.FileStream f = new System.IO.FileStream(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName), System.IO.FileMode.OpenOrCreate);
            image.InputStream.CopyTo(f);
            f.Close();

            try
            {
                System.Drawing.Image img = System.Drawing.Image.FromFile(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName));

                if (img.Height == 0)
                {
                    throw new Exception("file not image");
                }

                img.Dispose();


                Resource res = new Resource { CDNUrl = "http://cdn.epicuri.co.uk/" + g.ToString() + System.IO.Path.GetExtension(image.FileName), RestaurantId = id };
                db.AddToResources(res);

                db.SaveChanges();

                floor.RestaurantId = id;
                floor.Resource = res;

                db.SaveChanges();

                //EP-71
                Layout layout = new Layout
                {
                    Name = "TRANSIENT",
                    Temporary = true,
                    LastModified = DateTime.UtcNow,
                    FloorId = floor.Id
                };

                db.AddToLayouts(layout);
                db.SaveChanges();

                floor.LayoutId = layout.Id;
                db.SaveChanges();

                return RedirectToAction("Floors", "Restaurant", new { id = floor.RestaurantId });
            


            }
            catch (Exception e)
            {
                /*
                 * TODO file not image exception
                 */
                var a = e;
                System.IO.File.Delete(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName));
            }

            ViewData["id"] = id;
            ViewData["name"] = r.Name;
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", floor.RestaurantId);
            return View(floor);
        }

        
        public ActionResult Picture(int id = 0)
        {
            Floor floor = db.Floors.Single(f => f.Id == id);
            if (floor == null)
            {
                return HttpNotFound();
            }
       
            try
            {
                System.IO.Directory.CreateDirectory(Server.MapPath("~/static/"));
            }
            catch { }

            try
            {
                HttpPostedFileBase image = Request.Files[0];


                Guid g = Guid.NewGuid();

                System.IO.FileStream fs = new System.IO.FileStream(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName), System.IO.FileMode.OpenOrCreate);
                image.InputStream.CopyTo(fs);
                fs.Close();

                try
                {
                    System.Drawing.Image img = System.Drawing.Image.FromFile(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName));

                    if (img.Height == 0)
                    {
                        throw new Exception("file not image");
                    }

                    img.Dispose();



                    Resource res = new Resource { CDNUrl = "http://cdn.epicuri.co.uk/" + g.ToString() + System.IO.Path.GetExtension(image.FileName), RestaurantId = floor.RestaurantId };
                    db.AddToResources(res);
                    db.SaveChanges();

                    try
                    {
                        string oldpath = floor.Resource.CDNUrl;
                        int oldid = floor.Resource.Id;

                        db.Resources.DeleteObject(floor.Resource);
                        System.IO.File.Delete(Server.MapPath("~/static/") + oldpath.Substring("http://cdn.epicuri.co.uk/".Length));
                    }
                    catch { }
                    floor.Resource = res;
                    db.SaveChanges();
                    return RedirectToAction("Floors", "Restaurant", new { id = floor.RestaurantId });



                }
                catch(Exception e)
                {
                    var a = e;
                    /*
                     * TODO file not image exception
                     */

                    System.IO.File.Delete(Server.MapPath("~/static/") + g.ToString() + System.IO.Path.GetExtension(image.FileName));
                }
            }
            catch { }
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", floor.RestaurantId);
            return View(floor);
        }



        //
        // GET: /Floor/Edit/5

        public ActionResult Edit(int id = 0)
        {
            Floor floor = db.Floors.Single(f => f.Id == id);
            if (floor == null)
            {
                return HttpNotFound();
            }
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", floor.RestaurantId);
            return View(floor);
        }

        //
        // POST: /Floor/Edit/5

        private int GetResourceId(int FloorId)
        {
            using (db = new epicuriContainer())
            {
                return db.Floors.Single(f => f.Id == FloorId).ResourceId;
            }

        }
        [HttpPost]
        public ActionResult Edit(Floor floor)
        {
            if (ModelState.IsValid)
            {
                floor.ResourceId = GetResourceId(floor.Id);
                db = new epicuriContainer();
                db.Floors.Attach(floor);
                db.ObjectStateManager.ChangeObjectState(floor, EntityState.Modified);
                db.SaveChanges();
                return RedirectToAction("Floors", "Restaurant", new { id = floor.RestaurantId });
              
            }
            ViewBag.RestaurantId = new SelectList(db.Restaurants, "Id", "Name", floor.RestaurantId);
            return View(floor);
        }

        //
        // GET: /Floor/Delete/5

        public ActionResult Delete(int id = 0)
        {
            Floor floor = db.Floors.Single(f => f.Id == id);
            if (floor == null)
            {
                return HttpNotFound();
            }

            var floorQuery = db.Floors.Where(f => f.RestaurantId == floor.RestaurantId);

            if (floorQuery.ToList().Count == 1)
            {
                ViewBag.StatusMessage = "WARNING: Deleting the last floor will disable the Restaurant for the Waiter App";
            }
            ViewBag.RestaurantId = floor.RestaurantId;
            return View(floor);
        }

        //
        // POST: /Floor/Delete/5

        [HttpPost, ActionName("Delete")]
        public ActionResult DeleteConfirmed(int id)
        {
            Floor floor = db.Floors.Single(f => f.Id == id);

            //EP-110
            //Can't delete the floor because of the layout / TableLayout / Table / etc.

            /*
            floor.LayoutId = null;
            db.SaveChanges();

            var layouts = db.Layouts.Where(l => l.FloorId == id);
            List<Layout> layoutList = layouts.ToList();

            if (layoutList != null)
            {
                foreach (Layout l in layoutList)
                {
                    db.Layouts.DeleteObject(l);
                }
            }
            db.SaveChanges();
            db.Floors.DeleteObject(floor);
            */

            // So just mark the floor as deleted

            floor.Deleted = true;

            var floorQuery = db.Floors.Where(f => f.RestaurantId == floor.RestaurantId);
            var Restaurant = db.Restaurants.Single(r => r.Id == floor.RestaurantId);
           
            if (floorQuery.ToList().Count == 1)
            {
                Restaurant.EnabledForWaiter = false;
            }
           
            db.SaveChanges();
            return RedirectToAction("Floors", new { controller = "Restaurant", id = Restaurant.Id });
        }

        protected override void Dispose(bool disposing)
        {
            db.Dispose();
            base.Dispose(disposing);
        }
    }
}