import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VideoStoreTest {

    private Customer customer;

    @Before
    public void setUp() {
        customer = new Customer("Fred");
    }

    @Test
    public void whenNewRelease_generateNewReleaseStatement() {
        customer.addRental(new Rental(new Movie("The Cell", PriceCode.newRelease), 3));
        assertEquals("Rental Record for Fred\n\tThe Cell\t9.0\nYou owed 9.0\nYou earned 2 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenNewReleaseChanged_generateRegularStatement() {
        Movie movie = new Movie("The Cell", PriceCode.newRelease);
        movie.setPriceCode(PriceCode.regular);
        customer.addRental(new Rental(movie, 3));
        assertEquals("Rental Record for Fred\n\tThe Cell\t3.5\nYou owed 3.5\nYou earned 1 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenTwoNewReleases_generateTwoLineStatement() {
        customer.addRental(new Rental(new Movie("The Cell", PriceCode.newRelease), 3));
        customer.addRental(new Rental(new Movie("The Tigger Movie", PriceCode.newRelease), 3));
        assertEquals("Rental Record for Fred\n\tThe Cell\t9.0\n\tThe Tigger Movie\t9.0\nYou owed 18.0\nYou earned 4 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenChildrensVideo_generateChildrensStatement() {
        customer.addRental(new Rental(new Movie("The Tigger Movie", PriceCode.childrens), 3));
        assertEquals("Rental Record for Fred\n\tThe Tigger Movie\t1.5\nYou owed 1.5\nYou earned 1 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenLongChildrensRental_chargeFlatRateForFirstThreeDays() {
        customer.addRental(new Rental(new Movie("The Tigger Movie", PriceCode.childrens), 5));
        assertEquals("Rental Record for Fred\n\tThe Tigger Movie\t4.5\nYou owed 4.5\nYou earned 1 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenMultipleRentals_generateLineForEach() {
        customer.addRental(new Rental(new Movie("Plan 9 from Outer Space", PriceCode.regular), 1));
        customer.addRental(new Rental(new Movie("8 1/2", PriceCode.regular), 2));
        customer.addRental(new Rental(new Movie("Eraserhead", PriceCode.regular), 3));

        assertEquals("Rental Record for Fred\n\tPlan 9 from Outer Space\t2.0\n\t8 1/2\t2.0\n\tEraserhead\t3.5\nYou owed 7.5\nYou earned 3 frequent renter points\n", customer.statement());
    }
}