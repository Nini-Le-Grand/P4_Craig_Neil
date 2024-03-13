package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingDataBaseIT {
    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ParkingSpot ticketParkingSpot = ticket.getParkingSpot();

        assertEquals(ticketParkingSpot.getId(), 1);
        assertEquals(ticketParkingSpot.getParkingType(), ParkingType.CAR);
        assertFalse(ticketParkingSpot.isAvailable());
        assertEquals(ticket.getVehicleRegNumber(), "ABCDEF");
        assertEquals(ticket.getPrice(), 0);

        assertEquals(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR), 2);
    }

    @Test
    public void testParkingLotExit() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(1);
        mockTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        mockTicket.setVehicleRegNumber("ABCDEF");
        mockTicket.setInTime(new Date(System.currentTimeMillis() - 3605 * 1000));
        ticketDAO.saveTicket(mockTicket);

        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ParkingSpot parkingSpot = ticket.getParkingSpot();

        assertEquals(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR), parkingSpot.getId());
        assertEquals(parkingSpot.getId(), 1);
        assertEquals(parkingSpot.getParkingType(), ParkingType.CAR);
        assertEquals(ticket.getVehicleRegNumber(), "ABCDEF");
        assertEquals(ticket.getPrice(), 1.5);
    }

    @Test
    public void testParkingLotExitWithDiscount(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(1);
        mockTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        mockTicket.setVehicleRegNumber("ABCDEF");
        mockTicket.setInTime(new Date(System.currentTimeMillis() - (1000000 + 3605*1000)));
        mockTicket.setOutTime(new Date(System.currentTimeMillis() - 1000000));
        mockTicket.setPrice(1);
        ticketDAO.saveTicket(mockTicket);

        mockTicket.setInTime(new Date(System.currentTimeMillis() - 3605*1000));
        mockTicket.setOutTime(null);
        mockTicket.setPrice(0);
        ticketDAO.saveTicket(mockTicket);

        parkingService.processExitingVehicle();

        assertEquals(ticketDAO.getTicket("ABCDEF").getPrice(), 1.5*0.95);
    }

}
