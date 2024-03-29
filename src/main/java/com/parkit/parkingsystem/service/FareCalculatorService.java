package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import java.util.concurrent.TimeUnit;

public class FareCalculatorService {
    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();
        long duration = outTime - inTime;
        long durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        double price;

        if(durationInMinutes < Fare.FREE_FARE_DURATION) {
            price = 0;
        } else {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    price = durationInMinutes * Fare.CAR_RATE_PER_HOUR / 60;
                    break;
                }
                case BIKE: {
                    price = durationInMinutes * Fare.BIKE_RATE_PER_HOUR / 60;
                    break;
                }
                default: throw new IllegalArgumentException("Unknown Parking Type");
            }
            if (discount) {
                price = price * (1 - Fare.DISCOUNT_RATE);
            }
        }
        ticket.setPrice(price);
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}