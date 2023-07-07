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

package com.bbn.landsar.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.bbn.roger.message.AddressableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * <p>Status Update Messages are status reports that the server shares with the client
 * in order to inform the client of progress on a given request. These messages may include
 * warnings and errors, if there are issues when processing the request.</p>
 * 
 * Warnings can be present in regardless of the state of the message.  
 *
 * <p>Note: This class has a natural ordering that is inconsistent with equals.
 * Status update messages are ordered by timestamp, with the most recent message
 * appearing first. However, equality is determined based on the fields of the message.</p>
 */
public class StatusUpdateMessage implements Serializable, Comparable<StatusUpdateMessage> {
	
	/**
	 * Generated 
	 */
	private static final long serialVersionUID = 4053460633388354958L;
	
	public static final short UNKNOWN = 0;
	public static final short STATE_IN_PROGRESS = 1;
	public static final short STATE_DONE = 2;
	public static final short STATE_ERROR = 4;

	private UUID lpiId;
	private UUID searchId;
	private UUID requestId;
	private String summary;
	private short state;
	private List<String> warnings;
	private List<String> errors;
	private long timestamp;
	
	/**
	 * Captures network address / destination of client that is interested in updates
	 */
	@JsonIgnore
	private final AddressableEntity<?> dest;
	
	public StatusUpdateMessage() {
		summary = "";
		state = UNKNOWN;
		warnings = new ArrayList<>();
		errors = new ArrayList<>();
		timestamp = System.currentTimeMillis();
		dest = null;
	}
	
	public StatusUpdateMessage(UUID lpiId, UUID searchId, UUID requestId, AddressableEntity<?> dest) {
		summary = "";
		state = UNKNOWN;
		warnings = new ArrayList<>();
		errors = new ArrayList<>();
		timestamp = System.currentTimeMillis();
		this.lpiId = lpiId;
		this.searchId = searchId;
		this.requestId = requestId;
		this.dest = dest;
	}

	@JsonIgnore
	public boolean isDone() {
		return STATE_DONE == this.state;
	}
	
	@JsonIgnore
	public boolean isError() {
		return STATE_ERROR == this.state;
	}
	
	@JsonIgnore
	public boolean isInProgress() {
		return STATE_IN_PROGRESS == this.state;
	}
	
	@JsonIgnore
	public AddressableEntity<?> getDest() {
		return this.dest;
	}
	
	public void addWarning(String warning, Exception warningDetails) {
		warnings.add(warning);
		warnings.add("\tException message: " + warningDetails.getMessage());
		if (warningDetails.getCause() != null) {
			warnings.add("\tCause: " + warningDetails.getCause().toString());
		}
		warnings.add("\tStack trace: " + Arrays.toString(warningDetails.getStackTrace()));
	}
	
	public void addWarning(String warning, String warningDetails) {
		warnings.add(warning);
		warnings.add("\t" + warningDetails);
	}
	
	public void addWarning(String warning) {
		warnings.add(warning);
	}
	
	
	public void addError(String summaryMessage) {
		state = STATE_ERROR;
		summary += summaryMessage;
		errors.add(summaryMessage);
	}
	
	public void addError(String summaryMessage, String error) {
		state = STATE_ERROR;
		summary += summaryMessage;
		errors.add(error);
	}
	
	/**
	 * Sets the state to error
	 * @param summaryMessage
	 * @param e
	 */
	public void addError(String summaryMessage, Throwable e) {
		state = STATE_ERROR;
		summary += summaryMessage;
		errors.add(e.getMessage());
		if (e.getCause() != null) {
			errors.add("\tCause: " + e.getCause().toString());
		}
		errors.add("\tStack trace: " + Arrays.toString(e.getStackTrace()));
	}
	
	public UUID getLpiId() {
		return lpiId;
	}

	public void setLpiId(UUID lpiId) {
		this.lpiId = lpiId;
	}

	public UUID getSearchId() {
		return searchId;
	}

	public void setSearchId(UUID searchId) {
		this.searchId = searchId;
	}

	public UUID getRequestId() {
		return requestId;
	}

	public void setRequestId(UUID requestId) {
		this.requestId = requestId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public short getState() {
		return state;
	}

	public void setState(short state) {
		this.state = state;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int compareTo(StatusUpdateMessage that) {
		if (this.timestamp > that.timestamp) {
			// ** This status update message is more recent, so it should appear earlier in a list.
			return -1;
		} else if (this.timestamp < that.timestamp) {
			// ** This timestamp is older, so it should appear later in a list.
			return 1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		StatusUpdateMessage that = (StatusUpdateMessage) obj;
		return state == that.state &&
				timestamp == that.timestamp &&
				Objects.equals(lpiId, that.lpiId) &&
				Objects.equals(searchId, that.searchId) &&
				Objects.equals(requestId, that.requestId) &&
				Objects.equals(summary, that.summary) &&
				Objects.equals(warnings, that.warnings) &&
				Objects.equals(errors, that.errors);
	}

	@Override
	public int hashCode() {
		return Objects.hash(lpiId, searchId, requestId, summary, state, warnings, errors, timestamp);
	}

	@Override
	public String toString() {
		return "StatusUpdateMessage{" +
				"lpiId=" + lpiId +
				", searchId=" + searchId +
				", requestId=" + requestId +
				", summary='" + summary + '\'' +
				", state=" + state +
				", warnings=" + warnings +
				", errors=" + errors +
				", timestamp=" + timestamp +
				'}';
	}
}
