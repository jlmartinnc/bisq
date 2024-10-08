package bisq.desktop.util.validation;

import bisq.core.locale.Res;
import bisq.core.util.validation.InputValidator.ValidationResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhoneNumberValidatorTest {
    private PhoneNumberValidator validator;
    private ValidationResult validationResult;

    @BeforeEach
    public void setup() {
        Res.setup();
    }

    @Test
    public void testMissingCountryCode() {
        validator = new PhoneNumberValidator();
        validationResult = validator.validate("+12124567890");
        assertFalse(validationResult.isValid, "Should not be valid if validator's country code is missing");
        assertEquals(Res.get("validation.phone.missingCountryCode"), validationResult.errorMessage);
        assertNull(validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testNoInput() {
        validator = new PhoneNumberValidator("AT");
        validationResult = validator.validate("");
        assertFalse(validationResult.isValid, "'' should not be a valid number in AT");
        assertEquals(Res.get("validation.empty"), validationResult.errorMessage);
        assertNull(validator.getNormalizedPhoneNumber());

        validationResult = validator.validate(null);
        assertFalse(validationResult.isValid, "'' should not be a valid number in AT");
        assertEquals(Res.get("validation.empty"), validationResult.errorMessage);
        assertNull(validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testAustriaNumbers() {
        validator = new PhoneNumberValidator("AT");
        assertEquals(validator.getCallingCode(), "43");

        validationResult = validator.validate("(0316) 214 4366");
        assertTrue(validationResult.isValid);
        assertEquals("+4303162144366", validator.getNormalizedPhoneNumber());
        // 1 Vienna
        validationResult = validator.validate("+43 1 214-3512");
        assertTrue(validationResult.isValid);
        assertEquals("+4312143512", validator.getNormalizedPhoneNumber());
        // 1 Vienna cell
        validationResult = validator.validate("+43 1 650 454 0987");
        assertTrue(validationResult.isValid);
        assertEquals("+4316504540987", validator.getNormalizedPhoneNumber());
        // 676 T-Mobile
        validationResult = validator.validate("0676 2241647");
        assertTrue(validationResult.isValid);
        assertEquals("+4306762241647", validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testInvalidAustriaNumbers() {
        validator = new PhoneNumberValidator("AT"); // AT country code is +43
        validationResult = validator.validate("+43 1 214");
        assertFalse(validationResult.isValid, "+43 1 214 should not be a valid number in AT");
        assertEquals(Res.get("validation.phone.insufficientDigits", "+43 1 214"), validationResult.errorMessage);
        assertNull(validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("+42 1 650 454 0987");
        assertFalse(validationResult.isValid, "+42 1 650 454 0987 should not be a valid number in AT");
        assertEquals(Res.get("validation.phone.invalidDialingCode", "+42 1 650 454 0987", "AT", validator.getCallingCode()),
                validationResult.errorMessage);
        assertNull(validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testDominicanRepublicNumbers() {
        validator = new PhoneNumberValidator("DO");
        assertEquals(validator.getCallingCode(), "1");
        validationResult = validator.validate("1829-123 4567");
        assertTrue(validationResult.isValid);
        assertEquals("+18291234567", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("+18091234567");
        assertTrue(validationResult.isValid);
        assertEquals("+18091234567", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("+1 (849) 543-0098");
        assertTrue(validationResult.isValid);
        assertEquals("+18495430098", validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testBrazilNumbers() {
        validator = new PhoneNumberValidator("BR");
        assertEquals(validator.getCallingCode(), "55");
        validationResult = validator.validate("11 3333 5555");
        assertTrue(validationResult.isValid);
        assertEquals("+551133335555", validator.getNormalizedPhoneNumber());
        // Sao Paulo cell
        validationResult = validator.validate("55 11 9 3444 2567");
        assertTrue(validationResult.isValid);
        assertEquals("+5511934442567", validator.getNormalizedPhoneNumber());
        // 85 Fortaleza landline using long-distance carrier OI (31)
        validationResult = validator.validate("031 85 4433 8432");
        assertTrue(validationResult.isValid);
        assertEquals("+550318544338432", validator.getNormalizedPhoneNumber());
    }


    @Test
    public void testCanadaNumbers() {
        validator = new PhoneNumberValidator("CA");
        assertEquals(validator.getCallingCode(), "1");
        validationResult = validator.validate("867 374-8299");
        assertTrue(validationResult.isValid);
        assertEquals("+18673748299", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("+1 306-374-8299   ");
        assertTrue(validationResult.isValid);
        assertEquals("+13063748299", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("   (709) 374-8299");
        assertTrue(validationResult.isValid);
        assertEquals("+17093748299", validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testInvalidCanadaNumber() {
        validator = new PhoneNumberValidator("CA");
        validationResult = validator.validate("+2 1 650 454 0987");
        assertFalse(validationResult.isValid, "+2 1 650 454 0987 should not be a valid number in CA");
        assertEquals(Res.get("validation.phone.invalidDialingCode", "+2 1 650 454 0987", "CA", validator.getCallingCode()),
                validationResult.errorMessage);
        assertNull(validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testChinaNumbers() {
        validator = new PhoneNumberValidator("CN");
        assertEquals(validator.getCallingCode(), "86");
        // In major cities, landline-numbers consist of a two-digit area code followed
        // by an eight-digit inner-number. In other places, landline-numbers consist of
        // a three-digit area code followed by a seven- or eight-digit inner-number.
        // The numbers of mobile phones consist of eleven digits.  Hard to validate.
        // 10 Beijing
        validationResult = validator.validate("   10 4534 8214");
        assertTrue(validationResult.isValid);
        assertEquals("+861045348214", validator.getNormalizedPhoneNumber());
        //  21 Shanghai
        validationResult = validator.validate("+86 (21) 3422-5814");
        assertTrue(validationResult.isValid);
        assertEquals("+862134225814", validator.getNormalizedPhoneNumber());
        // 18x  Mobile phone numbers have 11 digits in the format 1xx-xxxx-xxxx,
        // in which the first three digits (e.g. 13x, 14x,15x,17x and 18x) designate
        // the mobile phone service provider.
        validationResult = validator.validate("180-4353-7877"); // no country code prefix
        assertTrue(validationResult.isValid);
        assertEquals("+8618043537877", validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testJapanNumbers() {
        validator = new PhoneNumberValidator("JP");
        assertEquals(validator.getCallingCode(), "81");
        // 11 Sapporo  011-XXX-XXXX
        validationResult = validator.validate("11 367-2345");
        assertTrue(validationResult.isValid);
        assertEquals("+81113672345", validator.getNormalizedPhoneNumber());

        //  0476 Narita,  0476-XX-XXXX
        validationResult = validator.validate("0476 87 2055");
        assertTrue(validationResult.isValid);
        assertEquals("+810476872055", validator.getNormalizedPhoneNumber());

        //  03 Tokyo  03-XXXX-XXXX
        validationResult = validator.validate("03-3129-5367");
        assertTrue(validationResult.isValid);
        assertEquals("+810331295367", validator.getNormalizedPhoneNumber());

        // 090 Cell number (area code)
        validationResult = validator.validate("(090) 3129-5367");
        assertTrue(validationResult.isValid);
        assertEquals("+8109031295367", validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testRussiaNumbers() {
        validator = new PhoneNumberValidator("RU");
        assertEquals(validator.getCallingCode(), "7");
        // Moscow has four area codes: 495, 496, 498 and 499
        validationResult = validator.validate("499 345-99-36");
        assertTrue(validationResult.isValid);
        assertEquals("+74993459936", validator.getNormalizedPhoneNumber());

        // 812 St. Peter
        validationResult = validator.validate("+7 812 567-22-11");
        assertTrue(validationResult.isValid);
        assertEquals("+78125672211", validator.getNormalizedPhoneNumber());

        // 395 Irkutsk Oblast Call from  outside  RU
        validationResult = validator.validate("+7 395 232-88-35");
        assertTrue(validationResult.isValid);
        assertEquals("+73952328835", validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testUKNumber() {
        validator = new PhoneNumberValidator("GB");
        assertEquals(validator.getCallingCode(), "44");
        validationResult = validator.validate("020 7946 0230");
        assertTrue(validationResult.isValid);
        assertEquals("+4402079460230", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("+ 44 20 79 46 00 93");
        assertTrue(validationResult.isValid);
        assertEquals("+442079460093", validator.getNormalizedPhoneNumber());

        // (0114) Sheffield
        validationResult = validator.validate("(0114) 436 8888");
        assertTrue(validationResult.isValid);
        assertEquals("+4401144368888", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("+44 11 44368888");
        assertTrue(validationResult.isValid);
        assertEquals("+441144368888", validator.getNormalizedPhoneNumber());

        // (0131) xxx xxxx Edinburgh
        validationResult = validator.validate("(0131) 267 1111");
        assertTrue(validationResult.isValid);
        assertEquals("+4401312671111", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("131 267-1111");
        assertTrue(validationResult.isValid);
        assertEquals("+441312671111", validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testUSNumber() {
        validator = new PhoneNumberValidator("US");
        assertEquals(validator.getCallingCode(), "1");
        validationResult = validator.validate("(800) 253 0000");
        assertTrue(validationResult.isValid);
        assertEquals("+18002530000", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("+ 1 800 253-0000");
        assertTrue(validationResult.isValid);
        assertEquals("+18002530000", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("8002530000");
        assertTrue(validationResult.isValid);
        assertEquals("+18002530000", validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testInvalidUSNumbers() {
        validator = new PhoneNumberValidator("US");
        validationResult = validator.validate("+1 512 GR8 0150");
        assertFalse(validationResult.isValid, "+1 512 GR8 0150 should not be a valid number in US");
        assertEquals(Res.get("validation.phone.invalidCharacters", "+1 512 GR8 0150", "US", validator.getCallingCode()),
                validationResult.errorMessage);
        assertNull(validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("+1 212-3456-0150-9832");
        assertFalse(validationResult.isValid, "+1 212-3456-0150-9832 should not be a valid number in US");
        assertEquals(Res.get("validation.phone.tooManyDigits", "+1 212-3456-0150-9832", "US", validator.getCallingCode()),
                validationResult.errorMessage);
        assertNull(validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testUSAreaCodeMatchesCallingCode() {
        // These are not valid US numbers because these area codes
        // do not exist, but validating all area codes on the globe
        // is probably too expensive.  We have to trust end users
        // to input correct area/region codes.
        validator = new PhoneNumberValidator("US");
        assertEquals(validator.getCallingCode(), "1");
        validationResult = validator.validate("1 (1) 253 0000");
        assertFalse(validationResult.isValid);
        assertNull(validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("1-120-253 0000");
        assertTrue(validationResult.isValid);
        assertNull(validationResult.errorMessage);
        assertEquals("+11202530000", validator.getNormalizedPhoneNumber());

        validationResult = validator.validate("(120) 253 0000");
        assertFalse(validationResult.isValid);
        assertNull(validator.getNormalizedPhoneNumber());
    }

    @Test
    public void testPhoneNumberLength() {
        // Construct the validator to be used in these test cases
        validator = new PhoneNumberValidator("US");  // requiredLength=10 to pass these tests

        // Most important, does the validator work for correct phone numbers?
        validationResult = validator.validate("+1 512 888 0150");
        assertTrue(validationResult.isValid, "+1 512 888 0150 should be a valid number in US");
        assertNull(validationResult.errorMessage);
        assertNotNull(validator.getNormalizedPhoneNumber());
        assertEquals(validator.getNormalizedPhoneNumber(), "+15128880150");

        // If no country code provided by user, normalized number should have it added
        validationResult = validator.validate("5128880150");
        assertTrue(validationResult.isValid, "5128880150 should be a valid number in US");
        assertNull(validationResult.errorMessage);
        assertNotNull(validator.getNormalizedPhoneNumber());
        assertEquals(validator.getNormalizedPhoneNumber(), "+15128880150");

        // If no phone number too short, there's a message for that
        validationResult = validator.validate("+15121");
        assertFalse(validationResult.isValid, "+15121 should be too short");
        assertNull(validator.getNormalizedPhoneNumber());
        assertEquals(Res.get("validation.phone.insufficientDigits", "+15121"), validationResult.errorMessage);

        // If no phone number too long, there's a message for that
        validationResult = validator.validate("51288801505128880150");
        assertFalse(validationResult.isValid, "51288801505128880150 should be too long");
        assertNull(validator.getNormalizedPhoneNumber());
        assertEquals(Res.get("validation.phone.tooManyDigits", "51288801505128880150"), validationResult.errorMessage);

        // If phone number not exactly the requiredLength, there's a message for that too
        validationResult = validator.validate("+1 888 123 456");
        assertFalse(validationResult.isValid, "+1 888 123 456 should not be a valid number in US");
        assertNull(validator.getNormalizedPhoneNumber());
        assertEquals(Res.get("validation.phone.incorrectLength", validator.getRequiredLength().get()), validationResult.errorMessage);
    }

    @Test
    public void testVariablePhoneNumberLengthCountry() {
        // Construct the validator to be used in these test cases
        validator = new PhoneNumberValidator("KP");  // requiredLength not defined for this country (North Korea +850)
        assertFalse(validator.getRequiredLength().isPresent());   // If this fails, find another country to test with

        // If phone number requiredLength is not defined, it is considered okay if length in the range 4-12
        validationResult = validator.validate("12345678");
        assertTrue(validationResult.isValid, "12345678 should be a considered a valid number");
        assertNull(validationResult.errorMessage);
        assertNotNull(validator.getNormalizedPhoneNumber());
        assertEquals(validator.getNormalizedPhoneNumber(), "+85012345678");
        
        validationResult = validator.validate("12345678901234");
        assertTrue(validationResult.isValid, "12345678901234 should be a considered a valid number");
        assertNull(validationResult.errorMessage);
        assertNotNull(validator.getNormalizedPhoneNumber());
        assertEquals(validator.getNormalizedPhoneNumber(), "+85012345678901234");
    }

}
