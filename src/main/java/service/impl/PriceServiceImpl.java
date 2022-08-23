package service.impl;

import model.Price;
import service.PriceService;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PriceServiceImpl implements PriceService {

    @Override
    public List<Price> updatePrices(List<Price> oldPrices, List<Price> newPrices) {

        List<Price> result = new LinkedList<>();

        oldPrices = sort(oldPrices);
        newPrices = sort(newPrices);

        result.addAll(handleDisjointPrices(oldPrices, newPrices));

        result.addAll(handleIntersectPrices(oldPrices, newPrices));

        result.addAll(newPrices);

        return result;
    }

    private List<Price> sort(List<Price> prices) {
        Comparator<Price> comparator = Comparator.comparing(Price::getProductCode).thenComparing(Price::getNumber)
                .thenComparing(Price::getDepart).thenComparing(Price::getBegin);
        return prices.stream().sorted(comparator).collect(Collectors.toList());
    }

    private List<Price> handleDisjointPrices(List<Price> oldPrices, List<Price> newPrices) {

        List<Price> result = oldPrices.stream()
                .filter(price -> !newPrices.contains(price))
                .collect(Collectors.toList());

        result.addAll(newPrices.stream()
                .filter(price -> !oldPrices.contains(price))
                .collect(Collectors.toList()));

        oldPrices.removeAll(result);
        newPrices.removeAll(result);

        return result;
    }

    private List<Price> handleIntersectPrices(List<Price> oldPrices, List<Price> newPrices) {

        List<Price> result = new LinkedList<>();

        for (Price oldPrice : oldPrices) {
            for (int i = 0; i < newPrices.size(); i++) {

                Price newPrice = newPrices.get(i);

                if (oldPrice.equals(newPrice)) {
                    if (oldPrice.getValue() == newPrice.getValue()) {

                        newPrice.setId(oldPrice.getId());
                        newPrice.setBegin(oldPrice.getBegin());
                        oldPrice.setEnd(newPrice.getEnd());

                    } else if (oldPrice.getBegin().before(newPrice.getBegin())) {

                        result.add(new Price(oldPrice.getId(), oldPrice.getProductCode(), oldPrice.getNumber(),
                                oldPrice.getDepart(), oldPrice.getBegin(), newPrice.getBegin(), oldPrice.getValue()));
                    }

                    if (oldPrice.getEnd().after(newPrice.getEnd())) {
                        oldPrice.setBegin(newPrice.getEnd());

                        if (i == newPrices.size() - 1 || !newPrices.get(i + 1).equals(oldPrice)) {
                            result.add(oldPrice);
                        }
                    }
                }
            }
        }

        return result;
    }
}
