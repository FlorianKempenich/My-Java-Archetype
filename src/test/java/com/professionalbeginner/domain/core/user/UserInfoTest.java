package com.professionalbeginner.domain.core.user;

import com.google.common.testing.EqualsTester;
import com.professionalbeginner.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.Assert.fail;

/**
 * @author Kempenich Florian
 */
public class UserInfoTest {

    private LocalDate validDate;
    private String validFirstName;
    private String validLastName;
    private TestUtils testUtils;

    // TODO: 1/19/2017 Add test on date
    // Here: tests if date not in future or like 150 years in past
    // On User: tests if user > 18 yo
    @Before
    public void setUp() throws Exception {
        testUtils = new TestUtils();
        validDate = testUtils.defaultDate();
        validFirstName = "Patrick";
        validLastName = "Smith";
    }

    @Test
    public void testNewInstance() throws Exception {
        LocalDate tooFarInPast = LocalDate.now().minusYears(150);
        LocalDate inFuture = LocalDate.now().plusDays(1);

        assertValid(validFirstName, validLastName, validDate);

        assertInvalid(null, validLastName, validDate);
        assertInvalid(validFirstName, null, validDate);
        assertInvalid(validFirstName, validLastName, null);
        assertInvalid(validFirstName, validLastName, tooFarInPast);
        assertInvalid(validFirstName, validLastName, inFuture);
    }

    private void assertValid(String firstName, String lastName, LocalDate birthdate) {
        new UserInfo(firstName, lastName, birthdate);
    }

    private void assertInvalid(String firstName, String lastName, LocalDate birthdate) {
        try {
            new UserInfo(firstName, lastName, birthdate);
            fail("Should throw exception");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testEquality() throws Exception {
        String anotherFirstName = "Another first name";
        String anotherLastName = "Another last name";
        LocalDate anotherDate = testUtils.defaultDate().plusMonths(1);
        new EqualsTester()
                .addEqualityGroup(new UserInfo(validFirstName, validLastName, validDate), new UserInfo(validFirstName, validLastName, validDate))
                .addEqualityGroup(new UserInfo(anotherFirstName, validLastName, validDate), new UserInfo(anotherFirstName, validLastName, validDate))
                .addEqualityGroup(new UserInfo(anotherFirstName, anotherLastName, anotherDate), new UserInfo(anotherFirstName, anotherLastName, anotherDate))
                .testEquals();
    }

}