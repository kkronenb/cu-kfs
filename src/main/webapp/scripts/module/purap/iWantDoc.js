
function loadDeliverToInfoSameAsInitiator(sameAsInitiatorFieldName, requestorNetIDFieldName, deliverToNetIDFieldName, requestorNameFieldName, deliverToNameFieldName,  requestorPhoneNbrFieldName, deliverToPhoneNbrFieldName, requestorEmailFieldName, deliverToEmailFieldName, requestorAddressFieldName, deliverToAddressFieldName ){
	//alert(requestorNetIDFieldName);
	//alert(deliverToNetIDFieldName);
	var sameAsInitiatorChecked;
	
	 var radioButton=(document.getElementsByName("document.sameAsInitiator"))[0];
	    if (radioButton.checked)
	         {
	    	sameAsInitiatorChecked = radioButton.value;
	         }
	
	
		
		if(sameAsInitiatorChecked == 'on'){
		
		var requestorNetID = DWRUtil.getValue( requestorNetIDFieldName ).trim();
		var deliverToNetID = DWRUtil.getValue( deliverToNetIDFieldName ).trim();
		var requestorName = DWRUtil.getValue( requestorNameFieldName ).trim();
		var deliverToName = DWRUtil.getValue( deliverToNameFieldName ).trim();
		var requestorPhoneNbr = DWRUtil.getValue( requestorPhoneNbrFieldName ).trim();
		var requestorEmail = DWRUtil.getValue( requestorEmailFieldName ).trim();
		var requestorAddress = DWRUtil.getValue( requestorAddressFieldName ).trim();


	    setRecipientValue( deliverToNetIDFieldName, requestorNetID );
	    setRecipientValue( deliverToNameFieldName, requestorName );
	    setRecipientValue( deliverToPhoneNbrFieldName, requestorPhoneNbr );
	    setRecipientValue( deliverToEmailFieldName, requestorEmail );
	    setRecipientValue( deliverToAddressFieldName, requestorAddress );
	    }
		else{
			clearRecipients( deliverToNetIDFieldName );
			clearRecipients( deliverToNameFieldName );
	        clearRecipients( deliverToPhoneNbrFieldName );
	        clearRecipients( deliverToEmailFieldName );
	        clearRecipients( deliverToAddressFieldName );
		}
    
}

function loadDeliverToInfo(sameAsInitiatorFieldName, deliverToNetIDFieldName, deliverToNameFieldName, deliverToPhoneNbrFieldName, deliverToEmailFieldName, deliverToAddressFieldName ){

	
	var deliverToNetID = DWRUtil.getValue( deliverToNetIDFieldName ).trim();
	

	 if (deliverToNetID == "") {
		// clearRecipients( deliverToNameFieldName );
         clearRecipients( deliverToPhoneNbrFieldName );
         clearRecipients( deliverToEmailFieldName );
         clearRecipients( deliverToAddressFieldName );
	    } else {
	        var dwrReply = {
	            callback:function(data) {
	                if ( data != null && typeof data == 'object' ) {
	                	
	                    //setRecipientValue( deliverToNameFieldName, data.personName );
	                    setRecipientValue( deliverToPhoneNbrFieldName, data.phoneNumber );
	                    setRecipientValue( deliverToEmailFieldName, data.emailAddress );
	                    
	                    setRecipientValue( deliverToAddressFieldName, data.campusAddress );
	                    
	                } else {
	                	
	                    //clearRecipients( deliverToNameFieldName );
	                    clearRecipients( deliverToPhoneNbrFieldName );
	                    clearRecipients( deliverToEmailFieldName );
	                    clearRecipients( deliverToAddressFieldName );
	                    
	                   // setRecipientValue( deliverToNameFieldName, wrapError( "Person not found" ), true );
	                } },
	            errorHandler:function( errorMessage ) {
	            	//alert(errorMessage);
	            	
	            	//clearRecipients( deliverToNameFieldName );
                    clearRecipients( deliverToPhoneNbrFieldName );
                    clearRecipients( deliverToEmailFieldName );
                    clearRecipients( deliverToAddressFieldName );
                    
	               // setRecipientValue( deliverToNameFieldName, wrapError( "Person not found error" ), true );
	            }
	        };
	        IWantDocumentService.getPersonData( deliverToNetID, dwrReply );
	    }
	}
    

function loadRequestorInfo(sameAsInitiatorFieldName, requestorNetIDFieldName, requestorNameFieldName, requestorPhoneNbrFieldName, requestorEmailFieldName, requestorAddressFieldName ){

	
	var requestorNetID = DWRUtil.getValue( requestorNetIDFieldName ).trim();
	

	 if (requestorNetID == "") {
		 //clearRecipients( requestorNameFieldName );
         clearRecipients( requestorPhoneNbrFieldName );
         clearRecipients( requestorEmailFieldName );
         clearRecipients( requestorAddressFieldName );
	    } else {
	        var dwrReply = {
	            callback:function(data) {
	                if ( data != null && typeof data == 'object' ) {
	                	
	                    //setRecipientValue( requestorNameFieldName, data.personName );
	                    setRecipientValue( requestorPhoneNbrFieldName, data.phoneNumber );
	                    setRecipientValue( requestorEmailFieldName, data.emailAddress );
	                    
	                    //setRecipientValue( requestorAddressFieldName, data.campusAddress );
	                    
	                } else {
	                	
	                   // clearRecipients( requestorNameFieldName );
	                    clearRecipients( requestorPhoneNbrFieldName );
	                    clearRecipients( requestorEmailFieldName );
	                    clearRecipients( requestorAddressFieldName );
	                    
	                   // setRecipientValue( requestorNameFieldName, wrapError( "Person not found" ), true );
	                } },
	            errorHandler:function( errorMessage ) {
	            	
	            //	clearRecipients( requestorNameFieldName );
                    clearRecipients( requestorPhoneNbrFieldName );
                    clearRecipients( requestorEmailFieldName );
                    clearRecipients( requestorAddressFieldName );
                    
	              //  setRecipientValue( requestorNameFieldName, wrapError( "Person not found error" ), true );
	            }
	        };
	        IWantDocumentService.getPersonData( requestorNetID, dwrReply );
	    }
	}

function loadDepartments( form){
	// set the previously selected org
	setRecipientValue( 'previousSelectedOrg', 'document.collegeLevelOrganization' );
	
    submitForm();
}

function loadAccountName(accountNumberFieldName, chartFieldName, accountNameFieldName){
	var accountNumber = DWRUtil.getValue( accountNumberFieldName );
	var chart = DWRUtil.getValue( chartFieldName );
	
	if (accountNumber != '') {
		accountNumber = accountNumber.toUpperCase();
		setRecipientValue(accountNumberFieldName, accountNumber);		
	}
	
	if (accountNumber =='' || chart == '') {
		setRecipientValue(accountNameFieldName, "");
	} else {
		var dwrReply = {
			callback:function(data) {
			if ( data != null && typeof data == 'object' ) {
				setRecipientValue(accountNameFieldName, data.accountName);
			} else {
				setRecipientValue(accountNameFieldName, wrapError( "account not found"), true);			
			} },
			errorHandler:function(errorMessage ) { 
				setRecipientValue(accountNameFieldName, wrapError( "account not found"), true);				
			}
		};
		
		AccountService.getByPrimaryIdWithCaching(chart, accountNumber, dwrReply);
	}
 }

function updateAccountsTotal(totalDollarAmountFieldName, totalAccountsField, itemsNbr, lineNbr) {
	var total = 0;
	var itemsTotal = DWRUtil.getValue(totalDollarAmountFieldName);

	for (i = 0; i < lineNbr; i++) {
		var amountToAdd = 0;
		var useAmountOrPercent = DWRUtil
				.getValue('document.account[' + i + '].useAmountOrPercent');
		var amountOrPercent = DWRUtil.getValue('document.account[' + i + '].amountOrPercent');

		if (amountOrPercent == "" || isNaN(amountOrPercent)) {
			amountOrPercent = 0;
			 
		}
		
		if ('PERCENT' == useAmountOrPercent) {
			amountToAdd = (amountOrPercent * itemsTotal)/100;
		}
		else {
			amountToAdd = amountOrPercent *1;
		}
		
		total += amountToAdd;
	}

	//format
	total = total.toFixed(2);
	total = addCommas(total);
	
	setRecipientValue(totalAccountsField, total);
	
	// Now update the difference between the item and account totals.
	updateItemAndAccountDifference(totalDollarAmountFieldName, totalAccountsField, itemsNbr, lineNbr);
}

function updateItemsTotal(totalDollarAmountFieldName, totalAccountsField, itemsNbr, accountsNbr, totalItemAmountFieldName, itemNbr) {
	
	var total = 0;
	
	updateItemTotal(totalItemAmountFieldName, itemNbr);

	for (i = 0; i < itemsNbr; i++) {
		var quantity = DWRUtil
				.getValue('document.item[' + i + '].itemQuantity');
		var price = DWRUtil.getValue('document.item[' + i + '].itemUnitPrice');

		if (quantity == "" || isNaN(quantity)) {
			quantity = 1;
		}
		if (price == "" || isNaN(price)) {
			price = 0;
		}
		total += price * quantity;
	}
	//format
	total = total.toFixed(2);
	total = addCommas(total);
	
	setRecipientValue(totalDollarAmountFieldName, total);
	
	// now update accounts total
	updateAccountsTotal(totalDollarAmountFieldName, totalAccountsField, itemsNbr, accountsNbr);
}

function updateItemTotal(totalDollarAmountFieldName, itemNbr) {
	var total = 0;

		var quantity = DWRUtil
				.getValue('document.item[' + itemNbr + '].itemQuantity');
		var price = DWRUtil.getValue('document.item[' + itemNbr + '].itemUnitPrice');

		if (quantity == "" || isNaN(quantity)) {
			quantity = 1;
		}
		if (price == "" || isNaN(price)) {
			price = 0;
		}
		total += price * quantity;
	
	//format
	total = total.toFixed(2);
	total = addCommas(total);
	
	setRecipientValue(totalDollarAmountFieldName, total);
}

function updateNewItemTotal() {
	var total = 0;

		var quantity = DWRUtil
				.getValue('newIWantItemLine.itemQuantity');
		var price = DWRUtil.getValue('newIWantItemLine.itemUnitPrice');

		if (quantity == "" || isNaN(quantity)) {
			quantity = 1;
		}
		if (price == "" || isNaN(price)) {
			price = 0;
		}
		total += price * quantity;
	
	//format
	total = total.toFixed(2);
	total = addCommas(total);
	
	setRecipientValue('newIWantItemLine.totalAmount', total);
}

function updateItemAndAccountDifference(totalDollarAmountFieldName, totalAccountsField, itemsNbr, accountsNbr) {
	var total = 0;
	
	// TODO: Is it safe to grab the total fields' formatted values, or is re-computation preferred?
	
	// Add item amounts to total.
	for (i = 0; i < itemsNbr; i++) {
		var quantity = DWRUtil
				.getValue('document.item[' + i + '].itemQuantity');
		var price = DWRUtil.getValue('document.item[' + i + '].itemUnitPrice');

		if (quantity == "" || isNaN(quantity)) {
			quantity = 1;
		}
		if (price == "" || isNaN(price)) {
			price = 0;
		}
		total += price * quantity;
	}
	
	var itemsTotal = total;
	
	// Subtract account amounts from total.
	for (j = 0; j < accountsNbr; j++) {
		var amountToAdd = 0;
		var useAmountOrPercent = DWRUtil
				.getValue('document.account[' + j + '].useAmountOrPercent');
		var amountOrPercent = DWRUtil.getValue('document.account[' + j + '].amountOrPercent');

		if (amountOrPercent == "" || isNaN(amountOrPercent)) {
			amountOrPercent = 0;
			 
		}
		
		if ('PERCENT' == useAmountOrPercent) {
			amountToAdd = (amountOrPercent * itemsTotal)/100;
		}
		else {
			amountToAdd = amountOrPercent *1;
		}
		
		total -= amountToAdd;
	}
	
	//format
	total = total.toFixed(2);
	total = addCommas(total);
	
	setRecipientValue('document.itemAndAccountDifference', total);
}

function addCommas(nStr)
{
	nStr += '';
	x = nStr.split('.');
	x1 = x[0];
	x2 = x.length > 1 ? '.' + x[1] : '';
	var rgx = /(\d+)(\d{3})/;
	while (rgx.test(x1)) {
		x1 = x1.replace(rgx, '$1' + ',' + '$2');
	}
	return x1 + x2;
}




