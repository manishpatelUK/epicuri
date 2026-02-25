using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace epicuri.CPE.Models
{
    public class QuickSessionInfo
    {
        IEnumerable<int> Tables { get; set; }
        int PartySize { get; set; }
    }
}
