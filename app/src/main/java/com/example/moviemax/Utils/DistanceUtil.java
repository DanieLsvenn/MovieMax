package com.example.moviemax.Utils;

import android.location.Location;

public class DistanceUtil {
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param lat1 Current latitude
     * @param lon1 Current longitude
     * @param lat2 Destination latitude
     * @param lon2 Destination longitude
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in kilometers
    }
    
    /**
     * Calculate distance using Android Location class
     * @param currentLocation User's current location
     * @param destinationLat Destination latitude
     * @param destinationLon Destination longitude
     * @return Distance in meters
     */
    public static float calculateDistanceInMeters(Location currentLocation, double destinationLat, double destinationLon) {
        if (currentLocation == null) {
            return -1;
        }
        
        Location destinationLocation = new Location("");
        destinationLocation.setLatitude(destinationLat);
        destinationLocation.setLongitude(destinationLon);
        
        return currentLocation.distanceTo(destinationLocation); // Returns distance in meters
    }
    
    /**
     * Format distance for display
     * @param distanceInMeters Distance in meters
     * @return Formatted distance string
     */
    public static String formatDistance(float distanceInMeters) {
        if (distanceInMeters < 0) {
            return "Khoảng cách không xác định";
        }
        
        if (distanceInMeters < 1000) {
            return String.format("%.0f m", distanceInMeters);
        } else {
            return String.format("%.1f km", distanceInMeters / 1000);
        }
    }
    
    /**
     * Format distance for display (from kilometers)
     * @param distanceInKm Distance in kilometers
     * @return Formatted distance string
     */
    public static String formatDistanceFromKm(double distanceInKm) {
        if (distanceInKm < 0) {
            return "Khoảng cách không xác định";
        }
        
        if (distanceInKm < 1) {
            return String.format("%.0f m", distanceInKm * 1000);
        } else {
            return String.format("%.1f km", distanceInKm);
        }
    }
}