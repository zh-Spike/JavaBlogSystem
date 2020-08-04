package net.blog.pojo;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table ( name ="tb_user" )
public class User {

  	@Id
	private String id;
  	@Column(name = "user_name" )
	private String user_name;
  	@Column(name = "password" )
	private String password;
  	@Column(name = "roles" )
	private String roles;
  	@Column(name = "avatar" )
	private String avatar;
  	@Column(name = "email" )
	private String email;
  	@Column(name = "sign" )
	private String sign;
  	@Column(name = "state" )
	private String state;
  	@Column(name = "reg_ip" )
	private String reg_ip;
  	@Column(name = "login_ip" )
	private String login_ip;
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

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getReg_ip() {
		return reg_ip;
	}

	public void setReg_ip(String reg_ip) {
		this.reg_ip = reg_ip;
	}

	public String getLogin_ip() {
		return login_ip;
	}

	public void setLogin_ip(String login_ip) {
		this.login_ip = login_ip;
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
