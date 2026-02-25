using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using epicuri.Core.DatabaseModel;

namespace epicuri.CPE.Models
{
    public class BusinessIntelligence
    {
        public Restaurant restaurant;

        private epicuriContainer db = new epicuriContainer();

        public Dictionary<string, Dictionary<string, double>> Panel1;

        public Dictionary<string, List<KeyValuePair<Core.DatabaseModel.MenuItem, int>>> Panel2;

        public Dictionary<string, Dictionary<string, double>> Panel3;

        public Dictionary<string, double> Panel4;

        public bool week = false;
        public bool month = false;
        public bool threeMonths = false;
        public bool year = false;
        public bool forever = false;

        public bool showFilters = false;
        public bool fromWebView = false;

        public string filteredBy;
    }

    public class Panel2Data
    {
        public string Name;
        public int Count;
    }
}