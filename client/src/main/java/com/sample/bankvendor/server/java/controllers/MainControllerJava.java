package com.sample.bankvendor.server.java.controllers;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sample.bankvendor.server.java.NodeRPCConnectionJava;
import com.sample.bankvendor.states.KYCState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.messaging.CordaRPCOps;

import net.corda.core.node.services.vault.QueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.*;


/**
 * A CorDapp-agnostic controller that exposes standard endpoints.
 */

@RestController
@RequestMapping("/corda") // The paths for GET and POST requests are relative to this base path.
public class MainControllerJava {
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);
    private final CordaRPCOps proxy;

    public MainControllerJava(NodeRPCConnectionJava rpc) {
        this.proxy = rpc.getProxy();
    }

    @GetMapping(value = "/status", produces = TEXT_PLAIN_VALUE)
    private String status() {
        return "200";
    }

    @GetMapping(value = "/servertime", produces = TEXT_PLAIN_VALUE)
    private String serverTime() {
        return (LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC"))).toString();
    }

    @GetMapping(value = "/peers", produces = TEXT_PLAIN_VALUE)
    private String peers() {
        return proxy.networkMapSnapshot().stream()
                .map(it -> it.getLegalIdentities().toString())
                .collect(Collectors.toList()).toString();
    }

    @GetMapping(value = "/notaries", produces = TEXT_PLAIN_VALUE)
    private String notaries() {
        return proxy.notaryIdentities().toString();
    }

    @GetMapping(value = "/flows", produces = TEXT_PLAIN_VALUE)
    private String flows() {
        return proxy.registeredFlows().toString();
    }

    @GetMapping(value = "/states", produces = TEXT_PLAIN_VALUE)
    private String states() {
        //return proxy.vaultQuery(ContractState.class).getStates().toString();

        return proxy.vaultQuery(KYCState.class).getStates().toString();
    }


    /**
     * Displays all IOU states that exist in the node's vault.
     */

    @GetMapping(value = "/ious", produces = APPLICATION_JSON_VALUE)
    private KYCState getIOUs(HttpServletRequest request) {

        String customerId = request.getParameter("customerId");
        System.out.println(customerId);

        //  List<StateAndRef<KYCState>> res=proxy.vaultQuery(KYCState.class).getStates().get(0);
        // KYCState res1=proxy.vaultQuery(KYCState.class).getStates().get(0).getState().getData();
        // QueryCriteria criteria = QueryCriteria.

        KYCState res1 = proxy.vaultQueryBy(null, null, null, KYCState.class).getStates().get(0).getState().getData();


        return (res1);
    }

//
//
//    @GetMapping(value = [ "ious" ], produces = [ APPLICATION_JSON_VALUE ])
//    fun getIOUs() : ResponseEntity<List<StateAndRef<KYCState>>> {
//        return ResponseEntity.ok(proxy.vaultQueryBy<KYCState>().states)
//    }


}