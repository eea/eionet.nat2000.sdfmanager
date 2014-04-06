/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author charbda
 */
public class ConversionTools {
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConversionTools.class .getName());

   /**
    * This method checks if the param is null.
    * @param val
    * @return
    */
   public static boolean isNull(String val) {
       return val == null || val.equals("");
   }
   /**
    * This method compares String objects.
    * @param val1
    * @param val2
    * @return
    */
   public static boolean compFields(String val1, String val2) {
       String strVal1 = val1 == null ? "" : val1;
       String strVal2 = val2 == null ? "" : val2;
       return strVal1.equals(strVal2);
   }
   /**
    * This method compares Double objects.
    * @param val1
    * @param val2
    * @return
    */
   public static boolean compFields(Double val1, Double val2) {
       Double strVal1 = val1 == null ? 0.0 : val1;
       Double strVal2 = val2 == null ? 0.0 : val2;
       return strVal1.equals(strVal2);
   }
   /**
    * This method compares Character objects.
    * @param val1
    * @param val2
    * @return
    */
   public static boolean compFields(Character val1, Character val2) {
       Character chVal1 = val1 == null ? '@' : val1; //impossible value
       return chVal1.equals(val2);
   }
   /**
    * This method compares java.util.Date objects.
    * @param val1
    * @param val2
    * @return
    */
   public static boolean compFields(Date val1, Date val2) {
       Date dateVal1 = val1 == null ? new Date() : val1; //impossible value
       return dateVal1.equals(val2);
   }

   /**
    * This method checks if the param is an Integer.
    * @param s
    * @return
    */
   public static boolean checkInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

   /**
    * This method checks if the param is an Double.
    * @param s
    * @return
    */
    public static boolean checkDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method converts Date into String.
     * @param date
     * @return
     */
    public static String convertDateToString(Date date) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
        return fmt.format(date);
    }

    /**
     * This method converts String into Date.
     * @param str
     * @return
     */
    public static Date convertStringToDate(String str)  {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
        try {
            return fmt.parse(str);
        } catch (ParseException ex) {
            ConversionTools.log.error("Couldn't parse date: " + str);
            return null;
        }
    }

    /**
     * This method checks if the date is a valid date.
     * @param s
     * @return
     */
    public static boolean checkDateStringFormat(String s) {
        if (s.length() != 7) return false;
        String month = s.substring(5, 7);
        String year = s.substring(0, 4);
        int imonth = converToInt(month);
        int iyear = converToInt(year);
        if (imonth == -1) {
            return false;
        }
        if (iyear == -1) {
            return false;
        }
        if (imonth > 12) {
            return false;
        }
        if (iyear < 1800 || iyear > 2100) {
            return false;
        }
        return true;
    }


    /**
     * This method converts String into Date.
     * @param sdate
     * @return
     */
    public static Date convertToDate(String sdate) {
         if (sdate == null) {
             return null;
         }
         if (!checkDateStringFormat(sdate)) {
             return null;
         }

         String month = sdate.substring(5, 7);
         String year = sdate.substring(0, 4);
         int imonth = converToInt(month);
         int iyear = converToInt(year);
         Date d = new Date();
         Calendar cal = GregorianCalendar.getInstance();
         cal.set(iyear, imonth - 1, 1);
         d = cal.getTime();
         return d;
     }


    /**
     * This method converts String into int.
     * @param num
     * @return
     */
    public static int converToInt(String num) {
         try {
             return Integer.parseInt(num);
         } catch (Exception e) {
            // e.printStackTrace();
             return -1;
         }
     }

    /**
     * This method converts double into String.
     * @param d
     * @return
     */
    public static String doubleToString(Double d) {
        try {
            String s = Double.toString(d);
            if (s != null) {
                return s;
            } else {
                return "0.0";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * This method converts String into Double.
     * @param s
     * @return
     */
    public static Double stringToDouble(String s) {
        try {
            Double d = Double.parseDouble(s);
            if (d != null) {
                return d;
            } else {
                return 0.0;
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * This method converts String into Double.
     * @param s
     * @return
     */
    public static Double stringToDoubleN(String s) {
        try {
            Double d = Double.parseDouble(s);
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This method converts int into String.
     * @param i
     * @return
     */
    public static String intToString(Integer i) {
        try {
            //return Double.toString(d);
            String s = Integer.toString(i);
            if (s != null) {
                return s;
            } else {
                return "0";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * This method converts String into int.
     * @param s
     * @return
     */
    public static int stringToInt(String s) {
        try {
            int d = Integer.parseInt(s);
            if (d >= 0) {
                return d;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * This method converts char into String.
     * @param c
     * @return
     */
    public static String charToString(Character c) {
        try {
            String s = Character.toString(c);
            if (s != null) {
                return s;
            } else {
                return null;
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * This method converts Stirng into char.
     * @param s
     * @return
     */
    public static Character stringToChar(String s) {
        try {
            Character c = s.charAt(0);
            if (c != null) {
                return c;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This method converts boolean into Short.
     * @param b
     * @return
     */
    public static Short boolToSmall(boolean b) {
        if (b) {
            return 1;

        } else {
            return 0;
        }
    }

    /**
     * This method converts smallInt into Boolean.
     * @param s
     * @return
     */
    public static boolean smallToBool(Short s) {
        if (s != null && s > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method converts String into String.
     * @param s
     * @return
     */
    public static String stringToString(String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    /**
     * This method converts String into boolean.
     * @param s
     * @return
     */
    public static boolean stringToBoolean(String s) {
        if (s == null) {
            return false;
        } else if (s.equals("true")) {

            return true;
        } else {
            return false;
        }
    }

    /**
     * This method converts String into short.
     * @param s
     * @return
     */
    public static Short stringToShort(String s) {
        if (s == null) {
            return 0;
        } else if (s.equals("true")) {
            return 1;
        } else {
            return 0;
        }
    }
}
