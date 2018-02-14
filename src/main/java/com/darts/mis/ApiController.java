package com.darts.mis;

import com.darts.mis.domain.Domain;
import com.darts.mis.model.AccountItem;
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
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ApiController(RevenueModel revenueModel){
        this.revenueModel = revenueModel;
    }

    @GetMapping("/check-subscriptions")
    public String checkSubscriptions(){
        for (final AccountItem accountItem: revenueModel.getAccountItems()){
            for (final SubscriptionItem subscriptionItem: accountItem.getSubscriptionItems()){
                LOGGER.debug("Checking subscription: " + subscriptionItem.getSubscription().getId());
            }
        }
        return "OK";
    }

    @GetMapping("/subscription-revenues/{id}")
    public ObjectNode subscriptionRevenues(@PathVariable("id") final Long id){
        final LocalDate now = LocalDate.now();
        final ObjectNode ret = mapper.createObjectNode();
        final Optional<SubscriptionItem> optionalSubscriptionItem = revenueModel.findSubscription(id);
        if (optionalSubscriptionItem.isPresent()) {
            final SubscriptionItem subscriptionItem = optionalSubscriptionItem.get();
            final Map<Domain, Schedule> revenues = subscriptionItem.getRevenues();
            ret.putPOJO("revenues", revenues);
            final Map<Domain, Position> totals = revenues.keySet().stream().collect(Collectors.toMap(Function.identity(), d -> revenues.get(d).accumulatedTo(now)));
            ret.putPOJO("totals", totals);
        }
        return ret;
    }

    @GetMapping("/account-revenues/{id}")
    public ObjectNode accountRevenues(@PathVariable("id") final Long id){
        final LocalDate now = LocalDate.now();
        final ObjectNode ret = mapper.createObjectNode();
        final Optional<AccountItem> optionalAccountItem = revenueModel.findAccount(id);
        if (optionalAccountItem.isPresent()) {
            final AccountItem accountItem = optionalAccountItem.get();
            final Map<Domain, Schedule> revenues = accountItem.getRevenues();
            ret.putPOJO("revenues", revenues);
            final Map<Domain, Position> totals = revenues.keySet().stream().collect(Collectors.toMap(Function.identity(), d -> revenues.get(d).accumulatedTo(now)));
            ret.putPOJO("totals", totals);
        }
        return ret;
    }
}
