package sdf_manager.validators;

import java.math.BigDecimal;

/**
 *
 * @author George Sofianos
 */
public class FuzzyValidatorTableRow {
    private String name;
    private String title;    
    private BigDecimal score;
    //private List<String> taxonUids;

    public FuzzyValidatorTableRow(String name, String title, BigDecimal score) {
        this.name = name;
        this.title = title;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
}
