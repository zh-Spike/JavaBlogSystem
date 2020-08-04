package net.blog.pojo;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table ( name ="tb_labels" )
public class Labels {

  	@Id
	private String id;
  	@Column(name = "name" )
	private String name;
  	@Column(name = "count" )
	private long count;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public Date getCreate_time(Date date) {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdate_time() {
		return updateTime;
	}

	public void setUpdate_time(Date update_time) {
		this.updateTime = update_time;
	}
}
