package net.blog.pojo;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table ( name ="tb_settings" )
public class Settings {

  	@Id
	private String id;
  	@Column(name = "`key`" )
	private String key;
  	@Column(name = "`value`" )
	private String value;
  	@Column(name = "create_time" )
	private Date createTime;
  	@Column(name = "update_time" )
	private Date updateTime;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getCreate_time() {
		return createTime;
	}

	public void setCreate_time(Date create_time) {
		this.createTime = create_time;
	}

	public Date getUpdate_time() {
		return updateTime;
	}

	public void setUpdate_time(Date update_time) {
		this.updateTime = update_time;
	}
}
