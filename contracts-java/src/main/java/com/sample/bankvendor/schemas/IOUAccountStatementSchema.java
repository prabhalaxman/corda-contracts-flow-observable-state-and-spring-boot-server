package com.sample.bankvendor.schemas;

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * An IOUState schema.
 */
public class IOUAccountStatementSchema extends MappedSchema {
    public IOUAccountStatementSchema() {
        super(IOUSchema.class, 1, ImmutableList.of(AccountStatementIOU.class));
    }



    @Entity
    @Table(name = "account_states")
    public static class AccountStatementIOU extends PersistentState {
        @Column(name = "bank") private final String bank;
        @Column(name = "account") private final String account;
        @Column(name = "acNo") private final String acNo;
        @Column(name = "txId") private final String txId;
        @Column(name = "amount") private final Double amount;
        @Column(name = "txType") private final String txType;
        @Column(name = "availBalance") private final Double availBalance;
        @Column(name = "vendorId") private final String vendorId;
        @Column(name = "dateTime") private final String dateTime;

        @Column(name = "linear_id") private final UUID linearId;



        public AccountStatementIOU(String bank, String account,
                             String acNo,String txId,Double amount,
                             String txType,Double availBalance,String vendorId,
                             String dateTime, UUID linearId) {
            this.bank = bank;
            this.account = account;
            // this.otherOrg = otherOrg;
            this.acNo = acNo;
            this.txId = txId;
            this.amount = amount;
            this.txType = txType;
            this.availBalance = availBalance;
            this.vendorId = vendorId;
            this.dateTime = dateTime;
            this.linearId = linearId;

        }

        // Default constructor required by hibernate.
        public AccountStatementIOU() {
            this.bank = null;
            this.account = null;
            this.acNo = null;
            this.txId = null;
            this.amount = null;
            this.txType = null;
            this.availBalance = null;
            this.vendorId = null;
            this.dateTime = null;
            this.linearId = null;
        }


        public String getBank() {
            return bank;
        }

        public String getAccount() {
            return account;
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

        public UUID getLinearId() {
            return linearId;
        }
    }
}