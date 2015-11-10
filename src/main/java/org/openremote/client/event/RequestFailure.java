/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.client.event;

import jsinterop.annotations.JsType;

@JsType
public class RequestFailure {

    public final String requestText;
    public final int statusCode;
    public final String statusText;
    public final String errorText;

    public RequestFailure(String requestText, int statusCode, String statusText, String errorText) {
        this.requestText = requestText;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.errorText = errorText;
    }

    public String getFailureMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Request \"").append(requestText).append("\" failed.");
        if (statusCode > 0) {
            sb.append(" Response was (").append(statusCode).append(") ");
            sb.append(statusText).append(".");
        } else {
            sb.append(" No response.");
        }
        if (errorText != null && !errorText.equals(statusText)) {
            sb.append(" ").append(errorText);
            if (!errorText.substring(errorText.length()-1).equals("."))
                sb.append(".");
        }
        return sb.toString();
    }
}
