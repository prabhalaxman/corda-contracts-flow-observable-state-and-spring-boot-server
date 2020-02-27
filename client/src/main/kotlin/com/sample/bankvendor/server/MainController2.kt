package com.sample.bankvendor.server


import com.sample.bankvendor.flow.CreateFlow.Initiator
import com.sample.bankvendor.states.KYCState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest

//val SERVICE_NAMES = listOf("Notary", "Network Map Service")

/**
 *  A Spring Boot Server API controller for interacting with the node via RPC.
 */

@RestController
@RequestMapping("/api/test/") // The paths for GET and POST requests are relative to this base path.
class MainController2(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val proxy = rpc.proxy

    /**
     * Returns the node's name.
     */
    @GetMapping(value = [ "me" ], produces = [ APPLICATION_JSON_VALUE ])
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @GetMapping(value = [ "peers" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    /**
     * Displays all IOU states that exist in the node's vault.
     */
    @GetMapping(value = [ "ious" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getIOUs() : ResponseEntity<List<StateAndRef<KYCState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<KYCState>().states)
    }

    /**
     * Initiates a flow to agree an IOU between two parties.
     *
     * Once the flow finishes it will have written the IOU to ledger. Both the lender and the borrower will be able to
     * see it when calling /spring/api/ious on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

//    @PostMapping(value = [ "create-iou" ], produces = [ TEXT_PLAIN_VALUE ], headers = [ "Content-Type=application/x-www-form-urlencoded" ])
//    fun createIOU(request: HttpServletRequest): ResponseEntity<String> {
//        val iouValue = request.getParameter("iouValue").toInt()
//        val partyName = request.getParameter("partyName")
//        val userId = request.getParameter("userId")
//        val linearId = request.getParameter("linearId")
//        if(partyName == null){
//            return ResponseEntity.badRequest().body("Query parameter 'partyName' must not be null.\n")
//        }
//        if (iouValue <= 0 ) {
//            return ResponseEntity.badRequest().body("Query parameter 'iouValue' must be non-negative.\n")
//        }
//        val partyX500Name = CordaX500Name.parse(partyName)
//        val otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name) ?: return ResponseEntity.badRequest().body("Party named $partyName cannot be found.\n")
//
//        return try {
//            val signedTx = proxy.startTrackedFlow(::Initiator, iouValue, otherParty,userId,linearId).returnValue.getOrThrow()
//            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")
//
//        } catch (ex: Throwable) {
//            logger.error(ex.message, ex)
//            ResponseEntity.badRequest().body(ex.message!!)
//        }
//    }

    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @GetMapping(value = [ "bank-ious" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getBankIOUs(): ResponseEntity<List<StateAndRef<KYCState>>>  {
        val myious = proxy.vaultQueryBy<KYCState>().states.filter { it.state.data.bank.equals(proxy.nodeInfo().legalIdentities.first()) }
        return ResponseEntity.ok(myious)
    }


    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @GetMapping(value = [ "vendor-ious" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getVendorIOUs(): ResponseEntity<List<StateAndRef<KYCState>>>  {
        val myious = proxy.vaultQueryBy<KYCState>().states.filter { it.state.data.vendor.equals(proxy.nodeInfo().legalIdentities.first()) }
        return ResponseEntity.ok(myious)
    }


    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @PostMapping(value = [ "get-data" ], produces = [ APPLICATION_JSON_VALUE ], headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun getData(request: HttpServletRequest): ResponseEntity<List<StateAndRef<KYCState>>>  {
        val customerId = request.getParameter("customerId")
        println(customerId)
        val myious = proxy.vaultQueryBy<KYCState>().states.filter { it.state.data.customerId.equals(customerId) }

       // val myiousMetaData = proxy.vaultQueryBy<IOUState>().statesMetadata;


        return ResponseEntity.ok(myious)
    }

    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @GetMapping(value = [ "get-states-metadata" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getStatesMetaData(request: HttpServletRequest): ResponseEntity<Vault.Page<KYCState>>  {
      //  val userId = request.getParameter("value").toInt()
      //  println(userId)
       // val myious = proxy.vaultQueryBy<IOUState>().statesMetadata.filter { it.state.data.value.toInt().equals(userId) }

        val result = proxy.vaultQueryBy<KYCState>();

        val myiousMetaData = proxy.vaultQueryBy<KYCState>().statesMetadata;


        return ResponseEntity.ok(result)
    }

    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @PostMapping(value = [ "get-history" ], produces = [ APPLICATION_JSON_VALUE ], headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun getHistoryData(request: HttpServletRequest): ResponseEntity<List<StateAndRef<KYCState>>>  {
      //  val external = request.getParameter("externalId");
        val uuid = request.getParameter("uuid");
        println(uuid)

      //  UUID linearId= UUID.fromString(uuid);

        val myious = proxy.vaultQueryBy<KYCState>().states.filter { it.state.data.linearId.id.equals(UUID.fromString(uuid)) }
        return ResponseEntity.ok(myious)
    }


    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @PostMapping(value = [ "get-latest" ], produces = [ APPLICATION_JSON_VALUE ], headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun getLatestData(request: HttpServletRequest): ResponseEntity<StateAndRef<KYCState>>  {
        //  val external = request.getParameter("externalId");
        val uuid = request.getParameter("uuid");
        val externalId = request.getParameter("externalId");
        println(uuid)

        //  UUID linearId= UUID.fromString(uuid);
        val linearId = UniqueIdentifier(externalId,UUID.fromString(uuid))
//UniqueIdentifier linearId1=new UniqueIdentifier(externalId,UUID.fromString(uuid));
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val myious = proxy.vaultQueryBy<KYCState>(queryCriteria).states.last();



       // val myious = proxy.vaultQueryBy<IOUState>().states.filter { it.state.data.linearId.id.equals(UUID.fromString(uuid)) }
        return ResponseEntity.ok(myious)
    }

}
