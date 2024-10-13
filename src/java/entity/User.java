package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "mobile", length = 10, nullable = false)
    private String mobile;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "password", length = 45, nullable = false)
    private String password;
    
    @Column(name = "registerd_date_time", nullable = false)
    private Date registerd_date_time;
    
    @ManyToOne
    @JoinColumn(name = "user_status_id")
    private User_Status user_Status;

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getRegisterd_date_time() {
        return registerd_date_time;
    }

    public void setRegisterd_date_time(Date registerd_date_time) {
        this.registerd_date_time = registerd_date_time;
    }

    public User_Status getUser_Status() {
        return user_Status;
    }

    public void setUser_Status(User_Status user_Status) {
        this.user_Status = user_Status;
    }

}
