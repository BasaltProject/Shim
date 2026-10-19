/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.Arrays;

public class StringStorageSearchModern {
  static int checks;
  static long digest;
  static void expect(boolean value) {
    if (!value) throw new AssertionError("storage/search check " + checks);
    ++checks;
  }
  static int find(String text, String target, int start) {
    start = Math.max(0, start);
    if (target.isEmpty()) return Math.min(start, text.length());
    for (int i = start; i <= text.length() - target.length(); ++i) {
      int j = 0;
      while (j < target.length() && text.charAt(i+j) == target.charAt(j)) ++j;
      if (j == target.length()) return i;
    }
    return -1;
  }
  static String replace(String text, String target, String value) {
    StringBuilder result = new StringBuilder();
    if (target.isEmpty()) {
      for (int i=0;i<text.length();++i) result.append(value).append(text.charAt(i));
      return result.append(value).toString();
    }
    int i=0;
    while (i<text.length()) {
      if (find(text,target,i)==i) {result.append(value);i+=target.length();}
      else {result.append(text.charAt(i));++i;}
    }
    return result.toString();
  }
  static void variants(String input) {
    String[] texts = {input, ("\u20ac"+input+"\u20ac").substring(1,1+input.length()), new String(input.toCharArray()), ("x"+input+"y").substring(1,1+input.length())};
    for (String text : texts) {
      int hash=0;
      for(int i=0;i<text.length();++i) hash=hash*31+text.charAt(i);
      expect(hash==text.hashCode());
      expect(hash==text.hashCode());
      for(int cp : new int[] {Integer.MIN_VALUE,-1,0,'a','z',0x7f,0x80,0xe9,0x20ac,0xd800,0xd83d,0xde00,0xdc00,0xffff,0x10000,0x1f600,0x10ffff,0x110000,Integer.MAX_VALUE}) {
        for(int start : new int[] {Integer.MIN_VALUE,-1,0,1,text.length()-1,text.length(),Integer.MAX_VALUE}) {
          int expected=-1;
          if(cp>=0 && cp<=0x10ffff) {
            for(int i=Math.max(0,start);i<text.length();++i) {
              if(cp<0x10000) {if(text.charAt(i)==cp) {expected=i;break;}}
              else if(i+1<text.length() && text.charAt(i)==Character.highSurrogate(cp) && text.charAt(i+1)==Character.lowSurrogate(cp)) {expected=i;break;}
            }
          }
          expect(text.indexOf(cp,start)==expected);
        }
      }
      char[] chars = text.toCharArray();
      for (int from=0;from<=text.length();++from) for(int to=from;to<=text.length();++to) {
        char[] mutable = Arrays.copyOfRange(chars,from,to);
        String copy = new String(mutable);
        String ranged = new String(chars,from,to-from);
        Arrays.fill(mutable,'!');
        expect(copy.equals(text.substring(from,to)));
        expect(ranged.equals(copy));
        char[] out=new char[to-from+4];Arrays.fill(out,'?');
        text.getChars(from,to,out,2);
        expect(Arrays.equals(Arrays.copyOfRange(out,2,out.length-2),copy.toCharArray()));
        expect(out[0]=='?' && out[1]=='?' && out[out.length-1]=='?' && out[out.length-2]=='?');
      }
      for(String raw : new String[] {"", "a", "aba", "zz", "\0", "\u00e9", "\u20ac", "\ud83d", "\ude00", "\ud83d\ude00", "longer than this input entirely"}) {
        for(String target : new String[] {raw, ("\u20ac"+raw+"\u20ac").substring(1,raw.length()+1)}) {
          int end=Math.min(text.length(),target.length()),comparison=text.length()-target.length();
          for(int i=0;i<end;++i) if(text.charAt(i)!=target.charAt(i)) {comparison=text.charAt(i)-target.charAt(i);break;}
          expect(text.compareTo(target)==comparison);
          expect(text.equals(target)==(comparison==0));
          for (int start : new int[] {Integer.MIN_VALUE,-1,0,1,2,text.length()-1,text.length(),text.length()+1,Integer.MAX_VALUE}) {
            int actual=text.indexOf(target,start);expect(actual==find(text,target,start));digest=digest*31+actual;
          }
          for(String value : new String[] {"", "x", "\u20ac", "\ud83d\ude00", "$\\"}) {
            String expected=replace(text,target,value);
            String actual=text.replace(target,value);expect(expected.equals(actual));
            expect(expected.equals(text.replace(new StringBuilder(target),new StringBuffer(value))));
            digest=digest*31+actual.hashCode();
          }
        }
      }
    }
  }
  public static void main(String[] args) {
    for(String text : new String[] {"", "a", "aaaa", "abababa", "plain ASCII", "x\0y\0", "\u00e9a\u20acaba", "a\ud83d\ude00b\ud800c\udc00"}) variants(text);
    for(int i=0;i<12000;++i) {
      char[] chars=new char[1024];Arrays.fill(chars,(char)(i%2==0?'a':0x20ac));
      String text=new String(chars,3,1000);chars[3]='!';
      expect(text.length()==1000 && text.charAt(0)==(i%2==0?'a':0x20ac) && text.charAt(999)==text.charAt(0));
      if(i%256==0) System.gc();
    }
    expect("\0".hashCode()==0 && "\0".hashCode()==0);
    expect("Aa".hashCode()=="BB".hashCode());
    try {"a".compareTo(null);throw new AssertionError();} catch(NullPointerException expected){++checks;}
    try {"a".indexOf((String)null);throw new AssertionError();} catch(NullPointerException expected){++checks;}
    try {"a".replace("z",(CharSequence)null);throw new AssertionError();} catch(NullPointerException expected){++checks;}
    try {"a".replace((CharSequence)null,"z");throw new AssertionError();} catch(NullPointerException expected){++checks;}
    System.out.println("storage/search checks="+checks+" digest="+digest);
  }
}
