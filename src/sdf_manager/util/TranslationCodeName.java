/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager.util;


public class TranslationCodeName {

    /**
     *
     * @param groupSpeciesCode
     * @return
     */
    public static String getGroupSpeciesByCode(String groupSpeciesCode){
        String groupSpeciesName="-";
        if(("A").equals(groupSpeciesCode)){
           groupSpeciesName = "Amphibians";
        }else if(("B").equals(groupSpeciesCode)){
            groupSpeciesName = "Birds";
        }else if(("F").equals(groupSpeciesCode)){
            groupSpeciesName = "Fish";
        }else if(("I").equals(groupSpeciesCode)){
            groupSpeciesName = "Invertebrates";
        }else if(("M").equals(groupSpeciesCode)){
            groupSpeciesName = "Mammals";
        }else if(("P").equals(groupSpeciesCode)){
            groupSpeciesName = "Plants";
        }else if(("R").equals(groupSpeciesCode)){
            groupSpeciesName = "Reptiles";
        }
        return groupSpeciesName;
    }

   /**
    *
    * @param groupOSpeciesCode
    * @return
    */
    public static String getGroupOtherSpeciesByCode(String groupOSpeciesCode){
    
        String groupOSpeciesName="-";
        if(("A").equals(groupOSpeciesCode)){
           groupOSpeciesName = "Amphibians";
        }else if(("B").equals(groupOSpeciesCode)){
            groupOSpeciesName = "Birds";
        }else if(("F").equals(groupOSpeciesCode)){
            groupOSpeciesName = "Fish";
        }else if(("Fu").equals(groupOSpeciesCode)){
            groupOSpeciesName = "Fungi";
        }else if(("I").equals(groupOSpeciesCode)){
            groupOSpeciesName = "Invertebrates";
        }else if(("L").equals(groupOSpeciesCode)){
            groupOSpeciesName = "Lichens";
        }else if(("M").equals(groupOSpeciesCode)){
            groupOSpeciesName = "Mammals";
        }else if(("P").equals(groupOSpeciesCode)){
            groupOSpeciesName = "Plants";
        }else if(("R").equals(groupOSpeciesCode)){
            groupOSpeciesName = "Reptiles";
        }
        return groupOSpeciesName;
    }

    /**
     *
     * @param groupSpeciesCode
     * @return
     */
    public static String getGroupSpeciesByName(String groupSpeciesName){
        String groupSpeciesCode="-";
        
        if(("Amphibians").equals(groupSpeciesName)){
           groupSpeciesCode = "A";
        }else if(("Birds").equals(groupSpeciesName)){
            groupSpeciesCode = "B";
        }else if(("Fish").equals(groupSpeciesName)){
            groupSpeciesCode = "F";
        }else if(("Invertebrates").equals(groupSpeciesName)){
            groupSpeciesCode = "I";
        }else if(("Mammals").equals(groupSpeciesName)){
            groupSpeciesCode = "M";
        }else if(("Plants").equals(groupSpeciesName)){
            groupSpeciesCode = "P";
        }else if(("Reptiles").equals(groupSpeciesName)){
            groupSpeciesCode = "R";
        }
        return groupSpeciesCode;
    }


     /**
    *
    * @param groupOSpeciesCode
    * @return
    */
    public static String getGroupOtherSpeciesByName(String groupOSpeciesName){

        String groupOSpeciesCode="-";
        if(("Amphibians").equals(groupOSpeciesName)){
           groupOSpeciesCode = "A";
        }else if(("Birds").equals(groupOSpeciesName)){
            groupOSpeciesCode = "B";
        }else if(("Fish").equals(groupOSpeciesName)){
            groupOSpeciesCode = "F";
        }else if(("Fungi").equals(groupOSpeciesName)){
            groupOSpeciesCode = "Fu";
        }else if(("Invertebrates").equals(groupOSpeciesName)){
            groupOSpeciesCode = "I";
        }else if(("Lichens").equals(groupOSpeciesName)){
            groupOSpeciesCode = "L";
        }else if(("Mammals").equals(groupOSpeciesName)){
            groupOSpeciesCode = "M";
        }else if(("Plants").equals(groupOSpeciesName)){
            groupOSpeciesCode = "P";
        }else if(("Reptiles").equals(groupOSpeciesName)){
            groupOSpeciesCode = "R";
        }
        return groupOSpeciesCode;
    }

    /**
     *
     * @param indexSelected
     * @return
     */
    public static String getOtherSpeciesGroupByIndex(int indexSelected){
        String groupCode="-";
        switch (indexSelected) {
            case 1:  groupCode="A";       break;
            case 2:   groupCode="B";      break;
            case 3:  groupCode="F";         break;
            case 4:   groupCode="Fu";         break;
            case 5:   groupCode="I";           break;
            case 6:   groupCode="L";          break;
            case 7:   groupCode="M";         break;
            case 8:   groupCode="P";        break;
            case 9:   groupCode="R";     break;

        }
        return groupCode;
        }


    /**
     *
     * @param indexSelected
     * @return
     */
     public static String getSpeciesGroupByIndex(int indexSelected){
         
        String groupCode="-";
        switch (indexSelected) {
            case 1:  groupCode="A";       break;
            case 2:  groupCode="B";      break;
            case 3:  groupCode="F";         break;
            case 4:  groupCode="I";           break;
            case 5:  groupCode="M";         break;
            case 6:  groupCode="P";        break;
            case 7:  groupCode="R";     break;

        }
         
        return groupCode;
        }

     /**
      *
      * @param groupOSpeciesCode
      * @return
      */
     public static int getSpeciesIndexByCodeGroup(String groupOSpeciesCode){
        int indexSelected=0;
        if(("A").equals(groupOSpeciesCode)){
           indexSelected=1;
        }else if(("B").equals(groupOSpeciesCode)){
           indexSelected=2;
        }else if(("F").equals(groupOSpeciesCode)){
            indexSelected=3;
        }else if(("Fu").equals(groupOSpeciesCode)){
            indexSelected=4;
        }else if(("I").equals(groupOSpeciesCode)){
            indexSelected=5;
        }else if(("L").equals(groupOSpeciesCode)){
            indexSelected=6;
        }else if(("M").equals(groupOSpeciesCode)){
            indexSelected=7;
        }else if(("P").equals(groupOSpeciesCode)){
            indexSelected=8;
        }else if(("R").equals(groupOSpeciesCode)){
            indexSelected=9;
        }
        return indexSelected;


        }

     /**
      *
      * @param groupOSpeciesCode
      * @return
      */
     public static int getOtherSpeciesIndexByCodeGroup(String groupOSpeciesCode){
        int indexSelected=0;
        if(("A").equals(groupOSpeciesCode)){
           indexSelected=1;
        }else if(("B").equals(groupOSpeciesCode)){
           indexSelected=2;
        }else if(("F").equals(groupOSpeciesCode)){
            indexSelected=3;
        }else if(("I").equals(groupOSpeciesCode)){
            indexSelected=4;
        }else if(("M").equals(groupOSpeciesCode)){
            indexSelected=5;
        }else if(("P").equals(groupOSpeciesCode)){
            indexSelected=6;
        }else if(("R").equals(groupOSpeciesCode)){
            indexSelected=7;
        }
        return indexSelected;


        }

     /***
      * 
      * @param relationType
      * @return
      */
     public static int getSelectedIndexByRelationType(String relationType){
         int selectedIndex=0;
         if(("=").equals(relationType)){
             selectedIndex=0;
         }else if(("+").equals(relationType)){
             selectedIndex=1;
         }else if(("-").equals(relationType)){
             selectedIndex=2;
         }else if(("*").equals(relationType)){
             selectedIndex=3;
         }else if(("/").equals(relationType)){
             selectedIndex=4;
         }
         return selectedIndex;

     }

    
}
