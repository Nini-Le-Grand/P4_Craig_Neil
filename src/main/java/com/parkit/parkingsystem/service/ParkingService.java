package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class ParkingService {
    private static final Logger logger = LogManager.getLogger("ParkingService");
    private static final FareCalculatorService fareCalculatorService = new FareCalculatorService();
    private final InputReaderUtil inputReaderUtil;
    private final ParkingSpotDAO parkingSpotDAO;
    private final TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    public void processIncomingVehicle() {
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if(parkingSpot !=null && parkingSpot.getId() > 0){
                String vehicleRegNumber = getVehicleRegNumber();
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);
                Date inTime = new Date();
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);
                boolean discount = ticketDAO.getNbTicket(vehicleRegNumber) >= Fare.TICKETS_FOR_DISCOUNT;
                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number:"+parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:"+vehicleRegNumber+" is:"+inTime);
               if (discount) {
                    System.out.println("Happy to see you again ! As a regular user of our parking, you get a "+Fare.DISCOUNT_RATE*100+"% discount");
                }
            }
        }catch(Exception e){
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    private String getVehicleRegNumber(){
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    private ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            System.out.println(parkingNumber);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            } else {
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        }catch(Exception e){
            logger.error("Cannot get next available parking slot", e);
        }
        return parkingSpot;
    }

    public ParkingType getVehicleType(){
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        try{
            int input = inputReaderUtil.readSelection();
            switch(input){
                case 1: {
                    return ParkingType.CAR;
                }
                case 2: {
                    return ParkingType.BIKE;
                }
                default: throw new IllegalArgumentException("Entered input is invalid");
            }
        }catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);
        }
        return null;
    }

    public void processExitingVehicle() {
        try{
            String vehicleRegNumber = getVehicleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            Date outTime = new Date();
            ticket.setOutTime(outTime);
            boolean discount = ticketDAO.getNbTicket(vehicleRegNumber) >= Fare.TICKETS_FOR_DISCOUNT;
            fareCalculatorService.calculateFare(ticket, discount);
            if(ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                System.out.println("Please pay the parking fare:" + ticket.getPrice());
                System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            }else{
                System.out.println("Unable to update ticket information. Error occurred");
            }
        }catch(Exception e){
            logger.error("Unable to process exiting vehicle", e);
        }
    }
}
