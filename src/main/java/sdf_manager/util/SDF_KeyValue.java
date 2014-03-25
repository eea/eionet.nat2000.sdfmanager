/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdf_manager.util;

public class SDF_KeyValue {
	private Object key;
        private String value;

	public SDF_KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getValue() {
            return value;
        }
	public String getKey() {
            return key.toString();
        }

	@Override
	public String toString() {
            return key.toString();
        }

	@Override
	public boolean equals(Object obj) {
            if (obj instanceof SDF_KeyValue) {
                    SDF_KeyValue kv = (SDF_KeyValue) obj;
                    return (kv.value.equals(this.value));
            }
            return false;
	}

        public void setValue(String value) {
            this.value=value;
        }
        public void setKey(Object key) {
            this.key=key;
        }




	@Override
	public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
            return hash;
	}
}
