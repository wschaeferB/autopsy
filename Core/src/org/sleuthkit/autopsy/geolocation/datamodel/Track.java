/*
 *
 * Autopsy Forensic Browser
 *
 * Copyright 2020 Basis Technology Corp.
 * contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.geolocation.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoTrackPoints;
import org.sleuthkit.datamodel.blackboardutils.attributes.GeoWaypoint.GeoTrackPoint;

/**
 * A GPS track with which wraps the TSK_GPS_TRACK artifact.
 */
public final class Track extends GeoPath{

    private final Long startTimestamp;
    private final Long endTimeStamp;

    /**
     * Construct a new Track for the given artifact.
     * 
     * @param artifact
     * 
     * @throws GeoLocationDataException 
     */
    public Track(BlackboardArtifact artifact) throws GeoLocationDataException {
        this(artifact, Waypoint.getAttributesFromArtifactAsMap(artifact));
    }

    /**
     * Construct a Track for the given artifact and attributeMap.
     * 
     * @param artifact TSK_GPD_TRACK artifact
     * @param attributeMap  Map of the artifact attributes
     * 
     * @throws GeoLocationDataException 
     */
    private Track(BlackboardArtifact artifact, Map<BlackboardAttribute.ATTRIBUTE_TYPE, BlackboardAttribute> attributeMap) throws GeoLocationDataException {
        super(artifact, getTrackName(attributeMap));

        List<GeoTrackPoint> points = getPointsList(attributeMap);
        buildPath(points);

        startTimestamp = findStartTime(points);
        endTimeStamp = findEndTime(points);
    }
    
    /**
     * Returns the start time of this track.
     * 
     * @return  Earliest time, or null if none was available. 
     *          (seconds from java epoch)
     */
    public Long getStartTime() {
        return startTimestamp;
    }
    
    /**
     * Returns the end time of this track.
     * 
     * @return  Earliest timestamp, or null if none was available. 
     *          (seconds from java epoch)
     */
    public Long getEndTime() {
        return endTimeStamp;
    }

    /**
     * Return the name of the track from the attributeMap. 
     * Track name is stored in the attribute TSK_NAME
     * 
     * @param attributeMap
     
     * @return Track name or empty string if none was available. 
     */
    private static String getTrackName(Map<BlackboardAttribute.ATTRIBUTE_TYPE, BlackboardAttribute> attributeMap) {
        BlackboardAttribute attribute = attributeMap.get(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_NAME);

        return attribute != null ? attribute.getValueString() : "";
    }

    /**
     * Create the list of TrackWaypoints from the GeoTrackPoint list.
     * 
     * @param points List of GeoTrackPoints
     * 
     * @throws GeoLocationDataException 
     */
    @Messages({
        "# {0} - track name",
        "GEOTrack_point_label_header=Trackpoint for track: {0}"
    })
    private void buildPath(List<GeoTrackPoint> points) throws GeoLocationDataException {
        for (GeoTrackPoint point : points) {
            addToPath(new TrackWaypoint(Bundle.GEOTrack_point_label_header(getLabel()), point));
        }
    }

    /**
     * Returns the list of GeoTrackPoints from the attributeMap.  Creates the 
     * GeoTrackPoint list from the TSK_GEO_TRACKPOINTS attribute.
     * 
     * @param attributeMap Map of artifact attributes.
     * 
     * @return GeoTrackPoint list empty list if the attribute was not found.
     */
    private List<GeoTrackPoint> getPointsList(Map<BlackboardAttribute.ATTRIBUTE_TYPE, BlackboardAttribute> attributeMap) {
        BlackboardAttribute attribute = attributeMap.get(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_TRACKPOINTS);
        if (attribute != null) {
            String value = attribute.getValueString();
            return GeoTrackPoints.deserializePoints(value);
        }

        return new ArrayList<>();
    }

    /**
     * Return the start time for the track. Assumes the points are in time
     * order.
     *
     * @param points List of GeoTrackPoints.
     *
     * @return First non-null time stamp or null, if one was not found.
     */
    private Long findStartTime(List<GeoTrackPoint> points) {
        if (points != null) {
            for (GeoTrackPoint point : points) {
                if (point.getTimeStamp() != null) {
                    return point.getTimeStamp();
                }
            }
        }
        return null;
    }

    /**
     * Return the ends time for the track. Assumes the points are in time
     * order.
     *
     * @param points List of GeoTrackPoints.
     *
     * @return First non-null time stamp or null, if one was not found.
     */
    private Long findEndTime(List<GeoTrackPoint> points) {
        if (points != null) {
            for (int index = points.size() - 1; index >= 0; index--) {
                GeoTrackPoint point = points.get(index);
                if (point.getTimeStamp() != null) {
                    return point.getTimeStamp();
                }
            }
        }
        return null;
    }

    /**
     * A Waypoint subclass for the points of a track.
     */
    final class TrackWaypoint extends Waypoint {

        private final List<Waypoint.Property> propertyList;

        /**
         * Construct a TrackWaypoint.
         * 
         * @param point GeoTrackPoint 
         * 
         * @throws GeoLocationDataException 
         */
        TrackWaypoint(String pointLabel, GeoTrackPoint point) throws GeoLocationDataException {
            super(null, pointLabel,
                    point.getTimeStamp(),
                    point.getLatitude(),
                    point.getLongitude(),
                    point.getAltitude(),
                    null,
                    null,
                    Track.this);

            propertyList = createPropertyList(point);
        }

        /**
         * Overloaded to return a property list that is generated from
         * the GeoTrackPoint instead of an artifact.
         * 
         * @return unmodifiable list of Waypoint.Property 
         */
        @Override
        public List<Waypoint.Property> getOtherProperties() {
            return Collections.unmodifiableList(propertyList);
        }

        /**
         * Create a propertyList specific to GeoTrackPoints.
         * 
         * @param point GeoTrackPoint to get values from.
         * 
         * @return A list of Waypoint.properies.
         */
        private List<Waypoint.Property> createPropertyList(GeoTrackPoint point) {
            List<Waypoint.Property> list = new ArrayList<>();

            Long timestamp = point.getTimeStamp();
            if (timestamp != null) {
                list.add(new Property(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME.getDisplayName(), timestamp.toString()));
            }

            Double value = point.getVelocity();
            if (value != null) {
                list.add(new Property(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_VELOCITY.getDisplayName(), value.toString()));
            }

            value = point.getDistanceTraveled();
            if (value != null) {
                list.add(new Property(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_DISTANCE_TRAVELED.getDisplayName(), value.toString()));
            }

            value = point.getDistanceFromHP();
            if (value != null) {
                list.add(new Property(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_DISTANCE_FROM_HOME_POINT.getDisplayName(), value.toString()));
            }

            return list;
        }
    }
}
