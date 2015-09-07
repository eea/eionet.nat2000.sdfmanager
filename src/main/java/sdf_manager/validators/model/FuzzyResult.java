package sdf_manager.validators.model;

import java.math.BigDecimal;

public class FuzzyResult {

	private String name;
	private String title;
	private BigDecimal score;
	
	public FuzzyResult() {
		// TODO Auto-generated constructor stub
	}	
	
	public FuzzyResult(String name, String title, BigDecimal score) {
		this();
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
