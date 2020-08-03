package net.blog.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table ( name ="tb_friendlink" )
public class FriendLink {

  	@Id
	private String id;
  	@Column(name = "name" )
	private String name;
  	@Column(name = "logo" )
	private String logo;
  	@Column(name = "url" )
	private String url;
  	@Column(name = "`order`" )
	private long order;
  	@Column(name = "state" )
	private String state;
  	@Column(name = "create_time" )
	private java.sql.Timestamp create_time;
  	@Column(name = "update_time" )
	private java.sql.Timestamp update_time;


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


	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	public long getOrder() {
		return order;
	}

	public void setOrder(long order) {
		this.order = order;
	}


	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}


	public java.sql.Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(java.sql.Timestamp create_time) {
		this.create_time = create_time;
	}


	public java.sql.Timestamp getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(java.sql.Timestamp update_time) {
		this.update_time = update_time;
	}

}
