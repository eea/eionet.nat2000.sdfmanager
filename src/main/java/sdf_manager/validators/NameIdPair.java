package sdf_manager.validators;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class NameIdPair {
	
	String id;
	String name;

	public NameIdPair() {
		// TODO Auto-generated constructor stub
	}
	public NameIdPair(String id, String name) {
		this();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	//Simple hack to show value in JTable
	@Override
	public String toString() {
		return this.name; 				
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NameIdPair)) return false;
		NameIdPair temp = (NameIdPair) obj;
		return new EqualsBuilder().
			append(name, temp.getName()).
			append(id, temp.getId()).isEquals();		
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).
				append(name).
				append(id).toHashCode();
	}
	
	

}
