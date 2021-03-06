package com.darts.mis;

import com.darts.mis.domain.Domain;
import com.darts.mis.model.AccountItem;
import com.darts.mis.model.ForexModel;
import com.darts.mis.model.RevenueModel;
import com.darts.mis.model.SubscriptionItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by jpc on 31-05-17.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);
    private final RevenueModel revenueModel;
    private final ForexModel forexModel;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ApiController(RevenueModel revenueModel, ForexModel forexModel){
        this.revenueModel = revenueModel;
        this.forexModel = forexModel;
    }

    @GetMapping("/rates/{ld}")
    public Position rate(@PathVariable("ld") final String localDateAsString){
        return forexModel.getRate(LocalDate.parse(localDateAsString));
    }

    @GetMapping("/average-rates/{ld1}/{ld2}")
    public Position rate(@PathVariable("ld1") final String incAsString, @PathVariable("ld2") final String excAsString){
        return forexModel.getAverageRate(new LocalDateRange(LocalDate.parse(incAsString), LocalDate.parse(excAsString)));
    }

    @GetMapping("/check-subscriptions")
    public String checkSubscriptions(){
        int count = 0;
        for (final AccountItem accountItem: revenueModel.getAccountItems()){
            for (final SubscriptionItem subscriptionItem: accountItem.getSubscriptionItems()){
                count++;
            }
        }
        return String.format("OK %s subscriptions checked", count);
    }

    @GetMapping("/subscription-revenues/{id}")
    public ObjectNode subscriptionRevenues(@PathVariable("id") final Long id){
        final LocalDate now = LocalDate.now();
        final ObjectNode ret = mapper.createObjectNode();
        revenueModel.findSubscription(id).ifPresent(subscriptionItem -> {
            final Map<Domain, Schedule> revenues = subscriptionItem.getRevenues();
            ret.putPOJO("revenues", revenues);
            final Map<Domain, Position> totals = revenues.keySet().stream().collect(Collectors.toMap(Function.identity(), d -> revenues.get(d).accumulatedTo(now)));
            ret.putPOJO("totals", totals);
        });
        return ret;
    }

    @GetMapping("/account-revenues/{id}")
    public ObjectNode accountRevenues(@PathVariable("id") final Long id){
        final LocalDate now = LocalDate.now();
        final ObjectNode ret = mapper.createObjectNode();
        revenueModel.findAccount(id).ifPresent(accountItem -> {
            final Map<Domain, Schedule> revenues = accountItem.getRevenues();
            ret.putPOJO("revenues", revenues);
            final Map<Domain, Position> totals = revenues.keySet().stream().collect(Collectors.toMap(Function.identity(), d -> revenues.get(d).accumulatedTo(now)));
            ret.putPOJO("totals", totals);
        });
        return ret;
    }
}
