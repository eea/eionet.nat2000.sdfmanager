package sdf_manager.validatorErrors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeywordsBasedCustomErrorMessageCondition implements CustomErrorMessageCondition {

    private List<String> Keywords;

    @Override
    public boolean isConditionMetBasedOnExceptionMessage(String message) {
        boolean result = false;
        for (String keyword: Keywords
             ) {
            if(message.contains(keyword)){
                result  = true;
            }else{
                return false;
            }
        }
        return result;
    }

    private KeywordsBasedCustomErrorMessageCondition(){
    }

    public KeywordsBasedCustomErrorMessageCondition(String[] keywords){
        Keywords = new ArrayList<String>();
        Keywords.addAll(Arrays.asList(keywords));
    };
}
