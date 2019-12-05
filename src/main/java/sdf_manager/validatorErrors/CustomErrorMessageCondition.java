package sdf_manager.validatorErrors;

public interface CustomErrorMessageCondition {
    boolean isConditionMetBasedOnExceptionMessage(String message);
}
