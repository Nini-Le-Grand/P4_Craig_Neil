package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {
    private static ParkingService parkingService;
    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        try {
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processIncomingCarTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

    @Test
    public void processIncomingBikeTest() {
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

    @Test
    public void processIncomingCarWithDiscountTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(2);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getNbTicket(anyString());
    }

    @Test
    public void processIncomingVehicleWithNoParkingSpotTest(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(0);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processIncomingVehicleWithWrongVehicleTypeTest(){
        when(inputReaderUtil.readSelection()).thenReturn(3);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processExitingVehicleTest() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processExitingVehicleTestError(){
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }
}
