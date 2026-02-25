using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Text;
using System.Security.Cryptography;

namespace epicuri.API.Support
{
    public class String 
    {
        public static string RandomString(int passwordLength)
        {
            string allowedChars = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ0123456789";
            char[] chars = new char[passwordLength];
            Random rd = new Random();

            for (int i = 0; i < passwordLength; i++)
            {
                chars[i] = allowedChars[rd.Next(0, allowedChars.Length)];
            }

            return new string(chars);
        }

        public static byte[] GetBytes(string str)
        {
            return Encoding.GetEncoding(28591).GetBytes(str);
        }

        public static string GetString(byte[] bytes)
        {
            char[] chars = new char[bytes.Length / sizeof(char)];
            System.Buffer.BlockCopy(bytes, 0, chars, 0, bytes.Length);
            return new string(chars);
        }

        public static string SHA1(string str)
        {
            byte[] result;
            using (SHA1 shaM = new SHA1Managed())
            {
                result = shaM.ComputeHash(epicuri.API.Support.String.GetBytes(str));
                string delimitedHexHash = BitConverter.ToString(result);
                str = delimitedHexHash.Replace("-", "").ToLower();
            }
            return str;
        }
    }
}