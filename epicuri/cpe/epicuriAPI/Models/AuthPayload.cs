
using System.ComponentModel.DataAnnotations;

namespace epicuri.API.Models
{
    public class AuthPayload
    {
        [Required]
        public string Email { get; set; }
        [Required]
        public string Password { get; set; }
    }
}