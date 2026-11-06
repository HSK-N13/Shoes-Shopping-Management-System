package model;

public class Brand {
    private Long id;
    private String name;
	private boolean active;

    public Brand() {
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
    	return active ;
    }
    public void setActive(Boolean active) {
    	this.active = active;
    }
}
