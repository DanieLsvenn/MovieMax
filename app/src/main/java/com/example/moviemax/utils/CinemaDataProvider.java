package com.example.moviemax.Utils;

import com.example.moviemax.Model.Cinema;
import java.util.ArrayList;
import java.util.List;

public class CinemaDataProvider {
    
    private static final List<Cinema> cinemas = new ArrayList<>();
    
    static {
        // Initialize cinema data with coordinates
        // CGV Vincom Landmark 81 - 772 Điện Biên Phủ, Bình Thạnh, HCM
        cinemas.add(new Cinema(
            1, 
            "CGV Vincom Landmark 81", 
            10.7956586, // Latitude for Landmark 81
            106.7218936, // Longitude for Landmark 81
            "772 Điện Biên Phủ, Bình Thạnh, HCM", 
            "02812345678"
        ));
        
        // Galaxy Nguyễn Du - 116 Nguyễn Du, Q1, HCM
        cinemas.add(new Cinema(
            2, 
            "Galaxy Nguyễn Du", 
            10.7813564, // Latitude for Nguyen Du area
            106.6958097, // Longitude for Nguyen Du area
            "116 Nguyễn Du, Q1, HCM", 
            "02887654321"
        ));
    }
    
    public static List<Cinema> getAllCinemas() {
        return new ArrayList<>(cinemas);
    }
    
    public static Cinema getCinemaByName(String cinemaName) {
        if (cinemaName == null) return null;
        
        for (Cinema cinema : cinemas) {
            if (cinema.getName().equalsIgnoreCase(cinemaName.trim())) {
                return cinema;
            }
        }
        return null;
    }
    
    public static Cinema getCinemaById(int id) {
        for (Cinema cinema : cinemas) {
            if (cinema.getId() == id) {
                return cinema;
            }
        }
        return null;
    }
}