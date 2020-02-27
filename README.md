Step One: Deploy the CorDapp locally


The first step is to deploy the CorDapp to nodes running locally.

Navigate to the root directory of the BankVendorSample CorDapp.
To deploy the nodes on Windows run the following command:
gradlew clean deployNodes
To deploy the nodes on Mac or Linux run the following command:
./gradlew clean deployNodes

To best understand the deployment process, there are several perspectives it is helpful to see. On Windows run the following command: 
workflow-java\build\nodes\runnodes
On Mac/Linux run the following command:
workflow-java/build/nodes/runnodes

This command opens four terminal windows: the notary, and a node each for Bank, Vendor, and Account. A notary is a validation service that prevents double-spending, enforces timestamping, and may also validate transactions. For more information on notaries, see the notary documentation.

Note

If any node fails to load just go workflow-java/build/nodes/ and then to the particular node directory then run the below command

java -jar corda.jar

Step Two: Run a CorDapp transaction


Open the terminal window for Bank. From this window, any flows executed will be from the perspective of Bank and the contract would verify with vendor node.

To execute the CreateFlow.java flow, run the following command:

flow start com.sample.bankvendor.flow.CreateFlow$Initiator customerId: vendor23, name: Vendor org, address: Chennai-Tn-In-600024, acNo: 1234567890, idType: Pancard, idNumber: XXXXXXX, phone: 9876543210, email: test23@test.com, vendor: Vendor


To check whether Vendor has received the transaction, open the terminal window showing Vendor’s perspective, and run the following command:

run vaultQuery contractStateType: com.sample.bankvendor.states.KYCState
This command displays all of the IOU states in the node’s vault. States are immutable objects that represent shared facts between the parties. States serve as the inputs and outputs of transactions.

To update the state value - here we need to pass uuid then only it will create the new record with the same uuid or it will be create as a new record with new uuid

flow start UpdateFlow customerId: vendor23, name: Vendor org, address: Chennai-Tn-In-600024, acNo: 1234567890, idType: Pancard, idNumber: xxxxxxxxx, phone: 9876543210, email: test23@test.com, bank: Bank, vendor: Vendor, uuid: b58efdf6-bb56-4eb7-b71d-b0581430fabc


To check flow with observable state

flow start com.sample.bankvendor.flow.CreateFlowWithMultiParty$AccountInitiator acNo: 1234567890, txId: tx12sfsdf, amount: 200.12, txType: Credit, availBalance: 12345.32, customerId: vendor23, dateTime: 2020-02-26, account: Account, vendor: Vendor

This state would be recorded in all the node.To check the data run the below command in any node's terminal

run vaultQuery contractStateType: com.sample.bankvendor.states.AccountStatementState
