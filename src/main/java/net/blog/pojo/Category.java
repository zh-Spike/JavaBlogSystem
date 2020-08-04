package net.blog.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table ( name ="tb_category" )
public class Category {

  	@Id
	private String id;
  	@Column(name = "name" )
	private String name;
  	@Column(name = "pinyin" )
	private String pinyin;
  	@Column(name = "description" )
	private String description;
  	@Column(name = "`order`" )
	private long order;
  	@Column(name = "status" )
	private String status;
  	@Column(name = "create_time" )
	private java.sql.Timestamp createTime;
  	@Column(name = "update_time" )
	private java.sql.Timestamp updateTime;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public long getOrder() {
		return order;
	}

	public void setOrder(long order) {
		this.order = order;
	}


	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public java.sql.Timestamp getCreate_time() {
		return createTime;
	}

	public void setCreate_time(java.sql.Timestamp create_time) {
		this.createTime = create_time;
	}


	public java.sql.Timestamp getUpdate_time() {
		return updateTime;
	}

	public void setUpdate_time(java.sql.Timestamp update_time) {
		this.updateTime = update_time;
	}

}
