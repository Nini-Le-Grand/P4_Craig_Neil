package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class FareCalculatorService {
    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();
        long duration = outTime - inTime;
        long durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);

        if(durationInMinutes < 30) {
            ticket.setPrice(0);
        } else {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    ticket.setPrice(durationInMinutes * Fare.CAR_RATE_PER_HOUR / 60);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(durationInMinutes * Fare.BIKE_RATE_PER_HOUR / 60);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }
}