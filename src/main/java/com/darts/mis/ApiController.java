package com.darts.mis;

import com.darts.mis.domain.Account;
import com.darts.mis.domain.Subscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by jpc on 31-05-17.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);
    private final DataFacade dataFacade;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ApiController(DataFacade dataFacade){
        this.dataFacade = dataFacade;
    }

    @GetMapping("/check-subscriptions")
    public String checkSubscriptions(){
        for (final Subscription subscription: dataFacade.findAllSubscriptions()){
            subscription.getRevenue();
        }
        return "OK";
    }


    @GetMapping("/revenues/{id}")
    public ObjectNode accountRevenues(@PathVariable("id") final Long id){
        final LocalDate now = LocalDate.now();
        final ObjectNode ret = mapper.createObjectNode();
        final Optional<Account> optionalAccount = dataFacade.findAccountById(id);
        if (optionalAccount.isPresent()) {
            Schedule revenue = optionalAccount.get().getRevenue();
            ret.putPOJO("revenue", revenue);
            ret.putPOJO("total", revenue.accumulatedTo(now));
        }
        return ret;
    }

    @GetMapping("/revenues")
    public ArrayNode revenues(){
        final LocalDate now = LocalDate.now();
        final ArrayNode ret = mapper.createArrayNode();
        for (final Account account: dataFacade.findAllAccounts()){
            final ObjectNode node = ret.addObject();
            System.out.println(account.getId());
            node.put("id", account.getId());
            node.put("name", account.getName());
            final Schedule revenue = account.getRevenue();
            node.put("revenue", revenue.accumulatedTo(now).toString());
        }
        return ret;
    }
}
