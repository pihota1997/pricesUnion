package service;

import model.Price;

import java.util.List;

public interface PriceService {

    List<Price> updatePrices(List<Price> oldPrices, List<Price> newPrices);
}
