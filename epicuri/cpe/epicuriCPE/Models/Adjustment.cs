using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;

namespace epicuri.CPE.Models
{
    public class Adjustment
    {
        public int Id;
        [Required]
        public int SessionId { get; set; }
        // Required for all but Mews
        public int TypeId { get; set; }
        [Required]
        public int NumericalTypeId;
        [Required]
        public decimal Value;
        public string Reference { get; set; }
        public double Created { get; set; }
        public AdjustmentType Type { get; set; }

        public string Name { get; set; }
        public string RoomNo { get; set; }


        public AdjustmentType AdjustmentType()
        {
                epicuri.Core.DatabaseModel.epicuriContainer db = new Core.DatabaseModel.epicuriContainer();
                return new AdjustmentType(db.AdjustmentTypes.Single(adjt => adjt.Id == this.TypeId));
        }

        public Staff Staff { get; set; }
        public Adjustment() { }
        public DateTime Date { get; set; }

        public String FirstName { get; set; }
        public String LastName { get; set; }
        public String ChargeId { get; set; }
        public Boolean Mews { get; private set; }
        public Adjustment(Core.DatabaseModel.Adjustment adj)
        {
            Id = adj.Id;
            SessionId = adj.Session.Id;
            TypeId = adj.AdjustmentTypeId;
            NumericalTypeId = adj.NumericalType;
            Value = (decimal) adj.Value;
            Reference = adj.Reference;
            Created = epicuri.Core.Utils.Time.DateTimeToUnixTimestamp(adj.Created);
            Type = new AdjustmentType(adj.AdjustmentType);
            Mews = false;
            if (adj.Staff != null)
            {
                Staff = new Staff(adj.Staff);
            }
            Date = adj.Created;

            if (adj.GetType() == typeof(Core.DatabaseModel.MewsAdjustment))
            {
                Mews = true;
                this.Name = "";
                if (!String.IsNullOrEmpty(((Core.DatabaseModel.MewsAdjustment)adj).FirstName))
                    this.Name += ((Core.DatabaseModel.MewsAdjustment)adj).FirstName;

                if (!String.IsNullOrEmpty(((Core.DatabaseModel.MewsAdjustment)adj).LastName))
                {
                    if (this.Name.Length != 0)
                        this.Name += " ";

                    this.Name += ((Core.DatabaseModel.MewsAdjustment)adj).LastName;
                }

                if (!String.IsNullOrEmpty(((Core.DatabaseModel.MewsAdjustment)adj).RoomNo))
                    this.RoomNo = ((Core.DatabaseModel.MewsAdjustment)adj).RoomNo;
                else
                {
                    this.RoomNo = "";
                }
                ChargeId = ((Core.DatabaseModel.MewsAdjustment)adj).ChargeId;

                FirstName = ((Core.DatabaseModel.MewsAdjustment)adj).FirstName;
                LastName = ((Core.DatabaseModel.MewsAdjustment)adj).LastName;
            }

            
        }
    }

}