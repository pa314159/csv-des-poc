package ascelion.poc.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.*;

@JsonPropertyOrder({ "country", "city", "zip", "street", "no", "extra" })
@Getter
@Setter
@ToString(includeFieldNames = false)
public class Address {
	private String country;
	private String city;
	private String street;
	private String no;
	private String extra;
	private String zip;
}
