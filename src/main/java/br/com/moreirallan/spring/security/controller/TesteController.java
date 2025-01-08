package br.com.moreirallan.spring.security.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping//(path = "api/ec")
public class TesteController {

//    @PreAuthorize(KeycloakConstants.Roles.CRM_USERS_ACCESS_ROLE_EXPR)
    @GetMapping(path = "public/ec",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String publicEc(){
        return "PUBLIC";
    }

    @GetMapping(path = "api/ec/myBalance",produces = MediaType.APPLICATION_JSON_VALUE)
    public String myBalance(){
        return "API";
    }

    @GetMapping(path = "myLoad",produces = MediaType.APPLICATION_JSON_VALUE)
    public String myLoad(){
        return "myLoad";
    }

    @GetMapping(path = "user",produces = MediaType.APPLICATION_JSON_VALUE)
    public String user(){
        return "user";
    }
}
