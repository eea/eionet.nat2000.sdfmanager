package sdf_manager.validatorErrors;

public class CustomErrorMessage {

    private String message;

    CustomErrorMessageCondition condition;

    public CustomErrorMessage(){
    }

    public CustomErrorMessage(String message, CustomErrorMessageCondition condition) {
        this.message = message;
        this.condition = condition;
    }

    public String getMessage() {
        return message;
    }

    public CustomErrorMessage addMessage(String message){
        this.message = message;
        return this;
    }

    public CustomErrorMessage addMessageCondition(CustomErrorMessageCondition condition){
        this.condition = condition;
        return this;
    }
}
