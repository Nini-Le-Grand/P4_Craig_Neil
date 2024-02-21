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

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    public void setUpPerTest() throws Exception {
        lenient().when(inputReaderUtil.readSelection()).thenReturn(2);
        lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
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

        assertEquals(ticketParkingSpot.getId(), 4);
        assertEquals(ticketParkingSpot.getParkingType(), ParkingType.BIKE);
        assertFalse(ticketParkingSpot.isAvailable());
        assertEquals(ticket.getVehicleRegNumber(), "ABCDEF");
        assertEquals(ticket.getPrice(), 0);

        assertEquals(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE), 5);

        String pattern = "dd/MM/yyyy HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        assertEquals(simpleDateFormat.format(ticket.getInTime()), simpleDateFormat.format(new Date()));
    }

    @Test
    public void testParkingLotExit() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);


        Ticket mockTicket = new Ticket();
        mockTicket.setId(1);
        mockTicket.setParkingSpot(new ParkingSpot(4, ParkingType.BIKE, false));
        mockTicket.setVehicleRegNumber("ABCDEF");
        mockTicket.setInTime(new Date(System.currentTimeMillis() - 3605 * 1000));
        ticketDAO.saveTicket(mockTicket);

        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ParkingSpot parkingSpot = ticket.getParkingSpot();

        assertEquals(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE), parkingSpot.getId());
        assertEquals(parkingSpot.getId(), 4);
        assertEquals(parkingSpot.getParkingType(), ParkingType.BIKE);
        assertEquals(ticket.getVehicleRegNumber(), "ABCDEF");
        assertEquals(ticket.getPrice(), 1 * 0.95);

        //String pattern = "dd/MM/yyyy HH:mm";
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        //assertEquals(simpleDateFormat.format(ticket.getInTime()), simpleDateFormat.format(new Date()));
    }

    @Test
    public void testParkingLotExitWithDiscount() throws InterruptedException {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(4);
        assertTrue(parkingSpot.isAvailable());
        parkingService.processIncomingVehicle();
        parkingSpot = parkingSpotDAO.getParkingSpot(4);
        assertFalse(parkingSpot.isAvailable());
        sleep(1000);
        parkingService.processExitingVehicle();
        parkingSpot = parkingSpotDAO.getParkingSpot(4);
        assertTrue(parkingSpot.isAvailable());
    }

}
