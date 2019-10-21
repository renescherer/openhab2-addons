/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.handler;

import static org.openhab.binding.surepetcare.internal.SurePetcareConstants.*;

import java.time.ZonedDateTime;

import javax.measure.quantity.Mass;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.surepetcare.internal.SurePetcareAPIHelper;
import org.openhab.binding.surepetcare.internal.SurePetcareApiException;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.data.SurePetcareHousehold;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePet;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePetLocation;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePetStatus.Activity;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePetStatus.Feeding;
import org.openhab.binding.surepetcare.internal.data.SurePetcareTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcarePetHandler} is responsible for handling the things created to represent Sure Petcare pets.
 *
 * @author Rene Scherer - Initial Contribution
 * @author Holger Eisold - Added pet feeder status
 */
@NonNullByDefault
public class SurePetcarePetHandler extends SurePetcareBaseObjectHandler {

    private final Logger logger = LoggerFactory.getLogger(SurePetcarePetHandler.class);

    public SurePetcarePetHandler(Thing thing, SurePetcareAPIHelper petcareAPI) {
        super(thing, petcareAPI);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("DeviceHandler handleCommand called with command: {}", command.toString());

        if (command instanceof RefreshType) {
            updateThing();
        } else {
            switch (channelUID.getId()) {
                case PET_CHANNEL_LOCATION:
                    logger.debug("received location update command: {}", command.toFullString());
                    if (command instanceof StringType) {
                        synchronized (petcareAPI) {
                            SurePetcarePet pet = petcareAPI.getPet(thing.getUID().getId());
                            if (pet != null) {
                                String newLocationIdStr = ((StringType) command).toString();
                                try {
                                    Integer newLocationId = Integer.valueOf(newLocationIdStr);
                                    logger.debug("received new location: {}", newLocationId);
                                    petcareAPI.setPetLocation(pet, newLocationId);
                                    updateState(PET_CHANNEL_LOCATION,
                                            new StringType(pet.getLocation().getWhere().toString()));
                                    updateState(PET_CHANNEL_LOCATION_CHANGED,
                                            new DateTimeType(pet.getLocation().getLocationChanged()));
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid location id: {}, ignoring command", newLocationIdStr);
                                } catch (SurePetcareApiException e) {
                                    logger.warn("Error from SurePetcare API. Can't update location {} for pet {}",
                                            newLocationIdStr, pet.toString());
                                }
                            }
                        }
                    }
                    break;
                default:
                    logger.warn("Update on unsupported channel {}", channelUID.getId());
            }
        }
    }

    @Override
    protected void updateThing() {
        synchronized (petcareAPI) {
            SurePetcarePet pet = petcareAPI.getPet(thing.getUID().getId());
            if (pet != null) {
                logger.debug("Updating all thing channels for pet : {}", pet.toString());
                updateState(PET_CHANNEL_ID, new DecimalType(pet.getId()));
                if (pet.getName() != null) {
                    updateState(PET_CHANNEL_NAME, new StringType(pet.getName()));
                }
                if (pet.getComments() != null) {
                    updateState(PET_CHANNEL_COMMENT, new StringType(pet.getComments()));
                }
                if (pet.getGenderId() != null) {
                    updateState(PET_CHANNEL_GENDER, new StringType(pet.getGenderId().toString()));
                }
                if (pet.getBreedId() != null) {
                    updateState(PET_CHANNEL_BREED, new StringType(pet.getBreedId().toString()));
                }
                if (pet.getSpeciesId() != null) {
                    updateState(PET_CHANNEL_SPECIES, new StringType(pet.getSpeciesId().toString()));
                }
                if (pet.getPhoto() != null) {
                    updateState(PET_CHANNEL_PHOTO_URL, new StringType(pet.getPhoto().getLocation()));
                }
                Activity loc = pet.getPetStatus().getActivity();
                if (loc != null) {
                    updateState(PET_CHANNEL_LOCATION, new StringType(loc.getWhere().toString()));
                    if (loc.getLocationChanged() != null) {
                        updateState(PET_CHANNEL_LOCATION_CHANGED, new DateTimeType(loc.getLocationChanged()));
                    }
                }
                ZonedDateTime dob = pet.getDateOfBirthAsZonedDateTime();
                if (dob != null) {
                    updateState(PET_CHANNEL_DATE_OF_BIRTH, new DateTimeType(dob));
                }
                if (pet.getWeight() != null) {
                    updateState(PET_CHANNEL_WEIGHT, new DecimalType(pet.getWeight()));
                }
                if (pet.getTagId() != null) {
                    SurePetcareTag tag = petcareAPI.getTag(pet.getTagId().toString());
                    if (tag != null) {
                        updateState(PET_CHANNEL_TAG_IDENTIFIER, new StringType(tag.getTag()));
                    }
                }
                if (pet.getPetStatus().getActivity().getDeviceId() != null) {
                    SurePetcareDevice device = petcareAPI
                            .getDevice(pet.getPetStatus().getActivity().getDeviceId().toString());
                    updateState(PET_CHANNEL_LOCATION_CHANGED_THROUGH, new StringType(device.getName()));
                } else if (pet.getPetStatus().getActivity().getUserId() != null) {
                    SurePetcareHousehold user = petcareAPI.getHousehold(pet.getHouseholdId().toString());
                    int numUsers = user.getHouseholdUsers().size();
                    for (int i = 0; (i < numUsers); i++) {
                        if (pet.getPetStatus().getActivity().getUserId()
                                .equals(user.getHouseholdUsers().get(i).getUser().getUserId())) {
                            updateState(PET_CHANNEL_LOCATION_CHANGED_THROUGH,
                                    new StringType(user.getHouseholdUsers().get(i).getUser().getUserName().toString()));
                        }
                    }
                }
                Feeding feeding = pet.getPetStatus().getFeeding();
                if (feeding != null) {
                    SurePetcareDevice device = petcareAPI.getDevice(feeding.getDeviceId().toString());
                    if (device != null) {
                        updateState(PET_CHANNEL_FEEDER_DEVICE, new StringType(device.getName()));
                        int numBowls = feeding.getFeedChange().size();
                        for (int i = 0; (i < 2) && (i < numBowls); i++) {
                            if (device.getControl().getBowls().getBowlId().equals(1)) {
                                updateState(PET_CHANNEL_FEEDER_LAST_CHANGE,
                                        new QuantityType<Mass>(feeding.getFeedChange().get(i), SIUnits.GRAM));
                            } else if (device.getControl().getBowls().getBowlId().equals(4)) {
                                if ((i + 1) == 1) {
                                    updateState(PET_CHANNEL_FEEDER_LAST_CHANGE_LEFT,
                                            new QuantityType<Mass>(feeding.getFeedChange().get(i), SIUnits.GRAM));
                                }
                                if ((i + 1) == 2) {
                                    updateState(PET_CHANNEL_FEEDER_LAST_CHANGE_RIGHT,
                                            new QuantityType<Mass>(feeding.getFeedChange().get(i), SIUnits.GRAM));
                                }
                            }
                        }
                        updateState(PET_CHANNEL_FEEDER_LASTFEEDING, new DateTimeType(feeding.getZonedFeedChangeAt()));
                    }
                }
            } else {
                logger.debug("Trying to update unknown pet: {}", thing.getUID().getId());
            }
        }
    }

    protected void updatePetLocation() {
        synchronized (petcareAPI) {
            SurePetcarePet pet = petcareAPI.getPet(thing.getUID().getId());
            if (pet != null) {
                SurePetcarePetLocation loc = pet.getLocation();
                if (loc != null) {
                    updateState(PET_CHANNEL_LOCATION, new StringType(loc.getWhere().toString()));
                    if (loc.getLocationChanged() != null) {
                        updateState(PET_CHANNEL_LOCATION_CHANGED, new DateTimeType(loc.getLocationChanged()));
                    }
                }
            }
        }
    }
}
