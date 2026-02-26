package com.thinktouchsee.bookingwidget;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 18/02/2015
 */
public class Encryptor {

    private final String c = "qpwoeirutylaksjdhfgmznxbcv";
    private final char[] n = "0123456789".toCharArray();
    private final char[] s = "-_~!$&'(@)*+,;=:.".toCharArray();
    private final char[] l = c.toCharArray();
    private final char[] L = c.toUpperCase().toCharArray();

    private final List<Character> list = new ArrayList<Character>();


    public static void main(String[] args) {
        Encryptor encryptor = new Encryptor();
        System.out.println(encryptor.encrypt("manni.patel@gmail.com","igadhia@gmail.com"));
    }

    public Encryptor() {
        for(char c : s) {
            list.add(c);
        }
        for(char c : l) {
            list.add(c);
        }
        for(char c : L) {
            list.add(c);
        }
        for(char c : n) {
            list.add(c);
        }
    }

    public String encrypt(String... emails) {
        StringBuilder s = new StringBuilder();
        for(String email : emails) {
            for(char c : email.toCharArray()) {
                int pos = list.indexOf(c);
                s.append(String.format("%03d", pos));
            }
            s.append("/");
        }
        return s.toString();
    }

    public Collection<String> decrypt(String string) {
        Collection<String> emails = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < string.length(); i++) {
            if(string.charAt(i) == '/' || i == string.length()-1) {
                String email = builder.toString();
                if(StringUtils.isNotBlank(email)) {
                    emails.add(email);
                }
                emails.addAll(decrypt(string.substring(i+1)));
                break;
            }
            else if(i > 0 && (i+1)%3==0) {
                int n = Integer.parseInt(string.substring(i-2,i+1));
                builder.append(list.get(n));
            }
        }
        return emails;
    }
}
