package sdf_manager.validatorErrors;

import org.xml.sax.SAXParseException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ValidatorErrorMessagesPreparator {

     private boolean displayLineNumber = true;

     private boolean displayColumnNumber = true ;

     private List<CustomErrorMessage> customErrorMessageList = new ArrayList<CustomErrorMessage>();

     public ValidatorErrorMessagesPreparator(){
     }

     public ValidatorErrorMessagesPreparator setDisplayLineNumber(boolean displayLineNumber){
         this.displayLineNumber = displayLineNumber;
         return this;
     }

     public ValidatorErrorMessagesPreparator setDisplayColumnNumber(boolean displayColumnNumber){
         this.displayColumnNumber = displayColumnNumber;
         return this;
     }

     public ValidatorErrorMessagesPreparator addCustomErrorMessage(CustomErrorMessage customErrorMessage){
         this.customErrorMessageList.add(customErrorMessage);
         return this;
     }
     public ValidatorErrorMessagesPreparator addCustomErrorMessages(List<CustomErrorMessage> customErrorMessages){
         this.customErrorMessageList.addAll(customErrorMessages);
         return this;
     }



    public String prepareMessageBasedOnValidationException(SAXParseException exception){
            String result = "";
        for (CustomErrorMessage customErrorMessage: customErrorMessageList
             ) {
            if(customErrorMessage.condition.isConditionMetBasedOnExceptionMessage(exception.getMessage())){
                if(this.displayLineNumber && !this.displayColumnNumber){
                    result  =result + "< Line: " + exception.getLineNumber() + " > " + customErrorMessage.getMessage();

                }
               else  if(this.displayColumnNumber && !this.displayLineNumber ){
                    result = result + "< Column: " + exception.getColumnNumber()
                            + " > " + customErrorMessage.getMessage();
                    return result;
                }
               else  if(this.displayLineNumber && this.displayColumnNumber){
                    result = result + "< Line: " + exception.getLineNumber() + ", Column: " + exception.getColumnNumber()
                            + " > " + customErrorMessage.getMessage();
                    return result;
                }
               else {
                    return customErrorMessage.getMessage();
                }
            }
        }
        return null;
    }
}
