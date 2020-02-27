package com.sample.bankvendor.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sample.bankvendor.contracts.AccountContract;

import com.sample.bankvendor.states.AccountStatementState;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CreateFlowWithMultiParty {



    @InitiatingFlow
    @StartableByRPC
    public static class AccountInitiator extends FlowLogic<SignedTransaction> {


        private final String acNo;
        private final String txId;
        private final Double amount;
        private final String txType;
        private final Double availBalance;
        private final String customerId;
        private final String dateTime;
        private final Party vendor;
        private final Party account;

        private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new IOU.");
        private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
        private final ProgressTracker.Step GATHERING_SIGS = new ProgressTracker.Step("Gathering the counterparty's signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
        // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
        // function.
        private final ProgressTracker progressTracker = new ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        );

        public AccountInitiator(String acNo,
                         String txId,
                         Double amount,
                         String txType,
                                Double availBalance,
                         String customerId,
                         String dateTime,
                         Party account,
                         Party vendor) {

            this.acNo = acNo;
            this.txId = txId;
            this.amount = amount;
            this.txType = txType;
            this.availBalance = availBalance;
            this.customerId = customerId;
            this.dateTime = dateTime;
            this.account = account;

            this.vendor = vendor;



        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Obtain a reference to the notary we want to use.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Stage 1.
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            // Generate an unsigned transaction.
            Party me = getOurIdentity();
            // UUID uid=
            UniqueIdentifier linearid = new UniqueIdentifier(customerId);


            // KYCState iouState =null;
//            if (vendor != null) {
//            KYCState iouState = new KYCState(me, vendor, customerId,name,address,acNo,idType,idNumber,phone,email,linearid);




            AccountStatementState iouState = new AccountStatementState(me, account, acNo,txId,amount,txType,availBalance,customerId,dateTime,linearid);
//            } else if (otherOrg != null) {
//                iouState = new KYCState(me, null,otherOrg,  customerId,name,address,acNo,idType,idNumber,phone,email,linearid);
//            }




            // KYCState iouState = new KYCState(me, otherParty, linearid, userId);
//
//  KYCState iouState = new KYCState(iouValue, me, otherParty, new UniqueIdentifier(),userId);
            final Command<AccountContract.Commands.Create> txCommand = new Command<>(
                    new AccountContract.Commands.Create(),
                    ImmutableList.of(iouState.getBank().getOwningKey(), iouState.getAccount().getOwningKey()));
            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(iouState, AccountContract.ID)
                    .addCommand(txCommand);

            // Stage 2.
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            // Verify that the transaction is valid.
            txBuilder.verify(getServiceHub());

            // Stage 3.
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            // Sign the transaction.
            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Stage 4.
            progressTracker.setCurrentStep(GATHERING_SIGS);
            // Send the state to the counterparty, and receive it back with their signature.
//            FlowSession otherPartySession = initiateFlow((vendor != null) ? vendor : otherOrg);
            FlowSession otherPartySession = initiateFlow(account);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, ImmutableSet.of(otherPartySession), CollectSignaturesFlow.Companion.tracker()));

            // Stage 5.
            progressTracker.setCurrentStep(FINALISING_TRANSACTION);
            // Notarise and record the transaction in both parties' vaults.
//            return subFlow(new FinalityFlow(fullySignedTx, ImmutableSet.of(otherPartySession)));


            SignedTransaction res = subFlow(new FinalityFlow(fullySignedTx, ImmutableSet.of(otherPartySession)));

            //return subFlow(FinalityFlow(fullySignedTx, setOf(otherPartySession), FINALISING_TRANSACTION.childProgressTracker()))

             subFlow(new ReportManually(fullySignedTx, vendor));

            return res;

        }
    }

    @InitiatedBy(AccountInitiator.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartySession;

        public Acceptor(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    requireThat(require -> {
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        require.using("This must be an IOU transaction.", output instanceof AccountStatementState);
                        AccountStatementState iou = (AccountStatementState) output;
                        System.out.println("user id " + iou.getVendorId());
                        //  require.using("I won't accept IOUs with a value over 100." + iou.getUserId(), iou.getValue() <= 100);
                        require.using("I won't accept IOUs with a customer id null." + iou.getVendorId(), iou.getVendorId() != null);
                        return null;
                    });
                }
            }
            final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
            final SecureHash txId = subFlow(signTxFlow).getId();

            return subFlow(new ReceiveFinalityFlow(otherPartySession, txId));
        }
    }




}
