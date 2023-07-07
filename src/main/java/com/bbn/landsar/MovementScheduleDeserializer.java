/*
LandSAR Motion Model Software Development Kit
Copyright (c) 2023 Raytheon Technologies 

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
https://github.com/atapas/add-copyright.git
*/

package com.bbn.landsar;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class MovementScheduleDeserializer extends StdDeserializer<MovementSchedule> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MovementScheduleDeserializer.class);
	
	public MovementScheduleDeserializer(Class<MovementSchedule> t) {
		super(t);
	}

	@Override
	public MovementSchedule deserialize(JsonParser jps, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jps.getCodec().readTree(jps);		
		JsonNode nameNode = node.get("name");
		if (nameNode == null) {
			LOGGER.error("Unable to deserialize MovementSchedule from node: {} "
					+ "Unable to read 'name' field from serializedMovementSchedule", node);
			return null;
		}
		String name = nameNode.asText();

		if (name == null) {
		
			LOGGER.error("Unable to deserialize MovementSchedule from node: {} "
					+ "Unable to read 'name' field from serializedMovementSchedule", node);
			return null;
		}
		
		return ScheduleManager.getOrRecreateScheduleForName(name);
	}
	
	
}
