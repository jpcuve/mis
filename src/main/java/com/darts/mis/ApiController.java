package com.darts.mis;

import com.darts.mis.domain.Subscription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by jpc on 31-05-17.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);
    private final DataFacade dataFacade;

    @Autowired
    public ApiController(DataFacade dataFacade){
        this.dataFacade = dataFacade;
    }

    @GetMapping("/subscription-revenues/{id}")
    public Schedule subscriptionRevenues(@PathVariable("id") final Long id){
        final Optional<Subscription> optionalSubscription = dataFacade.findSubscriptionById(id);
        if (optionalSubscription.isPresent()) {
            return optionalSubscription.get().getRevenue();
        }
        return null;
    }

    @GetMapping("/revenues")
    public Schedule revenues(){
        for (final long subscriptionId: dataFacade.findAllSubscriptionIds()){
            final Optional<Subscription> optionalSubscription = dataFacade.findSubscriptionById(subscriptionId);
            if (optionalSubscription.isPresent()) {
                System.out.println(subscriptionId);
                optionalSubscription.get().getRevenue();
            }
        }
        return null;
    }

/*
    @Autowired
    private JobOperator jobOperator;

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/job-names")
    public Set<String> allJobNames(){
        final Set<String> jobNames = jobOperator.getJobNames();
        return jobNames;
    }

    @GetMapping("/job-executions/{jobName}")
    public JsonNode test(@PathVariable("jobName") final String jobName) throws NoSuchJobException, NoSuchJobInstanceException, NoSuchJobExecutionException {
        final ArrayNode jobInstanceArray = mapper.createArrayNode();
        for (long jobInstanceId: jobOperator.getJobInstances(jobName,0, Integer.MAX_VALUE)){
            for (long jobExecutionId: jobOperator.getExecutions(jobInstanceId)){
                final Map<Long, String> map = jobOperator.getStepExecutionSummaries(jobExecutionId);
                final ObjectNode jobExecutionNode = jobInstanceArray.addObject();
                map.forEach((k, v) -> jobExecutionNode.put(Long.toString(k), v));
            }
        }
        return jobInstanceArray;
    }

    @GetMapping("/job-start/{jobName}")
    public JsonNode start(@PathVariable("jobName") final String jobName){
        final ObjectNode node = mapper.createObjectNode();
        final String parameters = String.format("now:%s", System.currentTimeMillis());
        try{
            long jobInstanceId = jobOperator.start(jobName, parameters);
            node.put("started", jobInstanceId);
        } catch (Exception e){
            LOGGER.error("Cannot start job", e);
            node.put("error", e.getMessage());
        }
        return node;
    }
*/
}
