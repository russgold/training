import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VideoStoreTest {

    private Customer customer;

    @Before
    public void setUp() {
        customer = new Customer("Fred");
    }

    @Test
    public void whenOneDayNewRelease_chargeThreeDollarsAndAwardOnePoint() {
        customer.addRental(new Rental(new Movie("The Cell", Movie.NEW_RELEASE), 1));
        assertEquals("Rental Record for Fred\n\tThe Cell\t3.0\nYou owed 3.0\nYou earned 1 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenLongerNewRelease_chargeThreeDollarsPerDayAndAwardTwoPoints() {
        customer.addRental(new Rental(new Movie("The Cell", Movie.NEW_RELEASE), 3));
        assertEquals("Rental Record for Fred\n\tThe Cell\t9.0\nYou owed 9.0\nYou earned 2 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenShortRegularRental_chargeFlatRate() {
        customer.addRental(new Rental(new Movie("Plan 9 from Outer Space", Movie.REGULAR), 2));
        assertEquals("Rental Record for Fred\n\tPlan 9 from Outer Space\t2.0\nYou owed 2.0\nYou earned 1 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenLongRegularRental_chargeFlatRateForFirstTwoDaysThenPerDayRate() {
        customer.addRental(new Rental(new Movie("Plan 9 from Outer Space", Movie.REGULAR), 5));
        assertEquals("Rental Record for Fred\n\tPlan 9 from Outer Space\t6.5\nYou owed 6.5\nYou earned 1 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenShortChildrensRental_chargeFlatRate() {
        customer.addRental(new Rental(new Movie("The Tigger Movie", Movie.CHILDRENS), 3));
        assertEquals("Rental Record for Fred\n\tThe Tigger Movie\t1.5\nYou owed 1.5\nYou earned 1 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenLongChildrensRental_chargeFlatRateForFirstThreeDaysThenPerDayRate() {
        customer.addRental(new Rental(new Movie("The Tigger Movie", Movie.CHILDRENS), 5));
        assertEquals("Rental Record for Fred\n\tThe Tigger Movie\t4.5\nYou owed 4.5\nYou earned 1 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenNewReleaseChangedToRegular_generateRegularStatement() {
        Movie movie = new Movie("The Cell", Movie.NEW_RELEASE);
        movie.setPriceCode(Movie.REGULAR);
        customer.addRental(new Rental(movie, 3));
        assertEquals("Rental Record for Fred\n\tThe Cell\t3.5\nYou owed 3.5\nYou earned 1 frequent renter points\n", customer.statement());
    }

    @Test
    public void whenMultipleRentals_generateLineForEach() {
        customer.addRental(new Rental(new Movie("Plan 9 from Outer Space", Movie.REGULAR), 1));
        customer.addRental(new Rental(new Movie("8 1/2", Movie.REGULAR), 2));
        customer.addRental(new Rental(new Movie("Eraserhead", Movie.REGULAR), 3));

        assertEquals("Rental Record for Fred\n\tPlan 9 from Outer Space\t2.0\n\t8 1/2\t2.0\n\tEraserhead\t3.5\nYou owed 7.5\nYou earned 3 frequent renter points\n", customer.statement());
    }

    // Requirement 1: Add support for generator a statement in HTML

    @Test @Ignore
    public void whenHtmlStatement_generateHtml() {
        customer.addRental(new Rental(new Movie("Plan 9 from Outer Space", Movie.REGULAR), 1));
        customer.addRental(new Rental(new Movie("8 1/2", Movie.REGULAR), 2));
        customer.addRental(new Rental(new Movie("Eraserhead", Movie.REGULAR), 3));

        assertEquals("<html><body><p>Rental Record for Fred</p><p><table>" +
                     "<tr><td>Plan 9 from Outer Space</td><td>2.0</td></tr>" +
                     "<tr><td>8 1/2</td><td>2.0</td></tr>" +
                     "<tr><td>Eraserhead</td><td>3.5</td></tr>" +
                     "</table></p><p>You owed 7.5. You earned 3 frequent renter points</p></body></html>",
                /* htmlstatement */ customer.statement());
    }

    // Requirement 2: Add support for classic movies. Rental charge is $1 for the first 3 days, and $1 per day thereafter.
    //                Award one frequent renter point for the entire rental

    @Test @Ignore
    public void whenShortClassicRental_chargeFlatRate() {
        customer.addRental(new Rental(new Movie("Gone With the Wind", Movie.NEW_RELEASE /* CLASSIC */ ), 3));
        assertEquals("Rental Record for Fred\n\tGone With the Wind\t1.0\nYou owed 1.0\nYou earned 1 frequent renter points\n",
                customer.statement());
    }

    @Test @Ignore
    public void whenLongClassicRental_chargeFlatRateForFirstThreeDaysThenPerDayRate() {
        customer.addRental(new Rental(new Movie("Gone With the Wind", Movie.NEW_RELEASE /* CLASSIC */ ), 5));
        assertEquals("Rental Record for Fred\n\tGone With the Wind\t3.0\nYou owed 3.0\nYou earned 1 frequent renter points\n",
                customer.statement());
    }
}