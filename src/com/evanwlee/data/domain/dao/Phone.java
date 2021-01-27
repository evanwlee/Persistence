package com.evanwlee.data.domain.evanwlee;

import java.sql.ResultSet;
import java.util.List;

import com.evanwlee.data.domain.Domain;
import com.evanwlee.data.domain.IDomain;

public class Phone extends Domain{
	
	public static final String TABLE_NAME = Domain.TABLE_PREFIX + "phone";


	private String number;
	private Long buyer_id;
	
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Long getBuyerId() {
		return buyer_id;
	}

	public void setBuyerId(Long buyer_id) {
		this.buyer_id = buyer_id;
	}

	public Phone save(){
		return (Phone)super.save();
	}
	public Phone fetchOrCreate(){
		return (Phone)super.fetchOrCreate();
	}
	
	protected void createFromRecord(ResultSet record){
		super.initFromRecord(record);
	}
	
	
	@Override
	protected String getCompositeKeySelectWhereClause() {
		String id = (null == super.getId() || super.getId() == 0)?"":"id="+super.getId()+" and ";
		String number = ( null ==  this.getNumber() || "".equals(this.getNumber()))?"":"number='"+super.makeSqlSafe(this.getNumber())+"' and ";
		String buyer_id = (null == this.getBuyerId() || this.getBuyerId() == 0)?"":"buyer_id="+this.getBuyerId()+" and ";
		
		String q = " WHERE " + id + number + buyer_id;
		
		return q;
	}
	
	@Override
	public String tableName() {
		return TABLE_NAME;
	}
	
	//Get Associated Objects
	public List<IDomain> fetchCustomer(){
		Customer c = new Customer();
		c.setId(this.getBuyerId());
		List<IDomain> customers = c.fetch();
		
		return customers;
	}
}
