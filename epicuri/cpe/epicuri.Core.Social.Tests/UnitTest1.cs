using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Threading;

namespace epicuri.Core.Social.Tests
{
    [TestClass]
    public class Facebook
    {
        [TestMethod]
        public void CheckGetsId()
        {
            
       //     epicuri.Core.Social.Facebook f =
        //        new Social.Facebook("AAABZCc87rJMkBAOxqa841gDkzssxJnFYBwpjpZBrUn0ZBz4Y8miLZBo3h6d3fEME2R83qPfZA9VVv1dxu61rYQesPda4q4u3DKMWLqIMJzNQAAepzjW0b");
            

       //     Assert.AreEqual<String>("100004299972530", f.Id());
        }



        void f_DataReceived(object sender, EventArgs e)
        {
            
            
        }
    }

    [TestClass]
    public class Twitter
    {
        [TestMethod]
        public void CheckOk()
        {
            //epicuri.Core.Social.Twitter t = new Social.Twitter("973888544-l3MLRPSvrdURXi4bAd5qFcHjL6QYyWmdweQFF7iY","mrxp15ZgzMS5reahixjtVJ3fmqIkb8FdZi1MisUY");
            //Assert.AreEqual("973888544", t.GetUserId());
        }

        [TestMethod]
        public void CheckBad()
        {
            //epicuri.Core.Social.Twitter t = new Social.Twitter("97388844-l3MLRPSvrdURXi4bAd5qFcHjL6QYyWmdweQFF7iY", "mrxp15ZgzMS5reahixjtVJ3fmqIkb8FdZi1MisUY");
            //Assert.AreNotEqual("973888544", t.GetUserId());
        }
    }
}
