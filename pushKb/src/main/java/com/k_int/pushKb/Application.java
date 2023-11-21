package com.k_int.pushKb;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
	info = @Info(
		title = "PushKb",
		version = "0.1",
		description = "A service to collate data from GoKB and push into FOLIO",
		license = @License(name = "EBSCO", url = "https://foo.bar"),
		contact = @Contact(url = "https://www.k-int.com", name = "Ian Ibbotson", email = "ian.ibbotson@k-int.com")
	)
)
public class Application {
	public static void main(String[] args) {
		Micronaut.run(Application.class, args);
	}
}
