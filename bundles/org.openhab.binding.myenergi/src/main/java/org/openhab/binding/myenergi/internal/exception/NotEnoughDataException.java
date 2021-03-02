/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.myenergi.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NotEnoughDataException} is thrown if not enough data is available for the function to be performed.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class NotEnoughDataException extends MyEnergiException {

    private static final long serialVersionUID = 4808345987650248340L;

    public NotEnoughDataException() {
        super();
    }

    public NotEnoughDataException(String message) {
        super(message);
    }

    public NotEnoughDataException(Throwable cause) {
        super(cause);
    }

    public NotEnoughDataException(String message, Throwable cause) {
        super(message, cause);
    }
}