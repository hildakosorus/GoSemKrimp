package datatools;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author hkosorus
 *
 */
public class Keyword {
	
	private int ID;
	private String original;
	private String baseform;
	private String stem;
	private String hypernym;
	private String tag;
	private String hypernymstem;
	
	public Keyword() {
		
	}

	public Keyword(int iD, String original, String baseform, String stem,
			String hypernym, String hypernymstem, String tag) {
		super();
		ID = iD;
		this.original = original;
		this.baseform = baseform;
		this.stem = stem;
		this.hypernym = hypernym;
		this.tag = tag;
		this.hypernymstem = hypernymstem;
	}

	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getOriginal() {
		return original;
	}
	public void setOriginal(String original) {
		this.original = original;
	}
	public String getBaseform() {
		return baseform;
	}
	public void setBaseform(String baseform) {
		this.baseform = baseform;
	}
	public String getStem() {
		return stem;
	}
	public void setStem(String stem) {
		this.stem = stem;
	}
	public String getHypernym() {
		return hypernym;
	}
	public void setHypernym(String hypernym) {
		this.hypernym = hypernym;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getHypernymstem() {
		return hypernymstem;
	}
	public void setHypernymstem(String hypernymstem) {
		this.hypernymstem = hypernymstem;
	}
	@Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(baseform).
            append(tag).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Keyword))
            return false;
        if (obj == this)
            return true;

        Keyword rhs = (Keyword) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
            append(baseform, rhs.baseform).
            append(tag, rhs.tag).
            isEquals();
    }
	@Override
	public String toString() {
		return ID+" "+original+" "+baseform+" "+stem+" "+hypernym+" "+hypernymstem+" "+tag;
	}
}
