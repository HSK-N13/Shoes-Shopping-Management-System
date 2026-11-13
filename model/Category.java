package model;

public class Category {
    private Long id;
    private String name;
    private boolean active;

    public Category() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public boolean isActive() {
    	return active;
    }
    public void setActive(boolean active) {
    	this.active = active;
    }
}
