package ec.com.technoloqie.ai.tecaiws.service;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import ec.com.technoloqie.ai.tecaiws.commons.exception.BookingCannotBeCancelledException;
import ec.com.technoloqie.ai.tecaiws.commons.exception.BookingNotFoundException;
import ec.com.technoloqie.ai.tecaiws.model.Booking;
import ec.com.technoloqie.ai.tecaiws.model.Customer;

@Component
public class BookingServiceImpl {
	
	public Booking getBookingDetails(String bookingNumber, String customerName, String customerSurname) {
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating retrieval from DB
        LocalDate bookingFrom = LocalDate.now().plusDays(1);
        LocalDate bookingTo = LocalDate.now().plusDays(3);
        Customer customer = new Customer(customerName, customerSurname);
        return new Booking(bookingNumber, bookingFrom, bookingTo, customer);
    }

    public void cancelBooking(String bookingNumber, String customerName, String customerSurname) {
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating cancellation
        throw new BookingCannotBeCancelledException(bookingNumber);
    }

    private void ensureExists(String bookingNumber, String customerName, String customerSurname) {
        // Imitating check
        if (!(bookingNumber.equals("123-456")
                && customerName.equals("Cristina")
                && customerSurname.equals("Smith"))) {
            throw new BookingNotFoundException(bookingNumber);
        }
    }
}
