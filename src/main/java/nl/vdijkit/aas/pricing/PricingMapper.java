package nl.vdijkit.aas.pricing;

import io.vertx.core.json.JsonObject;
import nl.vdijkit.aas.domain.ReactiveItem;
import nl.vdijkit.aas.webclient.ResponseItemMapper;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class PricingMapper implements ResponseItemMapper {
    private static final Logger LOGGER = Logger.getLogger(PricingMapper.class);

    public List<ReactiveItem> mapResponse(JsonObject response) {
        LOGGER.infof("prices received: '%s'", response);
        if (null != response) {
            return response.stream()
                    .map(entry -> ReactiveItem.pricing(entry.getKey(), this.mapAmount(entry.getValue())))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Double mapAmount(Object priceToMap) {
        if (priceToMap instanceof Double) {
            return (Double) priceToMap;
        } else if (priceToMap instanceof String) {
            return mapStringAmount((String) priceToMap);
        } else if (priceToMap instanceof Integer) {
            return mapIntegerAmount((Integer) priceToMap);
        }
        LOGGER.warnf("price in not mappable: %s", priceToMap);
        return null;
    }

    private Double mapStringAmount(String priceToMap) {
        try {
            return Double.valueOf(priceToMap);
        } catch (NumberFormatException nfe) {
            LOGGER.warnf("amount as string is not a number: %s", priceToMap);
        }
        return null;
    }

    private Double mapIntegerAmount(Integer priceToMap) {
        try {
            return Double.valueOf(priceToMap);
        } catch (NumberFormatException nfe) {
            LOGGER.warnf("amount as integer is not a number: %s", priceToMap);
        }
        return null;
    }
}
