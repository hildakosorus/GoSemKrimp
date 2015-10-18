package similarity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hkosorus
 *
 */
public enum RelatednessType {
	
	HirstStOnge(0), LeacockChodorow(1), Leskdb(2), WuPalmer(3), Resnik(4), JiangConrath(5), Lin(6), Path(7);
	
	private int id;

	private static final Map<Integer, RelatednessType> lookup = new HashMap<Integer, RelatednessType>();

	static {
		for (RelatednessType type : EnumSet.allOf(RelatednessType.class)) {
			lookup.put(type.getId(), type);
		}
	}

	private RelatednessType() {
		
	}

	private RelatednessType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static RelatednessType getById(int id) {
		return lookup.get(id);
	}

	public static RelatednessType getByString(String langString) {
		for (RelatednessType type : lookup.values()) {
			if (type.toString().equalsIgnoreCase(langString)) {
				return type;
			}
		}
		return null;
	}
}
