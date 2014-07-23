package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public enum RequisitionItemFixture {
	REQ_ITEM(new Integer(1), "EA", "1234567", "item desc", "ITEM", "Punchout",
			new KualiDecimal(1), new KualiDecimal(1), "80141605",
			new BigDecimal(1), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE);

	public final Integer itemLineNumber;
	public final String itemUnitOfMeasureCode;
	public final String itemCatalogNumber;
	public final String itemDescription;
	public final String itemTypeCode;
	public final String externalOrganizationB2bProductTypeName;
	public final KualiDecimal extendedPrice;
	public final KualiDecimal itemQuantity;
	public final String purchasingCommodityCode;
	public final BigDecimal itemUnitPrice;

	public final PurapAccountingLineFixture accountingLineFixture;

	private RequisitionItemFixture(Integer itemLineNumber,
			String itemUnitOfMeasureCode, String itemCatalogNumber,
			String itemDescription, String itemTypeCode,
			String externalOrganizationB2bProductTypeName,
			KualiDecimal extendedPrice, KualiDecimal itemQuantity,
			String purchasingCommodityCode, BigDecimal itemUnitPrice,
			PurapAccountingLineFixture accountingLineFixture) {
		
		this.itemLineNumber = itemLineNumber;
		this.itemUnitOfMeasureCode = itemUnitOfMeasureCode;
		this.itemCatalogNumber = itemCatalogNumber;
		this.itemDescription = itemDescription;
		this.itemTypeCode = itemTypeCode;
		this.externalOrganizationB2bProductTypeName = externalOrganizationB2bProductTypeName;
		this.extendedPrice = extendedPrice;
		this.itemQuantity = itemQuantity;
		this.purchasingCommodityCode = purchasingCommodityCode;
		this.itemUnitPrice = itemUnitPrice;

		this.accountingLineFixture = accountingLineFixture;

	}

	public RequisitionItem createRequisitionItem() {
		// item
		RequisitionItem item = new RequisitionItem();
		item.setItemLineNumber(itemLineNumber);
		item.setItemUnitOfMeasureCode(itemUnitOfMeasureCode);
		item.setItemCatalogNumber(itemCatalogNumber);
		item.setItemDescription(itemDescription);
		item.setItemTypeCode(itemTypeCode);
		item.setExternalOrganizationB2bProductTypeName(externalOrganizationB2bProductTypeName);
		item.setExtendedPrice(extendedPrice);
		item.setItemQuantity(itemQuantity);
		item.setPurchasingCommodityCode(purchasingCommodityCode);
		item.setItemUnitPrice(itemUnitPrice);

		PurApAccountingLine purapAcctLine = new RequisitionAccount();
		purapAcctLine.setAccountLinePercent(new BigDecimal(100));
		purapAcctLine.setAccountNumber("1000710");
		purapAcctLine.setChartOfAccountsCode("IT");
		purapAcctLine.setFinancialObjectCode("6100");
		purapAcctLine.setAmount(new KualiDecimal(1));

		item.getSourceAccountingLines().add(accountingLineFixture.createRequisitionAccount());

		return item;

	}

}
