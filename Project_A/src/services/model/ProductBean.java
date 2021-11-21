package services.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "product")
public class ProductBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private double price;

  public ProductBean() { }

  // Getters

  public String getName() { return name; }
  public String getID() { return id; }
  public double getPrice()  { return price; }

  // Setters

  public void setName(String name) { this.name = name; }
  public void setID(String id) { this.id = id; }
  public void setPrice(double price)   { this.price  = price; }

  public String toString() {
    return String.format("ID%s" + " NAME:%S" + "PRICE:%2f",
      id, name, price);
  }
}