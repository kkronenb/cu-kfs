package edu.cornell.kfs.sys.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.List;

import org.kuali.kfs.fp.document.IndirectCostAdjustmentDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.NoteService;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.document.service.CUFinancialSystemDocumentService;
import edu.cornell.kfs.sys.service.impl.fixture.IndirectCostAdjustmentDocumentFixture;

@ConfigureContext(session = ccs1)
public class CuFinancialSystemDocumentServiceImplTest extends KualiTestBase {
    private CUFinancialSystemDocumentService cUFinancialSystemDocumentService;
    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        cUFinancialSystemDocumentService = SpringContext.getBean(CUFinancialSystemDocumentService.class);
        }

    /*
     * test get user primary favorite account
     * 
     */
    public void testAddNoteForAccountingLineChange() {
        try {
            IndirectCostAdjustmentDocument icaDocument = IndirectCostAdjustmentDocumentFixture.ICA_GOOD.createIndirectCostAdjustmentDocument();
            AccountingDocumentTestUtils.routeDocument(icaDocument, SpringContext.getBean(DocumentService.class));
            String documentId = icaDocument.getDocumentNumber();
        // switch user to FO
            changeCurrentUser(UserNameFixture.kan2);
            // icaDocument, and the 'saveDoc' in cUFinancialSystemDocumentService are the same instance.
            // So, need a copied
            IndirectCostAdjustmentDocument copied = (IndirectCostAdjustmentDocument)ObjectUtils.deepCopy(icaDocument);         
            List<Note> savedNotes = copied.getNotes();
            List<Note> originalNotes = icaDocument.getNotes();
            cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
            assertTrue("Should have no New note added", savedNotes.size() == originalNotes.size());
            
            copied.getSourceAccountingLine(0).setOrganizationReferenceId("changed");
            cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
            // Note is only created, but not saved yet.
            assertTrue("Should have new note added for Source referenceid change ", savedNotes.size() > originalNotes.size());
            assertTrue("New note is accounting line change note ", savedNotes.get(0).getNoteText().startsWith("Accounting Line changed from:"));
            copied.getSourceAccountingLine(0).setOrganizationReferenceId("");
            copied.getTargetAccountingLine(0).setOrganizationReferenceId("changed");
            savedNotes.clear();
            cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
            assertTrue("Should have new note added for Target referenceid change ", savedNotes.size() > originalNotes.size());
            assertTrue("New note is accounting line change note ", savedNotes.get(0).getNoteText().startsWith("Accounting Line changed from:"));
            copied.getTargetAccountingLine(0).setOrganizationReferenceId("");
            copied.getSourceAccountingLine(0).setAccountNumber("G254700");
            savedNotes.clear();
            cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
            assertTrue("Should have new note added for Source Account Number change ", savedNotes.size() > originalNotes.size());
            assertTrue("New note is accounting line change note ", savedNotes.get(0).getNoteText().startsWith("Accounting Line changed from:"));
            copied.getSourceAccountingLine(0).setAccountNumber("G264700");
            copied.getTargetAccountingLine(0).setAccountNumber("G264700");
            savedNotes.clear();
            cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
            // Note is only created, but not saved yet.
            assertTrue("Should have new note added for Target Account Number change ", savedNotes.size() > originalNotes.size());
            assertTrue("New note is accounting line change note ", savedNotes.get(0).getNoteText().startsWith("Accounting Line changed from:"));
        } catch (Exception e) {
            assertTrue("should not get Exception " + e.getMessage(), false);
        }
   }
    
}
