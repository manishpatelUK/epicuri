using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.ComponentModel.DataAnnotations;
namespace epicuri.CPE.Models
{
    public class AdjustmentType
    {
        public int Id;
        [Required]
        public String Name;
        [Required]
        public epicuri.Core.DatabaseModel.Enums.AdjustmentTypeType Type;

        public bool ChangeGiven;

        public AdjustmentType(){}

        public AdjustmentType(Core.DatabaseModel.AdjustmentType adjtype)
        {
            Id = adjtype.Id;
            Name = adjtype.Name;
            Type = (epicuri.Core.DatabaseModel.Enums.AdjustmentTypeType)adjtype.Type;
            ChangeGiven = ((Core.DatabaseModel.Enums.SupportsChangeType)adjtype.SupportsChange) == Core.DatabaseModel.Enums.SupportsChangeType.Change;
        }

    }



}