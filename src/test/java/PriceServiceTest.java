import model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.PriceService;
import service.impl.PriceServiceImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PriceServiceTest {

    private final PriceService underTest = new PriceServiceImpl();
    private Price oldPrice;

    @BeforeEach
    void setUp() {
        oldPrice = new Price(1, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-05T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-01-31T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                80);
    }

    @Test
    void shouldSuccessUnionPriceWhenDifferentProducts() {
        //given
        Price newPrice = new Price(2, "product2", 1, 1,
                new Date(LocalDateTime.parse("2013-01-05T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-01-31T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                80);

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of(newPrice));

        //then
        assertTrue(resultList.contains(oldPrice));
        assertTrue(resultList.contains(newPrice));
        assertEquals(2, resultList.size());
    }

    @Test
    void shouldSuccessUnionPriceIfDifferentNumbers() {
        //given
        Price newPrice = new Price(2, "product1", 2, 1,
                new Date(LocalDateTime.parse("2013-01-05T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-01-31T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                80);

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of(newPrice));

        //then
        assertTrue(resultList.contains(oldPrice));
        assertTrue(resultList.contains(newPrice));
        assertEquals(2, resultList.size());
    }

    @Test
    void shouldSuccessUnionPriceIfDifferentDepartment() {
        //given
        Price newPrice = new Price(2, "product1", 1, 2,
                new Date(LocalDateTime.parse("2013-01-05T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-01-31T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                80);

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of(newPrice));

        //then
        assertTrue(resultList.contains(oldPrice));
        assertTrue(resultList.contains(newPrice));
        assertEquals(2, resultList.size());
    }

    /*Old |-----|
     * New          |-----|*/
    @Test
    void shouldSuccessUnionPriceIfDatesNoIntersect() {
        //given
        Price newPrice = new Price(2, "product1", 1, 1,
                new java.util.Date(LocalDateTime.parse("2013-03-01T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-03-31T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                80);

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of(newPrice));

        //then
        assertTrue(resultList.contains(oldPrice));
        assertTrue(resultList.contains(newPrice));
        assertEquals(2, resultList.size());
    }

    /*Old |-----|
            80
      New     |-----|
                80      */
    @Test
    void shouldSuccessUnionPriceIfDatesIntersectAndOldPriceEndEarlierAndValuesEquals() {
        //given
        Price newPrice = new Price(2, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-02-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                80);
        Price expectedPrice = new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(), oldPrice.getDepart(),
                oldPrice.getBegin(), newPrice.getEnd(), oldPrice.getValue());

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of(newPrice));

        //then
        assertEqualsPrices(expectedPrice, resultList.get(0));
        assertEquals(1, resultList.size());
    }

    /*Old |-----|
            80
      New     |-----|
                90      */
    @Test
    void shouldSuccessUnionPriceIfDatesIntersectAndOldPriceEndEarlierAndValuesDifferent() {
        //given
        Price newPrice = new Price(2, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-02-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                90);
        Price expectedPrice1 = new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(), oldPrice.getDepart(),
                oldPrice.getBegin(), newPrice.getBegin(), oldPrice.getValue());

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of(newPrice));

        //then
        assertEqualsPrices(expectedPrice1, resultList.get(0));
        assertEqualsPrices(newPrice, resultList.get(1));
        assertEquals(2, resultList.size());
    }

    /*Old |------------|
                80
      New     |-----|
                90      */
    @Test
    void shouldSuccessUnionPriceIfDatesIntersectAndOldPriceEndAfterNewPriceEndAndValuesDifferent() {
        //given
        Price newPrice = new Price(2, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-10T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-01-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                90);
        Price expectedPrice2 = new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(), oldPrice.getDepart(),
                oldPrice.getBegin(), newPrice.getBegin(), oldPrice.getValue());
        Price expectedPrice3 = new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(), oldPrice.getDepart(),
                newPrice.getEnd(), oldPrice.getEnd(), oldPrice.getValue());

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of(newPrice));

        //then
        assertEqualsPrices(expectedPrice2, resultList.get(0));
        assertEqualsPrices(expectedPrice3, resultList.get(1));
        assertEqualsPrices(newPrice, resultList.get(2));
        assertEquals(3, resultList.size());
    }

    /*Old    |-----|
               80
      New  |---------|
                90      */
    @Test
    void shouldSuccessUnionPriceIfDatesIntersectAndNewPriceOverlapsAndValuesDifferent() {
        //given
        Price newPrice = new Price(2, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-01T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-02-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                90);

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of(newPrice));

        //then
        assertEquals(newPrice, resultList.get(0));
        assertEquals(1, resultList.size());
    }


    /*Old |-----------------|
                   80
      New   |-----| |-----|
              90       75        */
    @Test
    void shouldSuccessUnionPriceIfDatesIntersectAndOldPriceEndAfterTwoNewPricesEndAndValuesDifferent() {
        //given
        Price newPrice1 = new Price(2, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-10T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-01-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                90);

        Price newPrice2 = new Price(3, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-20T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-01-25T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                75);

        Price expectedPrice1 = new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(), oldPrice.getDepart(),
                oldPrice.getBegin(), newPrice1.getBegin(), oldPrice.getValue());
        Price expectedPrice2 = new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(), oldPrice.getDepart(),
                newPrice1.getEnd(), newPrice2.getBegin(), oldPrice.getValue());
        Price expectedPrice3 = new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(), oldPrice.getDepart(),
                newPrice2.getEnd(), oldPrice.getEnd(), oldPrice.getValue());

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of(newPrice1, newPrice2));

        //then
        assertEqualsPrices(expectedPrice1, resultList.get(0));
        assertEqualsPrices(expectedPrice2, resultList.get(1));
        assertEqualsPrices(expectedPrice3, resultList.get(2));
        assertEqualsPrices(newPrice1, resultList.get(3));
        assertEqualsPrices(newPrice2, resultList.get(4));
        assertEquals(5, resultList.size());
    }

    /*Old |------|  |-----|
             80       85
     New        |-----|
                  90        */
    @Test
    void shouldSuccessUnionPriceIfNewPriceDatesIntersectTwoOldPricesDatesAndValuesDifferent() {
        //given
        Price oldPrice2 = new Price(2, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-02-28T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                85);

        Price newPrice = new Price(3, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-10T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-02-20T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                90);

        Price expectedPrice1 = new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(), oldPrice.getDepart(),
                oldPrice.getBegin(), newPrice.getBegin(), oldPrice.getValue());
        Price expectedPrice2 = new Price(oldPrice2.getId(), oldPrice2.getProductCode(), oldPrice2.getNumber(), oldPrice2.getDepart(),
                newPrice.getEnd(), oldPrice2.getEnd(), oldPrice2.getValue());

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice, oldPrice2), List.of(newPrice));

        //then
        assertEqualsPrices(expectedPrice1, resultList.get(0));
        assertEqualsPrices(expectedPrice2, resultList.get(1));
        assertEqualsPrices(newPrice, resultList.get(2));
        assertEquals(3, resultList.size());
    }

    /*Old |------|       |-----|
             80             85
      New      |-----|
                 80              */
    @Test
    void shouldSuccessUnionPriceIfNewDateIntersectFirstOldPricesDatesAndValuesEqualsAndNewDateNoIntersectSecondOldPrice() {
        //given
        Price oldPrice2 = new Price(2, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-02-15T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-02-28T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                85);

        Price newPrice = new Price(3, "product1", 1, 1,
                new Date(LocalDateTime.parse("2013-01-10T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-02-10T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                80);

        Price expectedPrice = new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(), oldPrice.getDepart(),
                oldPrice.getBegin(), newPrice.getEnd(), oldPrice.getValue());

        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice, oldPrice2), List.of(newPrice));

        //then
        assertEqualsPrices(oldPrice2, resultList.get(0));
        assertEqualsPrices(expectedPrice, resultList.get(1));
        assertEquals(2, resultList.size());
    }

    @Test
    void shouldSuccessUnionPriceIfOldPricesListIsEmpty() {
        //given
        Price newPrice = new Price(2, "product2", 1, 1,
                new Date(LocalDateTime.parse("2013-01-05T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Date(LocalDateTime.parse("2013-01-31T00:00:00").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                80);

        //when
        List<Price> resultList = underTest.updatePrices(List.of(), List.of(newPrice));

        //then
        assertEqualsPrices(newPrice, resultList.get(0));
        assertEquals(1, resultList.size());
    }

    @Test
    void shouldSuccessUnionPriceIfNewPricesListIsEmpty() {
        //when
        List<Price> resultList = underTest.updatePrices(List.of(oldPrice), List.of());

        //then
        assertEqualsPrices(oldPrice, resultList.get(0));
        assertEquals(1, resultList.size());
    }

    private void assertEqualsPrices(Price expected, Price actual) {
        assertEquals(expected.getProductCode(), actual.getProductCode());
        assertEquals(expected.getNumber(), actual.getNumber());
        assertEquals(expected.getDepart(), actual.getDepart());
        assertEquals(expected.getBegin(), actual.getBegin());
        assertEquals(expected.getEnd(), actual.getEnd());
        assertEquals(expected.getValue(), actual.getValue());
    }
}
