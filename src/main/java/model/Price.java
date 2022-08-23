package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Price {

    private long id;
    private String productCode;
    private int number;
    private int depart;
    private Date begin;
    private Date end;
    private long value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return number == price.number && depart == price.depart && productCode.equals(price.productCode)
                && end.after(price.getBegin()) && begin.before(price.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(productCode, number, depart);
    }
}
