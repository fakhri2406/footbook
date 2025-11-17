package com.footbook.service;

import com.footbook.dto.response.booking.BookingResponse;

import java.util.List;

/**
 * Service interface for booking management operations.
 */
public interface BookingService {
    /**
     * Get all bookings for the current user (upcoming and past)
     * Includes both individual rooms and team rooms
     *
     * @return list of all bookings
     */
    List<BookingResponse> getMyBookings();

    /**
     * Get upcoming bookings for the current user
     *
     * @return list of upcoming bookings
     */
    List<BookingResponse> getMyUpcomingBookings();

    /**
     * Get past bookings for the current user
     *
     * @return list of past bookings
     */
    List<BookingResponse> getMyPastBookings();
}
