/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.kfs.module.purap.batch;

import java.util.Date;

import org.kuali.kfs.module.purap.service.ElectronicInvoiceHelperService;
import org.kuali.kfs.sys.batch.AbstractStep;

public class ElectronicInvoiceRouteStep extends AbstractStep {
    
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ElectronicInvoiceRouteStep.class);

    private ElectronicInvoiceHelperService electronicInvoiceHelperService;

    public boolean execute(String jobName, Date jobRunDate) {
    	try {
    		Thread.sleep(60000);
    	} catch (Exception e) {}
        return electronicInvoiceHelperService.routeDocuments();
       
    }

    public void setElectronicInvoiceHelperService(ElectronicInvoiceHelperService electronicInvoiceHelperService) {
        this.electronicInvoiceHelperService = electronicInvoiceHelperService;
    }

}