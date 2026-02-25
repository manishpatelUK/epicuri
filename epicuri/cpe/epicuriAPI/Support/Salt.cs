using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace epicuri.API.Support
{
    public static class Salt
    {
        public static string GetSalt()
        {
            int passwordLength = 7;
            string allowedChars =@"!£$%^&*()-_[]{};:+=";
            char[] chars = new char[passwordLength];
            Random rd = new Random();

            for (int i = 0; i < passwordLength; i++)
            {
                chars[i] = allowedChars[rd.Next(0, allowedChars.Length)];
            }

            return new string(chars);
  
        }
    }
}