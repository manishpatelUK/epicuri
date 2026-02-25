using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Newtonsoft.Json;
using System.Text.RegularExpressions;
namespace epicuri.Core.Utils
{

    public class Location
    {
        public string lat { get; set; }
        public string lng { get; set; }
    }

    public class Geometry
    {
        public Location location { get; set; }
    }

    public class Result
    {
        public Geometry geometry { get; set; }
    }

    public class RootObject
    {
        public string status { get; set; }
        public Result[] results { get; set; }
 
    }


    public static class GeoCoding
    {
        public const double EarthRadiusInMiles = 3956.0;
        public const double EarthRadiusInKilometers = 6367.0;

        public static double ToRadian(double val) { return val * (Math.PI / 180); }
        public static double DiffRadian(double val1, double val2) { return ToRadian(val2) - ToRadian(val1); }

        public static double CalcDistance(double lat1, double lng1, double lat2, double lng2)
        {
            return CalcDistance(lat1, lng1, lat2, lng2, GeoCodeCalcMeasurement.Miles);
        }

        public static double CalcDistance(double lat1, double lng1, double lat2, double lng2, GeoCodeCalcMeasurement m)
        {
            double radius = EarthRadiusInMiles;

            if (m == GeoCodeCalcMeasurement.Kilometers) { radius = EarthRadiusInKilometers; }
            return radius * 2 * Math.Asin(Math.Min(1, Math.Sqrt((Math.Pow(Math.Sin((DiffRadian(lat1, lat2)) / 2.0), 2.0) + Math.Cos(ToRadian(lat1)) * Math.Cos(ToRadian(lat2)) * Math.Pow(Math.Sin((DiffRadian(lng1, lng2)) / 2.0), 2.0)))));
        }

        public enum GeoCodeCalcMeasurement : int
        {
            Miles = 0,
            Kilometers = 1
        }


        public static Tuple<double, double> LatLongFromPostCode(string PostCode)
        {
            double lat = 0;
            double lon = 0;
            var completion = new ManualResetEvent(false);

            string reg = @"(?i)(GIR 0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY])))) [0-9][A-Z-[CIKMOV]]{2})";

            if (Regex.IsMatch(PostCode, reg))
            {
                Redslide.HttpLib.Request.Get("http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=" + PostCode, new { }, (result) =>
                {
                    var obj = JsonConvert.DeserializeObject<RootObject>(result);

                    if (obj == null || obj.results == null || obj.results.Count() == 0)
                    {
                        lat = 0;
                        lon = 0;
                    }
                    else
                    {
                        lat = double.Parse(obj.results[0].geometry.location.lat);
                        lon = double.Parse(obj.results[0].geometry.location.lng);
                    }                   

                    completion.Set();
                },
                ex =>
                {

                    completion.Set();

                });

                completion.WaitOne();
                return new Tuple<double, double>(lat, lon);
            }
            else
            {
                return new Tuple<double, double>(0,0);
            }
        }
    }
}
