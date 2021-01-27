package com.evanwlee.data.domain.evanwlee;

import java.sql.ResultSet;
import java.util.List;

import com.evanwlee.data.domain.Domain;
import com.evanwlee.data.domain.IDomain;

public class Customer extends Domain{
	
	public static final String TABLE_NAME = Domain.TABLE_PREFIX + "Customer";


	private String name;
	private String email;
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return List of child Phone domain objects for this Buyer
	 */
	public List<IDomain> fetchPhones(){
		Phone phone = new Phone();
		phone.setBuyerId(this.getId());
		
		return phone.fetch();
	}
	
	public Customer save(){
		return (Buyer)super.save();
	}
	public Customer fetchOrCreate(){
		return (Buyer)super.fetchOrCreate();
	}
	
	protected void createFromRecord(ResultSet record){
		super.initFromRecord(record);
	}
	
	@Override
	protected String getCompositeKeySelectWhereClause() {
		String id = (null == super.getId() || super.getId() == 0)?"":"id="+super.getId()+" and ";
		String name = ( null ==  this.getName() || "".equals(getName()))?"":"name='"+super.makeSqlSafe(this.getName())+"' and ";
		
		String q = " WHERE " + id + name ;
		return q;
	}
	
	@Override
	public String tableName() {
		return TABLE_NAME;
	}
}
