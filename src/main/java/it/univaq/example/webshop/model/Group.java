package it.univaq.example.webshop.model;

import java.util.List;
import it.univaq.example.webshop.model.Group;
import it.univaq.framework.data.DataItemImpl;

public class Group extends DataItemImpl<Integer> {
    
    private UserRoleEnum name;
    private List<User> users;
    private List<Service> services;

    public Group() {
        super();
        name = UserRoleEnum.ORDINANTE;
        users = null;
        services = null;
    }
    
    public UserRoleEnum getName() {
        return name;
    }
    
    public void setName(UserRoleEnum name) {
        this.name = name;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
    
    public void addUser(User user) {
        users.add(user);
    }
    
    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
    
    public void addService(Service service) {
        services.add(service);
    } 
}
