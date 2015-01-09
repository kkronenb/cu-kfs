package edu.cornell.kfs.sys.web.struts;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.web.struts.KualiBatchFileAdminAction;
import org.kuali.kfs.sys.web.struts.KualiBatchFileAdminForm;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.sys.batch.CreateDoneBatchFile;

public class CreateDoneKualiBatchFileAdminAction extends
		KualiBatchFileAdminAction {
	
    public ActionForward createDone(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiBatchFileAdminForm fileAdminForm = (KualiBatchFileAdminForm) form;
        String filePath = BatchFileUtils.resolvePathToAbsolutePath(fileAdminForm.getFilePath());
        createDoneFile(filePath);
        return new ActionForward(getBackUrl(fileAdminForm), true);
    }

    protected String getBackUrl(KualiBatchFileAdminForm form) {
    	String basePath = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY);
        return "kr/lookup.do" + "?methodToCall=start&docFormKey=88888888&businessObjectClassName=" + CreateDoneBatchFile.class.getName() + "&docFormKey=88888888&returnLocation=" +basePath +"/portal.do&hideReturnLink=true";
    }
    
    /**
     * Creates a '.done' file with the name of the original file.
     */
    protected void createDoneFile(String filename) {
        String doneFileName =  StringUtils.substringBeforeLast(filename,".") + ".done";
        File doneFile = new File(doneFileName);

        if (!doneFile.exists()) {
            boolean doneFileCreated = false;
            try {
                doneFileCreated = doneFile.createNewFile();
            }
            catch (IOException e) {
                throw new RuntimeException("Errors encountered while saving the file: Unable to create .done file " + doneFileName, e);
            }

            if (!doneFileCreated) {
                throw new RuntimeException("Errors encountered while saving the file: Unable to create .done file " + doneFileName);
            }
        }
    }

}
