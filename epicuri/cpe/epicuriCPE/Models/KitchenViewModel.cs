using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
//using System.ComponentModel.DataAnnotations.Schema;
//using System.Data.Objects;
using epicuri.Core.DatabaseModel;
using epicuri.Core;

namespace epicuri.CPE.Models
{
    public class KitchenViewModel
    {
        public String PrinterName { get; private set; }
        public int Printer { get; private set; }
        public KitchenViewModel(int PrinterId, String printerName)
        {
            this.PrinterName = printerName;
            this.Printer = PrinterId;

        }
    }
}