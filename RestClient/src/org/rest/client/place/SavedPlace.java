/*******************************************************************************
 * Copyright 2012 Paweł Psztyć
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.rest.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class SavedPlace extends Place {
	
	private String placeToken = null;
	
	/**
	 * @param requestData
	 */
	public SavedPlace(String requestData){
		this.placeToken = requestData;
	}
	
	public String getToken(){
		return placeToken;
	}
	
	public static class Tokenizer implements PlaceTokenizer<SavedPlace> {

		@Override
		public String getToken(SavedPlace place) {
			return place.getToken();
		}

		@Override
		public SavedPlace getPlace(String token) {
			return new SavedPlace(token);
		}

	}
}
