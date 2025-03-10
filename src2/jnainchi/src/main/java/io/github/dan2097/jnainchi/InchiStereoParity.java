/**
 * JNA-InChI - Library for calling InChI from Java
 * Copyright © 2018 Daniel Lowe
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.dan2097.jnainchi;

import java.util.HashMap;
import java.util.Map;

import io.github.dan2097.jnainchi.inchi.InchiLibrary.tagINCHIStereoParity0D;

public enum InchiStereoParity {

  NONE(tagINCHIStereoParity0D.INCHI_PARITY_NONE),
  
  ODD(tagINCHIStereoParity0D.INCHI_PARITY_ODD),
  
  EVEN(tagINCHIStereoParity0D.INCHI_PARITY_EVEN),
  
  UNKNOWN(tagINCHIStereoParity0D.INCHI_PARITY_UNKNOWN),
  
  UNDEFINED(tagINCHIStereoParity0D.INCHI_PARITY_UNDEFINED);
  
	  private final int code;
	  
	  private InchiStereoParity(int code) {
	    this.code = code;
	  }

	  byte getCode() {
	    return (byte) code;
	  }
	  
	  private static final Map<Object, InchiStereoParity> map = new HashMap<>();
	  
	  static {
	    for (InchiStereoParity val : InchiStereoParity.values()) {
	      map.put(Integer.valueOf(val.code), val);
	      map.put(val.name().toLowerCase(), val);
	    }
	  }
	  
	  static InchiStereoParity of(int code) {
	    return map.get(Integer.valueOf(code));
	  }

		public static int getCodeObj(Object val) {
			if (val != null) {
				InchiStereoParity e = (val instanceof InchiStereoParity ? (InchiStereoParity) val 
						: map.get(val.toString().toLowerCase()));
				if (e != null)
					return e.getCode();
			}
			return NONE.getCode();
		}
	}
