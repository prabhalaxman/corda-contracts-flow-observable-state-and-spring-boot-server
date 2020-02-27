package com.sample.bankvendor.states;


import com.google.common.collect.ImmutableList;
import com.sample.bankvendor.contracts.IOUContract;
import com.sample.bankvendor.schemas.IOUSchemaV1;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;

import java.util.Arrays;
import java.util.List;

/**
 * The state object recording IOU agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 */
@BelongsToContract(IOUContract.class)
public class KYCState implements LinearState, QueryableState {
    private final String customerId;
    private final String name;
    private final String address;
    private final String acNo;
    private final String idType;
    private final String idNumber;
    private final String phone;
    private final String email;
    private final Party bank;
    private final Party vendor;
   // private final Party otherOrg;

    private final UniqueIdentifier linearId;


    public KYCState(
                    Party bank,
                    Party vendor,


                    String customerId,
                    String name,
                    String address,
                    String acNo,
                    String idType,
                    String idNumber,
                    String phone,
                    String email,

                    UniqueIdentifier linearId)
    {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.acNo = acNo;
        this.idType = idType;
        this.idNumber = idNumber;
        this.phone = phone;
        this.email = email;
        this.bank = bank;
        this.vendor = vendor;
       // this.otherOrg = otherOrg;
        this.linearId = linearId;

    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getAcNo() {
        return acNo;
    }

    public String getIdType() {
        return idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public Party getBank() {
        return bank;
    }

    public Party getVendor() {
        return vendor;
    }

//    public Party getOtherOrg() {
//        return otherOrg;
//    }

    @Override public UniqueIdentifier getLinearId() { return linearId; }
    @Override public List<AbstractParty> getParticipants() {
        return Arrays.asList(bank, vendor);
    }

    @Override public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof IOUSchemaV1) {
            return new IOUSchemaV1.PersistentIOU(
                    this.bank.getName().toString(),
                    this.vendor.getName().toString(),
                   // this.otherOrg.getName().toString(),
                    this.customerId,
                    this.name,
                    this.address,
                    this.acNo,
                    this.idType,
                    this.idNumber,
                    this.phone,
                    this.email,
                    this.linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @Override public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new IOUSchemaV1());
    }

    @Override
    public String toString() {
        return String.format("IOUState(value=%s, lender=%s, borrower=%s, linearId=%s, name=%s)", customerId, bank, vendor, linearId,name);
    }
}