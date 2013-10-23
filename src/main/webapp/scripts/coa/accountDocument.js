/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function onblur_subFundGroup( sfgField, callbackFunction ) {
    var subFundGroup = getElementValue(sfgField.name);

    if (subFundGroup != '') {
		var dwrReply = {
			callback:callbackFunction,
			errorHandler:function( errorMessage ) { 
				window.status = errorMessage;
			}
		};
		SubFundGroupService.getByPrimaryId( subFundGroup, dwrReply );
    }
}

/* Same as onblur_subFundGroup */
function updateSubFundGroup( sfgFieldName, callbackFunction ) {
    var subFundGroup = getElementValue(sfgFieldName);

    if (subFundGroup != '') {
		var dwrReply = {
			callback:callbackFunction,
			errorHandler:function( errorMessage ) { 
				window.status = errorMessage;
			}
		};
		SubFundGroupService.getByPrimaryId( subFundGroup, dwrReply );
    }
}

function onblur_accountRestrictedStatusCode( codeField, callbackFunction ) {
	var subFundGroupFieldName = findElPrefix( codeField.name ) + ".subFundGroupCode";
	updateSubFundGroup( subFundGroupFieldName, callbackFunction );
}

function checkRestrictedStatusCode_Callback( data ) {
	if ( data.accountRestrictedStatusCode != "" ) {
		if ( kualiElements["document.newMaintainableObject.accountRestrictedStatusCode"].type.toLowerCase() != "hidden" ) {
			setElementValue( "document.newMaintainableObject.accountRestrictedStatusCode", data.accountRestrictedStatusCode );
		}
	}
}

function update_laborBenefitRateCategoryCode ( codeField ) {
	var acctTypeCd = codeField.value;
	
	var dwrReply = {
		    callback:function(data) {
		    	if(kualiElements["document.newMaintainableObject.laborBenefitRateCategoryCode"].value == ''){
		    		setElementValue ( "document.newMaintainableObject.laborBenefitRateCategoryCode",data);
		    		alert("Setting the Labor Benefit Rate Category Code to the default described in the system parameter DEFAULT_BENEFIT_RATE_CATEGORY_CODE_BY_ACCOUNT_TYPE");
		    	}
		    },
		    errorHandler:function( errorMessage ) {
		    	window.status = errorMessage;
		    }
		};
	AccountService.getDefaultLaborBenefitRateCategoryCodeForAccountType(acctTypeCd, dwrReply);
}