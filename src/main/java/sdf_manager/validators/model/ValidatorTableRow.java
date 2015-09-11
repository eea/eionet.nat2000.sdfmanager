package sdf_manager.validators.model;

import org.apache.commons.lang.builder.EqualsBuilder;

import sdf_manager.validators.AcceptedNameTriple;
import sdf_manager.validators.NameIdPair;

/**
 * Represents a swing table row object
 * @author George Sofianos
 */
public class ValidatorTableRow implements ValidatorRow {
    private NameIdPair nameId;
    private String kingdom;
    private String family;
    private AcceptedNameTriple acceptedNameTriple;

    public ValidatorTableRow(NameIdPair nameId, String kingdom, String family, AcceptedNameTriple acceptedNameTriple) {
        this.nameId = nameId;
        this.kingdom = kingdom;
        this.family = family;        
        this.acceptedNameTriple = acceptedNameTriple;
    }
    
    public NameIdPair getNameId() {
		return nameId;
	}


	public void setNameId(NameIdPair nameId) {
		this.nameId = nameId;
	}


	public String getKingdom() {
        return kingdom;
    }

    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }
	
	@Override
	public Object[] getRow() { 
		return new Object[] {this.nameId, this.kingdom, this.family, this.acceptedNameTriple};
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ValidatorTableRow)) return false;
		ValidatorTableRow temp = (ValidatorTableRow) obj;		
		return new EqualsBuilder().
				append(nameId, temp.getNameId()).isEquals();					
	}
	
    
}
