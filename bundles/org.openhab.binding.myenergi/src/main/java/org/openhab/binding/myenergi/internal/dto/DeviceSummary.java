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
package org.openhab.binding.myenergi.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceSummary} is a DTO class used to represent the highlevel status of all myenergi devices. It's
 * used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 */
public class DeviceSummary {

    // private List<EddiSummary> eddi = new ArrayList<>();

    @SerializedName("harvi")
    public List<HarviSummary> harvis = new ArrayList<>();

    @SerializedName("zappi")
    public List<ZappiSummary> zappis = new ArrayList<>();

    @SerializedName("eddi")
    public List<EddiSummary> eddis = new ArrayList<>();

    public String asn;

    @Override
    public String toString() {
        return "DeviceSummary [harvis=" + harvis + ", zappis=" + zappis + ", asn=" + asn + "]";
    }
}