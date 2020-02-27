package com.sample.bankvendor.states;


import com.google.common.collect.ImmutableList;
import com.sample.bankvendor.contracts.AccountContract;


import com.sample.bankvendor.schemas.IOUAccountStatementSchema;
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
 * <p>
 * A state must implement [ContractState] or one of its descendants.
 */
@BelongsToContract(AccountContract.class)
public class AccountStatementState implements LinearState, QueryableState {
    private final String acNo;
    private final String txId;
    private final Double amount;
    private final String txType;
    private final Double availBalance;
    private final String vendorId;
    private final String dateTime;
    private final Party bank;
    private final Party account;
    // private final Party otherOrg;

    private final UniqueIdentifier linearId;


    public AccountStatementState(
            Party bank,
            Party account,
            String acNo,
            String txId,
            Double amount,
            String txType,
            Double availBalance,
            String vendorId,
            String dateTime,

            UniqueIdentifier linearId) {
        this.acNo = acNo;
        this.txId = txId;
        this.amount = amount;
        this.txType = txType;
        this.availBalance = availBalance;
        this.vendorId = vendorId;
        this.dateTime = dateTime;
        this.bank = bank;
        this.account = account;
        // this.otherOrg = otherOrg;
        this.linearId = linearId;

    }


    public String getAcNo() {
        return acNo;
    }

    public String getTxId() {
        return txId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getTxType() {
        return txType;
    }

    public Double getAvailBalance() {
        return availBalance;
    }

    public String getVendorId() {
        return vendorId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Party getBank() {
        return bank;
    }

    public Party getAccount() {
        return account;
    }

    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(bank, account);
    }



    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof IOUAccountStatementSchema) {
            return new IOUAccountStatementSchema.AccountStatementIOU(
                    this.bank.getName().toString(),
                    this.account.getName().toString(),

                    this.acNo,
                    this.txId,
                    this.amount,

                    this.txType,
                    this.availBalance,
                    this.vendorId,
                    this.dateTime,
                    this.linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new IOUAccountStatementSchema());
    }

    @Override
    public String toString() {
        return String.format("IOUState(value=%s, lender=%s, borrower=%s, linearId=%s, name=%s)", acNo, bank, txId, linearId, txType);
    }
}